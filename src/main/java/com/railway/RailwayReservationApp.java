package com.railway;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

/**
 * Standalone Swing reservation application backed by a local SQLite database.
 * The app is kept compact and easy to run while improving reliability and validation.
 */
public final class RailwayReservationApp {
    static final Color NAVY = new Color(16, 31, 57);
    static final Color BLUE = new Color(29, 78, 216);
    static final Color SKY = new Color(239, 246, 255);
    static final Color INK = new Color(25, 38, 60);
    static final Color MUTED = new Color(100, 116, 139);
    static final Color BORDER = new Color(218, 226, 238);
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    private final Database db = new Database();
    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new RailwayReservationApp().start();
        });
    }

    private void start() {
        try {
            db.initialize();
        } catch (SQLException e) {
            showError(null, "Unable to initialize the database: " + e.getMessage());
            return;
        }
        showLogin();
    }

    private void showLogin() {
        frame = baseFrame("RailEase | Sign in", 1050, 650);
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(Color.WHITE);

        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(NAVY);
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("✦");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 58));
        icon.setForeground(new Color(125, 211, 252));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = label("RailEase", 40, Color.WHITE, Font.BOLD);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = label("Travel, beautifully connected.", 17, new Color(203, 213, 225), Font.PLAIN);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel note = label("Book journeys. Manage plans.\nAll in one calm, simple space.", 15, new Color(148, 163, 184), Font.PLAIN);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandText.add(icon);
        brandText.add(Box.createVerticalStrut(18));
        brandText.add(title);
        brandText.add(Box.createVerticalStrut(10));
        brandText.add(subtitle);
        brandText.add(Box.createVerticalStrut(18));
        brandText.add(note);

        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 0, 0);
        brand.add(brandText, g);

        JPanel side = new JPanel(new GridBagLayout());
        side.setBackground(Color.WHITE);

        JPanel card = panel();
        card.setPreferredSize(new Dimension(360, 400));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel welcome = label("Welcome back", 27, INK, Font.BOLD);
        JLabel prompt = label("Sign in to plan your next journey.", 14, MUTED, Font.PLAIN);
        JTextField user = textField();
        JPasswordField password = passwordField();

        card.add(welcome);
        card.add(Box.createVerticalStrut(7));
        card.add(prompt);
        card.add(Box.createVerticalStrut(30));
        card.add(fieldLabel("USERNAME"));
        card.add(Box.createVerticalStrut(7));
        card.add(user);
        card.add(Box.createVerticalStrut(18));
        card.add(fieldLabel("PASSWORD"));
        card.add(Box.createVerticalStrut(7));
        card.add(password);
        card.add(Box.createVerticalStrut(25));

        JButton signIn = primaryButton("Sign in  →");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(signIn);
        card.add(Box.createVerticalStrut(20));

        JLabel demo = label("Demo: admin / admin123", 12, MUTED, Font.PLAIN);
        demo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(demo);

        ActionListener login = e -> {
            String username = user.getText().trim();
            String pass = new String(password.getPassword());
            if (username.isEmpty() || pass.isEmpty()) {
                showError(frame, "Enter both username and password.");
                return;
            }
            try {
                if (db.authenticate(username, pass)) {
                    showDashboard(username);
                } else {
                    showError(frame, "Access denied. Check your credentials and try again.");
                }
            } catch (SQLException ex) {
                showError(frame, "Login could not be verified: " + ex.getMessage());
            }
        };

        signIn.addActionListener(login);
        password.addActionListener(login);

        side.add(card);
        root.add(brand);
        root.add(side);
        frame.setContentPane(root);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showDashboard(String username) {
        frame.dispose();
        frame = baseFrame("RailEase | Reservation desk", 1120, 730);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SKY);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(16, 34, 16, 34));
        header.add(label("✦  RailEase", 23, Color.WHITE, Font.BOLD), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(label("Signed in as " + username, 13, new Color(203, 213, 225), Font.PLAIN));
        JButton logout = secondaryButton("Log out");
        logout.addActionListener(e -> {
            frame.dispose();
            showLogin();
        });
        right.add(logout);
        header.add(right, BorderLayout.EAST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(SKY);
        main.setBorder(new EmptyBorder(27, 34, 28, 34));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);
        heading.add(label("Reservation desk", 28, INK, Font.BOLD));
        heading.add(Box.createVerticalStrut(6));
        heading.add(label("Plan, reserve, and manage bookings in a few simple steps.", 14, MUTED, Font.PLAIN));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("  Book a journey  ", bookingPanel());
        tabs.addTab("  Cancel booking  ", cancellationPanel());
        tabs.addTab("  Reservations overview  ", reservationOverviewPanel());

        main.add(heading, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(main, BorderLayout.CENTER);
        frame.setContentPane(root);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel bookingPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);
        outer.setBorder(new EmptyBorder(23, 28, 23, 28));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = gbc();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JTextField passenger = textField();
        JTextField trainNo = textField();
        JTextField trainName = textField();
        JTextField date = textField();
        JTextField source = textField();
        JTextField destination = textField();
        trainName.setEditable(false);
        trainName.setBackground(new Color(248, 250, 252));

        JComboBox<String> travelClass = comboBox(new String[]{
                "Select class",
                "First AC (1A)",
                "Second AC (2A)",
                "Third AC (3A)",
                "Sleeper (SL)",
                "Second Sitting (2S)"
        });

        addField(form, c, 0, 0, "PASSENGER NAME", passenger);
        addField(form, c, 1, 0, "TRAIN NUMBER", trainNo);
        addField(form, c, 0, 2, "TRAIN NAME", trainName);
        addField(form, c, 1, 2, "TRAVEL CLASS", travelClass);
        addField(form, c, 0, 4, "DATE OF JOURNEY", date);
        addField(form, c, 1, 4, "SOURCE STATION", source);
        addField(form, c, 0, 6, "DESTINATION STATION", destination);

        JLabel hint = label("Format: DD-MM-YYYY  •  Example: 28-10-2026", 12, MUTED, Font.PLAIN);
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 2;
        c.insets = new Insets(4, 0, 17, 0);
        form.add(hint, c);

        JButton book = primaryButton("Confirm booking  →");
        c.gridy = 9;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        form.add(book, c);

        trainNo.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            try {
                trainName.setText(db.trainName(trainNo.getText().trim()));
            } catch (SQLException ignored) {
                trainName.setText("");
            }
        }));

        book.addActionListener(e -> {
            String p = passenger.getText().trim();
            String no = trainNo.getText().trim();
            String name = trainName.getText().trim();
            String d = date.getText().trim();
            String s = source.getText().trim();
            String dest = destination.getText().trim();
            String selectedClass = (String) travelClass.getSelectedItem();

            if (p.isEmpty() || no.isEmpty() || name.isEmpty() || d.isEmpty() || s.isEmpty() || dest.isEmpty() || selectedClass == null || selectedClass.equals("Select class")) {
                showError(frame, "Please complete every booking field before confirming the ticket.");
                return;
            }
            if (!no.matches("\\d+")) {
                showError(frame, "Train number must contain digits only.");
                return;
            }
            try {
                LocalDate journeyDate = LocalDate.parse(d, DATE_FORMAT);
                if (journeyDate.isBefore(LocalDate.now())) {
                    showError(frame, "Journey date cannot be in the past. Please select a valid upcoming date.");
                    return;
                }
            } catch (DateTimeParseException ex) {
                showError(frame, "Use a valid date in DD-MM-YYYY format.");
                return;
            }

            try {
                Booking b = db.book(p, no, name, selectedClass, d, s, dest);
                showBookingConfirmation(b);
                passenger.setText("");
                trainNo.setText("");
                trainName.setText("");
                date.setText("");
                source.setText("");
                destination.setText("");
                travelClass.setSelectedIndex(0);
            } catch (SQLException ex) {
                showError(frame, "The ticket could not be saved: " + ex.getMessage());
            }
        });

        outer.add(form);
        return outer;
    }

    private JPanel cancellationPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);
        outer.setBorder(new EmptyBorder(40, 28, 28, 28));

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(620, 450));
        box.add(label("Cancel with confidence", 25, INK, Font.BOLD));
        box.add(Box.createVerticalStrut(7));
        box.add(label("Enter the PNR to review a reservation before cancelling it.", 14, MUTED, Font.PLAIN));
        box.add(Box.createVerticalStrut(24));

        JTextField pnr = textField();
        JPanel line = new JPanel(new BorderLayout(10, 0));
        line.setOpaque(false);
        line.add(pnr, BorderLayout.CENTER);
        JButton fetch = secondaryButton("Fetch booking");
        line.add(fetch, BorderLayout.EAST);
        box.add(line);
        box.add(Box.createVerticalStrut(18));

        JPanel detail = panel();
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        detail.setVisible(false);
        JButton cancel = dangerButton("Cancel this booking");
        cancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detail.add(cancel);

        final Booking[] selected = {null};

        fetch.addActionListener(e -> {
            try {
                Booking b = db.find(pnr.getText().trim());
                if (b == null) {
                    detail.setVisible(false);
                    showError(frame, "No reservation was found for the entered PNR.");
                    return;
                }
                selected[0] = b;
                detail.removeAll();
                detail.add(bookingDetails(b));
                detail.add(Box.createVerticalStrut(18));
                detail.add(cancel);
                detail.setVisible(true);
                detail.revalidate();
                detail.repaint();
            } catch (SQLException ex) {
                showError(frame, "Unable to fetch booking details: " + ex.getMessage());
            }
        });

        cancel.addActionListener(e -> {
            if (selected[0] == null) {
                showError(frame, "Please fetch a booking first.");
                return;
            }
            int choice = JOptionPane.showConfirmDialog(
                    frame,
                    "Cancel reservation " + selected[0].pnr + " for " + selected[0].passenger + "?",
                    "Confirm cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    db.cancel(selected[0].pnr);
                    detail.setVisible(false);
                    pnr.setText("");
                    selected[0] = null;
                    JOptionPane.showMessageDialog(frame, "Reservation cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    showError(frame, "Cancellation failed: " + ex.getMessage());
                }
            }
        });

        outer.add(box);
        return outer;
    }

    private JPanel reservationOverviewPanel() {
        JPanel outer = new JPanel(new BorderLayout(12, 12));
        outer.setBackground(Color.WHITE);
        outer.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        JLabel searchLabel = label("Search by passenger or PNR", 12, MUTED, Font.BOLD);
        JTextField search = textField();
        search.setPreferredSize(new Dimension(280, 38));
        JButton refresh = secondaryButton("Refresh");
        JButton clear = secondaryButton("Clear");
        controls.add(searchLabel);
        controls.add(search);
        controls.add(refresh);
        controls.add(clear);
        outer.add(controls, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[] {"PNR", "Passenger", "Train", "Class", "Date", "Route"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        outer.add(scrollPane, BorderLayout.CENTER);

        Runnable reload = () -> {
            model.setRowCount(0);
            try {
                for (Booking booking : db.listBookings(search.getText().trim())) {
                    model.addRow(new Object[] {
                            booking.pnr,
                            booking.passenger,
                            booking.trainNo + " · " + booking.trainName,
                            booking.travelClass,
                            booking.date,
                            booking.source + " → " + booking.destination
                    });
                }
            } catch (SQLException ex) {
                showError(frame, "Unable to load reservation list: " + ex.getMessage());
            }
        };

        refresh.addActionListener(e -> reload.run());
        clear.addActionListener(e -> {
            search.setText("");
            reload.run();
        });
        search.addActionListener(e -> reload.run());
        reload.run();

        return outer;
    }

    private void showBookingConfirmation(Booking b) {
        JOptionPane.showMessageDialog(frame, bookingDetails(b), "Journey confirmed · PNR " + b.pnr, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel bookingDetails(Booking b) {
        JPanel d = new JPanel(new GridLayout(0, 2, 16, 9));
        d.setOpaque(false);
        String[][] rows = {
                {"PNR", b.pnr},
                {"Passenger", b.passenger},
                {"Train", b.trainNo + " · " + b.trainName},
                {"Class", b.travelClass},
                {"Date", b.date},
                {"Route", b.source + " → " + b.destination}
        };
        for (String[] row : rows) {
            d.add(label(row[0], 12, MUTED, Font.BOLD));
            d.add(label(row[1], 12, INK, Font.PLAIN));
        }
        return d;
    }

    private JFrame baseFrame(String title, int w, int h) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setSize(w, h);
        f.setMinimumSize(new Dimension(900, 600));
        f.setLocationByPlatform(true);
        return f;
    }

    private static JPanel panel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(20, 22, 20, 22)));
        return p;
    }

    private static JLabel label(String s, int size, Color col, int style) {
        JLabel l = new JLabel("<html>" + s.replace("\n", "<br>") + "</html>");
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(col);
        return l;
    }

    private static JLabel fieldLabel(String s) {
        return label(s, 11, MUTED, Font.BOLD);
    }

    private static JTextField textField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setPreferredSize(new Dimension(250, 42));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        return f;
    }

    private static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setPreferredSize(new Dimension(250, 42));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        return f;
    }

    private static JComboBox<String> comboBox(String[] values) {
        JComboBox<String> b = new JComboBox<>(values);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setPreferredSize(new Dimension(250, 42));
        b.setBorder(new LineBorder(BORDER));
        return b;
    }

    private static JButton button(String s, Color bg, Color fg) {
        JButton b = new JButton(s);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 42));
        return b;
    }

    private static JButton primaryButton(String s) {
        return button(s, BLUE, Color.WHITE);
    }

    private static JButton secondaryButton(String s) {
        return button(s, new Color(224, 231, 255), new Color(49, 46, 129));
    }

    private static JButton dangerButton(String s) {
        return button(s, new Color(220, 38, 38), Color.WHITE);
    }

    private static GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 7, 18);
        return c;
    }

    private static void addField(JPanel p, GridBagConstraints c, int x, int y, String labelText, JComponent field) {
        JLabel label = fieldLabel(labelText);
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        p.add(label, c);
        c.gridy = y + 1;
        p.add(field, c);
    }

    private static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "RailEase", JOptionPane.ERROR_MESSAGE);
    }

    /** Immutable reservation details. */
    static final class Booking {
        final String pnr;
        final String passenger;
        final String trainNo;
        final String trainName;
        final String travelClass;
        final String date;
        final String source;
        final String destination;

        Booking(String pnr, String passenger, String trainNo, String trainName,
                String travelClass, String date, String source, String destination) {
            this.pnr = pnr;
            this.passenger = passenger;
            this.trainNo = trainNo;
            this.trainName = trainName;
            this.travelClass = travelClass;
            this.date = date;
            this.source = source;
            this.destination = destination;
        }
    }

    @FunctionalInterface
    interface Change {
        void run();
    }

    static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Change change;

        SimpleDocumentListener(Change change) {
            this.change = change;
        }

        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { change.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { change.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { change.run(); }
    }

    static final class Database {
        private static final String DB_FILE_NAME = "railease.db";

        private Connection connect() throws SQLException {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found in the classpath.", e);
            }
            String dbPath = new File(DB_FILE_NAME).getAbsolutePath();
            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        }

        void initialize() throws SQLException {
            try (Connection c = connect(); Statement s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'USER')");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS trains (number TEXT PRIMARY KEY, name TEXT NOT NULL)");
                s.executeUpdate("CREATE TABLE IF NOT EXISTS reservations (pnr TEXT PRIMARY KEY, passenger TEXT NOT NULL, train_no TEXT NOT NULL, train_name TEXT NOT NULL, travel_class TEXT NOT NULL, journey_date TEXT NOT NULL, source TEXT NOT NULL, destination TEXT NOT NULL)");
                s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reservations_pnr ON reservations(pnr)");
                seedDemoData(c);
            }
        }

        private void seedDemoData(Connection c) throws SQLException {
            try (PreparedStatement countUsers = c.prepareStatement("SELECT COUNT(*) FROM users");
                 PreparedStatement insertUser = c.prepareStatement("INSERT INTO users(username, password, role) VALUES (?, ?, ?)")) {
                ResultSet rs = countUsers.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    insertUser.setString(1, "admin");
                    insertUser.setString(2, "admin123");
                    insertUser.setString(3, "ADMIN");
                    insertUser.executeUpdate();

                    insertUser.setString(1, "traveler");
                    insertUser.setString(2, "welcome123");
                    insertUser.setString(3, "USER");
                    insertUser.executeUpdate();
                }
                rs.close();
            }

            try (PreparedStatement countTrains = c.prepareStatement("SELECT COUNT(*) FROM trains");
                 PreparedStatement insertTrain = c.prepareStatement("INSERT INTO trains(number, name) VALUES (?, ?)")) {
                ResultSet rs = countTrains.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    insertTrain.setString(1, "12002");
                    insertTrain.setString(2, "Bhopal Shatabdi");
                    insertTrain.executeUpdate();

                    insertTrain.setString(1, "12952");
                    insertTrain.setString(2, "Mumbai Rajdhani");
                    insertTrain.executeUpdate();

                    insertTrain.setString(1, "12301");
                    insertTrain.setString(2, "Howrah Rajdhani");
                    insertTrain.executeUpdate();

                    insertTrain.setString(1, "12627");
                    insertTrain.setString(2, "Karnataka Express");
                    insertTrain.executeUpdate();

                    insertTrain.setString(1, "22435");
                    insertTrain.setString(2, "Jabalpur Express");
                    insertTrain.executeUpdate();

                    insertTrain.setString(1, "20801");
                    insertTrain.setString(2, "Patna Superfast");
                    insertTrain.executeUpdate();
                }
                rs.close();
            }
        }

        boolean authenticate(String username, String password) throws SQLException {
            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement("SELECT 1 FROM users WHERE username = ? AND password = ?")) {
                p.setString(1, username.trim());
                p.setString(2, password);
                try (ResultSet r = p.executeQuery()) {
                    return r.next();
                }
            }
        }

        String trainName(String number) throws SQLException {
            if (number == null || !number.matches("\\d+")) {
                return "";
            }
            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement("SELECT name FROM trains WHERE number = ?")) {
                p.setString(1, number.trim());
                try (ResultSet r = p.executeQuery()) {
                    return r.next() ? r.getString("name") : "";
                }
            }
        }

        List<Booking> listBookings(String query) throws SQLException {
            String keyword = query == null ? "" : query.trim();
            StringBuilder sql = new StringBuilder("SELECT * FROM reservations");
            List<String> values = new ArrayList<>();
            if (!keyword.isEmpty()) {
                sql.append(" WHERE LOWER(pnr) LIKE ? OR LOWER(passenger) LIKE ? OR LOWER(train_no) LIKE ? OR LOWER(train_name) LIKE ? OR LOWER(source) LIKE ? OR LOWER(destination) LIKE ?");
                String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                for (int i = 0; i < 6; i++) {
                    values.add(pattern);
                }
            }
            sql.append(" ORDER BY rowid DESC");

            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    p.setString(i + 1, values.get(i));
                }
                try (ResultSet r = p.executeQuery()) {
                    List<Booking> bookings = new ArrayList<>();
                    while (r.next()) {
                        bookings.add(new Booking(
                                r.getString("pnr"),
                                r.getString("passenger"),
                                r.getString("train_no"),
                                r.getString("train_name"),
                                r.getString("travel_class"),
                                r.getString("journey_date"),
                                r.getString("source"),
                                r.getString("destination")
                        ));
                    }
                    return bookings;
                }
            }
        }

        Booking book(String passenger, String no, String name, String travelClass, String date, String source, String destination) throws SQLException {
            String cleanPassenger = passenger == null ? "" : passenger.trim();
            String cleanNo = no == null ? "" : no.trim();
            String cleanName = name == null ? "" : name.trim();
            String cleanDate = date == null ? "" : date.trim();
            String cleanSource = source == null ? "" : source.trim();
            String cleanDestination = destination == null ? "" : destination.trim();

            if (cleanPassenger.isEmpty() || cleanNo.isEmpty() || cleanName.isEmpty() || cleanDate.isEmpty() || cleanSource.isEmpty() || cleanDestination.isEmpty()) {
                throw new SQLException("Booking fields cannot be blank.");
            }
            if (travelClass == null || travelClass.trim().isEmpty() || travelClass.equals("Select class")) {
                throw new SQLException("Please select a valid travel class.");
            }
            if (!cleanNo.matches("\\d+")) {
                throw new SQLException("Train number must contain digits only.");
            }
            LocalDate journeyDate;
            try {
                journeyDate = LocalDate.parse(cleanDate, DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                throw new SQLException("Date must be in DD-MM-YYYY format.");
            }
            if (journeyDate.isBefore(LocalDate.now())) {
                throw new SQLException("Journey date cannot be in the past.");
            }

            String pnr = "RE" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
            try (Connection c = connect()) {
                c.setAutoCommit(false);
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO reservations(pnr, passenger, train_no, train_name, travel_class, journey_date, source, destination) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    p.setString(1, pnr);
                    p.setString(2, cleanPassenger);
                    p.setString(3, cleanNo);
                    p.setString(4, cleanName);
                    p.setString(5, travelClass.trim());
                    p.setString(6, cleanDate);
                    p.setString(7, cleanSource);
                    p.setString(8, cleanDestination);
                    p.executeUpdate();
                    c.commit();
                } catch (SQLException ex) {
                    c.rollback();
                    throw ex;
                }
            }
            return new Booking(pnr, cleanPassenger, cleanNo, cleanName, travelClass.trim(), cleanDate, cleanSource, cleanDestination);
        }

        Booking find(String pnr) throws SQLException {
            String clean = pnr == null ? "" : pnr.trim();
            if (clean.isEmpty()) {
                return null;
            }
            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement("SELECT * FROM reservations WHERE pnr = ?")) {
                p.setString(1, clean);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        return null;
                    }
                    return new Booking(
                            r.getString("pnr"),
                            r.getString("passenger"),
                            r.getString("train_no"),
                            r.getString("train_name"),
                            r.getString("travel_class"),
                            r.getString("journey_date"),
                            r.getString("source"),
                            r.getString("destination")
                    );
                }
            }
        }

        void cancel(String pnr) throws SQLException {
            String clean = pnr == null ? "" : pnr.trim();
            if (clean.isEmpty()) {
                throw new SQLException("PNR cannot be empty for cancellation.");
            }
            try (Connection c = connect();
                 PreparedStatement p = c.prepareStatement("DELETE FROM reservations WHERE pnr = ?")) {
                p.setString(1, clean);
                p.executeUpdate();
            }
        }
    }
}


