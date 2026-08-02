package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.models.ReservationCreateRequest;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Karl Cuaresma
 */

public class ReservationServiceTest {

    /*
     * Test reservation validation requires a user.
     */
    @Test
    public void testCreateReservationRequiresUser() {
        System.out.println("Testing reservation user validation");

        ReservationService service = new ReservationService();
        ReservationCreateRequest req = validReservation();
        req.setUserId(0);

        IllegalArgumentException ex = expectIllegalArgument(() -> service.create(req));

        assertEquals("userId is required.", ex.getMessage());
    }

    /*
     * Test reservation validation requires a valid date.
     */
    @Test
    public void testCreateReservationRequiresValidDate() {
        System.out.println("Testing reservation date validation");

        ReservationService service = new ReservationService();
        ReservationCreateRequest req = validReservation();
        req.setDate("not-a-date");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.create(req));

        assertEquals("date must be a valid date (YYYY-MM-DD).", ex.getMessage());
    }

    /*
     * Test reservation validation rejects past booking dates.
     */
    @Test
    public void testCreateReservationRejectsPastDate() {
        System.out.println("Testing reservation past date validation");

        ReservationService service = new ReservationService();
        ReservationCreateRequest req = validReservation();
        req.setDate(LocalDate.now().minusDays(1).toString());

        IllegalArgumentException ex = expectIllegalArgument(() -> service.create(req));

        assertEquals("Booking date cannot be in the past.", ex.getMessage());
    }

    /*
     * Test reservation validation requires start time before end time.
     */
    @Test
    public void testCreateReservationRequiresStartBeforeEnd() {
        System.out.println("Testing reservation time validation");

        ReservationService service = new ReservationService();
        ReservationCreateRequest req = validReservation();
        req.setStartTime("11:00");
        req.setEndTime("10:00");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.create(req));

        assertEquals("startTime must be before endTime.", ex.getMessage());
    }

    private ReservationCreateRequest validReservation() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setUserId(1);
        req.setRoomId(1);
        req.setDate(LocalDate.now().plusDays(1).toString());
        req.setStartTime("09:00");
        req.setEndTime("10:00");
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
