package service;

import model.Medication;
import model.Patient;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MedicationSchedulerService {
    private final List<Medication> allMedications = new ArrayList<>();
    private final Set<String> triggeredKeys = new HashSet<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });
    private Patient currentPatient;
    private AuthService authService;
    private TrayIcon trayIcon;

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        refreshMedications();
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    public void refreshMedications() {
        allMedications.clear();
        if (currentPatient != null) {
            allMedications.addAll(currentPatient.getMedications());
        }
    }

    public void startScheduler(Component parentComponent) {
        // Run scheduler every 1 second for zero-delay instant reminder popup & chime!
        scheduler.scheduleAtFixedRate(() -> {
            LocalTime nowTime = LocalTime.now();
            LocalDate nowDate = LocalDate.now();

            if (currentPatient != null) {
                refreshMedications();
            }

            for (Medication med : new ArrayList<>(allMedications)) {
                med.checkAndTriggerReminder(nowTime, nowDate, (medication, scheduledTime) -> {
                    String key = medication.getId() + "_" + scheduledTime.toString() + "_" + 
                            nowDate.toString() + "_" + nowTime.getHour() + ":" + nowTime.getMinute();

                    synchronized (triggeredKeys) {
                        if (triggeredKeys.contains(key)) {
                            return; // Already triggered this minute
                        }
                        triggeredKeys.add(key);
                    }

                    triggerReminder(parentComponent, medication, scheduledTime);
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void triggerReminder(Component parentComponent, Medication medication, LocalTime scheduledTime) {
        // Check if dose has already been taken today
        LocalDate today = LocalDate.now();
        boolean alreadyTaken = medication.getLogs().stream().anyMatch(l -> 
                l.getScheduledTime() != null &&
                l.getScheduledTime().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
                        .equals(scheduledTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES)) &&
                l.getTakenTimestamp() != null &&
                l.getTakenTimestamp().toLocalDate().equals(today) &&
                l.isWasTaken()
        );

        if (alreadyTaken) {
            return; // Don't trigger sound or popup if already logged today
        }

        // Play reminder sound IMMEDIATELY on notification thread!
        playReminderSound();

        SwingUtilities.invokeLater(() -> {
            String timeStr = scheduledTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US));
            String message = "REMINDER!\n\nIt is " + timeStr + ".\nTime to take your " + 
                    medication.getName() + " (" + medication.getDosageWithUnit() + ")!\n\n" +
                    medication.getDosageWarning();

            if (trayIcon != null) {
                trayIcon.displayMessage("Medication Reminder", 
                        "Time to take " + medication.getName() + " (" + medication.getDosageWithUnit() + ")", 
                        TrayIcon.MessageType.INFO);
            }

            Object[] options = {"Take Now", "Snooze (10 Mins)", "Dismiss"};
            int choice = JOptionPane.showOptionDialog(
                    parentComponent,
                    message,
                    "Medication Reminder - " + medication.getName(),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) { // Take Now
                java.time.LocalDateTime currentNow = java.time.LocalDateTime.now();
                java.time.LocalDateTime scheduledDateTime = java.time.LocalDateTime.of(LocalDate.now(), scheduledTime);
                long minutesPassed = java.time.Duration.between(scheduledDateTime, currentNow).toMinutes();

                if (minutesPassed >= 360) {
                    if (currentPatient != null) {
                        currentPatient.logMissedDose(medication.getId(), scheduledTime, "Missed dose (Exceeded 6 hours)");
                        if (authService != null) {
                            authService.saveData();
                        }
                    }
                    JOptionPane.showMessageDialog(
                            parentComponent,
                            "Dose Missed!\n\n" +
                            "This medication was scheduled for " + timeStr + ".\n" +
                            "More than 6 hours have passed since the scheduled time.\n" +
                            "This dose is marked as Missed and cannot be taken.",
                            "Dose Missed",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (currentPatient != null) {
                    currentPatient.logMedicationDose(medication.getId(), scheduledTime, true, "Taken on time (via Reminder)");
                    if (authService != null) {
                        authService.saveData();
                    }
                }
                JOptionPane.showMessageDialog(
                        parentComponent,
                        medication.getName() + " marked as taken!",
                        "Dose Logged",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else if (choice == 1) { // Snooze 10 Mins
                scheduler.schedule(() -> {
                    triggerReminder(parentComponent, medication, scheduledTime);
                }, 10, TimeUnit.MINUTES);

                JOptionPane.showMessageDialog(
                        parentComponent,
                        "Reminder for " + medication.getName() + " snoozed for 10 minutes.",
                        "Reminder Snoozed",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    private void playReminderSound() {
        new Thread(() -> {
            boolean playedFile = false;
            try {
                File soundFile = new File("reminder.wav");
                if (!soundFile.exists()) soundFile = new File("alarm.wav");
                if (!soundFile.exists()) soundFile = new File("sound.wav");

                if (soundFile.exists()) {
                    AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInput);
                    clip.start();
                    playedFile = true;
                    Thread.sleep(Math.max(1000, clip.getMicrosecondLength() / 1000));
                }
            } catch (Exception ignored) {}

            if (!playedFile) {
                playSyntheticChime();
            }
        }).start();
    }

    private void playSyntheticChime() {
        try {
            byte[] buf = new byte[1];
            AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af, 1600);
            sdl.start();

            // Tone 1: 800 Hz (150ms)
            for (int i = 0; i < 1200; i++) {
                double angle = i / (8000f / 800) * 2.0 * Math.PI;
                buf[0] = (byte) (Math.sin(angle) * 100);
                sdl.write(buf, 0, 1);
            }
            Thread.sleep(40);
            // Tone 2: 1050 Hz (250ms)
            for (int i = 0; i < 2000; i++) {
                double angle = i / (8000f / 1050) * 2.0 * Math.PI;
                buf[0] = (byte) (Math.sin(angle) * 100);
                sdl.write(buf, 0, 1);
            }

            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public void addMedication(Medication med) {
        if (!allMedications.contains(med)) {
            allMedications.add(med);
        }
    }

    public void removeMedication(Medication med) {
        allMedications.remove(med);
    }

    public void stopScheduler() {
        scheduler.shutdownNow();
    }
}
