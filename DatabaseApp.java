import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class DatabaseApp extends JFrame {

    private JTextField dbUrlField, userField, passField;
    private JTextField idField, nameField, emailField;
    private JButton connectButton, insertButton, retrieveButton;
    private JTable dataTable;
    private Connection connection;

    public DatabaseApp() {
        setTitle("Database Application");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    
        // Connection panel
        JPanel connectPanel = new JPanel(new GridLayout(4, 2));
        connectPanel.add(new JLabel("Database URL:"));
        dbUrlField = new JTextField("jdbc:mysql://127.0.0.1:3306/testdb"); // Default URL format
        connectPanel.add(dbUrlField);
    
        connectPanel.add(new JLabel("Username:"));
        userField = new JTextField("root"); // Default username
        connectPanel.add(userField);
    
        connectPanel.add(new JLabel("Password:"));
        passField = new JPasswordField("your_password"); // Default password
        connectPanel.add(passField);
    
        connectButton = new JButton("Connect");
        connectPanel.add(connectButton);

        // Input panel for inserting data
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        // Button panel for actions
        insertButton = new JButton("Insert Data");
        retrieveButton = new JButton("Retrieve Data");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(insertButton);
        buttonPanel.add(retrieveButton);

        // Table for displaying data
        dataTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(dataTable);

        // Adding components to the frame
        add(connectPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(tableScrollPane, BorderLayout.EAST);

        // Action listeners
        connectButton.addActionListener(new ConnectListener());
        insertButton.addActionListener(new InsertListener());
        retrieveButton.addActionListener(new RetrieveListener());
    }

    // Listener for the "Connect" button
    private class ConnectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String dbUrl = dbUrlField.getText().trim();
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (dbUrl.isEmpty() || user.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please provide the database URL and username.");
                return;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure MySQL driver is loaded
                connection = DriverManager.getConnection(dbUrl, user, pass);
                JOptionPane.showMessageDialog(null, "Connected to Database");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "JDBC Driver not found. Ensure the MySQL connector is included.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Connection Failed: " + ex.getMessage());
            }
        }
    }

    // Listener for the "Insert Data" button
    private class InsertListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (connection == null) {
                JOptionPane.showMessageDialog(null, "Not connected to Database");
                return;
            }

            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields (ID, Name, Email) must be filled.");
                return;
            }

            try {
                String query = "INSERT INTO Users (id, name, email) VALUES (?, ?, ?)";
                PreparedStatement pst = connection.prepareStatement(query);
                pst.setString(1, id);
                pst.setString(2, name);
                pst.setString(3, email);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(null, "Data Inserted Successfully");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Insertion Failed: " + ex.getMessage());
            }
        }
    }

    // Listener for the "Retrieve Data" button
    private class RetrieveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (connection == null) {
                JOptionPane.showMessageDialog(null, "Not connected to Database");
                return;
            }

            try {
                String query = "SELECT * FROM Users";
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                dataTable.setModel(new ResultSetTableModel(rs)); // Set data model for JTable
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Data Retrieval Failed: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DatabaseApp().setVisible(true));
    }
}

// Custom Table Model to handle ResultSet
class ResultSetTableModel extends AbstractTableModel {
    private ResultSet resultSet;
    private ResultSetMetaData metaData;
    private int rowCount;

    public ResultSetTableModel(ResultSet rs) throws SQLException {
        this.resultSet = rs;
        this.metaData = rs.getMetaData();
        rs.last();
        rowCount = rs.getRow();
        rs.beforeFirst();
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        try {
            return metaData.getColumnCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            resultSet.absolute(rowIndex + 1);
            return resultSet.getObject(columnIndex + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getColumnName(int columnIndex) {
        try {
            return metaData.getColumnName(columnIndex + 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
}