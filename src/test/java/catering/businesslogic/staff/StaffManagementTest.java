package catering.businesslogic.staff;

import static org.junit.jupiter.api.Assertions.*;

import catering.businesslogic.holiday.HolidayRequest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.event.EventCard;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class StaffManagementTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffManagementTest.class);

    private static CatERing app;
    private static User organizer;
    private static Event testEvent;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            organizer = User.load("Giovanni");
            assertNotNull(organizer, "Organizer user should be loaded");

            testEvent = Event.loadByName("Gala Aziendale Annuale");
            assertNotNull(testEvent, "Test event should be loaded");

            app.getUserManager().fakeLogin(organizer.getUserName());

        } catch (UseCaseLogicException e) {
            LOGGER.severe(e.getMessage());
            fail("Setup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testEventCardCreationAndRoleDefinition() {
        LOGGER.info("Testing event card creation and role definition");

        try {
            EventCard card = app.getStaffManager().openEventSheet(testEvent);

            assertNotNull(card, "EventCard should not be null");
            assertEquals(testEvent.getId(), card.getEvent().getId(), "Card should match the event");

            String roleName = "Maitre_" + System.currentTimeMillis();
            RoleRequest role = app.getStaffManager().defineRole(roleName);

            assertNotNull(role, "RoleRequest should be created");
            assertEquals(roleName, role.getPosition(), "Role position should match");
            assertTrue(card.getRoleRequests().contains(role), "Card should contain the new role");

            LOGGER.info("Defined role: " + role.getPosition());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testStaffAssignment() {
        LOGGER.info("Testing staff assignment");

        try {
            app.getStaffManager().openEventSheet(testEvent);
            RoleRequest role = app.getStaffManager().defineRole("Cameriere_" + System.currentTimeMillis());

            String staffName = "Paolo_" + System.currentTimeMillis();
            app.getStaffManager().saveNewStaff(staffName, "Bianchi", "333-0000");

            ArrayList<Staff> allStaff = Staff.loadAllStaff();
            Staff newStaff = allStaff.get(allStaff.size() - 1);

            assertNotNull(newStaff, "New staff should be loaded");

            app.getStaffManager().assignRole(role, newStaff);

            assertNotNull(role.getAssignee(), "Assignee should not be null");
            assertEquals(newStaff.getId(), role.getAssignee().getId(), "Role should be assigned to correct staff");

            LOGGER.info("Successfully assigned " + newStaff.getName() + " to " + role.getPosition());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testAssignmentFailsIfOnHoliday() {
        LOGGER.info("Test: Assegnazione deve fallire se lo staff è in ferie");
        try {
            Staff worker = Staff.loadAllStaff().get(0);
            HolidayRequest hr = app.getHolidayManager().createRequest(worker, testEvent.getDateStart(), testEvent.getDateEnd());

            User owner = User.load("Giovanni");
            app.getUserManager().setCurrentUser(owner);
            app.getHolidayManager().manageHolidayRequest(hr, HolidayRequest.STATUS_APPROVED);

            app.getUserManager().fakeLogin("Giovanni");

            RoleRequest role = app.getStaffManager().defineRole("Aiuto Cuoco Test");

            assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().assignRole(role, worker);
            });

            LOGGER.info("Test Superato: L'eccezione è stata lanciata correttamente.");

        } catch (Exception e) {
            fail("Errore imprevisto nel test: " + e.getMessage());
        }
    }
}