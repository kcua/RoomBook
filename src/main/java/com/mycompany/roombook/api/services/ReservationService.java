/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.database.DB;
import com.mycompany.roombook.api.models.Reservation;
import com.mycompany.roombook.api.models.ReservationCreateRequest;
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
public class ReservationService {

    public Reservation create(ReservationCreateRequest req) {
        validate(req);

        if (hasConflict(req.getRoomId(), req.getDate(), req.getStartTime(), req.getEndTime())) {
            throw new IllegalArgumentException("Room is already booked for that time on this date.");
        }

        String sql = """
            INSERT INTO reservations (user_id, room_id, date, start_time, end_time, status)
            VALUES (?, ?, ?, ?, ?, 'CONFIRMED')
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, req.getUserId());
            ps.setInt(2, req.getRoomId());
            ps.setString(3, req.getDate());
            ps.setString(4, req.getStartTime());
            ps.setString(5, req.getEndTime());

            ps.executeUpdate();

            int newId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) newId = keys.getInt(1);
            }

            Reservation r = new Reservation();
            r.setResId(newId);
            r.setUserId(req.getUserId());
            r.setRoomId(req.getRoomId());
            r.setDate(req.getDate());
            r.setStartTime(req.getStartTime());
            r.setEndTime(req.getEndTime());
            r.setStatus("CONFIRMED");

            return r;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create reservation): " + e.getMessage(), e);
        }
    }

    // Overlap rule
    private boolean hasConflict(int roomId, String date, String start, String end) {
        String sql = """
            SELECT 1
            FROM reservations
            WHERE room_id = ?
              AND date = ?
              AND status = 'CONFIRMED'
              AND (? < end_time AND ? > start_time)
            LIMIT 1
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setString(2, date);
            ps.setString(3, start);
            ps.setString(4, end);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (conflict check): " + e.getMessage(), e);
        }
    }

    private void validate(ReservationCreateRequest req) {
        if (req.getUserId() <= 0)
            throw new IllegalArgumentException("userId is required.");

        if (req.getRoomId() <= 0)
            throw new IllegalArgumentException("roomId is required.");

        if (req.getDate() == null || req.getDate().isBlank())
            throw new IllegalArgumentException("date is required (YYYY-MM-DD).");

        if (req.getStartTime() == null || req.getStartTime().isBlank())
            throw new IllegalArgumentException("startTime is required (HH:MM).");

        if (req.getEndTime() == null || req.getEndTime().isBlank())
            throw new IllegalArgumentException("endTime is required (HH:MM).");

        if (req.getStartTime().compareTo(req.getEndTime()) >= 0)
            throw new IllegalArgumentException("startTime must be before endTime.");
    }
    
    public List<Reservation> getAll() {
    List<Reservation> reservations = new ArrayList<>();

    String sql = """
        SELECT res_id, user_id, room_id, date, start_time, end_time, status
        FROM reservations
        ORDER BY res_id DESC
    """;

    try (Connection conn = DB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Reservation r = new Reservation();
            r.setResId(rs.getInt("res_id"));
            r.setUserId(rs.getInt("user_id"));
            r.setRoomId(rs.getInt("room_id"));
            r.setDate(rs.getString("date"));
            r.setStartTime(rs.getString("start_time"));
            r.setEndTime(rs.getString("end_time"));
            r.setStatus(rs.getString("status"));
            reservations.add(r);
        }

        return reservations;

    } catch (SQLException e) {
        throw new RuntimeException("DB error (list reservations): " + e.getMessage(), e);
    }
}
    
    public String cancel(int resId) {
    String sql = """
        UPDATE reservations
        SET status = 'CANCELLED'
        WHERE res_id = ?
    """;

    try (Connection conn = DB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, resId);
        int rows = ps.executeUpdate();

        if (rows == 0) {
            throw new IllegalArgumentException("Reservation not found.");
        }

        return "Reservation cancelled successfully.";

    } catch (SQLException e) {
        throw new RuntimeException("DB error (cancel reservation): " + e.getMessage(), e);
    }
}
}
