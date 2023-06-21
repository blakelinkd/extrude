package com.blakelink.us;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final int PAGE_SIZE = 50;
    private static int currentPage = 0;
    private static List<String> existingViews = new ArrayList<>();
    private static String currentView;
    private static JTabbedPane tabbedPane;
    private static JList<String> viewList;

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
        JFrame frame = new JFrame("SQL Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        URL imageUrl = App.class.getResource("yellow_icon.png");
        frame.setIconImage(new ImageIcon(imageUrl).getImage());

        tabbedPane = new JTabbedPane();

        // Create original table tab
        DefaultTableModel originalTableModel = new DefaultTableModel();
        JTable originalTable = new JTable(originalTableModel);
        JScrollPane originalScrollPane = new JScrollPane(originalTable);
        JPanel originalTabPanel = new JPanel(new BorderLayout());
        originalTabPanel.add(originalScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Original Table", originalTabPanel);

        // Create modify view tab
        viewList = new JList<>();

        viewList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    JList<String> list = (JList) evt.getSource();
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        currentView = list.getModel().getElementAt(index);
                        displayView(currentView, originalTableModel, originalTable);
                    }
                }
            }

        });
        JScrollPane viewScrollPane = new JScrollPane(viewList);
        JPanel modifyViewTabPanel = new JPanel(new BorderLayout());
        modifyViewTabPanel.add(viewScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Select View", modifyViewTabPanel);

        // Styling and layout for the tabbed pane
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // Pagination for the original table
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Prev");
        nextButton.addActionListener(e -> {
            currentPage++;
            fillTable(originalTableModel, originalTable, currentPage * PAGE_SIZE);

        });
        prevButton.addActionListener(e -> {
            currentPage--;
            fillTable(originalTableModel, originalTable, currentPage * PAGE_SIZE);

        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        originalTabPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add tabbed pane to the frame
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);

        // Fetch the existing views from the database
        fetchExistingViews();

        // Populate the view list in the modify view tab
        DefaultListModel<String> viewListModel = new DefaultListModel<>();
        for (String view : existingViews) {
            viewListModel.addElement(view);
        }
        viewList.setModel(viewListModel);

        // Fill the original table with the first view
        if (!existingViews.isEmpty()) {
            displayView(existingViews.get(0), originalTableModel, originalTable);
        }
    }

    private static void fetchExistingViews() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type = 'view'")) {

            while (rs.next()) {
                String viewName = rs.getString("name");
                existingViews.add(viewName);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void fillTable(DefaultTableModel tableModel, JTable table, int offset) {
        if (existingViews.isEmpty()) {
            return;
        }
        if (currentView == null || currentView.isEmpty()) {
            return;
        }

        String selectedView = currentView;
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT * FROM " + selectedView + " LIMIT " + PAGE_SIZE + " OFFSET " + offset)) {

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

            TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void displayView(String viewName, DefaultTableModel tableModel, JTable table) {
        currentPage = 0;
        currentView = viewName;
        tabbedPane.setTitleAt(0, currentView);
        viewList.repaint(); // this line is accessible now
        fillTable(tableModel, table, currentPage * PAGE_SIZE);
    }

}
