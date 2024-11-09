package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HotelManagementApp {

    private JFrame mainFrame;
    private JTextArea bookingInfoArea;
    private JTextField customerNameField, checkInDateField, checkOutDateField, phoneNumberField, aadhaarNumberField, adultsField, childrenField, numberOfRoomsField;
    private JComboBox<String> roomTypeComboBox, bedTypeComboBox;

    // MySQL Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Sandeep@12";

    public HotelManagementApp() {
        mainFrame = new JFrame("Hotel Management System");
        mainFrame.setSize(500, 750);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Header
        JLabel headerLabel = new JLabel("Hotel Booking System", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 24));
        mainFrame.add(headerLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Welcome Message
        JLabel welcomeLabel = new JLabel("Welcome to Our Hotel Booking System!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.BLUE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(welcomeLabel);

        // Add input fields
        formPanel.add(createInputRow("Customer Name:", customerNameField = new JTextField(20)));
        formPanel.add(createInputRow("Check-In Date (YYYY-MM-DD):", checkInDateField = new JTextField(20)));
        formPanel.add(createInputRow("Check-Out Date (YYYY-MM-DD):", checkOutDateField = new JTextField(20)));
        formPanel.add(createInputRow("Phone Number:", phoneNumberField = new JTextField(20)));
        formPanel.add(createInputRow("Aadhaar Number:", aadhaarNumberField = new JTextField(20)));
        formPanel.add(createLabelAndCombo("Room Type:", new String[]{"AC", "Non-AC", "AC Smoking", "Non-AC Smoking"}, roomTypeComboBox = new JComboBox<>()));
        formPanel.add(createInputRow("Number of Adults:", adultsField = new JTextField(20)));
        formPanel.add(createInputRow("Number of Children:", childrenField = new JTextField(20)));
        formPanel.add(createInputRow("Number of Rooms:", numberOfRoomsField = new JTextField(20)));
        formPanel.add(createLabelAndCombo("Bed Type:", new String[]{"Single", "Double"}, bedTypeComboBox = new JComboBox<>()));

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Book Room");
        JButton showBookingsButton = new JButton("Show All Bookings");
        JButton removeBookingsButton = new JButton("Remove All Bookings");

        buttonPanel.add(submitButton);
        buttonPanel.add(showBookingsButton);
        buttonPanel.add(removeBookingsButton);

        formPanel.add(buttonPanel);

        mainFrame.add(formPanel, BorderLayout.CENTER);

        // Text Area for displaying bookings
        bookingInfoArea = new JTextArea();
        bookingInfoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(bookingInfoArea);
        mainFrame.add(scrollPane, BorderLayout.SOUTH);

        mainFrame.setVisible(true);

        // Action Listeners
        submitButton.addActionListener(e -> bookRoom());
        showBookingsButton.addActionListener(e -> showBookings());
        removeBookingsButton.addActionListener(e -> removeAllBookings());
    }

    // Helper method to create input row with label and text field
    private JPanel createInputRow(String labelText, JTextField textField) {
        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel(labelText));
        row.add(textField);
        return row;
    }

    // Helper method to create input row with label and combo box
    private JPanel createLabelAndCombo(String labelText, String[] options, JComboBox<String> comboBox) {
        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel(labelText));
        comboBox.setModel(new DefaultComboBoxModel<>(options));
        row.add(comboBox);
        return row;
    }

    // Method to book a room
    private void bookRoom() {
        String customerName = customerNameField.getText().trim();
        String checkInDate = checkInDateField.getText().trim();
        String checkOutDate = checkOutDateField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String aadhaarNumber = aadhaarNumberField.getText().trim();
        String roomType = (String) roomTypeComboBox.getSelectedItem();
        String numberOfAdults = adultsField.getText().trim();
        String numberOfChildren = childrenField.getText().trim();
        String numberOfRoomsStr = numberOfRoomsField.getText().trim();
        String bedType = (String) bedTypeComboBox.getSelectedItem();

        if (customerName.isEmpty() || checkInDate.isEmpty() || checkOutDate.isEmpty() ||
                phoneNumber.isEmpty() || aadhaarNumber.isEmpty() || numberOfAdults.isEmpty() ||
                numberOfChildren.isEmpty() || numberOfRoomsStr.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "All fields must be filled out.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "INSERT INTO bookings (customer_name, check_in_date, check_out_date, phone_number, aadhaar_number, room_type, number_of_adults, number_of_children, number_of_rooms, bed_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, customerName);
            statement.setString(2, checkInDate);
            statement.setString(3, checkOutDate);
            statement.setString(4, phoneNumber);
            statement.setString(5, aadhaarNumber);
            statement.setString(6, roomType);
            statement.setInt(7, Integer.parseInt(numberOfAdults));
            statement.setInt(8, Integer.parseInt(numberOfChildren));
            statement.setInt(9, Integer.parseInt(numberOfRoomsStr));
            statement.setString(10, bedType);

            statement.executeUpdate();
            JOptionPane.showMessageDialog(mainFrame, "Room booked successfully!");
            clearFields();

            // Open the restaurant menu after booking
            RestaurantMenu restaurantMenu = new RestaurantMenu();
            restaurantMenu.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error booking room: " + e.getMessage());
        }
    }

    // Method to show all bookings
    private void showBookings() {
        bookingInfoArea.setText("");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String sql = "SELECT * FROM bookings";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String bookingInfo = "ID: " + resultSet.getInt("id") +
                        ", Name: " + resultSet.getString("customer_name") +
                        ", Check-In: " + resultSet.getString("check_in_date") +
                        ", Check-Out: " + resultSet.getString("check_out_date") +
                        ", Phone: " + resultSet.getString("phone_number") +
                        ", Aadhaar: " + resultSet.getString("aadhaar_number") +
                        ", Room Type: " + resultSet.getString("room_type") +
                        ", Adults: " + resultSet.getInt("number_of_adults") +
                        ", Children: " + resultSet.getInt("number_of_children") +
                        ", Rooms: " + resultSet.getInt("number_of_rooms") +
                        ", Bed Type: " + resultSet.getString("bed_type") + "\n";
                bookingInfoArea.append(bookingInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error retrieving bookings: " + e.getMessage());
        }
    }

    // Method to remove all bookings
    private void removeAllBookings() {
        int confirmation = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to remove all bookings?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                String sql = "DELETE FROM bookings";
                Statement statement = conn.createStatement();
                statement.executeUpdate(sql);
                JOptionPane.showMessageDialog(mainFrame, "All bookings have been removed.");
                bookingInfoArea.setText("");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Error removing bookings: " + e.getMessage());
            }
        }
    }

    // Method to clear input fields
    private void clearFields() {
        customerNameField.setText("");
        checkInDateField.setText("");
        checkOutDateField.setText("");
        phoneNumberField.setText("");
        aadhaarNumberField.setText("");
        adultsField.setText("");
        childrenField.setText("");
        numberOfRoomsField.setText("");
    }

    // Restaurant Menu class
    class RestaurantMenu extends JFrame {
        private JTextArea menuArea;

        public RestaurantMenu() {
            setTitle("Restaurant Menu");
            setSize(300, 400);
            setLayout(new BorderLayout());

            menuArea = new JTextArea();
            menuArea.setEditable(false);

            add(new JScrollPane(menuArea), BorderLayout.CENTER);
            generateMenu();
        }

        private void generateMenu() {
            StringBuilder menuBuilder = new StringBuilder();

            menuBuilder.append("Breakfast:\n- Paratha\n- Omelette\n- Poha\n- Idly Sambhar\n\n")
                       .append("Lunch:\n- Pasta\n- Pizza\n- Salad\n- Burger\n\n")
                       .append("Tea:\n- Tea\n- Coffee\n- Sandwich\n- Cookies\n\n")
                       .append("Dinner:\n- Kadhai Paneer\n- Chicken Curry\n- Butter Naan\n- Garlic Naan\n");

            menuArea.setText(menuBuilder.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HotelManagementApp::new);
    }
}
