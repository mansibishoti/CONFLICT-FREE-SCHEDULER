package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ViewBookings extends JFrame {

    private String loggedInUser;
    private String loggedInRole;

    // the table and its data model
    private JTable bookingsTable;
    private DefaultTableModel tableModel;

    // message label for feedback
    private JLabel messageLabel;

    public ViewBookings(String username, String role) {
        this.loggedInUser = username;
        this.loggedInRole = role;
        setupWindow();
        setupComponents();
        loadBookings();       // reads from database and fills the table
    }

    // ── WINDOW SETUP ──────────────────────────────────────
    private void setupWindow() {
        setTitle("Conflict-Free Scheduler — View All Bookings");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = (int)(screen.width  * 0.85);
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

        // left side of header — title + subtitle
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setBackground(Color.BLUE);

        JLabel headerTitle = new JLabel("All Bookings");
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);

        JLabel headerSub = new JLabel("Viewing as: " + loggedInUser + "  |  " + loggedInRole);
        headerSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        headerSub.setForeground(Color.LIGHT_GRAY);

        headerLeft.add(headerTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(headerSub);

        // right side of header — refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(Color.BLUE);
        refreshBtn.setBorder(new LineBorder(Color.WHITE, 1, true));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadBookings());

        header.add(headerLeft,  BorderLayout.WEST);
        header.add(refreshBtn,  BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── TABLE SETUP ───────────────────────────────────
        // column names match the order saved in events.txt
        String[] columns = {
            "#", "Event Name", "Organizer",
            "Venue", "Date", "Start", "End", "Capacity"
        };

        // DefaultTableModel holds the table data
        // the anonymous subclass makes all cells non-editable
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;   // user cannot type into the table
            }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        bookingsTable.setRowHeight(42);
        bookingsTable.setGridColor(Color.LIGHT_GRAY);
        bookingsTable.setSelectionBackground(Color.CYAN);
        bookingsTable.setSelectionForeground(Color.BLACK);
        bookingsTable.setShowVerticalLines(true);
        bookingsTable.setShowHorizontalLines(true);

        // style the column headers
        JTableHeader tableHeader = bookingsTable.getTableHeader();
        tableHeader.setBackground(Color.BLUE);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 15));
        tableHeader.setReorderingAllowed(false); // columns cant be dragged

        // set column widths
        bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // #
        bookingsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // event name
        bookingsTable.getColumnModel().getColumn(2).setPreferredWidth(140); // organizer
        bookingsTable.getColumnModel().getColumn(3).setPreferredWidth(160); // venue
        bookingsTable.getColumnModel().getColumn(4).setPreferredWidth(110);  // date
        bookingsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // start
        bookingsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // end
        bookingsTable.getColumnModel().getColumn(7).setPreferredWidth(90);  // capacity

        // alternate row colors for readability
        bookingsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE
                                                 : new Color(240, 245, 255));
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // wrap table in scroll pane so headers stay visible
        JScrollPane tableScroll = new JScrollPane(bookingsTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        add(tableScroll, BorderLayout.CENTER);

        // ── BOTTOM PANEL — message + buttons ──────────────
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));

        // message label on the left
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // button panel on the right
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        // delete button — only shown to Admin
        if (loggedInRole.equalsIgnoreCase("Admin")) {
            JButton deleteBtn = new JButton("Delete Selected");
            deleteBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            deleteBtn.setBackground(Color.RED);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteBtn.addActionListener(e -> deleteSelectedBooking());
            btnPanel.add(deleteBtn);
        }

        // back button — everyone sees this
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

    private void loadBookings() {
    tableModel.setRowCount(0);
    List<String[]> events = db.DatabaseHandler.getAllEvents();
    int row = 1;
    for (String[] e : events) {
        tableModel.addRow(new Object[]{
            row++, e[0], e[1], e[2], e[3], e[4], e[5], e[6]
        });
    }
    if (tableModel.getRowCount() == 0)
        showMessage("No bookings found.", Color.GRAY);
    else
        showMessage("Showing " + tableModel.getRowCount()
            + " booking(s).", Color.GREEN);
    }

    // ── DELETE SELECTED ROW — Admin only ──────────────────
    private void deleteSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();

        // no row selected
        if (selectedRow == -1) {
            showMessage("Please select a booking to delete.", Color.RED);
            return;
        }

        // confirm before deleting
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this booking?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // get the event name of selected row to identify it in the file
        String selectedEventName = tableModel.getValueAt(selectedRow, 1).toString();
        String selectedVenue     = tableModel.getValueAt(selectedRow, 3).toString();
        String selectedDate      = tableModel.getValueAt(selectedRow, 4).toString();
        String selectedStart = tableModel.getValueAt(selectedRow, 5).toString();

        // delete from database
        boolean deleted = db.DatabaseHandler.deleteEvent(
            selectedEventName, selectedVenue, selectedDate, selectedStart);
        
        if (deleted) {
            loadBookings();
            showMessage("Booking deleted successfully.", Color.RED);
        } else {
            showMessage("Error deleting from database.", Color.RED);
        }
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new ViewBookings("Mansi", "Admin").setVisible(true));
    }
}