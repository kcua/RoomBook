/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.roombook.api.services;

import java.security.MessageDigest;

/**
 *
 * @author Karl Cuaresma
 */
public class PasswordUtil {
    // Converts a plain password into a SHA-256 hashed password.
    public static String hashPassword(String password) {
        try {
            // Create the SHA-256 hashing tool.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Hash the password bytes.
            byte[] hashedBytes = md.digest(password.getBytes());

            // Convert the hashed bytes into readable hexadecimal text.
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            // Show a clear error if hashing fails.
            throw new RuntimeException("Password hashing error: " + e.getMessage());
        }
    }
}
