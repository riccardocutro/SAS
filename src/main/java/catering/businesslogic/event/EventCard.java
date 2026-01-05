package catering.businesslogic.event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.businesslogic.staff.RoleRequest;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import lombok.Data;

@Data
public class EventCard {

    private int id;
    private Event event;
    private String transportInfo;
    private boolean isTerminated;

    private ArrayList<RoleRequest> roleRequests;

    public EventCard(Event event) {
        this.event = event;
        this.roleRequests = new ArrayList<>();
        this.transportInfo = "";
        this.isTerminated = false;
    }

    private EventCard() {
        this.roleRequests = new ArrayList<>();
    }


    public java.sql.Date getStartDate() { return event.getDateStart(); }

    public boolean isTerminated() { return isTerminated; }

    public void setTerminated(boolean terminated) { isTerminated = terminated; }

    public void addRoleRequest(RoleRequest rr) {
        this.roleRequests.add(rr);
    }

    public static void create(EventCard ec) {
        String query = "INSERT INTO EventCards (event_id, transport_info, is_terminated) VALUES (?, ?, ?)";

        PersistenceManager.executeUpdate(query,
                ec.event.getId(),
                ec.transportInfo,
                ec.isTerminated
        );

        ec.id = PersistenceManager.getLastId();
    }

    public static void saveTransportInfo(EventCard ec) {
        String query = "UPDATE EventCards SET transport_info = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, ec.transportInfo, ec.id);
    }

    public static EventCard loadByEventId(int eventId) {
        String query = "SELECT * FROM EventCards WHERE event_id = ?";
        final EventCard[] result = new EventCard[1];

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                EventCard ec = new EventCard();
                ec.id = rs.getInt("id");
                ec.transportInfo = rs.getString("transport_info");
                ec.isTerminated = rs.getBoolean("is_terminated");
                ec.event = Event.loadById(rs.getInt("event_id"));
                result[0] = ec;
            }
        }, eventId);

        if (result[0] != null) {
            result[0].roleRequests = RoleRequest.loadByEventCardId(result[0].id);
        }

        return result[0];
    }

    @Override
    public String toString() {
        return "EventCard [event=" + (event!=null ? event.getName() : "null") + "]";
    }
}