/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author kcuar
 */
public class DBInit {
     public static void initialize() {
        try (Connection conn = DB.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Cannot initialize DB: no connection!");
                return;
            }

            try (Statement stmt = conn.createStatement()) {

                // Rooms table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS rooms (
                        room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        capacity INTEGER NOT NULL,
                        location TEXT,
                        equipment TEXT
                    );
                """);

                // Users table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL
                    );
                """);

                // Reservations table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reservations (
                        res_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        room_id INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        start_time TEXT NOT NULL,
                        end_time TEXT NOT NULL,
                        status TEXT NOT NULL,
                        FOREIGN KEY(user_id) REFERENCES users(user_id),
                        FOREIGN KEY(room_id) REFERENCES rooms(room_id)
                    );
                """);
            }

            seedRoomsIfEmpty(conn);

            System.out.println("✔ Database initialized successfully.");

        } catch (Exception e) {
            System.out.println("DBInit Error: " + e.getMessage());
        }
    }

    private static void seedRoomsIfEmpty(Connection conn) throws Exception {
        // Check if rooms table is empty
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM rooms");
             ResultSet rs = check.executeQuery()) {

            int count = rs.next() ? rs.getInt(1) : 0;
            if (count > 0) return;
        }

        // Seed 3 rooms
        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO rooms (name, capacity, location, equipment) VALUES (?, ?, ?, ?)")) {

            insert.setString(1, "Meeting Room A");
            insert.setInt(2, 6);
            insert.setString(3, "Floor 1");
            insert.setString(4, "TV, HDMI");
            insert.executeUpdate();

            insert.setString(1, "Meeting Room B");
            insert.setInt(2, 10);
            insert.setString(3, "Floor 2");
            insert.setString(4, "Projector");
            insert.executeUpdate();

            insert.setString(1, "Conference Room");
            insert.setInt(2, 20);
            insert.setString(3, "Floor 3");
            insert.setString(4, "Projector, Audio");
            insert.executeUpdate();
        }
    }
}
