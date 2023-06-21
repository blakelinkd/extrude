package com.blakelink.us;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

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

        URL url = App.class.getResource("/yellow_icon.png");
        if (url != null) {
            ImageIcon img = new ImageIcon(url);
            frame.setIconImage(img.getImage());
        } else {
            System.err.println("Could not find icon file.");
        }

        tabbedPane = new JTabbedPane();

        DefaultTableModel originalTableModel = new DefaultTableModel();
        JTable originalTable = new JTable(originalTableModel);
        JScrollPane originalScrollPane = new JScrollPane(originalTable);
        JPanel originalTabPanel = new JPanel(new BorderLayout());
        originalTabPanel.add(originalScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Original Table", originalTabPanel);

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

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));

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

        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);

        fetchExistingViews();
        populateViewList();

        if (!existingViews.isEmpty()) {
            displayView(existingViews.get(0), originalTableModel, originalTable);
        }
        
        // Add a new button to open a new window with the database schema
       JButton schemaButton = new JButton("View Schema");
schemaButton.addActionListener(e -> showSchema());
JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
buttonPanel2.add(schemaButton);
frame.getContentPane().add(buttonPanel2, BorderLayout.NORTH);

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

    private static void populateViewList() {
        DefaultListModel<String> viewListModel = new DefaultListModel<>();
        for (String view : existingViews) {
            viewListModel.addElement(view);
        }
        viewList.setModel(viewListModel);
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

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            for (int column = 1; column <= columnCount; column++) {
                tableModel.addColumn(metaData.getColumnName(column));
            }

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
        fillTable(tableModel, table, currentPage * PAGE_SIZE);
    }

    private static void showSchema() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                ResultSet tables = conn.getMetaData().getTables(null, null, null, null)) {

            RSyntaxTextArea textArea = new RSyntaxTextArea();
            textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
            textArea.setEditable(false);
            textArea.setCodeFoldingEnabled(true);

            RTextScrollPane scrollPane = new RTextScrollPane(textArea);

            JFrame schemaFrame = new JFrame("Database Schema");
            schemaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            schemaFrame.getContentPane().add(scrollPane);
            schemaFrame.setSize(800, 600);
            schemaFrame.setVisible(true);

            StringBuilder schemaText = new StringBuilder();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                schemaText.append("Table: ").append(tableName).append("\n");

                try (ResultSet columns = conn.getMetaData().getColumns(null, null, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String columnType = columns.getString("TYPE_NAME");
                        int columnSize = columns.getInt("COLUMN_SIZE");
                        schemaText.append("  - ").append(columnName).append(" (").append(columnType).append(")").append(" [")
                                .append(columnSize).append("]").append("\n");
                    }
                }

                schemaText.append("\n");
            }

            textArea.setText(schemaText.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
