package com.blakelink.us;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import com.github.javafaker.Faker;

public class App {

    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final String SQL_VIEW_NAME = "customer_view";
    private static final int PAGE_SIZE = 50;
    private static int currentPage = 0;

    public static void main(String[] args) {
        createDatabase();
        seedDatabase();
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            createAndShowGUI();
        });
    }

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS customers (first_name TEXT, last_name TEXT, income REAL, age INTEGER, elevation REAL)";
    private static final String CREATE_VIEW_SQL = "CREATE VIEW IF NOT EXISTS customer_view AS SELECT first_name, last_name, income, age, elevation FROM customers";
    private static final String INSERT_SQL = "INSERT INTO customers(first_name, last_name, income, age, elevation) VALUES (?, ?, ?, ?, ?)";

    public static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Create table and view if they don't exist
            stmt.execute(CREATE_TABLE_SQL);
            stmt.execute(CREATE_VIEW_SQL);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void seedDatabase() {
        Faker faker = new Faker();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {

            // Insert 1000 rows of fake customer data
            for (int i = 0; i < 1000; i++) {
                pstmt.setString(1, faker.name().firstName());
                pstmt.setString(2, faker.name().lastName());
                pstmt.setDouble(3, faker.number().randomDouble(2, 1_000, 100_000));  // income
                pstmt.setInt(4, faker.number().numberBetween(18, 100));  // age
                pstmt.setDouble(5, faker.number().randomDouble(2, 1, 10_000));  // elevation
                pstmt.executeUpdate();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("SQLite View Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);

        // Styling
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setRowHeight(24);

        fillTable(tableModel, currentPage * PAGE_SIZE);

        // Pagination
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Prev");
        nextButton.addActionListener(e -> {
            currentPage++;
            fillTable(tableModel, currentPage * PAGE_SIZE);
        });
        prevButton.addActionListener(e -> {
            currentPage--;
            fillTable(tableModel, currentPage * PAGE_SIZE);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private static void fillTable(DefaultTableModel tableModel, int offset) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + SQL_VIEW_NAME + " LIMIT " + PAGE_SIZE + " OFFSET " + offset)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Clear existing data
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            // Set column names
            for (int column = 1; column <= columnCount; column++) {
                tableModel.addColumn(metaData.getColumnName(column));
            }

            // Add data
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

