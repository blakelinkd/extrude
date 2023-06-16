package com.blakelink.us;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.*;

public class App {

    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final String VIEW_NAME = "UsefulView";
    private static final int PAGE_SIZE = 50;
    private static int currentPage = 0;

    private static boolean isViewModified = false;
    private static String originalViewCode = "";

    public static void main(String[] args) {
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

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("SQLite View Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Create original table tab
        DefaultTableModel originalTableModel = new DefaultTableModel();
        JTable originalTable = new JTable(originalTableModel);
        JScrollPane originalScrollPane = new JScrollPane(originalTable);
        JPanel originalTabPanel = new JPanel(new BorderLayout());
        originalTabPanel.add(originalScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Original Table", originalTabPanel);

        // Create modify view tab
        JTextArea viewTextArea = new JTextArea();
        JScrollPane viewScrollPane = new JScrollPane(viewTextArea);
        JPanel modifyViewTabPanel = new JPanel(new BorderLayout());
        modifyViewTabPanel.add(viewScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Modify View", modifyViewTabPanel);

        // Styling and layout for the tabbed pane
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // Pagination for the original table
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Prev");
        nextButton.addActionListener(e -> {
            currentPage++;
            fillTable(originalTableModel, currentPage * PAGE_SIZE);
        });
        prevButton.addActionListener(e -> {
            currentPage--;
            fillTable(originalTableModel, currentPage * PAGE_SIZE);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        originalTabPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add tabbed pane to the frame
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);

        // Fill the original table
        fillTable(originalTableModel, currentPage * PAGE_SIZE);

        // Fetch and display the current SQL view in the modify view tab
        fetchView(viewTextArea);

        // Add document listener to the view text area to track modifications
        viewTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isViewModified = true;
                updateIcon();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isViewModified = true;
                updateIcon();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isViewModified = true;
                updateIcon();
            }
        });

        // Save button to update the view and refresh the original table
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isViewModified) {
                    saveView(viewTextArea.getText());
                    isViewModified = false;
                    updateIcon();
                    refreshOriginalTable(originalTableModel);
                }
            }
        });

        // Undo button to revert the view to its original state
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isViewModified) {
                    viewTextArea.setText(originalViewCode);
                    isViewModified = false;
                    updateIcon();
                    refreshOriginalTable(originalTableModel);
                }
            }
        });

        // Panel to hold the save and undo buttons
        JPanel modifyViewButtonPanel = new JPanel();
        modifyViewButtonPanel.add(saveButton);
        modifyViewButtonPanel.add(undoButton);
        modifyViewTabPanel.add(modifyViewButtonPanel, BorderLayout.NORTH);
    }

    private static void fillTable(DefaultTableModel tableModel, int offset) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + VIEW_NAME + " LIMIT " + PAGE_SIZE + " OFFSET " + offset)) {

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

    private static void fetchView(JTextArea viewTextArea) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sql FROM sqlite_master WHERE type = 'view' AND name = '" + VIEW_NAME + "'")) {

            if (rs.next()) {
                String sql = rs.getString("sql");
                viewTextArea.setText(sql);
                originalViewCode = sql;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveView(String newViewCode) {
    try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement()) {
        // Validate the syntax of the new view code
        try {
            stmt.executeUpdate("CREATE VIEW " + VIEW_NAME + " AS " + newViewCode);
        } catch (SQLException ex) {
            System.out.println("Syntax error in CREATE VIEW code. View not saved.");
            ex.printStackTrace();
            return; // Return without saving the view
        }

        // Drop the existing view
        stmt.executeUpdate("DROP VIEW IF EXISTS " + VIEW_NAME);

        // Create the updated view
        stmt.executeUpdate("CREATE VIEW " + VIEW_NAME + " AS " + newViewCode);

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}


    private static void refreshOriginalTable(DefaultTableModel tableModel) {
        currentPage = 0;
        fillTable(tableModel, currentPage * PAGE_SIZE);
    }

    private static void updateIcon() {
        try {
            URL iconURL = App.class.getResource("yellow_icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                // Use the icon variable as needed
            } else {
                // Handle the case when the iconURL is null
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
