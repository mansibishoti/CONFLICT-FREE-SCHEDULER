package ui;

import javax.swing.*;
import logic.AlertThread;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CheckAvailability extends JFrame {

    // ── INPUT FIELDS ──────────────────────────────────────
    private JTextField venueField;
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JLabel messageLabel;
    private JPanel resultCard;
    private JLabel resultIcon;
    private JLabel resultTitle;
    private JLabel resultDetail;

    private String loggedInUser;
    private String loggedInRole;

    public CheckAvailability(String username, String role) {
        this.loggedInUser = username;
        this.loggedInRole = role;
        setupWindow();
        setupComponents();
    }

    // ── WINDOW SETUP ──────────────────────────────────────
    private void setupWindow() {
        setTitle("Conflict-Free Scheduler — Check Availability");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = (int)(screen.width  * 0.38);
        int height = (int)(screen.height * 0.75);
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
        JPanel header = new JPanel();
        header.setBackground(Color.BLUE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setLayout(new GridBagLayout());

        JLabel headerTitle = new JLabel("Check Availability");
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);

        JLabel headerSub = new JLabel("Check if a venue is free at a specific date and time");
        headerSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        headerSub.setForeground(Color.LIGHT_GRAY);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setBackground(Color.BLUE);
        headerText.add(headerTitle);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(headerSub);
        header.add(headerText);

        add(header, BorderLayout.NORTH);

        // ── FORM PANEL ────────────────────────────────────
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weightx   = 1.0;

        // ── ROW 0 & 1 : Venue ─────────────────────────────
        addLabel(formPanel, "Venue", gbc, 0);
        venueField = new JTextField();
        styleField(venueField, "e.g.  Seminar Hall A");
        gbc.gridy = 1; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(venueField, gbc);

        // ── ROW 2 & 3 : Date ──────────────────────────────
        addLabel(formPanel, "Date  (DD-MM-YYYY)", gbc, 2);
        dateField = new JTextField();
        styleField(dateField, "e.g.  25-12-2025");
        gbc.gridy = 3; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(dateField, gbc);

        // ── ROW 4 & 5 : Start Time ────────────────────────
        addLabel(formPanel, "Start Time  (HH:MM)", gbc, 4);
        startTimeField = new JTextField();
        styleField(startTimeField, "e.g.  09:00");
        gbc.gridy = 5; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(startTimeField, gbc);

        // ── ROW 6 & 7 : End Time ──────────────────────────
        addLabel(formPanel, "End Time  (HH:MM)", gbc, 6);
        endTimeField = new JTextField();
        styleField(endTimeField, "e.g.  13:00");
        gbc.gridy = 7; gbc.insets = new Insets(4, 0, 20, 0);
        formPanel.add(endTimeField, gbc);

        // ── ROW 8 : CHECK BUTTON ──────────────────────────
        JButton checkBtn = new JButton("CHECK AVAILABILITY");
        checkBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        checkBtn.setBackground(Color.BLUE);
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFocusPainted(false);
        checkBtn.setBorderPainted(false);
        checkBtn.setPreferredSize(new Dimension(0, 42));
        checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBtn.addActionListener(e -> handleCheck());
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(checkBtn, gbc);

        // ── ROW 9 : RESULT CARD ───────────────────────────
        resultCard = new JPanel();
        resultCard.setLayout(new BorderLayout(12, 0));
        resultCard.setBackground(Color.WHITE);
        resultCard.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        resultCard.setVisible(false);  // hidden until a check is run

        // Left coloured circle icon
        resultIcon = new JLabel("", SwingConstants.CENTER) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                int x = (getWidth()  - fm.stringWidth(txt)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(txt, x, y);
            }
        };
        resultIcon.setPreferredSize(new Dimension(46, 46));
        resultIcon.setOpaque(false);

        // Right text block
        JPanel resultTextPanel = new JPanel();
        resultTextPanel.setLayout(new BoxLayout(resultTextPanel, BoxLayout.Y_AXIS));
        resultTextPanel.setBackground(Color.WHITE);

        resultTitle = new JLabel();
        resultTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        resultDetail = new JLabel();
        resultDetail.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultDetail.setForeground(Color.GRAY);

        resultTextPanel.add(resultTitle);
        resultTextPanel.add(Box.createVerticalStrut(4));
        resultTextPanel.add(resultDetail);

        resultCard.add(resultIcon,      BorderLayout.WEST);
        resultCard.add(resultTextPanel, BorderLayout.CENTER);

        gbc.gridy = 9; gbc.insets = new Insets(6, 0, 10, 0);
        formPanel.add(resultCard, gbc);

        // ── ROW 10 : MESSAGE LABEL ────────────────────────
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 10; gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(messageLabel, gbc);

        // ── ROW 11 : BACK BUTTON ──────────────────────────
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(Color.BLUE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        gbc.gridy = 11; gbc.insets = new Insets(4, 0, 0, 0);
        formPanel.add(backBtn, gbc);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

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

    // ── CHECK LOGIC ───────────────────────────────────────
    private void handleCheck() {
        String venue     = venueField.getText().trim();
        String date      = dateField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime   = endTimeField.getText().trim();

        // Reject placeholder text or empty fields
        if (venue.isEmpty()     || venue.startsWith("e.g.")
         || date.isEmpty()      || date.startsWith("e.g.")
         || startTime.isEmpty() || startTime.startsWith("e.g.")
         || endTime.isEmpty()   || endTime.startsWith("e.g.")) {
            showMessage("Please fill in all fields.", Color.RED);
            resultCard.setVisible(false);
            return;
        }

        // Basic time parse to compare HH:MM
        int[] start = parseTime(startTime);
        int[] end   = parseTime(endTime);
        if (start == null || end == null) {
            showMessage("Time must be in HH:MM format.", Color.RED);
            resultCard.setVisible(false);
            return;
        }
        if (toMinutes(end) <= toMinutes(start)) {
            showMessage("End time must be after start time.", Color.RED);
            resultCard.setVisible(false);
            return;
        }

        showMessage(" ", Color.BLACK);

        boolean conflict = checkConflictInDB(venue, date, startTime, endTime);
        // In handleCheck(), after boolean conflict = checkConflictInDB(...)
        String condition = conflict ? "FULL" : "AVAILABLE";
        new AlertThread(condition).start();

        resultCard.setVisible(true);
        if (!conflict) {
            // AVAILABLE
            resultIcon.setText("✓");
            resultIcon.setBackground(new Color(34, 197, 94));   // green
            resultTitle.setText("Venue is Available!");
            resultTitle.setForeground(new Color(22, 163, 74));
            resultDetail.setText(venue + "  |  " + date
                               + "  |  " + startTime + " – " + endTime);
            resultCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(34, 197, 94), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
        } else {
            // CONFLICT
            resultIcon.setText("✗");
            resultIcon.setBackground(new Color(239, 68, 68));   // red
            resultTitle.setText("Conflict Detected!");
            resultTitle.setForeground(new Color(220, 38, 38));
            resultDetail.setText("Another event is booked at "
                               + venue + " on " + date
                               + " that overlaps this time.");
            resultCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(239, 68, 68), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
        }

        revalidate();
        repaint();
    }

    // ── CHECK CONFLICT FROM DATABASE ─────────────────────
    private boolean checkConflictInDB(String venue, String date,
                                       String startTime, String endTime) {
        int newStart = toMinutes(parseTime(startTime));
        int newEnd   = toMinutes(parseTime(endTime));

        // Fetch all events from DB and check for overlap
        List<String[]> events = db.DatabaseHandler.getAllEvents();
        for (String[] ev : events) {
            String existingVenue = ev[2].trim();
            String existingDate  = ev[3].trim();
            String existingStart = ev[4].trim();
            String existingEnd   = ev[5].trim();

            if (!existingVenue.equalsIgnoreCase(venue)) continue;
            if (!existingDate.equals(date))             continue;

            int[] es = parseTime(existingStart);
            int[] ee = parseTime(existingEnd);
            if (es == null || ee == null) continue;

            if (newStart < toMinutes(ee) && toMinutes(es) < newEnd)
                return true;  // conflict found
        }
        return false;
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

    private int toMinutes(int[] hm) {
        return hm[0] * 60 + hm[1];
    }

    // ── HELPERS ───────────────────────────────────────────
    private void addLabel(JPanel p, String text,
                          GridBagConstraints gbc, int row) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        gbc.gridy  = row;
        gbc.insets = new Insets(0, 0, 2, 0);
        p.add(lbl, gbc);
    }

    private void styleField(JTextField f, String placeholder) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        f.setText(placeholder);
        f.setForeground(Color.GRAY);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText("");
                    f.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new CheckAvailability("Mansi", "Admin").setVisible(true));
    }
}