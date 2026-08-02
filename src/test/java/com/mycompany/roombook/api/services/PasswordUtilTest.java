package com.mycompany.roombook.api.services;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Karl Cuaresma
 */

public class PasswordUtilTest {

    /*
     * Test strong password validation.
     */
    @Test
    public void testStrongPassword() {
        System.out.println("Testing strong password");

        boolean result = PasswordUtil.isStrongPassword("Admin123!");

        assertTrue(result);
    }

    /*
     * Test weak password validation.
     */
    @Test
    public void testWeakPassword() {
        System.out.println("Testing weak password");

        boolean result = PasswordUtil.isStrongPassword("password");

        assertFalse(result);
    }

    /*
     * Test password hashing.
     */
    @Test
    public void testHashPassword() {
        System.out.println("Testing password hashing");

        String password = "Admin123!";
        String hashedPassword = PasswordUtil.hashPassword(password);

        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertEquals(64, hashedPassword.length());
    }
}
