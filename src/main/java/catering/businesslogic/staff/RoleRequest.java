package catering.businesslogic.staff;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import lombok.Data;

@Data
public class RoleRequest {

    private int id;
    private String position;
    private String status;
    private Staff assignee;
    private ArrayList<Staff> candidates;

    public RoleRequest(String position) {
        this.position = position;
        this.status = "defined";
        this.candidates = new ArrayList<>();
    }

    private RoleRequest() {
        this.candidates = new ArrayList<>();
    }

    public void assign(Staff s) {
        this.assignee = s;
        this.status = "assigned";
    }

    public void addCandidate(Staff s) {
        if (!this.candidates.contains(s)) {
            this.candidates.add(s);
        }
    }

    public void removeCandidate(Staff s) {
        this.candidates.remove(s);
    }

    public static void create(int eventCardId, RoleRequest rr) {
        String query = "INSERT INTO RoleRequests (eventcard_id, position, status) VALUES (?, ?, ?)";
        PersistenceManager.executeUpdate(query, eventCardId, rr.position, rr.status);
        rr.id = PersistenceManager.getLastId();
    }

    public static void saveAssignment(RoleRequest rr) {
        String query = "UPDATE RoleRequests SET assignee_id = ?, status = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, (rr.assignee != null ? rr.assignee.getId() : null), rr.status, rr.id);
    }

    public static void saveCandidates(RoleRequest rr) {
        String del = "DELETE FROM RoleCandidates WHERE request_id = ?";
        PersistenceManager.executeUpdate(del, rr.id);

        String ins = "INSERT INTO RoleCandidates (request_id, staff_id) VALUES (?, ?)";
        for (Staff s : rr.candidates) {
            PersistenceManager.executeUpdate(ins, rr.id, s.getId());
        }
    }

    public static ArrayList<RoleRequest> loadByEventCardId(int eventCardId) {
        ArrayList<RoleRequest> list = new ArrayList<>();
        String query = "SELECT * FROM RoleRequests WHERE eventcard_id = ?";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                RoleRequest rr = new RoleRequest();
                rr.id = rs.getInt("id");
                rr.position = rs.getString("position");
                rr.status = rs.getString("status");

                int assigneeId = rs.getInt("assignee_id");
                if (assigneeId > 0) {
                    rr.assignee = Staff.loadById(assigneeId);
                }
                list.add(rr);
            }
        }, eventCardId);

        for (RoleRequest rr : list) {
            loadCandidates(rr);
        }

        return list;
    }

    private static void loadCandidates(RoleRequest rr) {
        String query = "SELECT staff_id FROM RoleCandidates WHERE request_id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff s = Staff.loadById(rs.getInt("staff_id"));
                if (s != null) rr.candidates.add(s);
            }
        }, rr.id);
    }
}