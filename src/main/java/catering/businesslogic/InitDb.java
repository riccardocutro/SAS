package catering.businesslogic;

import catering.persistence.PersistenceManager;

public class InitDb {

    public static void main(String[] args) {
        System.out.println("=== INIZIALIZZAZIONE TABELLE STAFF & HOLIDAY ===");

        // 1. Tabella EventCards
        String sql1 = "CREATE TABLE IF NOT EXISTS EventCards (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "event_id INTEGER NOT NULL, " +
                "transport_info TEXT, " +
                "is_terminated BOOLEAN DEFAULT 0" +
                ");";

        // 2. Tabella Staff
        String sql2 = "CREATE TABLE IF NOT EXISTS Staff (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "surname TEXT NOT NULL, " +
                "contact TEXT" +
                ");";

        // 3. Tabella RoleRequests
        String sql3 = "CREATE TABLE IF NOT EXISTS RoleRequests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "eventcard_id INTEGER NOT NULL, " +
                "position TEXT NOT NULL, " +
                "status TEXT DEFAULT 'defined', " +
                "assignee_id INTEGER, " +
                "FOREIGN KEY(assignee_id) REFERENCES Staff(id)" +
                ");";

        // 4. Tabella HolidayRequests
        String sql4 = "CREATE TABLE IF NOT EXISTS HolidayRequests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "staff_id INTEGER NOT NULL, " +
                "start_date INTEGER, " +
                "end_date INTEGER, " +
                "status TEXT" +
                ");";

        // 5. Tabella Candidati (RoleCandidates)
        String sql5 = "CREATE TABLE IF NOT EXISTS RoleCandidates (" +
                "request_id INTEGER NOT NULL, " +
                "staff_id INTEGER NOT NULL, " +
                "PRIMARY KEY(request_id, staff_id)" +
                ");";

        // 6. Tabella Scadenze (ResponseDeadlines)
        String sql6 = "CREATE TABLE IF NOT EXISTS ResponseDeadlines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date_deadline INTEGER, " +
                "staff_id INTEGER, " +
                "request_id INTEGER" +
                ");";

        try {
            PersistenceManager.executeUpdate(sql1);
            System.out.println("Tabella EventCards... OK");

            PersistenceManager.executeUpdate(sql2);
            System.out.println("Tabella Staff... OK");

            PersistenceManager.executeUpdate(sql3);
            System.out.println("Tabella RoleRequests... OK");

            PersistenceManager.executeUpdate(sql4);
            System.out.println("Tabella HolidayRequests... OK");

            PersistenceManager.executeUpdate(sql5);
            System.out.println("Tabella RoleCandidates... OK");

            PersistenceManager.executeUpdate(sql6);
            System.out.println("Tabella ResponseDeadlines... OK");

            System.out.println("=== FATTO! ORA PUOI LANCIARE TESTSTAFF ===");

        } catch (Exception e) {
            System.err.println("Errore durante la creazione tabelle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}