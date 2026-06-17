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
    public static final String STRONG_PASSWORD_MESSAGE =
            "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.";

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

    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasLowercase && hasNumber && hasSpecial;
    }
    
     public static void main(String[] args) {
        String password = "Admin123456!";
        String hashedPassword = hashPassword(password);

        System.out.println("Plain password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
    }
}
