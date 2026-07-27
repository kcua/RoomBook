/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.database.DB;
import com.mycompany.roombook.api.models.User;
import com.mycompany.roombook.api.models.UserChangePasswordRequest;
import com.mycompany.roombook.api.models.UserLoginRequest;
import com.mycompany.roombook.api.models.UserRegisterRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kcuar
 */
public class UserService {
    private static final String DEFAULT_ROLE = "USER";
    private static final String ADMIN_ROLE = "ADMIN";

     public User register(UserRegisterRequest req) {
        validateRegister(req);

        if (emailExists(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String sql = """
            INSERT INTO users (name, email, password, role)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, req.getName());
            ps.setString(2, req.getEmail());
            String hashedPassword = PasswordUtil.hashPassword(req.getPassword());
            ps.setString(3, hashedPassword);
            // New users are regular users by default.
            ps.setString(4, DEFAULT_ROLE);

            ps.executeUpdate();

            int newId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = keys.getInt(1);
                }
            }

            User user = new User();
            user.setUserId(newId);
            user.setName(req.getName());
            user.setEmail(req.getEmail());
            user.setPassword(hashedPassword);
            user.setRole(DEFAULT_ROLE);

            return user;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (register user): " + e.getMessage(), e);
        }
    }

    public User login(UserLoginRequest req) {
        validateLogin(req);

        String sql = """
            SELECT user_id, name, email, password, role
            FROM users
            WHERE email = ? AND password = ?
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getEmail());
            String hashedPassword = PasswordUtil.hashPassword(req.getPassword());
            ps.setString(2, hashedPassword);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    recordLoginEvent(conn, req.getEmail(), null, false, "Invalid email or password.");
                    throw new IllegalArgumentException("Invalid email or password.");
                }

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));

                recordLoginEvent(conn, user.getEmail(), user.getUserId(), true, "Login successful.");

                return user;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (login user): " + e.getMessage(), e);
        }
    }

    private void recordLoginEvent(Connection conn, String email, Integer userId, boolean success, String message) {
        String sql = """
            INSERT INTO login_events (email, user_id, success, message)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            if (userId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, userId);
            }
            ps.setInt(3, success ? 1 : 0);
            ps.setString(4, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error (record login event): " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT user_id, name, email, role FROM users ORDER BY name";
        List<User> users = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUserWithoutPassword(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (list users): " + e.getMessage(), e);
        }
    }

    public User createUserByAdmin(UserRegisterRequest req) {
        validateRegister(req);

        if (emailExists(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String role = normalizeRole(req.getRole());
        String sql = """
            INSERT INTO users (name, email, password, role)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, req.getName());
            ps.setString(2, req.getEmail());
            ps.setString(3, PasswordUtil.hashPassword(req.getPassword()));
            ps.setString(4, role);

            ps.executeUpdate();

            int newId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = keys.getInt(1);
                }
            }

            return getUserById(newId);

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create user): " + e.getMessage(), e);
        }
    }

    public User updateUserRole(int userId, String role) {
        String normalizedRole = normalizeRole(role);
        User existing = getUserById(userId);

        if (ADMIN_ROLE.equalsIgnoreCase(existing.getRole())
                && !ADMIN_ROLE.equals(normalizedRole)
                && countAdminUsers() <= 1) {
            throw new IllegalArgumentException("At least one admin user is required.");
        }

        String sql = "UPDATE users SET role = ? WHERE user_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizedRole);
            ps.setInt(2, userId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("User not found.");
            }

            return getUserById(userId);

        } catch (SQLException e) {
            throw new RuntimeException("DB error (update role): " + e.getMessage(), e);
        }
    }

    public String deleteUser(int userId) {
        User existing = getUserById(userId);

        if (ADMIN_ROLE.equalsIgnoreCase(existing.getRole()) && countAdminUsers() <= 1) {
            throw new IllegalArgumentException("At least one admin user is required.");
        }

        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteReservations =
                         conn.prepareStatement("DELETE FROM reservations WHERE user_id = ?");
                 PreparedStatement deleteUser =
                         conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {

                deleteReservations.setInt(1, userId);
                deleteReservations.executeUpdate();

                deleteUser.setInt(1, userId);
                int deleted = deleteUser.executeUpdate();
                if (deleted == 0) {
                    throw new IllegalArgumentException("User not found.");
                }

                conn.commit();
                return "User deleted successfully.";

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (delete user): " + e.getMessage(), e);
        }
    }

    private boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (check email): " + e.getMessage(), e);
        }
    }

    private void validateRegister(UserRegisterRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("User details are required.");
        }

        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (!PasswordUtil.isStrongPassword(req.getPassword())) {
            throw new IllegalArgumentException(PasswordUtil.STRONG_PASSWORD_MESSAGE);
        }

    }

    private User getUserById(int userId) {
        String sql = "SELECT user_id, name, email, role FROM users WHERE user_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("User not found.");
                }

                return mapUserWithoutPassword(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (find user): " + e.getMessage(), e);
        }
    }

    private int countAdminUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(role) = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ADMIN_ROLE);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (count admin users): " + e.getMessage(), e);
        }
    }

    private User mapUserWithoutPassword(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        return user;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        String normalizedRole = role.trim().toUpperCase();
        if (!DEFAULT_ROLE.equals(normalizedRole) && !ADMIN_ROLE.equals(normalizedRole)) {
            throw new IllegalArgumentException("Role must be USER or ADMIN.");
        }

        return normalizedRole;
    }

    // Returns true when the given user id belongs to an admin account.
    public boolean isAdmin(int userId) {
        String sql = "SELECT role FROM users WHERE user_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && ADMIN_ROLE.equalsIgnoreCase(rs.getString("role"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (check admin role): " + e.getMessage(), e);
        }
    }

    public String changePassword(UserChangePasswordRequest req) {
        validateChangePassword(req);

        String currentHashedPassword = PasswordUtil.hashPassword(req.getCurrentPassword());
        String newHashedPassword = PasswordUtil.hashPassword(req.getNewPassword());

        String sql = """
            UPDATE users
            SET password = ?
            WHERE user_id = ? AND password = ?
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHashedPassword);
            ps.setInt(2, req.getUserId());
            ps.setString(3, currentHashedPassword);

            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                throw new IllegalArgumentException("Current password is incorrect.");
            }

            return "Password changed successfully.";

        } catch (SQLException e) {
            throw new RuntimeException("DB error (change password): " + e.getMessage(), e);
        }
    }

    private void validateLogin(UserLoginRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
    }

    private void validateChangePassword(UserChangePasswordRequest req) {
        if (req.getUserId() <= 0) {
            throw new IllegalArgumentException("User is required.");
        }

        if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is required.");
        }

        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password is required.");
        }

        if (!PasswordUtil.isStrongPassword(req.getNewPassword())) {
            throw new IllegalArgumentException(PasswordUtil.STRONG_PASSWORD_MESSAGE);
        }
    }
}
