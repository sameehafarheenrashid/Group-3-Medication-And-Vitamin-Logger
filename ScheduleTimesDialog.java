package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleTimesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final String[] HOUR_OPTIONS = generateHourOptions();
    private static final String[] MINUTE_OPTIONS = generateMinuteOptions();
    private static final String[] AMPM_OPTIONS = new String[]{"AM", "PM"};

    private final List<DoseTimePickerRow> timePickerRows = new ArrayList<>();
    private String resultScheduledTimes = "";
    private boolean saved = false;

    private static String[] generateHourOptions() {
        String[] hours = new String[12];
        for (int i = 1; i <= 12; i++) {
            hours[i - 1] = String.format("%02d", i);
        }
        return hours;
    }

    private static String[] generateMinuteOptions() {
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i);
        }
        return minutes;
    }

    private static class DoseTimePickerRow {
        JComboBox<String> hourCombo;
        JComboBox<String> minuteCombo;
        JComboBox<String> amPmCombo;

        DoseTimePickerRow(JComboBox<String> hourCombo, JComboBox<String> minuteCombo, JComboBox<String> amPmCombo) {
            this.hourCombo = hourCombo;
            this.minuteCombo = minuteCombo;
            this.amPmCombo = amPmCombo;
        }

        String getFormattedTime() {
            String h = hourCombo.getSelectedItem() != null ? hourCombo.getSelectedItem().toString() : "08";
            String m = minuteCombo.getSelectedItem() != null ? minuteCombo.getSelectedItem().toString() : "00";
            String ap = amPmCombo.getSelectedItem() != null ? amPmCombo.getSelectedItem().toString() : "AM";
            return h + ":" + m + " " + ap;
        }
    }

    public ScheduleTimesDialog(Dialog owner, int count, List<String> existingTimes) {
        super(owner, "Select Dosage Times", ModalityType.APPLICATION_MODAL);
        IconUtil.applyAppIcon(this);
        initUI(owner, count, existingTimes);
    }

    public ScheduleTimesDialog(Frame owner, int count, List<String> existingTimes) {
        super(owner, "Select Dosage Times", ModalityType.APPLICATION_MODAL);
        IconUtil.applyAppIcon(this);
        initUI(owner, count, existingTimes);
    }

    private void initUI(Window owner, int count, List<String> existingTimes) {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        int dialogHeight = 220 + (count * 55);
        setSize(520, dialogHeight);
        setMinimumSize(new Dimension(500, dialogHeight));
        setResizable(false);
        setLocationRelativeTo(owner);

        JPanel main = new JPanel(new BorderLayout(0, 15));
        main.setBackground(Theme.appBackground());
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.appBackground());

        JLabel title = new JLabel("Select Dosage Times");
        title.setFont(Theme.font(Font.BOLD, 22));
        title.setForeground(Theme.textColor());

        JLabel subtitle = new JLabel("Select the scheduled time for each dose (" + count + " dose" + (count > 1 ? "s" : "") + " per day).");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.mutedTextColor());

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setBackground(Theme.appBackground());

        List<String> defaultTimes = new ArrayList<>();
        if (existingTimes != null && !existingTimes.isEmpty()) {
            defaultTimes.addAll(existingTimes);
        } else {
            if (count == 1) {
                defaultTimes.add("08:00 AM");
            } else if (count == 2) {
                defaultTimes.add("08:00 AM");
                defaultTimes.add("08:00 PM");
            } else if (count == 3) {
                defaultTimes.add("08:00 AM");
                defaultTimes.add("02:00 PM");
                defaultTimes.add("08:00 PM");
            } else if (count == 4) {
                defaultTimes.add("08:00 AM");
                defaultTimes.add("12:00 PM");
                defaultTimes.add("04:00 PM");
                defaultTimes.add("08:00 PM");
            }
        }

        for (int i = 0; i < count; i++) {
            JPanel doseRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            doseRow.setBackground(Theme.panelBackground());
            doseRow.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.borderColor()),
                    new EmptyBorder(6, 12, 6, 12)
            ));

            JLabel label = new JLabel(count == 1 ? "Dose Time:" : "Dose " + (i + 1) + " Time:");
            label.setFont(Theme.font(Font.BOLD, 13));
            label.setForeground(Theme.textColor());
            label.setPreferredSize(new Dimension(110, 28));

            JComboBox<String> hourCombo = new JComboBox<>(HOUR_OPTIONS);
            styleComboBox(hourCombo);
            hourCombo.setPreferredSize(new Dimension(75, 32));

            JLabel colonLabel = new JLabel(":");
            colonLabel.setFont(Theme.font(Font.BOLD, 14));
            colonLabel.setForeground(Theme.textColor());

            JComboBox<String> minuteCombo = new JComboBox<>(MINUTE_OPTIONS);
            styleComboBox(minuteCombo);
            minuteCombo.setPreferredSize(new Dimension(75, 32));

            JComboBox<String> amPmCombo = new JComboBox<>(AMPM_OPTIONS);
            styleComboBox(amPmCombo);
            amPmCombo.setPreferredSize(new Dimension(80, 32));

            String rawTime = i < defaultTimes.size() ? defaultTimes.get(i).trim() : "08:00 AM";
            String[] spaceSplit = rawTime.split("\\s+");
            String timeDigits = spaceSplit.length > 0 ? spaceSplit[0] : "08:00";
            String ampmPart = spaceSplit.length > 1 ? spaceSplit[1].toUpperCase() : "AM";

            String hourPart = "08";
            String minutePart = "00";
            if (timeDigits.contains(":")) {
                String[] colonSplit = timeDigits.split(":");
                hourPart = colonSplit[0].trim();
                minutePart = colonSplit[1].trim();
            }

            if (hourPart.length() == 1) hourPart = "0" + hourPart;
            if (minutePart.length() == 1) minutePart = "0" + minutePart;

            hourCombo.setSelectedItem(hourPart);
            minuteCombo.setSelectedItem(minutePart);
            amPmCombo.setSelectedItem("PM".equalsIgnoreCase(ampmPart) ? "PM" : "AM");

            timePickerRows.add(new DoseTimePickerRow(hourCombo, minuteCombo, amPmCombo));

            doseRow.add(label);
            doseRow.add(hourCombo);
            doseRow.add(colonLabel);
            doseRow.add(minuteCombo);
            doseRow.add(amPmCombo);

            rowsPanel.add(doseRow);
            rowsPanel.add(Box.createVerticalStrut(8));
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setBackground(Theme.appBackground());

        JButton backButton = new JButton("Back");
        JButton saveButton = new JButton("Save Medication");

        backButton.setBackground(Theme.panelBackground());
        backButton.setForeground(Theme.textColor());
        backButton.setFocusPainted(false);
        backButton.setMargin(new Insets(8, 16, 8, 16));

        saveButton.setBackground(Theme.primaryColor());
        saveButton.setForeground(Theme.buttonTextColor());
        saveButton.setFocusPainted(false);
        saveButton.setMargin(new Insets(8, 16, 8, 16));

        backButton.addActionListener(e -> {
            saved = false;
            dispose();
        });

        saveButton.addActionListener(e -> {
            List<String> times = new ArrayList<>();
            for (DoseTimePickerRow row : timePickerRows) {
                times.add(row.getFormattedTime());
            }
            resultScheduledTimes = String.join(", ", times);
            saved = true;
            dispose();
        });

        buttons.add(backButton);
        buttons.add(saveButton);

        main.add(header, BorderLayout.NORTH);
        main.add(rowsPanel, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ENTER_SAVE"
        );
        getRootPane().getActionMap().put("ENTER_SAVE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveButton.doClick();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "ESCAPE_CANCEL"
        );
        getRootPane().getActionMap().put("ESCAPE_CANCEL", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                backButton.doClick();
            }
        });

        setContentPane(main);
        pack();
        int calcWidth = Math.max(520, getWidth() + 30);
        int calcHeight = Math.max(260 + (count * 45), getHeight() + 25);
        setSize(calcWidth, calcHeight);
        setLocationRelativeTo(owner);

        if (!timePickerRows.isEmpty()) {
            SwingUtilities.invokeLater(() -> timePickerRows.get(0).hourCombo.requestFocusInWindow());
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        Theme.styleComboBox(comboBox);
    }

    public boolean isSaved() {
        return saved;
    }

    public String getScheduledTimes() {
        return resultScheduledTimes;
    }
}
