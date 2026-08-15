package ui;

import model.Medication;
import model.MedicationLog;
import model.Patient;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    private final AuthService authService;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JComboBox<String> statusFilter;
    private JPanel individualAdherencePanel;

    public HistoryPanel(AuthService authService) {
        this.authService = authService;
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.appBackground());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createCenterContent(), BorderLayout.CENTER);

        refreshHistory();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        header.setBackground(Theme.appBackground());
        header.setBorder(new EmptyBorder(0, 0, 5, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Theme.appBackground());

        JLabel title = new JLabel("Medication History & Adherence");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Theme.textColor());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Track individual medication adherence and log history.");
        subtitle.setFont(Theme.font(Font.PLAIN, 14));
        subtitle.setForeground(Theme.mutedTextColor());
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        header.add(titlePanel);

        return header;
    }

    private JPanel createCenterContent() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Theme.appBackground());

        individualAdherencePanel = new JPanel();
        individualAdherencePanel.setLayout(new BoxLayout(individualAdherencePanel, BoxLayout.Y_AXIS));
        individualAdherencePanel.setBackground(Theme.appBackground());

        JScrollPane adherenceScrollPane = new JScrollPane(individualAdherencePanel);
        adherenceScrollPane.setBackground(Theme.appBackground());
        adherenceScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                "Individual Medication Progress (Completed / Total Doses)",
                0, 0,
                Theme.font(Font.BOLD, 15),
                Theme.textColor()
        ));
        adherenceScrollPane.getViewport().setBackground(Theme.appBackground());
        adherenceScrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 160));
        adherenceScrollPane.getVerticalScrollBar().setUnitIncrement(12);

        JPanel topAndFilter = new JPanel(new BorderLayout(8, 8));
        topAndFilter.setBackground(Theme.appBackground());
        topAndFilter.add(adherenceScrollPane, BorderLayout.CENTER);
        topAndFilter.add(createFilterBar(), BorderLayout.SOUTH);

        content.add(topAndFilter, BorderLayout.NORTH);
        content.add(createTable(), BorderLayout.CENTER);

        return content;
    }

    private JPanel createFilterBar() {
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        filterBar.setBackground(Theme.appBackground());

        JLabel statusLabel = new JLabel("Filter History by Status:");
        statusLabel.setFont(Theme.font(Font.PLAIN, 14));
        statusLabel.setForeground(Theme.textColor());

        statusFilter = new JComboBox<>(new String[]{"All", "Taken", "Late", "Missed"});
        Theme.styleComboBox(statusFilter);
        statusFilter.addActionListener(e -> applyFilters());

        filterBar.add(statusLabel);
        filterBar.add(statusFilter);

        return filterBar;
    }

    private JScrollPane createTable() {
        String[] columns = {"Date", "Medication", "Scheduled Time", "Status", "Notes"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(36);
        historyTable.setFont(Theme.font(Font.PLAIN, 14));
        historyTable.setFillsViewportHeight(true);

        historyTable.setBackground(Theme.panelBackground());
        historyTable.setForeground(Theme.textColor());
        historyTable.setSelectionBackground(Theme.primaryColor());
        historyTable.setSelectionForeground(Theme.buttonTextColor());
        historyTable.setGridColor(Theme.borderColor());

        historyTable.getTableHeader().setFont(Theme.font(Font.BOLD, 14));
        historyTable.getTableHeader().setBackground(Theme.panelBackground());
        historyTable.getTableHeader().setForeground(Theme.textColor());
        historyTable.getTableHeader().setResizingAllowed(true);
        historyTable.getTableHeader().setReorderingAllowed(false);

        setColumnWidths();
        historyTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        sorter = new TableRowSorter<>(tableModel);
        historyTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBackground(Theme.panelBackground());
        scrollPane.getViewport().setBackground(Theme.panelBackground());

        return scrollPane;
    }

    private void setColumnWidths() {
        if (historyTable == null) return;
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(140); // Medication
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(130); // Scheduled Time
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(90);  // Status
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(350); // Notes
    }

    public void refreshHistory() {
        int selectedRow = historyTable != null ? historyTable.getSelectedRow() : -1;

        tableModel.setRowCount(0);

        Patient p = authService.getCurrentPatient();
        List<MedicationLog> logs = p != null ? p.getAllLogs() : new ArrayList<>();

        for (MedicationLog log : logs) {
            String dateStr = log.getTakenTimestamp() != null ? log.getTakenTimestamp().format(DATE_FORMATTER) : "-";
            String timeStr = log.getScheduledTime() != null ? log.getScheduledTime().format(TIME_FORMATTER) : "-";

            tableModel.addRow(new Object[]{
                    dateStr,
                    log.getMedicationName(),
                    timeStr,
                    log.getStatus(),
                    log.getNotes()
            });
        }

        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount() && historyTable != null) {
            historyTable.setRowSelectionInterval(selectedRow, selectedRow);
        }

        refreshIndividualAdherence(p);
    }

    private void refreshIndividualAdherence(Patient p) {
        if (individualAdherencePanel == null) return;
        individualAdherencePanel.removeAll();

        if (p == null || p.getMedications().isEmpty()) {
            JLabel emptyLabel = new JLabel("No medications registered.");
            emptyLabel.setFont(new Font("Times New Roman", Font.ITALIC, 14));
            emptyLabel.setForeground(Theme.mutedTextColor());
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            individualAdherencePanel.add(emptyLabel);
            individualAdherencePanel.revalidate();
            individualAdherencePanel.repaint();
            return;
        }

        for (Medication med : p.getMedications()) {
            int freq = med.getFrequencyPerDay();
            if (freq <= 0) {
                freq = Math.max(1, med.getIntake().size());
            }

            boolean isLifetime = false;
            long totalDays = 30;

            if (med.getEndDate() == null) {
                isLifetime = true;
            } else if (med.getStartDate() != null) {
                totalDays = ChronoUnit.DAYS.between(med.getStartDate(), med.getEndDate());
                if (totalDays >= 365 * 30) {
                    isLifetime = true;
                }
                if (totalDays <= 0) totalDays = 1;
            }

            int totalDoses = (int) (totalDays * freq);
            if (totalDoses <= 0) totalDoses = 1;

            long takenCount = med.getLogs().stream().filter(MedicationLog::isWasTaken).count();
            int completedDoses = (int) Math.min(takenCount, totalDoses);

            JPanel row = new JPanel(new BorderLayout(15, 0));
            row.setBackground(Theme.panelBackground());
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.borderColor()),
                    new EmptyBorder(8, 12, 8, 12)
            ));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

            String timesText = freq + " time" + (freq != 1 ? "s" : "") + " a day";
            String labelText;

            JProgressBar pb = new JProgressBar(0, Math.max(1, totalDoses));
            pb.setFont(Theme.font(Font.BOLD, 12));
            pb.setPreferredSize(new Dimension(180, 22));
            pb.setForeground(Theme.primaryColor());
            pb.setBackground(Theme.secondaryPanelBackground());

            String textColorHex = Theme.textColorToHex();
            if (isLifetime) {
                labelText = "<html><body style='color:" + textColorHex + ";'><span style='font-family: Segoe UI, sans-serif;'><b>" + med.getName() + "</b> (" + med.getType() + ") &nbsp;—&nbsp; " +
                        timesText + " (Lifetime)</span></body></html>";
                pb.setValue(100);
                pb.setStringPainted(true);
                pb.setString("Will continue");
            } else {
                String daysText = totalDays + " day" + (totalDays != 1 ? "s" : "");
                labelText = "<html><body style='color:" + textColorHex + ";'><span style='font-family: Segoe UI, sans-serif;'><b>" + med.getName() + "</b> (" + med.getType() + ") &nbsp;—&nbsp; " +
                        timesText + " for " + daysText + ". Total doses: <b>" + totalDoses + "</b></span></body></html>";
                pb.setValue(completedDoses);
                pb.setStringPainted(true);
                pb.setString(completedDoses + "/" + totalDoses);
            }

            JLabel nameLabel = new JLabel(labelText);
            nameLabel.setFont(Theme.font(Font.PLAIN, 14));
            nameLabel.setForeground(Theme.textColor());

            row.add(nameLabel, BorderLayout.CENTER);
            row.add(pb, BorderLayout.EAST);

            individualAdherencePanel.add(row);
        }

        individualAdherencePanel.revalidate();
        individualAdherencePanel.repaint();
    }

    private void applyFilters() {
        String selectedStatus = statusFilter.getSelectedItem().toString();

        RowFilter<DefaultTableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                String status = entry.getStringValue(3);
                return selectedStatus.equals("All") || status.equalsIgnoreCase(selectedStatus);
            }
        };

        sorter.setRowFilter(filter);
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                label.setBackground(Theme.panelBackground());
                String valStr = String.valueOf(value);
                if (valStr.contains("Taken") && !valStr.contains("Late")) {
                    label.setForeground(Theme.successColor()); // Green (On Time)
                } else if (valStr.contains("Late")) {
                    label.setForeground(Theme.warningColor()); // Yellow (Late)
                } else if (valStr.contains("Missed") || valStr.contains("Overdue")) {
                    label.setForeground(Theme.dangerColor()); // Red (Missed)
                } else {
                    label.setForeground(Theme.textColor());
                }
            } else {
                label.setBackground(Theme.primaryColor());
                label.setForeground(Theme.buttonTextColor());
            }
            return label;
        }
    }
}
