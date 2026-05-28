package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleSelector;
    private JLabel messageLabel;

    public LoginScreen() {
        setupWindow();
        setupComponents();
    }

    private void setupWindow() {
        setTitle("Conflict-Free Scheduler — Login");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = (int)(screen.width  * 0.30);
        int height = (int)(screen.height * 0.65);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    private void setupComponents() {
        setLayout(new BorderLayout());

        // ── TOP HEADER PANEL ──────────────────────────────
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.BLUE);
        headerPanel.setPreferredSize(new Dimension(420, 110));
        headerPanel.setLayout(new GridBagLayout());

        GridBagConstraints hgbc = new GridBagConstraints();
        hgbc.gridx = 0; hgbc.gridy = 0;

        // Logo circle
        JLabel logoLabel = new JLabel("CFS", SwingConstants.CENTER) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.BLUE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth("CFS")) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("CFS", x, y);
            }
        };
        logoLabel.setPreferredSize(new Dimension(56, 56));
        headerPanel.add(logoLabel, hgbc);

        hgbc.gridy = 1;
        hgbc.insets = new Insets(8, 0, 0, 0);
        JLabel appTitle = new JLabel("Conflict-Free Scheduler",
                                      SwingConstants.CENTER);
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        appTitle.setForeground(Color.WHITE);
        headerPanel.add(appTitle, hgbc);

        hgbc.gridy = 2;
        hgbc.insets = new Insets(2, 0, 0, 0);
        JLabel appSub = new JLabel("Event Management System",
                                    SwingConstants.CENTER);
        appSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        appSub.setForeground(Color.LIGHT_GRAY);
        headerPanel.add(appSub, hgbc);

        add(headerPanel, BorderLayout.NORTH);

        // ── FORM PANEL ────────────────────────────────────
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(28, 40, 20, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weightx   = 1.0;

        // Role selector
        addLabel(formPanel, "Login As", gbc, 0);
        roleSelector = new JComboBox<>(new String[]{"Admin", "User"});
        styleComboBox(roleSelector);
        gbc.gridy = 1; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(roleSelector, gbc);

        // Username
        addLabel(formPanel, "Username", gbc, 2);
        usernameField = new JTextField();
        styleField(usernameField);
        gbc.gridy = 3; gbc.insets = new Insets(4, 0, 14, 0);
        formPanel.add(usernameField, gbc);

        // Password
        addLabel(formPanel, "Password", gbc, 4);
        passwordField = new JPasswordField();
        styleField(passwordField);
        gbc.gridy = 5; gbc.insets = new Insets(4, 0, 20, 0);
        formPanel.add(passwordField, gbc);

        // Login button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(0, 42));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(loginButton, gbc);

        // Message label
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(messageLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // ── FOOTER ────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setPreferredSize(new Dimension(420, 36));
        JLabel footerLabel = new JLabel(
            "Conflict-Free Scheduler v1.0 | CTRL+ALT+ELITE",
            SwingConstants.CENTER);
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footer.add(footerLabel);
        add(footer, BorderLayout.SOUTH);
    }

    // ── HELPER: add a label row ────────────────────────────
    private void addLabel(JPanel p, String text,
                          GridBagConstraints gbc, int row) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        gbc.gridy  = row;
        gbc.insets = new Insets(0, 0, 2, 0);
        p.add(lbl, gbc);
    }

    // ── HELPER: style a text/password field ───────────────
    private void styleField(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    // ── HELPER: style combo box ───────────────────────────
    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(0, 36));
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
    }

    // ── LOGIN LOGIC ───────────────────────────────────────
   private void handleLogin() {
    String username     = usernameField.getText().trim();
    String password     = new String(passwordField.getPassword()).trim();
    String selectedRole = (String) roleSelector.getSelectedItem();

    if (username.isEmpty() || password.isEmpty()) {
        showMessage("Please fill in all fields.", Color.RED);
        return;
    }

    if (db.DatabaseHandler.validateUser(username, password, selectedRole)) {
        showMessage("Welcome, " + username + "! (" + selectedRole + ")",
                    Color.GREEN);
        dispose();
        new Dashboard(username, selectedRole).setVisible(true);
    } else {
        showMessage("Invalid credentials or wrong role selected.", Color.RED);
    }
}

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginScreen screen = new LoginScreen();
            screen.setVisible(true);
        });
    }
}