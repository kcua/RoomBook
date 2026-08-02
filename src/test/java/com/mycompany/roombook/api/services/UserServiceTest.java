package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.models.UserChangePasswordRequest;
import com.mycompany.roombook.api.models.UserLoginRequest;
import com.mycompany.roombook.api.models.UserRegisterRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Karl Cuaresma
 */

public class UserServiceTest {

    /*
     * Test user registration requires a name.
     */
    @Test
    public void testRegisterRequiresName() {
        System.out.println("Testing user registration name validation");

        UserService service = new UserService();
        UserRegisterRequest req = validRegisterRequest();
        req.setName("");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.register(req));

        assertEquals("Name is required.", ex.getMessage());
    }

    /*
     * Test user registration requires a strong password.
     */
    @Test
    public void testRegisterRequiresStrongPassword() {
        System.out.println("Testing user registration password strength validation");

        UserService service = new UserService();
        UserRegisterRequest req = validRegisterRequest();
        req.setPassword("password");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.register(req));

        assertEquals(PasswordUtil.STRONG_PASSWORD_MESSAGE, ex.getMessage());
    }

    /*
     * Test login requires an email.
     */
    @Test
    public void testLoginRequiresEmail() {
        System.out.println("Testing login email validation");

        UserService service = new UserService();
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail(" ");
        req.setPassword("Admin123!");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.login(req));

        assertEquals("Email is required.", ex.getMessage());
    }

    /*
     * Test password change requires a valid user id.
     */
    @Test
    public void testChangePasswordRequiresUser() {
        System.out.println("Testing change password user validation");

        UserService service = new UserService();
        UserChangePasswordRequest req = validChangePasswordRequest();
        req.setUserId(0);

        IllegalArgumentException ex = expectIllegalArgument(() -> service.changePassword(req));

        assertEquals("User is required.", ex.getMessage());
    }

    /*
     * Test password change requires a strong new password.
     */
    @Test
    public void testChangePasswordRequiresStrongNewPassword() {
        System.out.println("Testing change password strength validation");

        UserService service = new UserService();
        UserChangePasswordRequest req = validChangePasswordRequest();
        req.setNewPassword("newpass");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.changePassword(req));

        assertEquals(PasswordUtil.STRONG_PASSWORD_MESSAGE, ex.getMessage());
    }

    private UserRegisterRequest validRegisterRequest() {
        UserRegisterRequest req = new UserRegisterRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("Admin123!");
        return req;
    }

    private UserChangePasswordRequest validChangePasswordRequest() {
        UserChangePasswordRequest req = new UserChangePasswordRequest();
        req.setUserId(1);
        req.setCurrentPassword("Admin123!");
        req.setNewPassword("NewPass123!");
        return req;
    }

    private IllegalArgumentException expectIllegalArgument(Runnable action) {
        try {
            action.run();
            fail("Expected IllegalArgumentException");
            return null;
        } catch (IllegalArgumentException ex) {
            return ex;
        }
    }
}
