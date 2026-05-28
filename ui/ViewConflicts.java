package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ViewConflicts extends JFrame {

    private String loggedInUser;
    private String loggedInRole;

    private JTable conflictsTable;
    private DefaultTableModel tableModel;
    private JLabel messageLabel;
    private JLabel summaryLabel;

    public ViewConflicts(String username, String role) {
        this.loggedInUser = username;
        this.loggedInRole = role;
        setupWindow();
        setupComponents();
        loadConflicts();      // reads from database and fills the table
    }

    // ── WINDOW SETUP ──────────────────────────────────────
    private void setupWindow() {
        setTitle("Conflict-Free Scheduler — View Conflicts");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = (int)(screen.width  * 0.88);
        int height = (int)(screen.height * 0.80);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    // ── BUILD ALL COMPONENTS ──────────────────────────────
    private void setupComponents() {
        setLayout(new BorderLayout());

        // ── HEADER ────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.BLUE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setBackground(Color.BLUE);

        JLabel headerTitle = new JLabel("Conflict Viewer");
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);

        JLabel headerSub = new JLabel(
            "Events at the same venue and date whose times overlap");
        headerSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        headerSub.setForeground(Color.LIGHT_GRAY);

        headerLeft.add(headerTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(headerSub);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(Color.BLUE);
        refreshBtn.setBorder(new LineBorder(Color.WHITE, 1, true));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadConflicts());

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(refreshBtn,  BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── TABLE SETUP ───────────────────────────────────
        String[] columns = {
            "#",
            "Event A", "Organizer A", "Venue", "Date",
            "Start A", "End A",
            "Event B", "Organizer B",
            "Start B", "End B",
            "Overlap"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        conflictsTable = new JTable(tableModel);
        conflictsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        conflictsTable.setRowHeight(40);
        conflictsTable.setGridColor(Color.LIGHT_GRAY);
        conflictsTable.setSelectionBackground(new Color(254, 226, 226));
        conflictsTable.setSelectionForeground(Color.BLACK);
        conflictsTable.setShowVerticalLines(true);
        conflictsTable.setShowHorizontalLines(true);

        JTableHeader tableHeader = conflictsTable.getTableHeader();
        tableHeader.setBackground(new Color(220, 38, 38));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
        tableHeader.setReorderingAllowed(false);

        int[] widths = {32, 170, 110, 140, 90, 65, 65, 170, 110, 65, 65, 80};
        for (int i = 0; i < widths.length; i++)
            conflictsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        conflictsTable.setDefaultRenderer(Object.class,
            new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                    if (!isSelected)
                        c.setBackground(row % 2 == 0
                            ? new Color(255, 248, 248)
                            : new Color(255, 237, 237));
                    ((JLabel) c).setBorder(
                        BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return c;
                }
            });

        JScrollPane tableScroll = new JScrollPane(conflictsTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 8, 24));

        // ── CENTER WRAPPER (summary banner + table) ────────
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.WHITE);

        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 0, 24));

        summaryLabel = new JLabel(" ", SwingConstants.LEFT);
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        summaryLabel.setOpaque(true);
        summaryLabel.setBackground(new Color(255, 245, 245));
        summaryLabel.setForeground(new Color(220, 38, 38));
        summaryLabel.setBorder(new CompoundBorder(
            new LineBorder(new Color(254, 202, 202), 1, true),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        bannerPanel.add(summaryLabel, BorderLayout.CENTER);

        centerWrapper.add(bannerPanel, BorderLayout.NORTH);
        centerWrapper.add(tableScroll, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // ── BOTTOM PANEL ──────────────────────────────────
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        if (loggedInRole.equalsIgnoreCase("Admin")) {
            JButton resolveBtn = new JButton("Resolve Selected");
            resolveBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            resolveBtn.setBackground(new Color(220, 38, 38));
            resolveBtn.setForeground(Color.WHITE);
            resolveBtn.setFocusPainted(false);
            resolveBtn.setBorderPainted(false);
            resolveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            resolveBtn.addActionListener(e -> resolveSelected());
            btnPanel.add(resolveBtn);
        }

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backBtn.setBackground(Color.BLUE);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        btnPanel.add(backBtn);

        bottomPanel.add(messageLabel, BorderLayout.WEST);
        bottomPanel.add(btnPanel,     BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ── LOAD + DETECT CONFLICTS FROM DATABASE ─────────────
    private void loadConflicts() {
        tableModel.setRowCount(0);
        messageLabel.setText(" ");

        // Step 1 — fetch all events from DB
        List<String[]> events = db.DatabaseHandler.getAllEvents();

        if (events.isEmpty()) {
            summaryLabel.setText("  No events found in the database.");
            summaryLabel.setForeground(new Color(22, 163, 74));
            summaryLabel.setBackground(new Color(240, 253, 244));
            summaryLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(134, 239, 172), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
            return;
        }

        // Step 2 — compare every pair for overlap
        int conflictNumber = 0;
        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {
                String[] a = events.get(i);
                String[] b = events.get(j);

                // Same venue and same date?
                if (!a[2].trim().equalsIgnoreCase(b[2].trim())) continue;
                if (!a[3].trim().equals(b[3].trim()))           continue;

                int[] as = parseTime(a[4].trim());
                int[] ae = parseTime(a[5].trim());
                int[] bs = parseTime(b[4].trim());
                int[] be = parseTime(b[5].trim());
                if (as == null || ae == null || bs == null || be == null) continue;

                int aStart = toMinutes(as), aEnd = toMinutes(ae);
                int bStart = toMinutes(bs), bEnd = toMinutes(be);

                // Overlap condition
                if (aStart < bEnd && bStart < aEnd) {
                    conflictNumber++;
                    int overlapStart = Math.max(aStart, bStart);
                    int overlapEnd   = Math.min(aEnd,   bEnd);
                    String overlapStr = minutesToTime(overlapStart)
                                      + "–" + minutesToTime(overlapEnd);

                    tableModel.addRow(new Object[]{
                        conflictNumber,
                        a[0].trim(),   // Event A name
                        a[1].trim(),   // Organizer A
                        a[2].trim(),   // Venue (shared)
                        a[3].trim(),   // Date  (shared)
                        a[4].trim(),   // Start A
                        a[5].trim(),   // End A
                        b[0].trim(),   // Event B name
                        b[1].trim(),   // Organizer B
                        b[4].trim(),   // Start B
                        b[5].trim(),   // End B
                        overlapStr
                    });
                }
            }
        }

        // Step 3 — update summary banner
        if (conflictNumber == 0) {
            summaryLabel.setText(
                "  No conflicts detected. All " + events.size()
                + " event(s) are scheduled without clashes.");
            summaryLabel.setForeground(new Color(22, 163, 74));
            summaryLabel.setBackground(new Color(240, 253, 244));
            summaryLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(134, 239, 172), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
        } else {
            summaryLabel.setText(
                "  " + conflictNumber + " conflict(s) detected across "
                + events.size() + " event(s). Review and resolve below.");
            summaryLabel.setForeground(new Color(220, 38, 38));
            summaryLabel.setBackground(new Color(255, 245, 245));
            summaryLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(254, 202, 202), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
        }
    }

    // ── RESOLVE: find alternate venue or delete ────────────
    private void resolveSelected() {
        int selectedRow = conflictsTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Please select a conflict row to resolve.", Color.RED);
            return;
        }

        String eventAName = tableModel.getValueAt(selectedRow, 1).toString();
        String organizerA = tableModel.getValueAt(selectedRow, 2).toString();
        String venue      = tableModel.getValueAt(selectedRow, 3).toString();
        String date       = tableModel.getValueAt(selectedRow, 4).toString();
        String startA     = tableModel.getValueAt(selectedRow, 5).toString();
        String endA       = tableModel.getValueAt(selectedRow, 6).toString();

        // Try to find a free alternate venue from DB
        String alternate = findAlternateVenue(venue, date, startA, endA);

        if (alternate != null) {
            // ── ALTERNATE FOUND → offer to reassign ───────
            int choice = JOptionPane.showConfirmDialog(this,
                "Conflict detected for:\n\n"
                + "  Event  :  \"" + eventAName + "\"\n"
                + "  Venue  :  " + venue + "\n"
                + "  Date   :  " + date
                + "     Time  :  " + startA + " – " + endA + "\n\n"
                + "An alternate venue is available:\n\n"
                + "  \u2713   " + alternate + "\n\n"
                + "Reassign \"" + eventAName
                + "\" to \"" + alternate + "\"?",
                "Alternate Venue Found",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) return;

            boolean updated = db.DatabaseHandler.updateEventVenue(
                eventAName, venue, date, startA, alternate);

            if (updated) {
                loadConflicts();
                showMessage("Resolved — \"" + eventAName
                    + "\" moved to " + alternate + ".",
                    new Color(22, 163, 74));
            } else {
                showMessage("Error updating venue in database.", Color.RED);
            }

        } else {
            // ── NO ALTERNATE → offer to delete ────────────
            int choice = JOptionPane.showConfirmDialog(this,
                "Conflict detected for:\n\n"
                + "  Event  :  \"" + eventAName + "\"\n"
                + "  Venue  :  " + venue + "\n"
                + "  Date   :  " + date
                + "     Time  :  " + startA + " – " + endA + "\n\n"
                + "No alternate venue is available at this time slot.\n\n"
                + "Delete \"" + eventAName + "\" to resolve the conflict?",
                "No Alternate Venue Available",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) return;

            boolean deleted = db.DatabaseHandler.deleteEvent(
                eventAName, venue, date, startA);

            if (deleted) {
                loadConflicts();
                showMessage("Resolved — \"" + eventAName + "\" deleted.",
                    Color.RED);
            } else {
                showMessage("Error deleting event from database.", Color.RED);
            }
        }
    }

    // ── FIND FREE ALTERNATE VENUE FROM DB ─────────────────
    // Reads master venue list from DB, checks each against
    // existing events for the requested date + time slot.
    private String findAlternateVenue(String conflictVenue,
                                       String date,
                                       String startTime,
                                       String endTime) {
        // Get all registered venues from DB
        List<String[]> venues = db.DatabaseHandler.getAllVenues();
        if (venues.isEmpty()) return null;

        // Get all events from DB
        List<String[]> allEvents = db.DatabaseHandler.getAllEvents();

        int reqStart = toMinutes(parseTime(startTime));
        int reqEnd   = toMinutes(parseTime(endTime));

        for (String[] v : venues) {
            String candidate = v[0].trim();

            // Skip the conflicting venue itself
            if (candidate.equalsIgnoreCase(conflictVenue)) continue;

            // Check if this candidate is free at the requested slot
            boolean occupied = false;
            for (String[] ev : allEvents) {
                if (!ev[2].trim().equalsIgnoreCase(candidate)) continue;
                if (!ev[3].trim().equals(date))                continue;

                int[] es = parseTime(ev[4].trim());
                int[] ee = parseTime(ev[5].trim());
                if (es == null || ee == null) continue;

                if (reqStart < toMinutes(ee) && toMinutes(es) < reqEnd) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) return candidate;  // free venue found
        }
        return null;  // all venues occupied at this slot
    }

    // ── TIME HELPERS ──────────────────────────────────────
    private int[] parseTime(String t) {
        try {
            String[] parts = t.split(":");
            if (parts.length != 2) return null;
            int h = Integer.parseInt(parts[0].trim());
            int m = Integer.parseInt(parts[1].trim());
            if (h < 0 || h > 23 || m < 0 || m > 59) return null;
            return new int[]{h, m};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int toMinutes(int[] hm) { return hm[0] * 60 + hm[1]; }

    private String minutesToTime(int mins) {
        return String.format("%02d:%02d", mins / 60, mins % 60);
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new ViewConflicts("Mansi", "Admin").setVisible(true));
    }
}