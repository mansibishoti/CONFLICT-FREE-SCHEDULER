package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import logic.Event;           
import logic.EventValidator;
import java.util.ArrayList;
import java.util.List;

public class BookingForm extends JFrame {

    // ── INPUT FIELDS ──────────────────────────────────────
    private JTextField eventNameField;
    private JTextField venueField;
    private JTextField dateField;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextField capacityField;
    private JTextField organizerField;
    private JLabel messageLabel;

    private String loggedInUser;
    private String loggedInRole;

    public BookingForm(String username, String role) {
        this.loggedInUser = username;
        this.loggedInRole = role;
        setupWindow();
        setupComponents();
    }

    // ── WINDOW SETUP ──────────────────────────────────────
    private void setupWindow() {
        setTitle("Conflict-Free Scheduler — Book New Event");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = (int)(screen.width  * 0.40);
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
        JPanel header = new JPanel();
        header.setBackground(Color.BLUE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setLayout(new GridBagLayout());

        JLabel headerTitle = new JLabel("Book New Event");
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);

        JLabel headerSub = new JLabel("Fill in the details below to schedule an event");
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

        // ── ROW 0 & 1 : Event Name ────────────────────────
        addLabel(formPanel, "Event Name", gbc, 0);
        eventNameField = new JTextField();
        styleField(eventNameField, "e.g.  Annual Tech Fest");
        gbc.gridy = 1; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(eventNameField, gbc);

        // ── ROW 2 & 3 : Organizer Name ────────────────────
        addLabel(formPanel, "Organizer Name", gbc, 2);
        organizerField = new JTextField();
        styleField(organizerField, "e.g.  Mansi");
        gbc.gridy = 3; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(organizerField, gbc);

        // ── ROW 4 & 5 : Venue ─────────────────────────────
        addLabel(formPanel, "Venue", gbc, 4);
        venueField = new JTextField();
        styleField(venueField, "e.g.  Seminar Hall A");
        gbc.gridy = 5; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(venueField, gbc);

        // ── ROW 6 & 7 : Date ──────────────────────────────
        addLabel(formPanel, "Date  (DD-MM-YYYY)", gbc, 6);
        dateField = new JTextField();
        styleField(dateField, "e.g.  25-12-2025");
        gbc.gridy = 7; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(dateField, gbc);

        // ── ROW 8 & 9 : Start Time ────────────────────────
        addLabel(formPanel, "Start Time  (HH:MM)", gbc, 8);
        startTimeField = new JTextField();
        styleField(startTimeField, "e.g.  09:00");
        gbc.gridy = 9; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(startTimeField, gbc);

        // ── ROW 10 & 11 : End Time ────────────────────────
        addLabel(formPanel, "End Time  (HH:MM)", gbc, 10);
        endTimeField = new JTextField();
        styleField(endTimeField, "e.g.  13:00");
        gbc.gridy = 11; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(endTimeField, gbc);

        // ── ROW 12 & 13 : Capacity ────────────────────────
        addLabel(formPanel, "Capacity  (number of seats)", gbc, 12);
        capacityField = new JTextField();
        styleField(capacityField, "e.g.  120");
        gbc.gridy = 13; gbc.insets = new Insets(4, 0, 20, 0);
        formPanel.add(capacityField, gbc);

        // ── ROW 14 : SUBMIT BUTTON ────────────────────────
        JButton submitBtn = new JButton("SUBMIT BOOKING");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitBtn.setBackground(Color.BLUE);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setPreferredSize(new Dimension(0, 42));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> handleSubmit());
        gbc.gridy = 14; gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(submitBtn, gbc);

        // ── ROW 15 : BACK BUTTON ──────────────────────────
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(Color.BLUE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        gbc.gridy = 15; gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(backBtn, gbc);

        // ── ROW 16 : MESSAGE LABEL ────────────────────────
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 16; gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(messageLabel, gbc);

        // ── SCROLL PANE wraps formPanel ───────────────────
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

    // ── SUBMIT LOGIC ──────────────────────────────────────
    private void handleSubmit() {

        // Step 1 — read all field values
        String eventName  = eventNameField.getText().trim();
        String organizer  = organizerField.getText().trim();
        String venue      = venueField.getText().trim();
        String date       = dateField.getText().trim();
        String startTime  = startTimeField.getText().trim();
        String endTime    = endTimeField.getText().trim();
        String capacity   = capacityField.getText().trim();

        // Step 2 — check no field is empty or still showing placeholder
        if (eventName.isEmpty() || eventName.startsWith("e.g.")
                || organizer.isEmpty() || organizer.startsWith("e.g.")
                || venue.isEmpty()     || venue.startsWith("e.g.")
                || date.isEmpty()      || date.startsWith("e.g.")
                || startTime.isEmpty() || startTime.startsWith("e.g.")
                || endTime.isEmpty()   || endTime.startsWith("e.g.")
                || capacity.isEmpty()  || capacity.startsWith("e.g.")) {
            showMessage("Please fill in all fields.", Color.RED);
            return;
        }

        // Step 3 — validate capacity is a number
        try {
            int cap = Integer.parseInt(capacity);
            if (cap <= 0) {
                showMessage("Capacity must be a positive number.", Color.RED);
                return;
            }
        } catch (NumberFormatException ex) {
            showMessage("Capacity must be a valid number.", Color.RED);
            return;
        }

        // Step 4 — conflict check using DB
        List<String[]> existingEvents = db.DatabaseHandler.getAllEvents();
        List<Event> eventList = new ArrayList<Event>();
        for (String[] p : existingEvents)
            eventList.add(fromCSVLine(p));
        
        EventValidator validator = new EventValidator();
        if (!validator.canSchedule(fromCSVLine(new String[]{
                eventName, organizer, venue, date,
                startTime, endTime, capacity}), eventList)) {
            showMessage("Conflict! " + venue +
                " is already booked at this time.", Color.RED);
            return;
        }
        
        // Step 5 — save to DB instead of file
        int cap = Integer.parseInt(capacity);
        boolean saved = db.DatabaseHandler.addEvent(
            eventName, organizer, venue, date, startTime, endTime, cap);
        
        if (saved) {
            showMessage("Event booked successfully!", Color.GREEN);
            clearFields();
        } else {
            showMessage("Error saving event to database.", Color.RED);
        }
    }

    // ── CLEAR ALL FIELDS AFTER SUCCESS ────────────────────
    private void clearFields() {
        eventNameField.setText("");
        organizerField.setText("");
        venueField.setText("");
        dateField.setText("");
        startTimeField.setText("");
        endTimeField.setText("");
        capacityField.setText("");
    }

    // ── HELPER : add a gray label ─────────────────────────
    private void addLabel(JPanel p, String text,
                          GridBagConstraints gbc, int row) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        gbc.gridy  = row;
        gbc.insets = new Insets(0, 0, 2, 0);
        p.add(lbl, gbc);
    }

    // ── HELPER : style a field with placeholder text ──────
    private void styleField(JTextField f, String placeholder) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        // Placeholder text logic
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

    // ── HELPER : show message ─────────────────────────────
    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    // ── HELPER : convert CSV parts → Event object ─────────
public static Event fromCSVLine(String[] parts) {
    String[] dateParts  = parts[3].split("-");  // DD-MM-YYYY
    String[] startParts = parts[4].split(":");  // HH:MM
    String[] endParts   = parts[5].split(":");  // HH:MM

    return new Event(
        parts[2],                        // venue
        Integer.parseInt(dateParts[2]),  // year
        Integer.parseInt(dateParts[1]),  // month
        Integer.parseInt(dateParts[0]),  // day
        Integer.parseInt(startParts[0]), // startHour
        Integer.parseInt(startParts[1]), // startMinute
        Integer.parseInt(endParts[0]),   // endHour
        Integer.parseInt(endParts[1])    // endMinute
    );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new BookingForm("Mansi", "Admin").setVisible(true));
    }
}