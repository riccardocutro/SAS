package catering.businesslogic.staff;

import java.sql.Date;
import catering.persistence.PersistenceManager;

public class ResponseDeadline {
    private int id;
    private Date date;
    private Staff staff;
    private RoleRequest roleRequest;

    public ResponseDeadline(Date date, Staff s, RoleRequest rr) {
        this.date = date;
        this.staff = s;
        this.roleRequest = rr;
    }

    public static ResponseDeadline create(Date date, Staff s, RoleRequest rr) {
        ResponseDeadline rd = new ResponseDeadline(date, s, rr);
        String query = "INSERT INTO ResponseDeadlines (date_deadline, staff_id, request_id) VALUES (?, ?, ?)";
        PersistenceManager.executeUpdate(query, date, s.getId(), rr.getId());
        rd.id = PersistenceManager.getLastId();
        return rd;
    }

    public boolean isExpired() {
        return new java.util.Date().after(date);
    }
}
