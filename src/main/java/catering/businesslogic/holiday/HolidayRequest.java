package catering.businesslogic.holiday;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.businesslogic.staff.Staff;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import lombok.Data;

@Data
public class HolidayRequest {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private int id;
    private Staff staff;
    private Date startDate;
    private Date endDate;
    private String status;

    public HolidayRequest(Staff staff, Date start, Date end) {
        this.staff = staff;
        this.startDate = start;
        this.endDate = end;
        this.status = STATUS_PENDING;
    }

    public HolidayRequest() {}

    public static void create(HolidayRequest hr) {
        String query = "INSERT INTO HolidayRequests (staff_id, start_date, end_date, status) VALUES (?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query,
                hr.staff.getId(),
                hr.startDate,
                hr.endDate,
                hr.status
        );
        hr.id = PersistenceManager.getLastId();
    }

    public static void updateStatus(HolidayRequest hr) {
        String query = "UPDATE HolidayRequests SET status = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, hr.status, hr.id);
    }

    public static boolean hasApprovedHoliday(int staffId, Date date) {
        String query = "SELECT count(*) as total FROM HolidayRequests " +
                "WHERE staff_id = ? AND status = 'APPROVED' " +
                "AND ? BETWEEN start_date AND end_date";

        final boolean[] result = {false};

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                if (rs.getInt("total") > 0) {
                    result[0] = true;
                }
            }
        }, staffId, date);

        return result[0];
    }
}