/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.database.DB;
import com.mycompany.roombook.api.models.User;
import com.mycompany.roombook.api.models.UserLoginRequest;
import com.mycompany.roombook.api.models.UserRegisterRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author kcuar
 */
public class UserService {
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
            ps.setString(3, req.getPassword());
            ps.setString(4, req.getRole());

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
            user.setPassword(req.getPassword());
            user.setRole(req.getRole());

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
            ps.setString(2, req.getPassword());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Invalid email or password.");
                }

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));

                return user;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (login user): " + e.getMessage(), e);
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
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (req.getRole() == null || req.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required.");
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
}
