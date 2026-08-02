package com.mycompany.roombook.api.services;

import com.mycompany.roombook.api.models.Room;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Karl Cuaresma
 */

public class RoomServiceTest {

    /*
     * Test room creation validation requires a name.
     */
    @Test
    public void testCreateRoomRequiresName() {
        System.out.println("Testing room name validation");

        RoomService service = new RoomService();
        Room room = validRoom();
        room.setName("");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.create(room));

        assertEquals("Room name is required.", ex.getMessage());
    }

    /*
     * Test room creation validation requires positive capacity.
     */
    @Test
    public void testCreateRoomRequiresPositiveCapacity() {
        System.out.println("Testing room capacity validation");

        RoomService service = new RoomService();
        Room room = validRoom();
        room.setCapacity(0);

        IllegalArgumentException ex = expectIllegalArgument(() -> service.create(room));

        assertEquals("Capacity must be greater than 0.", ex.getMessage());
    }

    /*
     * Test room update uses the same validation rules.
     */
    @Test
    public void testUpdateRoomRequiresName() {
        System.out.println("Testing room update validation");

        RoomService service = new RoomService();
        Room room = validRoom();
        room.setName(" ");

        IllegalArgumentException ex = expectIllegalArgument(() -> service.update(1, room));

        assertEquals("Room name is required.", ex.getMessage());
    }

    private Room validRoom() {
        Room room = new Room();
        room.setName("Training Room");
        room.setCapacity(8);
        room.setLocation("Floor 1");
        room.setEquipment("Projector");
        return room;
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
