package ui;

import model.Medication;
import model.Patient;
import service.AuthService;
import service.MedicationSchedulerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicationManagerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);

    private final AuthService authService;
    private final MedicationSchedulerService schedulerService;

    private JTable medicationTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JComboBox<String> typeFilter;

    private JButton editButton;
    private JButton removeButton;

    private JLabel detailNameLabel;
    private JLabel detailTypeLabel;
    private JLabel detailDosageLabel;
    private JLabel detailFrequencyLabel;
    private JLabel detailReminderLabel;
    private JLabel detailStatusLabel;
    private JLabel detailInstructionsLabel;
    private JLabel detailWarningLabel;

    private final List<Medication> currentMedList = new ArrayList<>();

    public MedicationManagerPanel(AuthService authService, MedicationSchedulerService schedulerService) {
        this.authService = authService;
        this.schedulerService = schedulerService;

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.appBackground());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createCenterContent(), BorderLayout.CENTER);
        add(createBottomButtons(), BorderLayout.SOUTH);

        refreshTableData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        header.setBackground(Theme.appBackground());
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Theme.appBackground());

        JLabel title = new JLabel("Medication Manager");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Theme.textColor());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Manage your medication");
        subtitle.setFont(Theme.font(Font.PLAIN, 14));
        subtitle.setForeground(Theme.mutedTextColor());
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        header.add(titlePanel);

        return header;
    }

    private JPanel createCenterContent() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Theme.appBackground());

        content.add(createFilterBar(), BorderLayout.NORTH);
        content.add(createTableAndDetails(), BorderLayout.CENTER);

        return content;
    }

    private JPanel createFilterBar() {
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filterBar.setBackground(Theme.appBackground());

        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setForeground(Theme.textColor());

        typeFilter = new JComboBox<>(new String[]{"All", "Prescribed Medicine", "Supplement"});
        Theme.styleComboBox(typeFilter);
        typeFilter.addActionListener(e -> applyFilters());

        JButton addButton = createPrimaryButton("+ Add Medication");
        addButton.addActionListener(e -> handleAddMedication());

        filterBar.add(typeLabel);
        filterBar.add(typeFilter);
        filterBar.add(Box.createHorizontalStrut(15));
        filterBar.add(addButton);

        return filterBar;
    }

    private JSplitPane createTableAndDetails() {
        JScrollPane tableScrollPane = createTablePanel();
        JPanel detailsPanel = createDetailsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, detailsPanel);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerLocation(580);
        splitPane.setBorder(null);

        return splitPane;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.panelBackground());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Medication Details");
        title.setFont(Theme.font(Font.BOLD, 20));
        title.setForeground(Theme.textColor());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailNameLabel = createDetailLabel("Name: Select a medication");
        detailTypeLabel = createDetailLabel("Type: -");
        detailDosageLabel = createDetailLabel("Dosage: -");
        detailFrequencyLabel = createDetailLabel("Frequency: -");
        detailReminderLabel = createDetailLabel("Next reminder: -");
        detailStatusLabel = createDetailLabel("Status: -");
        detailInstructionsLabel = createDetailLabel("Instructions: -");
        detailWarningLabel = createDetailLabel("Tip/Warning: -");

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(detailNameLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailTypeLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailDosageLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailFrequencyLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailReminderLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailStatusLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailInstructionsLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(detailWarningLabel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        label.setForeground(Theme.textColor());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Name", "Type", "Dosage", "Frequency", "Next Reminder", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        medicationTable = new JTable(tableModel);
        medicationTable.setRowHeight(38);
        medicationTable.setFont(Theme.font(Font.PLAIN, 14));
        medicationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medicationTable.setFillsViewportHeight(true);
        medicationTable.setGridColor(Theme.borderColor());
        medicationTable.setBackground(Theme.panelBackground());
        medicationTable.setForeground(Theme.textColor());
        medicationTable.setSelectionBackground(Theme.primaryColor());
        medicationTable.setSelectionForeground(Theme.buttonTextColor());

        medicationTable.getTableHeader().setFont(Theme.font(Font.BOLD, 14));
        medicationTable.getTableHeader().setBackground(Theme.panelBackground());
        medicationTable.getTableHeader().setForeground(Theme.textColor());

        setColumnWidths();

        medicationTable.getColumnModel().getColumn(1).setCellRenderer(new TypeRenderer());
        medicationTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        sorter = new TableRowSorter<>(tableModel);
        medicationTable.setRowSorter(sorter);

        medicationTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) return;

            int selectedRow = medicationTable.getSelectedRow();
            boolean rowSelected = selectedRow != -1;

            if (editButton != null) editButton.setEnabled(rowSelected);
            if (removeButton != null) removeButton.setEnabled(rowSelected);

            if (rowSelected) {
                int modelRow = medicationTable.convertRowIndexToModel(selectedRow);
                updateDetailsPanel(modelRow);
            } else {
                clearDetailsPanel();
            }
        });

        medicationTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "EDIT_SELECTED_MED"
        );
        medicationTable.getActionMap().put("EDIT_SELECTED_MED", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (medicationTable.getSelectedRow() != -1) {
                    handleEditMedication();
                }
            }
        });

        medicationTable.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), "REMOVE_SELECTED_MED"
        );
        medicationTable.getActionMap().put("REMOVE_SELECTED_MED", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (medicationTable.getSelectedRow() != -1) {
                    handleRemoveMedication();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(medicationTable);
        scrollPane.setBackground(Theme.panelBackground());
        scrollPane.getViewport().setBackground(Theme.panelBackground());

        return scrollPane;
    }

    public void refreshTableData() {
        int selectedRow = medicationTable != null ? medicationTable.getSelectedRow() : -1;
        int selectedMedId = -1;
        if (selectedRow != -1 && selectedRow < medicationTable.getRowCount()) {
            int modelRow = medicationTable.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < currentMedList.size()) {
                selectedMedId = currentMedList.get(modelRow).getId();
            }
        }

        tableModel.setRowCount(0);
        currentMedList.clear();

        Patient p = authService.getCurrentPatient();
        if (p != null) {
            currentMedList.addAll(p.getMedications());
        }

        int newSelectedViewRow = -1;
        for (int i = 0; i < currentMedList.size(); i++) {
            Medication med = currentMedList.get(i);
            String timesStr = formatIntakeTimes(med.getIntake());
            tableModel.addRow(new Object[]{
                    med.getName(),
                    med.getType(),
                    med.getDosageWithUnit(),
                    med.getFrequencyPerDay() + " time(s)/day",
                    timesStr,
                    med.isActive() ? "Active" : "Inactive"
            });

            if (selectedMedId != -1 && med.getId() == selectedMedId) {
                newSelectedViewRow = i;
            }
        }

        if (newSelectedViewRow != -1 && medicationTable != null) {
            int viewRow = medicationTable.convertRowIndexToView(newSelectedViewRow);
            if (viewRow != -1) {
                medicationTable.setRowSelectionInterval(viewRow, viewRow);
                updateDetailsPanel(newSelectedViewRow);
            } else {
                clearDetailsPanel();
            }
        } else {
            clearDetailsPanel();
        }
    }

    private String formatIntakeTimes(List<LocalTime> times) {
        if (times == null || times.isEmpty()) return "-";
        List<String> formatted = new ArrayList<>();
        for (LocalTime t : times) {
            formatted.add(t.format(TIME_FORMATTER));
        }
        return String.join(", ", formatted);
    }

    private void setColumnWidths() {
        medicationTable.getTableHeader().setResizingAllowed(false);
        medicationTable.getTableHeader().setReorderingAllowed(false);

        medicationTable.getColumnModel().getColumn(0).setPreferredWidth(160); // Name
        medicationTable.getColumnModel().getColumn(1).setPreferredWidth(110); // Type
        medicationTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Dosage
        medicationTable.getColumnModel().getColumn(3).setPreferredWidth(110); // Frequency
        medicationTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Next Reminder
        medicationTable.getColumnModel().getColumn(5).setPreferredWidth(90);  // Status
    }

    private JPanel createBottomButtons() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.appBackground());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setBackground(Theme.appBackground());

        editButton = createSecondaryButton("Edit");
        removeButton = createDangerButton("Remove");

        editButton.setEnabled(false);
        removeButton.setEnabled(false);

        editButton.addActionListener(e -> handleEditMedication());
        removeButton.addActionListener(e -> handleRemoveMedication());

        actionPanel.add(editButton);
        actionPanel.add(removeButton);

        bottom.add(actionPanel, BorderLayout.EAST);

        return bottom;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setBackground(Theme.primaryColor());
        button.setForeground(Theme.buttonTextColor());
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 38));
        button.setMargin(new Insets(8, 16, 8, 16));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.PLAIN, 13));
        button.setBackground(Theme.panelBackground());
        button.setForeground(Theme.textColor());
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 36));
        button.setMargin(new Insets(8, 16, 8, 16));
        return button;
    }

    private JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.BOLD, 13));
        button.setBackground(Theme.dangerColor());
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 36));
        button.setMargin(new Insets(8, 16, 8, 16));
        return button;
    }

    private void handleAddMedication() {
        Patient p = authService.getCurrentPatient();
        List<String> existingNames = p != null ? p.getMedications().stream().map(Medication::getName).toList() : new ArrayList<>();

        Window owner = SwingUtilities.getWindowAncestor(this);
        AddMedicationDialog dialog = new AddMedicationDialog(owner);
        dialog.setExistingNames(existingNames, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            if (p != null) {
                int nextId = p.getMedications().stream().mapToInt(Medication::getId).max().orElse(0) + 1;
                Medication newMed = dialog.buildMedication(nextId);
                p.addMedication(newMed);
                authService.saveData();
                schedulerService.addMedication(newMed);
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Medication added successfully.");
            }
        }
    }

    private void handleEditMedication() {
        int selectedRow = medicationTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = medicationTable.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= currentMedList.size()) return;

        Medication med = currentMedList.get(modelRow);
        Window owner = SwingUtilities.getWindowAncestor(this);

        String timesStr = formatIntakeTimes(med.getIntake());
        String startStr = med.getStartDate() != null ? med.getStartDate().format(DATE_FORMATTER) : "";
        String endStr = med.getEndDate() != null ? med.getEndDate().format(DATE_FORMATTER) : "";

        Patient p = authService.getCurrentPatient();
        List<String> existingNames = p != null ? p.getMedications().stream().map(Medication::getName).toList() : new ArrayList<>();

        EditMedicationDialog dialog = new EditMedicationDialog(
                owner,
                med.getName(),
                med.getType(),
                med.getDosage() != null ? med.getDosage() : "1",
                med.getUnit() != null ? med.getUnit() : "tablet",
                med.getFrequencyPerDay(),
                timesStr,
                startStr,
                endStr,
                med.getInstructions() != null ? med.getInstructions() : "",
                med.isActive()
        );
        dialog.setExistingNames(existingNames, med.getName());
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Medication updated = dialog.buildMedication(med.getId());
            for (model.MedicationLog log : med.getLogs()) {
                updated.addLog(log);
            }
            if (p != null) {
                p.removeMedication(med.getId());
                p.addMedication(updated);
            }

            authService.saveData();
            schedulerService.refreshMedications();
            refreshTableData();
            JOptionPane.showMessageDialog(this, "Medication updated successfully.");
        }
    }

    private void handleRemoveMedication() {
        int selectedRow = medicationTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = medicationTable.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= currentMedList.size()) return;

        Medication med = currentMedList.get(modelRow);

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Remove " + med.getName() + "?",
                "Confirm removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer == JOptionPane.YES_OPTION) {
            Patient p = authService.getCurrentPatient();
            if (p != null) {
                p.removeMedication(med.getId());
                authService.saveData();
                schedulerService.removeMedication(med);
                refreshTableData();
            }
        }
    }

    private void applyFilters() {
        String selectedType = typeFilter.getSelectedItem().toString();

        RowFilter<DefaultTableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                String type = entry.getStringValue(1);
                if (selectedType.equals("All")) return true;
                if (selectedType.equalsIgnoreCase("Prescribed Medicine") || selectedType.equalsIgnoreCase("Medicine")) {
                    return type.equalsIgnoreCase("Prescribed Medicine") || type.equalsIgnoreCase("Medicine") || type.equalsIgnoreCase("Prescription");
                }
                return type.equalsIgnoreCase(selectedType);
            }
        };

        sorter.setRowFilter(filter);
    }

    private void updateDetailsPanel(int row) {
        if (row < 0 || row >= currentMedList.size()) {
            clearDetailsPanel();
            return;
        }

        Medication med = currentMedList.get(row);

        detailNameLabel.setText("Name: " + med.getName());
        detailTypeLabel.setText("Type: " + med.getType());
        detailDosageLabel.setText("Dosage: " + med.getDosageWithUnit());
        detailFrequencyLabel.setText("Frequency: " + med.getFrequencyPerDay() + " time(s)/day");
        detailReminderLabel.setText("Next reminder: " + formatIntakeTimes(med.getIntake()));
        detailStatusLabel.setText("Status: " + (med.isActive() ? "Active" : "Inactive"));
        
        String instructionsText = (med.getInstructions() == null || med.getInstructions().isBlank()) ? "-" : med.getInstructions();
        String textColorHex = Theme.textColorToHex();
        detailInstructionsLabel.setText("<html><body style=\"font-family: 'Segoe UI Emoji', 'Segoe UI', sans-serif; color: " + textColorHex + ";\"><b>Instructions:</b> " + instructionsText + "</body></html>");
        detailWarningLabel.setText("<html><body style=\"font-family: 'Segoe UI Emoji', 'Segoe UI', sans-serif; color: " + textColorHex + ";\"><b>Tip/Warning:</b> " + med.getDosageWarning() + "</body></html>");

        if (med.isActive()) {
            detailStatusLabel.setForeground(Theme.successColor());
        } else {
            detailStatusLabel.setForeground(Theme.mutedTextColor());
        }
    }

    private void clearDetailsPanel() {
        String textColorHex = Theme.textColorToHex();
        detailNameLabel.setText("Name: Select a medication");
        detailTypeLabel.setText("Type: -");
        detailDosageLabel.setText("Dosage: -");
        detailFrequencyLabel.setText("Frequency: -");
        detailReminderLabel.setText("Next reminder: -");
        detailStatusLabel.setText("Status: -");
        detailInstructionsLabel.setText("<html><body style=\"color: " + textColorHex + ";\"><b>Instructions:</b> -</body></html>");
        detailWarningLabel.setText("<html><body style=\"color: " + textColorHex + ";\"><b>Tip/Warning:</b> -</body></html>");
        detailStatusLabel.setForeground(Theme.textColor());
    }

    private class TypeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                label.setBackground(Theme.panelBackground());
                if ("Prescribed Medicine".equalsIgnoreCase(String.valueOf(value)) || "Medicine".equalsIgnoreCase(String.valueOf(value)) || "Prescription".equalsIgnoreCase(String.valueOf(value))) {
                    label.setForeground(Theme.dangerColor());
                } else {
                    label.setForeground(Theme.primaryColor());
                }
            } else {
                label.setBackground(Theme.primaryColor());
                label.setForeground(Theme.buttonTextColor());
            }
            return label;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                label.setBackground(Theme.panelBackground());
                if ("Active".equals(value)) {
                    label.setForeground(Theme.successColor());
                } else {
                    label.setForeground(Theme.mutedTextColor());
                }
            } else {
                label.setBackground(Theme.primaryColor());
                label.setForeground(Theme.buttonTextColor());
            }
            return label;
        }
    }
}
