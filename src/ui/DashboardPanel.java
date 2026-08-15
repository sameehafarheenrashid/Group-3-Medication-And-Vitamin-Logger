package ui;

import model.Medication;
import model.MedicationLog;
import model.Patient;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AuthService authService;
    private final Color backgroundColor;
    private final Color panelColor;
    private final Color textColor;
    private final Color mutedTextColor;
    private Timer clockTimer;

    public DashboardPanel(AuthService authService) {
        this.authService = authService;
        this.backgroundColor = Theme.appBackground();
        this.panelColor = Theme.panelBackground();
        this.textColor = Theme.textColor();
        this.mutedTextColor = Theme.mutedTextColor();

        setLayout(new BorderLayout(15, 15));
        setBackground(backgroundColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        refreshDashboard();
    }

    public void refreshDashboard() {
        removeAll();
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        header.setBackground(backgroundColor);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(backgroundColor);

        String greeting = getGreeting();
        String username = authService.getCurrentUsername();
        JLabel welcomeLabel = new JLabel(greeting + (username.isEmpty() ? "" : ", " + username + "!"));
        welcomeLabel.setFont(Theme.font(Font.BOLD, 28));
        welcomeLabel.setForeground(textColor);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  •  hh:mm:ss a", Locale.US));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(Theme.font(Font.PLAIN, 15));
        dateLabel.setForeground(mutedTextColor);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (clockTimer != null && clockTimer.isRunning()) {
            clockTimer.stop();
        }

        clockTimer = new Timer(1000, e -> {
            LocalDateTime currentNow = LocalDateTime.now();
            String newDateStr = currentNow.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  •  hh:mm:ss a", Locale.US));
            if (!newDateStr.equals(dateLabel.getText())) {
                dateLabel.setText(newDateStr);
            }
        });
        clockTimer.start();

        titlePanel.add(welcomeLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(dateLabel);

        header.add(titlePanel);

        return header;
    }

    private String getGreeting() {
        LocalTime now = LocalTime.now();
        if (!now.isBefore(LocalTime.of(3, 0)) && now.isBefore(LocalTime.of(12, 0))) return "Good morning";
        if (!now.isBefore(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(17, 0))) return "Good afternoon";
        return "Good evening";
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(backgroundColor);

        JPanel summaryCards = createSummaryCards();
        content.add(summaryCards, BorderLayout.NORTH);

        JPanel schedule = createSchedule();
        JScrollPane scrollPane = new JScrollPane(schedule);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(backgroundColor);

        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 2, 15, 0));
        cards.setBackground(backgroundColor);

        Patient p = authService.getCurrentPatient();
        List<DoseEntry> todayDoses = getTodayDoses(p);
        
        int totalToday = todayDoses.size();
        int completedToday = 0;

        for (DoseEntry de : todayDoses) {
            if (de.isTaken) completedToday++;
        }

        String nextReminderTime = "--:--";
        String nextReminderName = "None scheduled";
        LocalTime now = LocalTime.now();

        DoseEntry nextDose = todayDoses.stream()
                .filter(de -> de.time.isAfter(now) && !de.isTaken)
                .min(Comparator.comparing(de -> de.time))
                .orElse(null);

        if (nextDose != null) {
            nextReminderTime = nextDose.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
            nextReminderName = nextDose.medication.getName();
        }

        cards.add(createSummaryCard(
                "Today's doses",
                completedToday + " / " + totalToday,
                "completed",
                Theme.primaryColor()
        ));

        cards.add(createSummaryCard(
                "Next reminder",
                nextReminderTime,
                nextReminderName,
                Theme.accentColor()
        ));

        return cards;
    }

    private JPanel createSummaryCard(
            String title,
            String value,
            String subtitle,
            Color accentColor
    ) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(panelColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.font(Font.PLAIN, 14));
        titleLabel.setForeground(mutedTextColor);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(Theme.font(Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Theme.font(Font.PLAIN, 12));
        subtitleLabel.setForeground(mutedTextColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(subtitleLabel);

        return card;
    }

    private JPanel createSchedule() {
        JPanel schedule = new JPanel();
        schedule.setLayout(new BoxLayout(schedule, BoxLayout.Y_AXIS));
        schedule.setBackground(backgroundColor);
        schedule.setBorder(new EmptyBorder(5, 0, 5, 0));

        Patient p = authService.getCurrentPatient();
        List<DoseEntry> todayDoses = getTodayDoses(p);

        if (todayDoses.isEmpty()) {
            JLabel emptyLabel = new JLabel("No active medications scheduled for today.");
            emptyLabel.setFont(new Font("Times New Roman", Font.ITALIC, 16));
            emptyLabel.setForeground(mutedTextColor);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            schedule.add(Box.createVerticalStrut(30));
            schedule.add(emptyLabel);
            return schedule;
        }

        List<DoseEntry> morning = new ArrayList<>();
        List<DoseEntry> noon = new ArrayList<>();
        List<DoseEntry> afterNoon = new ArrayList<>();
        List<DoseEntry> night = new ArrayList<>();

        for (DoseEntry de : todayDoses) {
            LocalTime t = de.time;
            if (!t.isBefore(LocalTime.of(3, 0)) && t.isBefore(LocalTime.of(12, 0))) {
                morning.add(de);
            } else if (!t.isBefore(LocalTime.of(12, 0)) && t.isBefore(LocalTime.of(16, 0))) {
                noon.add(de);
            } else if (!t.isBefore(LocalTime.of(16, 0)) && t.isBefore(LocalTime.of(18, 0))) {
                afterNoon.add(de);
            } else {
                night.add(de);
            }
        }

        addTimeSectionIfNotEmpty(schedule, "Morning", morning);
        addTimeSectionIfNotEmpty(schedule, "Noon", noon);
        addTimeSectionIfNotEmpty(schedule, "After Noon", afterNoon);
        addTimeSectionIfNotEmpty(schedule, "Night", night);

        schedule.add(Box.createVerticalGlue());

        return schedule;
    }

    private void addTimeSectionIfNotEmpty(JPanel schedule, String sectionTitle, List<DoseEntry> doses) {
        if (doses.isEmpty()) return;

        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(panelColor);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.borderColor()),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(panelColor);
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel title = new JLabel(sectionTitle);
        title.setFont(Theme.font(Font.BOLD, 18));
        title.setForeground(textColor);

        sectionHeader.add(title, BorderLayout.WEST);

        section.add(sectionHeader);
        section.add(Box.createVerticalStrut(10));

        for (DoseEntry de : doses) {
            section.add(createMedicationRow(de));
            section.add(Box.createVerticalStrut(6));
        }

        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));

        schedule.add(section);
        schedule.add(Box.createVerticalStrut(15));
    }

    private JPanel createMedicationRow(DoseEntry dose) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setBackground(panelColor);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = LocalDateTime.of(LocalDate.now(), dose.time);
        long minutesPassed = java.time.Duration.between(scheduled, now).toMinutes();
        boolean isMissed = dose.isMissed || (!dose.isTaken && minutesPassed >= 360);

        JCheckBox doseCheckBox = new JCheckBox();
        doseCheckBox.setBackground(panelColor);
        doseCheckBox.setFocusable(true);
        doseCheckBox.setFocusPainted(false);
        doseCheckBox.setSelected(dose.isTaken);

        if (isMissed) {
            doseCheckBox.setEnabled(false);
            doseCheckBox.setToolTipText("Missed dose - past 6 hour limit. Cannot be marked as taken.");
            doseCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    String timeStr = dose.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Dose Missed!\n\n" +
                            "This medication was scheduled for " + timeStr + ".\n" +
                            "More than 6 hours have passed since the scheduled time.\n" +
                            "Missed doses cannot be marked as taken.",
                            "Dose Missed",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            });
        }

        doseCheckBox.registerKeyboardAction(
                e -> {
                    if (isMissed || !doseCheckBox.isEnabled()) {
                        String timeStr = dose.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
                        JOptionPane.showMessageDialog(
                                this,
                                "Dose Missed!\n\n" +
                                "This medication was scheduled for " + timeStr + ".\n" +
                                "More than 6 hours have passed since the scheduled time.\n" +
                                "Missed doses cannot be marked as taken.",
                                "Dose Missed",
                                JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        doseCheckBox.doClick();
                    }
                },
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_FOCUSED
        );

        String timeStr = dose.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
        String medText = dose.medication.getName() + " — " + dose.medication.getDosageWithUnit() + " (" + timeStr + ")";
        JLabel medicationLabel = new JLabel(medText);
        medicationLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        medicationLabel.setForeground(textColor);

        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        updateStatusLabel(statusLabel, dose);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(panelColor);

        textPanel.add(medicationLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(statusLabel);

        doseCheckBox.addActionListener(e -> {
            boolean selected = doseCheckBox.isSelected();
            LocalDateTime currentNow = LocalDateTime.now();
            LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), dose.time);
            String scheduledTimeStr = dose.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
            long currentMinutesPassed = java.time.Duration.between(scheduledDateTime, currentNow).toMinutes();

            if (selected) {
                // Prevent checking if 6 hours have passed (Missed)
                if (currentMinutesPassed >= 360 || dose.isMissed) {
                    doseCheckBox.setSelected(false);
                    dose.isTaken = false;
                    JOptionPane.showMessageDialog(
                            this,
                            "Dose Missed!\n\n" +
                            "This medication was scheduled for " + scheduledTimeStr + ".\n" +
                            "More than 6 hours have passed since the scheduled time.\n" +
                            "Missed doses cannot be marked as taken.",
                            "Dose Missed",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Prevent checking before scheduled time
                if (currentNow.isBefore(scheduledDateTime)) {
                    doseCheckBox.setSelected(false);
                    dose.isTaken = false;
                    JOptionPane.showMessageDialog(
                            this,
                            "Cannot log dose before scheduled time!\n\n" +
                            "This medication is scheduled for " + scheduledTimeStr + ".\n" +
                            "Please wait until " + scheduledTimeStr + " to mark it as taken.",
                            "Dose Not Due Yet",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Check if taken within 15 mins vs after 15 mins (Late)
                boolean isLate = currentMinutesPassed > 15;
                String note;
                if (isLate) {
                    note = "Taken late - Please take your medication on time!";
                } else {
                    note = "Taken on time";
                }

                dose.isTaken = true;
                Patient p = authService.getCurrentPatient();
                if (p != null) {
                    p.logMedicationDose(dose.medication, dose.time, true, note);
                    authService.saveData();

                    // Re-fetch log to keep reference accurate
                    dose.log = dose.medication.getLogs().stream().filter(l -> 
                            l.getScheduledTime() != null &&
                            l.getScheduledTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                                    .equals(dose.time.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)) && 
                            l.getTakenTimestamp() != null &&
                            l.getTakenTimestamp().toLocalDate().equals(LocalDate.now()) && 
                            l.isWasTaken()
                    ).findFirst().orElse(null);
                }

                updateStatusLabel(statusLabel, dose);

                if (isLate) {
                    String formattedDelay = formatMinutesToHoursAndMinutes(currentMinutesPassed);
                    JOptionPane.showMessageDialog(
                            this,
                            "Dose Marked as Late!\n\n" +
                            "You logged this dose " + formattedDelay + " past its scheduled time (" + scheduledTimeStr + ").\n" +
                            "Please take your medication on time!\n\n" +
                            "For optimal health results, please try to follow your schedule.",
                            "Taken Late Notice",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } else {
                // Unchecking dose
                dose.isTaken = false;
                dose.log = null;
                Patient p = authService.getCurrentPatient();
                if (p != null) {
                    if (currentMinutesPassed >= 360) {
                        p.logMissedDose(dose.medication, dose.time, "Missed dose (Exceeded 6 hours)");
                    } else {
                        p.logMedicationDose(dose.medication, dose.time, false, null);
                    }
                    authService.saveData();
                }
                updateStatusLabel(statusLabel, dose);
            }
            refreshDashboard();
        });

        row.add(doseCheckBox);
        row.add(textPanel);

        return row;
    }

    private void updateStatusLabel(JLabel statusLabel, DoseEntry dose) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = LocalDateTime.of(LocalDate.now(), dose.time);
        String scheduledTimeStr = dose.time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
        long minutesPassed = java.time.Duration.between(scheduled, now).toMinutes();

        if (dose.isTaken) {
            if (dose.log != null && dose.log.getNotes().toLowerCase().contains("late")) {
                statusLabel.setText("Taken (Late)");
                statusLabel.setForeground(Theme.warningColor()); // Yellow
            } else {
                statusLabel.setText("Taken (On Time)");
                statusLabel.setForeground(Theme.successColor()); // Green
            }
        } else if (dose.isMissed || minutesPassed >= 360) {
            statusLabel.setText("Not taken (Missed)");
            statusLabel.setForeground(Theme.dangerColor()); // Red
        } else {
            if (now.isBefore(scheduled)) {
                statusLabel.setText("Scheduled for " + scheduledTimeStr + " (Not due yet)");
                statusLabel.setForeground(mutedTextColor);
            } else if (minutesPassed > 15) {
                statusLabel.setText("Not taken (Overdue)");
                statusLabel.setForeground(Theme.dangerColor()); // Red
            } else {
                statusLabel.setText("Not taken (Due now)");
                statusLabel.setForeground(Theme.primaryColor());
            }
        }
    }

    private List<DoseEntry> getTodayDoses(Patient p) {
        List<DoseEntry> doses = new ArrayList<>();
        if (p == null) return doses;

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        List<Medication> activeMeds = p.getActiveMedications();
        boolean dataChanged = false;

        for (Medication med : activeMeds) {
            for (LocalTime t : med.getIntake()) {
                MedicationLog logForToday = med.getLogs().stream().filter(l -> 
                        l.getScheduledTime() != null &&
                        l.getScheduledTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                                .equals(t.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)) && 
                        l.getTakenTimestamp() != null &&
                        l.getTakenTimestamp().toLocalDate().equals(today)
                ).findFirst().orElse(null);

                boolean isTaken = logForToday != null && logForToday.isWasTaken();
                boolean isMissed = logForToday != null && !logForToday.isWasTaken();

                if (logForToday == null) {
                    LocalDateTime scheduledDateTime = LocalDateTime.of(today, t);
                    long minutesPassed = java.time.Duration.between(scheduledDateTime, now).toMinutes();
                    if (minutesPassed >= 360) {
                        med.logMissedDose(t, "Missed dose (Exceeded 6 hours)");
                        dataChanged = true;
                        isMissed = true;
                        logForToday = med.getLogs().stream().filter(l -> 
                                l.getScheduledTime() != null &&
                                l.getScheduledTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                                        .equals(t.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)) && 
                                l.getTakenTimestamp() != null &&
                                l.getTakenTimestamp().toLocalDate().equals(today)
                        ).findFirst().orElse(null);
                    }
                }

                doses.add(new DoseEntry(med, t, isTaken, isMissed, logForToday));
            }
        }
        if (dataChanged && authService != null) {
            authService.saveData();
        }
        doses.sort(Comparator.comparing(de -> de.time));
        return doses;
    }

    private static String formatMinutesToHoursAndMinutes(long totalMinutes) {
        if (totalMinutes < 60) {
            return totalMinutes + " minute" + (totalMinutes != 1 ? "s" : "");
        }
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String hourStr = hours + " hour" + (hours != 1 ? "s" : "");
        if (minutes == 0) {
            return hourStr;
        }
        String minuteStr = minutes + " minute" + (minutes != 1 ? "s" : "");
        return hourStr + " " + minuteStr;
    }

    private static class DoseEntry {
        Medication medication;
        LocalTime time;
        boolean isTaken;
        boolean isMissed;
        MedicationLog log;

        DoseEntry(Medication medication, LocalTime time, boolean isTaken, boolean isMissed, MedicationLog log) {
            this.medication = medication;
            this.time = time;
            this.isTaken = isTaken;
            this.isMissed = isMissed;
            this.log = log;
        }
    }
}
