/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.database.DB;
import com.mycompany.roombook.api.models.Room;
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
public class RoomService {
    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<>();

        String sql = """
            SELECT room_id, name, capacity, location, equipment
            FROM rooms
            ORDER BY room_id
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("room_id"));
                room.setName(rs.getString("name"));
                room.setCapacity(rs.getInt("capacity"));
                room.setLocation(rs.getString("location"));
                room.setEquipment(rs.getString("equipment"));
                rooms.add(room);
            }

            return rooms;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get rooms): " + e.getMessage(), e);
        }
    }

    public Room create(Room req) {
        validate(req);

        String sql = """
            INSERT INTO rooms (name, capacity, location, equipment)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, req.getName());
            ps.setInt(2, req.getCapacity());
            ps.setString(3, req.getLocation());
            ps.setString(4, req.getEquipment());

            ps.executeUpdate();

            int newId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newId = keys.getInt(1);
                }
            }

            Room room = new Room();
            room.setId(newId);
            room.setName(req.getName());
            room.setCapacity(req.getCapacity());
            room.setLocation(req.getLocation());
            room.setEquipment(req.getEquipment());

            return room;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create room): " + e.getMessage(), e);
        }
    }

    public Room update(int id, Room req) {
        validate(req);

        String sql = """
            UPDATE rooms
            SET name = ?, capacity = ?, location = ?, equipment = ?
            WHERE room_id = ?
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getName());
            ps.setInt(2, req.getCapacity());
            ps.setString(3, req.getLocation());
            ps.setString(4, req.getEquipment());
            ps.setInt(5, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Room not found.");
            }

            Room room = new Room();
            room.setId(id);
            room.setName(req.getName());
            room.setCapacity(req.getCapacity());
            room.setLocation(req.getLocation());
            room.setEquipment(req.getEquipment());

            return room;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (update room): " + e.getMessage(), e);
        }
    }

    public String delete(int id) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Room not found.");
            }

            return "Room deleted successfully.";

        } catch (SQLException e) {
            throw new RuntimeException("DB error (delete room): " + e.getMessage(), e);
        }
    }

    private void validate(Room req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Room name is required.");
        }

        if (req.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }
    }
}
