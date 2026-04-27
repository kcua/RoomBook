/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.database;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author kcuar
 */
public class DB {
    private static final String URL = "jdbc:sqlite:C:\\DB\\Booking.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.out.println("DB Error: " + e.getMessage());
            return null;
        }
    }

}
