package catering.businesslogic.staff;

import java.sql.Date;
import java.util.ArrayList;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.event.EventCard;
import catering.businesslogic.user.User;

public class StaffManager {

    private EventCard currentEventCard;
    private ArrayList<StaffEventReceiver> eventReceivers;

    public StaffManager() {
        this.eventReceivers = new ArrayList<>();
    }

    public void addEventReceiver(StaffEventReceiver rec) {
        this.eventReceivers.add(rec);
    }

    public void removeEventReceiver(StaffEventReceiver rec) {
        this.eventReceivers.remove(rec);
    }

    // --- 1. APERTURA SCHEDA ---
    public EventCard openEventSheet(Event e) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (user == null || !user.hasRole(User.Role.ORGANIZZATORE)) {
            throw new UseCaseLogicException("User is not authorized");
        }

        EventCard ec = EventCard.loadByEventId(e.getId()); // Corretto: loadByEventId
        if (ec == null) {
            ec = new EventCard(e);
            EventCard.create(ec);
        }

        this.currentEventCard = ec;
        return ec;
    }

    // --- 2. DEFINIZIONE RUOLO---
    public RoleRequest defineRole(String position) throws UseCaseLogicException {
        if (currentEventCard == null) throw new UseCaseLogicException("No event sheet open");

        RoleRequest rr = new RoleRequest(position);
        RoleRequest.create(currentEventCard.getId(), rr);
        currentEventCard.addRoleRequest(rr);
        notifyRoleDefined(currentEventCard, rr);

        return rr;
    }

    // --- 3. ASSEGNAZIONE RUOLO ---
    public void assignRole(RoleRequest role, Staff s) throws UseCaseLogicException {
        if (currentEventCard == null) throw new UseCaseLogicException();

        Date eventDate = currentEventCard.getStartDate();

        boolean onHoliday = CatERing.getInstance().getHolidayManager().isStaffOnHoliday(s, eventDate);
        if (onHoliday) {
            throw new UseCaseLogicException("Staff is on holiday");
        }

        if (!s.isAvailable(eventDate)) {
            throw new UseCaseLogicException("Staff is not available");
        }

        if (role.getAssignee() != null) {
            throw new UseCaseLogicException("Role already assigned");
        }

        role.assign(s);
        RoleRequest.saveAssignment(role); // Aggiorna DB

        notifyRoleAssigned(role, s);
    }

    // --- 4. CONTATTO OCCASIONALE ---
    public void contactTemporaryStaff(Staff s, Date deadline, RoleRequest role) throws UseCaseLogicException {
        if (currentEventCard == null) throw new UseCaseLogicException();

        role.addCandidate(s);
        RoleRequest.saveCandidates(role);

        ResponseDeadline rd = ResponseDeadline.create(deadline, s, role);

        notifyCandidateContacted(role, s, rd);
    }

    // --- 5. GESTIONE RISPOSTA ---
    public void saveResponse(Staff s, RoleRequest role, String outcome) throws UseCaseLogicException {
        if (currentEventCard == null) throw new UseCaseLogicException();

        if (outcome.equalsIgnoreCase("positivo")) {
            role.assign(s);
            RoleRequest.saveAssignment(role);
        } else {
            role.removeCandidate(s);
            RoleRequest.saveCandidates(role);
        }

        notifyResponseSaved(role, s, outcome);
    }

    // --- 6. ALGORITMO DI RICERCA ---
    public ArrayList<Staff> getAvailableStaff(String category) throws UseCaseLogicException {
        if (currentEventCard == null) throw new UseCaseLogicException();
        Date date = currentEventCard.getStartDate();

        ArrayList<Staff> allStaff = Staff.loadAllStaff();
        ArrayList<Staff> availableList = new ArrayList<>();

        for (Staff s : allStaff) {

            boolean onHoliday = CatERing.getInstance().getHolidayManager().isStaffOnHoliday(s, date);
            if (!onHoliday) {
                if (s.isAvailable(date)) {
                    availableList.add(s);
                }
            }
        }
        return availableList;
    }

    // --- 7. ANAGRAFICA ---
    public void saveNewStaff(String name, String surname, String contact) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (user == null || !(user.isOwner() || user.hasRole(User.Role.ORGANIZZATORE))) {
            throw new UseCaseLogicException("Non autorizzato a inserire nuovo personale");
        }

        Staff s = new Staff(name, surname, contact);
        Staff.saveNewStaff(s);
        notifyStaffCreated(s);
    }

    // --- NOTIFICHE (Observer Pattern) ---

    private void notifyRoleDefined(EventCard sch, RoleRequest r) {
        for (StaffEventReceiver er : eventReceivers) {
            er.updateRoleDefined(sch, r);
        }
    }

    private void notifyRoleAssigned(RoleRequest r, Staff s) {
        for (StaffEventReceiver er : eventReceivers) {
            er.updateRoleAssigned(r, s);
        }
    }

    private void notifyCandidateContacted(RoleRequest r, Staff s, ResponseDeadline d) {
        for (StaffEventReceiver er : eventReceivers) {
            er.updateCandidateContacted(r, s, d);
        }
    }

    private void notifyResponseSaved(RoleRequest r, Staff s, String outcome) {
        for (StaffEventReceiver er : eventReceivers) {
            er.updateResponseSaved(r, s, outcome);
        }
    }

    private void notifyStaffCreated(Staff s) {
        for (StaffEventReceiver er : eventReceivers) {
            er.updateStaffCreated(s);
        }
    }
}