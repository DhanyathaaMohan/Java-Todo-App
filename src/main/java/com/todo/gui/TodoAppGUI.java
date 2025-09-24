package com.todo.gui;

import com.todo.model.Todo;
import com.todo.dao.TodoAppDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class TodoAppGUI extends JFrame {
    private TodoAppDAO todoDAO;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckBox;
    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton refreshButton;
    private JComboBox<String> filterComboBox;

    public TodoAppGUI() {
        this.todoDAO = new TodoAppDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadTodos();
    }

    private void initializeComponents() {
        setTitle("Todo Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Title", "Description", "Completed", "Created At", "Updated At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return String.class; // show as plain text
                return super.getColumnClass(columnIndex);
            }
        };

        todoTable = new JTable(tableModel);
        // Table styling
        todoTable.setBackground(new Color(245, 245, 245)); // White Smoke
        todoTable.setForeground(Color.DARK_GRAY);
        todoTable.getTableHeader().setBackground(new Color(220, 220, 220)); // Light Gray header
        todoTable.getTableHeader().setForeground(Color.BLACK);

        // Table row striping
        todoTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(255, 255, 255)); // White
                    } else {
                        c.setBackground(new Color(240, 248, 255)); // AliceBlue stripe
                    }
                } else {
                    c.setBackground(new Color(173, 216, 230)); // highlight when selected
                }
                c.setForeground(Color.DARK_GRAY);
                return c;
            }
        });

        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedTodo();
        });

        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        completedCheckBox = new JCheckBox("Completed");

        // Buttons
        addButton = new JButton("Add Todo");
        updateButton = new JButton("Update Todo");
        deleteButton = new JButton("Delete Todo");
        refreshButton = new JButton("Refresh Todo");

        Color addUpdateColor = new Color(11, 61, 145); 
        Color deleteColor = new Color(211, 47, 47);    
        Color buttonTextAddUpdate = Color.WHITE;
        Color buttonTextDelete = Color.WHITE;

        addButton.setBackground(addUpdateColor);
        addButton.setForeground(buttonTextAddUpdate);

        updateButton.setBackground(addUpdateColor);
        updateButton.setForeground(buttonTextAddUpdate);

        deleteButton.setBackground(deleteColor);
        deleteButton.setForeground(buttonTextDelete);

        refreshButton.setBackground(addUpdateColor);
        refreshButton.setForeground(buttonTextAddUpdate);

        String[] filterOptions = {"All", "Completed", "Pending"};
        filterComboBox = new JComboBox<>(filterOptions);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Todo Details"));
        inputPanel.setBackground(new Color(255, 250, 240)); // Creamy

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Title"), gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        inputPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Description"), gbc);

        gbc.gridx = 1;
        inputPanel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        inputPanel.add(completedCheckBox, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(245, 245, 245)); // neutral light
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(new Color(255, 250, 240)); // Creamy
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);

        // North panel
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(filterPanel, BorderLayout.NORTH);
        northPanel.add(inputPanel, BorderLayout.CENTER);
        northPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Center (table)
        add(new JScrollPane(todoTable), BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        statusPanel.setBackground(new Color(245, 245, 245));
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        addButton.addActionListener(e -> addTodo());
        updateButton.addActionListener(e -> updateTodo());
        deleteButton.addActionListener(e -> deleteTodo());
        refreshButton.addActionListener(e -> refreshTodo());
        filterComboBox.addActionListener(e -> filterTodos());
    }

    private void addTodo() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Todo todo = new Todo(title, description);
            todo.setCompleted(completed);
            todoDAO.createTodo(todo);

            JOptionPane.showMessageDialog(this, "Todo added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTodos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding todo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTodo() {
        int row = todoTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a todo to update", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) todoTable.getValueAt(row, 0);
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Todo todo = todoDAO.getTodoByID(id);
            if (todo != null) {
                todo.setTitle(title);
                todo.setDescription(description);
                todo.setCompleted(completed);

                if (todoDAO.updateTodo(todo)) {
                    JOptionPane.showMessageDialog(this, "Todo updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTodos();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update todo", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selected todo not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating todo: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTodo() {
        int row = todoTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a todo to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int todoId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected todo?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (todoDAO.deleteTodo(todoId)) {
                    JOptionPane.showMessageDialog(this, "Todo deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    titleField.setText("");
                    descriptionArea.setText("");
                    completedCheckBox.setSelected(false);

                    loadTodos();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete todo", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting todo: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTodo() {
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckBox.setSelected(false);
        loadTodos();
        todoTable.clearSelection();
    }

    private void filterTodos() {
        String filter = (String) filterComboBox.getSelectedItem();
        try {
            List<Todo> todos;
            if (filter.equals("Completed")) {
                todos = todoDAO.filterTodos(true);
            } else if (filter.equals("Pending")) {
                todos = todoDAO.filterTodos(false);
            } else {
                todos = todoDAO.getAllTodos();
            }
            updateTable(todos);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error filtering todos: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadTodos() {
        try {
            List<Todo> todos = todoDAO.getAllTodos();
            updateTable(todos);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading todos: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedTodo() {
        int row = todoTable.getSelectedRow();
        if (row >= 0) {
            String title = (String) tableModel.getValueAt(row, 1);
            String description = (String) tableModel.getValueAt(row, 2);
            boolean completed = Boolean.parseBoolean(String.valueOf(tableModel.getValueAt(row, 3)));

            titleField.setText(title);
            descriptionArea.setText(description);
            completedCheckBox.setSelected(completed);
        }
    }

    private void updateTable(List<Todo> todos) {
        tableModel.setRowCount(0);
        for (Todo t : todos) {
            Object[] rowData = {
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    String.valueOf(t.isCompleted()),
                    t.getCreated_at(),
                    t.getUpdated_at()
            };
            tableModel.addRow(rowData);
        }
    }
}
