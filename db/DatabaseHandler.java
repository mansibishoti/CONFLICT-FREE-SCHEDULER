package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    // ── CONNECTION SETTINGS ───────────────────────────────
    // Change PASSWORD to match your MySQL installation.
    private static final String URL      = "jdbc:mysql://localhost:3307/scheduler_db"
                                         + "?useSSL=false&allowPublicKeyRetrieval=true"
                                         + "&serverTimezone=UTC";
    private static final String DB_USER  = "root";
    private static final String PASSWORD = "mansibishoti@2007";

    // ── GET CONNECTION ────────────────────────────────────
    /**
     * Opens and returns a new JDBC connection.
     * Called internally by every method below.
     * Returns null and prints error if connection fails.
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, DB_USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL Driver not found. "
                + "Add mysql-connector-j.jar to your classpath.");
            return null;
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            return null;
        }
    }

    /*
     * Checks if a username + password + role combination exists.
     * Called by LoginScreen instead of reading users.txt.
     *
     * @return true if credentials match, false otherwise
     */
    public static boolean validateUser(String username,
                                        String password,
                                        String role) {
        String sql = "SELECT id FROM users "
                   + "WHERE username = ? AND password = ? "
                   + "AND role = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            ResultSet rs = ps.executeQuery();
            return rs.next();   // true = a matching row was found

        } catch (Exception e) {
            System.err.println("[DB] validateUser error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the role of a user by username.
     * Useful after login to pass the role to the Dashboard.
     *
     * @return "Admin" / "User", or null if not found
     */
    public static String getUserRole(String username) {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("role");

        } catch (Exception e) {
            System.err.println("[DB] getUserRole error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Adds a new user account.
     * Can be used by an admin panel later.
     *
     * @return true if inserted successfully
     */
    public static boolean addUser(String username,
                                   String password,
                                   String role) {
        String sql = "INSERT INTO users (username, password, role) "
                   + "VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[DB] addUser: username already exists.");
            return false;
        } catch (Exception e) {
            System.err.println("[DB] addUser error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════
    //  VENUE METHODS  (replaces venues.txt)
    // ══════════════════════════════════════════════════════

    /**
     * Returns all venues as a list of String arrays.
     * Each array: { name, capacity }
     * Called by VenueManager to populate the table.
     */
    public static List<String[]> getAllVenues() {
        List<String[]> venues = new ArrayList<>();
        String sql = "SELECT name, capacity FROM venues ORDER BY name";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                venues.add(new String[]{
                    rs.getString("name"),
                    String.valueOf(rs.getInt("capacity"))
                });
            }
        } catch (Exception e) {
            System.err.println("[DB] getAllVenues error: " + e.getMessage());
        }
        return venues;
    }

    /**
     * Returns the total number of registered venues.
     * Used by Dashboard "Venues Available" stat card.
     */
    public static int getVenueCount() {
        String sql = "SELECT COUNT(*) FROM venues";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[DB] getVenueCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Adds a new venue.
     * Called by VenueManager "Add Venue" button.
     *
     * @return true if inserted, false if name already exists or error
     */
    public static boolean addVenue(String name, int capacity) {
        String sql = "INSERT INTO venues (name, capacity) VALUES (?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, capacity);
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("[DB] addVenue: venue already exists.");
            return false;
        } catch (Exception e) {
            System.err.println("[DB] addVenue error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a venue by name.
     * Called by VenueManager "Delete Selected" button.
     *
     * @return true if deleted successfully
     */
    public static boolean deleteVenue(String name) {
        String sql = "DELETE FROM venues WHERE name = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[DB] deleteVenue error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a venue name already exists (case-insensitive).
     * Called by VenueManager before adding to prevent duplicates.
     */
    public static boolean venueExists(String name) {
        String sql = "SELECT id FROM venues WHERE LOWER(name) = LOWER(?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            System.err.println("[DB] venueExists error: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════
    //  EVENT METHODS  (replaces events.txt)
    // ══════════════════════════════════════════════════════

    /**
     * Returns all events as a list of String arrays.
     * Each array: { event_name, organizer, venue,
     *               date, start_time, end_time, capacity }
     * Called by ViewBookings, ViewConflicts, Dashboard stats,
     * CheckAvailability, and BookingForm conflict check.
     */
    public static List<String[]> getAllEvents() {
        List<String[]> events = new ArrayList<>();
        String sql = "SELECT event_name, organizer, venue, "
                   + "date, start_time, end_time, capacity "
                   + "FROM events ORDER BY date, start_time";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                events.add(new String[]{
                    rs.getString("event_name"),
                    rs.getString("organizer"),
                    rs.getString("venue"),
                    rs.getString("date"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    String.valueOf(rs.getInt("capacity"))
                });
            }
        } catch (Exception e) {
            System.err.println("[DB] getAllEvents error: " + e.getMessage());
        }
        return events;
    }

    /**
     * Returns total number of events.
     * Used by Dashboard "Total Events" stat card.
     */
    public static int getEventCount() {
        String sql = "SELECT COUNT(*) FROM events";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[DB] getEventCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Returns count of events scheduled for a specific date.
     * Used by Dashboard "Today's Bookings" stat card.
     * Pass date as DD-MM-YYYY string.
     */
    public static int getEventCountByDate(String date) {
        String sql = "SELECT COUNT(*) FROM events WHERE date = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.err.println("[DB] getEventCountByDate error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Inserts a new event into the database.
     * Called by BookingForm after conflict check passes.
     *
     * @return true if inserted successfully
     */
    public static boolean addEvent(String eventName,
                                    String organizer,
                                    String venue,
                                    String date,
                                    String startTime,
                                    String endTime,
                                    int    capacity) {
        String sql = "INSERT INTO events "
                   + "(event_name, organizer, venue, date, "
                   + " start_time, end_time, capacity) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, eventName);
            ps.setString(2, organizer);
            ps.setString(3, venue);
            ps.setString(4, date);
            ps.setString(5, startTime);
            ps.setString(6, endTime);
            ps.setInt(7, capacity);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[DB] addEvent error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a specific event identified by name + venue + date + start.
     * Called by ViewBookings "Delete Selected" and
     * ViewConflicts "Resolve → Delete" fallback.
     *
     * @return true if deleted successfully
     */
    public static boolean deleteEvent(String eventName,
                                       String venue,
                                       String date,
                                       String startTime) {
        String sql = "DELETE FROM events "
                   + "WHERE event_name = ? AND venue = ? "
                   + "AND date = ? AND start_time = ? "
                   + "LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, eventName);
            ps.setString(2, venue);
            ps.setString(3, date);
            ps.setString(4, startTime);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[DB] deleteEvent error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the venue of a specific event.
     * Called by ViewConflicts "Resolve → Reassign" when
     * an alternate venue is found.
     *
     * @return true if updated successfully
     */
    public static boolean updateEventVenue(String eventName,
                                            String oldVenue,
                                            String date,
                                            String startTime,
                                            String newVenue) {
        String sql = "UPDATE events SET venue = ? "
                   + "WHERE event_name = ? AND venue = ? "
                   + "AND date = ? AND start_time = ? "
                   + "LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newVenue);
            ps.setString(2, eventName);
            ps.setString(3, oldVenue);
            ps.setString(4, date);
            ps.setString(5, startTime);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("[DB] updateEventVenue error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a venue is already booked at an overlapping time.
     * Can be used as an alternative to Utkarsh's EventValidator
     * by pushing conflict detection into the database query.
     *
     * Logic: same venue, same date, and time ranges overlap
     *   (newStart < existingEnd AND existingStart < newEnd)
     *
     * @return true if a conflict exists
     */
    public static boolean hasConflict(String venue,
                                       String date,
                                       String startTime,
                                       String endTime) {
        // Convert HH:MM strings to minutes for numeric comparison
        int newStart = timeToMinutes(startTime);
        int newEnd   = timeToMinutes(endTime);

        String sql = "SELECT id FROM events "
                   + "WHERE LOWER(venue) = LOWER(?) "
                   + "AND date = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, venue);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Fetch existing start and end from result
                // We need to re-query for time fields
            }

            // Full query with time overlap check
            String fullSql = "SELECT start_time, end_time FROM events "
                           + "WHERE LOWER(venue) = LOWER(?) AND date = ?";
            PreparedStatement ps2 = con.prepareStatement(fullSql);
            ps2.setString(1, venue);
            ps2.setString(2, date);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                int exStart = timeToMinutes(rs2.getString("start_time"));
                int exEnd   = timeToMinutes(rs2.getString("end_time"));
                if (newStart < exEnd && exStart < newEnd) return true;
            }

        } catch (Exception e) {
            System.err.println("[DB] hasConflict error: " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════

    /**
     * Converts "HH:MM" string to total minutes since midnight.
     * Used internally by hasConflict().
     */
    private static int timeToMinutes(String time) {
        try {
            String[] p = time.split(":");
            return Integer.parseInt(p[0].trim()) * 60
                 + Integer.parseInt(p[1].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // ══════════════════════════════════════════════════════
    //  CONNECTION TEST  — run this main() to verify DB setup
    // ══════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.out.println("Testing database connection...\n");

        Connection con = getConnection();
        if (con == null) {
            System.out.println("FAILED — could not connect.");
            System.out.println("Check: MySQL running? Password correct? scheduler_db exists?");
            return;
        }
        System.out.println("Connection: OK");

        // Test users
        System.out.println("\n── Users ─────────────────────────");
        boolean valid = validateUser("admin", "admin123", "Admin");
        System.out.println("Login test (admin/admin123/Admin): "
            + (valid ? "PASS" : "FAIL"));

        // Test venues
        System.out.println("\n── Venues ────────────────────────");
        System.out.println("Total venues : " + getVenueCount());
        List<String[]> venues = getAllVenues();
        for (String[] v : venues)
            System.out.println("  " + v[0] + "  |  capacity: " + v[1]);

        // Test events
        System.out.println("\n── Events ────────────────────────");
        System.out.println("Total events : " + getEventCount());
        List<String[]> events = getAllEvents();
        for (String[] e : events)
            System.out.println("  " + e[0] + "  @  " + e[2]
                + "  on  " + e[3]
                + "  " + e[4] + "–" + e[5]);

        System.out.println("\nAll tests complete.");

        try { con.close(); } catch (Exception e) { /* ignore */ }
    }
}
