package ui;

import model.Medication;
import model.Prescription;
import model.Supplement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddMedicationDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);

    private final JTextField nameField;
    private final JComboBox<String> typeComboBox;
    private final JTextField dosageField;
    private final JComboBox<String> unitComboBox;
    private final JComboBox<Integer> frequencyComboBox;
    
    private final DatePickerButton startDatePicker;
    
    private final JComboBox<String> durationComboBox;
    private final JPanel customDurationPanel;
    private final JTextField customDurationField;
    private final JComboBox<String> customUnitComboBox;

    private final JTextArea instructionsArea;
    private final JCheckBox activeCheckBox;

    private final List<String> existingNames = new ArrayList<>();
    private final List<String> existingScheduledTimes = new ArrayList<>();
    private String scheduledTimes = "";
    private boolean saved;

    public AddMedicationDialog(Window owner) {
        super(owner, "Add Medication", ModalityType.APPLICATION_MODAL);
        IconUtil.applyAppIcon(this);

        saved = false;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(650, 620);
        setMinimumSize(new Dimension(600, 580));
        setLocationRelativeTo(owner);

        nameField = new JTextField();
        typeComboBox = new JComboBox<>(new String[]{"Prescribed Medicine", "Supplement"});
        Theme.styleComboBox(typeComboBox);
        dosageField = new JTextField("1");
        unitComboBox = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Drops"});
        Theme.styleComboBox(unitComboBox);
        
        frequencyComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        Theme.styleComboBox(frequencyComboBox);

        startDatePicker = new DatePickerButton(LocalDate.now());

        durationComboBox = new JComboBox<>(new String[]{
                "3 days",
                "7 days",
                "14 days",
                "1 month",
                "6 months",
                "1 year",
                "Lifetime",
                "Custom..."
        });
        Theme.styleComboBox(durationComboBox);
        durationComboBox.setSelectedItem("1 month");

        customDurationField = new JTextField("10", 6);
        Theme.styleTextField(customDurationField);
        customUnitComboBox = new JComboBox<>(new String[]{"Days", "Months", "Years"});
        Theme.styleComboBox(customUnitComboBox);

        customDurationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        customDurationPanel.setBackground(Theme.appBackground());

        JLabel customLabel = new JLabel("Custom Value:");
        customLabel.setForeground(Theme.textColor());

        customDurationPanel.add(customLabel);
        customDurationPanel.add(customDurationField);
        customDurationPanel.add(customUnitComboBox);
        customDurationPanel.setVisible(false);

        durationComboBox.addActionListener(e -> {
            boolean isCustom = "Custom...".equals(durationComboBox.getSelectedItem());
            customDurationPanel.setVisible(isCustom);
            revalidate();
            repaint();
        });

        instructionsArea = new JTextArea(4, 20);
        activeCheckBox = new JCheckBox("Medication is active", true);

        setContentPane(createContent());
    }

    private JPanel createContent() {
        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(Theme.appBackground());
        main.setBorder(new EmptyBorder(24, 24, 24, 24));

        main.add(createHeader(), BorderLayout.NORTH);
        main.add(createFormSection(), BorderLayout.CENTER);
        main.add(createButtons(), BorderLayout.SOUTH);

        return main;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.appBackground());
        header.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel title = new JLabel("Add Medication");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Theme.textColor());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Fill in the medication details below.");
        subtitle.setFont(Theme.font(Font.PLAIN, 14));
        subtitle.setForeground(Theme.mutedTextColor());
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    private JScrollPane createFormSection() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.appBackground());
        form.setBorder(new EmptyBorder(12, 6, 12, 6));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;

        int row = 0;

        addField(form, gbc, row++, "Name", nameField);
        addField(form, gbc, row++, "Type", typeComboBox);
        addField(form, gbc, row++, "Dosage", dosageField);
        addField(form, gbc, row++, "Unit", unitComboBox);
        addField(form, gbc, row++, "Times per day", frequencyComboBox);
        addField(form, gbc, row++, "Start date", startDatePicker);
        addField(form, gbc, row++, "Duration / Period", durationComboBox);

        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(customDurationPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel instructionsLabel = new JLabel("Instructions");
        instructionsLabel.setForeground(Theme.textColor());
        instructionsLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
        form.add(instructionsLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;

        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        Theme.styleTextArea(instructionsArea);

        JScrollPane instructionsScroll = new JScrollPane(instructionsArea);
        instructionsScroll.setPreferredSize(new Dimension(250, 90));

        form.add(instructionsScroll, gbc);
        row++;

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        activeCheckBox.setBackground(Theme.appBackground());
        activeCheckBox.setForeground(Theme.textColor());

        form.add(activeCheckBox, gbc);

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Theme.appBackground());
        scrollPane.getViewport().setBackground(Theme.appBackground());

        return scrollPane;
    }

    private void addField(
            JPanel form,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JComponent field
    ) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.textColor());
        label.setBorder(new EmptyBorder(0, 0, 0, 8));
        form.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        if (field instanceof JTextField textField) {
            Theme.styleTextField(textField);
        } else if (field instanceof JComboBox<?> comboBox) {
            Theme.styleComboBox(comboBox);
        }

        field.setPreferredSize(new Dimension(250, 32));
        form.add(field, gbc);
    }

    private JPanel createButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setBackground(Theme.appBackground());

        JButton cancelButton = new JButton("Cancel");
        JButton nextButton = new JButton("Next");

        cancelButton.setBackground(Theme.panelBackground());
        cancelButton.setForeground(Theme.textColor());
        cancelButton.setFocusPainted(false);
        cancelButton.setMargin(new Insets(8, 16, 8, 16));

        nextButton.setBackground(Theme.primaryColor());
        nextButton.setForeground(Theme.buttonTextColor());
        nextButton.setFocusPainted(false);
        nextButton.setMargin(new Insets(8, 16, 8, 16));

        cancelButton.addActionListener(e -> {
            saved = false;
            dispose();
        });

        nextButton.addActionListener(e -> saveMedication());

        getRootPane().setDefaultButton(nextButton);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_NEXT"
        );
        getRootPane().getActionMap().put("ENTER_NEXT", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner instanceof JTextArea) {
                    return;
                }
                nextButton.doClick();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "ESCAPE_CANCEL"
        );
        getRootPane().getActionMap().put("ESCAPE_CANCEL", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelButton.doClick();
            }
        });

        SwingUtilities.invokeLater(nameField::requestFocusInWindow);

        buttons.add(cancelButton);
        buttons.add(nextButton);

        return buttons;
    }

    private LocalDate computeEndDate(LocalDate start) throws IllegalArgumentException {
        String selected = durationComboBox.getSelectedItem().toString();
        switch (selected) {
            case "3 days": return start.plusDays(3);
            case "7 days": return start.plusDays(7);
            case "14 days": return start.plusDays(14);
            case "1 month": return start.plusDays(30);
            case "6 months": return start.plusDays(180);
            case "1 year": return start.plusDays(360);
            case "Lifetime": return start.plusYears(50);
            case "Custom...": {
                String numStr = customDurationField.getText().trim();
                if (numStr.isEmpty() || numStr.startsWith("-")) {
                    throw new IllegalArgumentException("Custom period duration must be a positive number.");
                }
                int val;
                try {
                    val = Integer.parseInt(numStr);
                    if (val <= 0) {
                        throw new IllegalArgumentException("Custom period duration must be a positive number.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Custom period duration must be a valid positive whole number.");
                }

                String unit = customUnitComboBox.getSelectedItem().toString();
                if ("Days".equalsIgnoreCase(unit)) return start.plusDays(val);
                if ("Months".equalsIgnoreCase(unit)) return start.plusDays(val * 30L);
                if ("Years".equalsIgnoreCase(unit)) return start.plusDays(val * 360L);
                return start.plusDays(30);
            }
            default: return start.plusDays(30);
        }
    }

    private void saveMedication() {
        String name = nameField.getText().trim();
        String dosage = dosageField.getText().trim();

        if (name.isEmpty()) {
            showError("Please enter a medication name.");
            nameField.requestFocusInWindow();
            return;
        }

        if (existingNames.contains(name.toLowerCase())) {
            showError("A medication named \"" + name + "\" already exists. Duplicate medications are not allowed.");
            nameField.requestFocusInWindow();
            return;
        }

        if (dosage.isEmpty()) {
            showError("Please enter the dosage.");
            dosageField.requestFocusInWindow();
            return;
        }

        if (dosage.startsWith("-")) {
            showError("Dosage cannot be zero or negative.");
            dosageField.requestFocusInWindow();
            return;
        }

        try {
            double val = Double.parseDouble(dosage);
            if (val <= 0) {
                showError("Dosage cannot be zero or negative.");
                dosageField.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException e) {
            if (dosage.equalsIgnoreCase("0") || dosage.startsWith("0 ")) {
                showError("Dosage cannot be zero or negative.");
                dosageField.requestFocusInWindow();
                return;
            }
        }

        LocalDate start = startDatePicker.getSelectedDate();

        try {
            computeEndDate(start);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            if (customDurationPanel.isVisible()) {
                customDurationField.requestFocusInWindow();
            }
            return;
        }

        int count = getFrequencyPerDay();
        ScheduleTimesDialog timesDialog = new ScheduleTimesDialog(this, count, existingScheduledTimes);
        timesDialog.setVisible(true);

        if (timesDialog.isSaved()) {
            this.scheduledTimes = timesDialog.getScheduledTimes();
            this.saved = true;
            dispose();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Invalid information",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public void setExistingNames(List<String> names, String currentName) {
        existingNames.clear();
        if (names != null) {
            for (String n : names) {
                if (n != null && (currentName == null || !n.equalsIgnoreCase(currentName))) {
                    existingNames.add(n.trim().toLowerCase());
                }
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Medication buildMedication(int id) {
        String name = nameField.getText().trim();
        String type = typeComboBox.getSelectedItem().toString();
        String dosage = dosageField.getText().trim();
        String unit = unitComboBox.getSelectedItem().toString();
        int freq = getFrequencyPerDay();
        
        LocalDate start = startDatePicker.getSelectedDate();
        LocalDate end = computeEndDate(start);
        String instructions = instructionsArea.getText().trim();
        boolean active = activeCheckBox.isSelected();

        String[] timesStr = getScheduledTimes().split(",");
        List<LocalTime> timesList = new ArrayList<>();
        for (String t : timesStr) {
            timesList.add(LocalTime.parse(t.trim(), TIME_FORMATTER));
        }

        LocalTime[] intakeArr = timesList.toArray(new LocalTime[0]);

        if ("Prescribed Medicine".equalsIgnoreCase(type) || "Medicine".equalsIgnoreCase(type) || "Prescription".equalsIgnoreCase(type)) {
            return new Prescription(id, name, dosage, unit, freq, start, end, instructions, active, intakeArr);
        } else {
            return new Supplement(id, name, dosage, unit, freq, start, end, instructions, active, intakeArr);
        }
    }

    public String getMedicationName() { return nameField.getText().trim(); }
    public String getMedicationType() { return typeComboBox.getSelectedItem().toString(); }
    public String getDosage() { return dosageField.getText().trim(); }
    public String getUnit() { return unitComboBox.getSelectedItem().toString(); }
    
    public int getFrequencyPerDay() {
        Object sel = frequencyComboBox.getSelectedItem();
        if (sel instanceof Integer) {
            return (Integer) sel;
        }
        return 1;
    }

    public String getScheduledTimes() {
        return scheduledTimes;
    }

    public void setExistingMedication(
            String name,
            String type,
            String dosage,
            String unit,
            int frequencyPerDay,
            String scheduledTimes,
            String startDate,
            String endDate,
            String instructions,
            boolean active
    ) {
        nameField.setText(name);
        typeComboBox.setSelectedItem(type);
        dosageField.setText(dosage);
        unitComboBox.setSelectedItem(unit);
        
        if (frequencyPerDay >= 1 && frequencyPerDay <= 4) {
            frequencyComboBox.setSelectedItem(frequencyPerDay);
        } else {
            frequencyComboBox.addItem(frequencyPerDay);
            frequencyComboBox.setSelectedItem(frequencyPerDay);
        }

        existingScheduledTimes.clear();
        if (scheduledTimes != null && !scheduledTimes.isBlank()) {
            String[] split = scheduledTimes.split(",");
            for (String s : split) {
                existingScheduledTimes.add(s.trim());
            }
        }
        this.scheduledTimes = scheduledTimes != null ? scheduledTimes : "";

        instructionsArea.setText(instructions);
        activeCheckBox.setSelected(active);

        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            startDatePicker.setSelectedDate(start);

            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            long days = ChronoUnit.DAYS.between(start, end);

            if (days >= 18000 || start.plusYears(50).equals(end)) {
                durationComboBox.setSelectedItem("Lifetime");
            } else if (days == 3) {
                durationComboBox.setSelectedItem("3 days");
            } else if (days == 7) {
                durationComboBox.setSelectedItem("7 days");
            } else if (days == 14) {
                durationComboBox.setSelectedItem("14 days");
            } else if (days == 30 || start.plusMonths(1).equals(end)) {
                durationComboBox.setSelectedItem("1 month");
            } else if (days == 180 || start.plusMonths(6).equals(end)) {
                durationComboBox.setSelectedItem("6 months");
            } else if (days == 360 || start.plusYears(1).equals(end)) {
                durationComboBox.setSelectedItem("1 year");
            } else {
                durationComboBox.setSelectedItem("Custom...");
                customDurationField.setText(String.valueOf(days));
                customUnitComboBox.setSelectedItem("Days");
                customDurationPanel.setVisible(true);
            }
        } catch (Exception e) {
            durationComboBox.setSelectedItem("1 month");
        }
    }
}
