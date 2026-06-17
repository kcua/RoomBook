package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.database.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService {

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("summary", getSummary());
        dashboard.put("roomUtilisation", getRoomUtilisation());
        dashboard.put("bookingTrends", getBookingTrends());
        dashboard.put("userActivity", getUserActivity());
        dashboard.put("securityMetrics", getSecurityMetrics());

        return dashboard;
    }

    private Map<String, Object> getSummary() {
        String sql = """
            SELECT
                (SELECT COUNT(*) FROM reservations) AS total_bookings,
                (SELECT COUNT(*) FROM reservations WHERE status = 'CONFIRMED') AS confirmed_bookings,
                (SELECT COUNT(*) FROM reservations WHERE status = 'CANCELLED') AS cancelled_bookings,
                (SELECT COUNT(*) FROM rooms) AS total_rooms,
                (SELECT COUNT(*) FROM users) AS total_users
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Object> summary = new LinkedHashMap<>();
            if (rs.next()) {
                summary.put("totalBookings", rs.getInt("total_bookings"));
                summary.put("confirmedBookings", rs.getInt("confirmed_bookings"));
                summary.put("cancelledBookings", rs.getInt("cancelled_bookings"));
                summary.put("totalRooms", rs.getInt("total_rooms"));
                summary.put("totalUsers", rs.getInt("total_users"));
            }
            return summary;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (analytics summary): " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> getRoomUtilisation() {
        String sql = """
            SELECT
                r.room_id,
                r.name,
                r.capacity,
                SUM(CASE WHEN res.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS booking_count,
                COALESCE(SUM(
                    CASE
                        WHEN res.status = 'CONFIRMED' THEN
                            (strftime('%s', res.date || ' ' || res.end_time) -
                             strftime('%s', res.date || ' ' || res.start_time)) / 3600.0
                        ELSE 0
                    END
                ), 0) AS booked_hours
            FROM rooms r
            LEFT JOIN reservations res ON res.room_id = r.room_id
            GROUP BY r.room_id, r.name, r.capacity
            ORDER BY booking_count DESC, r.name
        """;

        return queryList(sql, rs -> {
            Map<String, Object> room = new LinkedHashMap<>();
            room.put("roomId", rs.getInt("room_id"));
            room.put("name", rs.getString("name"));
            room.put("capacity", rs.getInt("capacity"));
            room.put("bookingCount", rs.getInt("booking_count"));
            room.put("bookedHours", Math.round(rs.getDouble("booked_hours") * 10.0) / 10.0);
            return room;
        });
    }

    private List<Map<String, Object>> getBookingTrends() {
        String sql = """
            SELECT date, COUNT(*) AS booking_count
            FROM reservations
            GROUP BY date
            ORDER BY date DESC
            LIMIT 14
        """;

        return queryList(sql, rs -> {
            Map<String, Object> trend = new LinkedHashMap<>();
            trend.put("date", rs.getString("date"));
            trend.put("bookingCount", rs.getInt("booking_count"));
            return trend;
        });
    }

    private List<Map<String, Object>> getUserActivity() {
        String sql = """
            SELECT
                u.user_id,
                u.name,
                u.email,
                COUNT(res.res_id) AS booking_count,
                SUM(CASE WHEN res.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS active_bookings,
                SUM(CASE WHEN res.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_bookings
            FROM users u
            LEFT JOIN reservations res ON res.user_id = u.user_id
            GROUP BY u.user_id, u.name, u.email
            ORDER BY booking_count DESC, u.name
            LIMIT 10
        """;

        return queryList(sql, rs -> {
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("userId", rs.getInt("user_id"));
            user.put("name", rs.getString("name"));
            user.put("email", rs.getString("email"));
            user.put("bookingCount", rs.getInt("booking_count"));
            user.put("activeBookings", rs.getInt("active_bookings"));
            user.put("cancelledBookings", rs.getInt("cancelled_bookings"));
            return user;
        });
    }

    private Map<String, Object> getSecurityMetrics() {
        String sql = """
            SELECT
                SUM(CASE WHEN UPPER(role) = 'ADMIN' THEN 1 ELSE 0 END) AS admin_users,
                SUM(CASE WHEN UPPER(role) <> 'ADMIN' THEN 1 ELSE 0 END) AS standard_users
            FROM users
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Object> metrics = new LinkedHashMap<>();
            if (rs.next()) {
                metrics.put("adminUsers", rs.getInt("admin_users"));
                metrics.put("standardUsers", rs.getInt("standard_users"));
            }

            int total = ((Number) getSummary().getOrDefault("totalBookings", 0)).intValue();
            int cancelled = ((Number) getSummary().getOrDefault("cancelledBookings", 0)).intValue();
            metrics.put("cancelledBookings", cancelled);
            metrics.put("cancellationRate", total == 0 ? 0 : Math.round((cancelled * 1000.0 / total)) / 10.0);
            metrics.put("loginEventTracking", "Not enabled yet");
            return metrics;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (security metrics): " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> queryList(String sql, RowMapper mapper) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(mapper.map(rs));
            }

            return rows;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (analytics list): " + e.getMessage(), e);
        }
    }

    private interface RowMapper {
        Map<String, Object> map(ResultSet rs) throws SQLException;
    }
}
