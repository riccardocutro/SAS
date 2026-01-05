package catering.businesslogic.staff;

import catering.businesslogic.event.EventCard;

public interface StaffEventReceiver {
    void updateRoleDefined(EventCard sch, RoleRequest r);
    void updateTransportInfoUpdated(EventCard sch, String info);
    void updateRoleAssigned(RoleRequest r, Staff s);
    void updateRoleRevoked(RoleRequest r);
    void updateCandidateContacted(RoleRequest r, Staff s, ResponseDeadline d);
    void updateResponseSaved(RoleRequest r, Staff s, String outcome);
    void updateStaffCreated(Staff s);
    void updateStaffDeleted(Staff s);
    void updateStaffUpdated(Staff s);
}