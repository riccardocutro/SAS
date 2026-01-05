package catering.businesslogic.staff;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Staff {

    private int id;
    private String name;
    private String surname;
    private String contact;
    private String fiscalCode;

    public Staff(String name, String surname, String contact) {
        this.name = name;
        this.surname = surname;
        this.contact = contact;
    }

    public Staff() { }

    public boolean isAvailable(Date date) {
        String query = "SELECT count(*) FROM RoleRequests rr " +
                "JOIN EventCards ec ON rr.eventcard_id = ec.id " +
                "JOIN Events e ON ec.event_id = e.id " +
                "WHERE rr.assignee_id = ? " +
                "AND rr.status = 'assigned' " +
                "AND ? BETWEEN e.date_start AND e.date_end";
        final boolean[] isBusy = {false};
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                if (rs.getInt(1) > 0) {
                    isBusy[0] = true;
                }
            }
        }, this.id, date);
        return !isBusy[0];
    }


    public static void saveNewStaff(Staff s) {
        String query = "INSERT INTO Staff (name, surname, contact) VALUES (?, ?, ?)";
        PersistenceManager.executeUpdate(query, s.name, s.surname, s.contact);
        s.id = PersistenceManager.getLastId();
    }

    public static ArrayList<Staff> loadAllStaff() {
        ArrayList<Staff> staffList = new ArrayList<>();
        String query = "SELECT * FROM Staff ORDER BY surname, name";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff s = new Staff();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");
                s.surname = rs.getString("surname");
                s.contact = rs.getString("contact");
                staffList.add(s);
            }
        });
        return staffList;
    }

    public static Staff loadById(int id) {
        String query = "SELECT * FROM Staff WHERE id = ?";
        final Staff[] result = new Staff[1];
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff s = new Staff();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");
                s.surname = rs.getString("surname");
                s.contact = rs.getString("contact");
                result[0] = s;
            }
        }, id);
        return result[0];
    }
}