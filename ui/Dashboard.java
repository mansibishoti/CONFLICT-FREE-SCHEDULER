package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Dashboard extends JFrame {

    private String loggedInUser;
    private String loggedInRole;

    // ── LIVE STAT LABELS (updated by loadStats) ───────────
    private JLabel totalEventsVal;
    private JLabel venuesVal;
    private JLabel conflictsVal;
    private JLabel todayVal;

    public Dashboard(String username, String role) {
        this.loggedInUser = username;
        this.loggedInRole = role;
        setupWindow();
        setupComponents();
    }

    private void setupWindow() {
        setTitle("Conflict-Free Scheduler — Dashboard");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = (int)(screen.width  * 0.55);
        int height = (int)(screen.height * 0.70);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Refresh live stats whenever the dashboard window comes back into focus
        // (e.g. after the user closes BookingForm or ViewBookings)
        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) { loadStats(); }
        });
    }

    private void setupComponents() {
        setLayout(new BorderLayout());

        // ── TOP NAVBAR ────────────────────────────────────
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.BLUE);
        navbar.setPreferredSize(new Dimension(0, 65));
        navbar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left side — logo + title
        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        navLeft.setBackground(Color.BLUE);

        // CFS circle logo
        JLabel logo = new JLabel("CFS", SwingConstants.CENTER) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.BLUE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth("CFS")) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("CFS", x, y);
            }
        };
        logo.setPreferredSize(new Dimension(38, 38));

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setBackground(Color.BLUE);
        JLabel navTitle = new JLabel("Conflict-Free Scheduler");
        navTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        navTitle.setForeground(Color.WHITE);
        JLabel navSub = new JLabel("Event Management System");
        navSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        navSub.setForeground(Color.LIGHT_GRAY);
        titleStack.add(navTitle);
        titleStack.add(navSub);

        navLeft.add(logo);
        navLeft.add(titleStack);

        // Right side — user info + logout
        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        navRight.setBackground(Color.BLUE);

        JLabel userInfo = new JLabel("Welcome, " + loggedInUser
                                   + "  |  " + loggedInRole);
        userInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userInfo.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(Color.BLUE);
        logoutBtn.setBorder(new LineBorder(Color.WHITE, 1, true));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen().setVisible(true);
        });

        navRight.add(userInfo);
        navRight.add(logoutBtn);

        navbar.add(navLeft,  BorderLayout.WEST);
        navbar.add(navRight, BorderLayout.EAST);
        add(navbar, BorderLayout.NORTH);

        // ── CENTER CONTENT ────────────────────────────────
        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Stats row — values filled by loadStats()
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setBackground(Color.WHITE);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Build the four cards and keep references to the value labels
        JPanel[] cards = new JPanel[4];
        String[] labels = {"Total Events", "Venues Available",
                           "Conflicts Detected", "Today's Bookings"};
        Color[]  colors = {Color.BLUE, Color.GREEN, Color.RED, Color.BLUE};

        totalEventsVal = new JLabel("…");
        venuesVal      = new JLabel("…");
        conflictsVal   = new JLabel("…");
        todayVal       = new JLabel("…");
        JLabel[] vals  = {totalEventsVal, venuesVal, conflictsVal, todayVal};

        for (int i = 0; i < 4; i++) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            ));
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setForeground(Color.GRAY);
            vals[i].setFont(new Font("SansSerif", Font.BOLD, 22));
            vals[i].setForeground(colors[i]);
            card.add(lbl);
            card.add(Box.createVerticalStrut(4));
            card.add(vals[i]);
            statsRow.add(card);
        }

        loadStats();   // populate real numbers from events.txt
        center.add(statsRow);
        center.add(Box.createVerticalStrut(20));

        // Action buttons grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(Color.WHITE);
        grid.add(makeActionCard("Book New Event",
        "Schedule a new event at a venue",
        Color.CYAN, Color.BLUE,
        e -> openBookingForm()));

        // FIXED
        grid.add(makeActionCard("Check Availability",
            "Check if a venue is free at a time",
            Color.GREEN, Color.WHITE,
            e -> new CheckAvailability(loggedInUser, loggedInRole).setVisible(true)));

        grid.add(makeActionCard("View All Bookings",
            "See all scheduled events",
            Color.ORANGE, Color.WHITE,
            e -> new ViewBookings(loggedInUser, loggedInRole).setVisible(true)));

        grid.add(makeActionCard("View Conflicts",
            "Detect and resolve clashes",
            Color.RED, Color.WHITE,
            e -> new ViewConflicts(loggedInUser, loggedInRole).setVisible(true)));

        center.add(grid);
        add(center, BorderLayout.CENTER);

        // ── FOOTER ────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JLabel footerLabel = new JLabel(
            "Conflict-Free Scheduler v1.0  |  Team Project",
            SwingConstants.CENTER);
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footer.add(footerLabel);
        add(footer, BorderLayout.SOUTH);
    }

    // ── HELPER: stat card ─────────────────────────────────
    private JPanel makeStatCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 22));
        val.setForeground(valueColor);
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(val);
        return card;
    }

    // ── HELPER: action card ───────────────────────────────
    private JPanel makeActionCard(String title, String subtitle,
                                   Color iconBg, Color iconColor,
                                   ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon box
        JPanel iconBox = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
        };
        iconBox.setPreferredSize(new Dimension(42, 42));
        iconBox.setOpaque(false);

        JLabel iconLabel = new JLabel(title.substring(0, 1), SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        iconLabel.setForeground(iconColor);
        iconBox.setLayout(new BorderLayout());
        iconBox.add(iconLabel);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(Color.BLACK);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(Color.GRAY);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subLabel);

        card.add(iconBox,   BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        // Click anywhere on card
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
            public void mouseEntered(MouseEvent e) {
                card.setBackground(Color.LIGHT_GRAY);
                textPanel.setBackground(Color.LIGHT_GRAY);
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                textPanel.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    // ── COMPUTE REAL STATS FROM events.txt ────────────────
    private void loadStats() {
    // Get today's date in DD-MM-YYYY format
    java.time.LocalDate now = java.time.LocalDate.now();
    String todayStr = String.format("%02d-%02d-%04d",
        now.getDayOfMonth(), now.getMonthValue(), now.getYear());

    int total       = db.DatabaseHandler.getEventCount();
    int venues      = db.DatabaseHandler.getVenueCount();
    int todayCount  = db.DatabaseHandler.getEventCountByDate(todayStr);

    // Conflict count — still compute from event list
    List<String[]> events = db.DatabaseHandler.getAllEvents();
    int conflicts = 0;
    for (int i = 0; i < events.size(); i++) {
        for (int j = i + 1; j < events.size(); j++) {
            String[] a = events.get(i), b = events.get(j);
            if (!a[2].equalsIgnoreCase(b[2])) continue;
            if (!a[3].equals(b[3]))           continue;
            int[] as = parseTime(a[4]), ae = parseTime(a[5]);
            int[] bs = parseTime(b[4]), be = parseTime(b[5]);
            if (as==null||ae==null||bs==null||be==null) continue;
            if (toMinutes(as) < toMinutes(be)
             && toMinutes(bs) < toMinutes(ae)) conflicts++;
        }
    }

    totalEventsVal.setText(String.valueOf(total));
    venuesVal.setText(String.valueOf(venues));
    conflictsVal.setText(String.valueOf(conflicts));
    todayVal.setText(String.valueOf(todayCount));
    }

    // ── TIME HELPERS (shared with stat calculation) ────────
    private int[] parseTime(String t) {
        try {
            String[] p = t.split(":");
            if (p.length != 2) return null;
            int h = Integer.parseInt(p[0].trim());
            int m = Integer.parseInt(p[1].trim());
            if (h < 0 || h > 23 || m < 0 || m > 59) return null;
            return new int[]{h, m};
        } catch (NumberFormatException e) { return null; }
    }

    private int toMinutes(int[] hm) { return hm[0] * 60 + hm[1]; }

    private void openPlaceholder(String screenName) {
        JFrame frame = new JFrame(screenName);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int)(screen.width * 0.45), (int)(screen.height * 0.60));
        frame.setLocationRelativeTo(this);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel(screenName, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.BLUE);

        JLabel sub = new JLabel("This screen is under development.", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(Color.GRAY);

        JButton back = new JButton("← Back to Dashboard");
        back.setFont(new Font("SansSerif", Font.PLAIN, 13));
        back.setBackground(Color.BLUE);
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.setBorderPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> frame.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(back);

        panel.add(title, BorderLayout.NORTH);
        panel.add(sub,   BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void openBookingForm() {
    if (loggedInRole.equalsIgnoreCase("Admin")) {
        new BookingForm(loggedInUser, loggedInRole).setVisible(true);
    } else {
        JOptionPane.showMessageDialog(this,
            "Access Denied. Only Admins can book events.",
            "Permission Error",
            JOptionPane.WARNING_MESSAGE);
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new Dashboard("Mansi", "Admin").setVisible(true));
    }
}