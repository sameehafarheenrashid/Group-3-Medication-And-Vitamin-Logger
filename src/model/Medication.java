package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class Medication implements Serializable, Loggable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String type;
    private String dosage;
    private String unit;
    private int frequencyPerDay;
    private ArrayList<LocalTime> intake;
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;
    private boolean isActive;
    private final List<MedicationLog> logs = new ArrayList<>();

    public Medication(int id, String name, String type, String dosage, String unit, int frequencyPerDay,
                      LocalDate startDate, LocalDate endDate, String instructions, boolean isActive, LocalTime... intake) {
        this.id = id;
        this.name = name;
        this.type = type;
        setDosage(dosage);
        this.unit = unit;
        setFrequencyPerDay(frequencyPerDay);
        this.intake = intake != null ? new ArrayList<>(Arrays.asList(intake)) : new ArrayList<>();
        this.startDate = startDate;
        this.endDate = endDate;
        this.instructions = instructions;
        this.isActive = isActive;
    }

    public Medication(int id, String name, String type, int frequencyPerDay,
                      LocalDate startDate, LocalDate endDate, boolean isActive, LocalTime... intake) {
        this(id, name, type, "1", "tablet", frequencyPerDay, startDate, endDate, "", isActive, intake);
    }

    public Medication() {
        this.intake = new ArrayList<>();
        this.dosage = "1";
        this.unit = "tablet";
        this.instructions = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        if (dosage != null) {
            String trimmed = dosage.trim();
            if (trimmed.startsWith("-")) {
                throw new IllegalArgumentException("Dosage cannot be negative");
            }
            try {
                double val = Double.parseDouble(trimmed);
                if (val <= 0) {
                    throw new IllegalArgumentException("Dosage must be positive");
                }
            } catch (NumberFormatException ignored) {}
        }
        this.dosage = dosage;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDosageWithUnit() {
        if (dosage == null || dosage.isBlank()) return unit != null ? unit : "";
        if (unit == null || unit.isBlank()) return dosage;
        return dosage + " " + unit;
    }

    public int getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public void setFrequencyPerDay(int frequencyPerDay) {
        if (frequencyPerDay <= 0) {
            throw new IllegalArgumentException("Frequency per day must be at least 1");
        }
        this.frequencyPerDay = frequencyPerDay;
    }

    public ArrayList<LocalTime> getIntake() {
        return new ArrayList<>(this.intake);
    }

    public void setIntake(List<LocalTime> intake) {
        if (intake == null) {
            throw new IllegalArgumentException("Intake schedule cannot be null");
        }
        this.intake = new ArrayList<>(intake);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void checkAndTriggerReminder(LocalTime currentTime, LocalDate currentDate, ReminderCallback callback) {
        if (!isActive || (startDate != null && currentDate.isBefore(startDate)) || (endDate != null && currentDate.isAfter(endDate))) {
            return;
        }

        LocalTime nowMinute = currentTime.truncatedTo(ChronoUnit.MINUTES);

        for (LocalTime scheduledTime : intake) {
            LocalTime scheduledMinute = scheduledTime.truncatedTo(ChronoUnit.MINUTES);
            if (scheduledMinute.equals(nowMinute)) {
                if (callback != null) {
                    callback.onReminder(this, scheduledTime);
                } else {
                    sendReminderNotification(scheduledTime);
                }
            }
        }
    }

    public void checkAndTriggerReminder(LocalTime currentTime, LocalDate currentDate) {
        checkAndTriggerReminder(currentTime, currentDate, null);
    }

    private void sendReminderNotification(LocalTime scheduledTime) {
        System.out.println("🔔 REMINDER: It is " + scheduledTime + "! Time to take your " + name + ".");
    }

    public boolean checkIfActive() {
        LocalDate today = LocalDate.now();
        if (endDate != null && today.isAfter(this.endDate))
            return false;
        else
            return isActive;
    }

    abstract public String getDosageWarning();

    @Override
    public void recordDose(Date time) {
        LocalTime scheduledTime = (time != null) 
                ? LocalTime.ofInstant(time.toInstant(), ZoneId.systemDefault()) 
                : LocalTime.now();
        logDose(scheduledTime, true);
    }

    public void logDose(LocalTime scheduledTime, boolean isChecked, String customNotes) {
        LocalTime cleanTime = scheduledTime != null ? scheduledTime.truncatedTo(ChronoUnit.MINUTES) : LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        if (isChecked) {
            removeLogForTime(cleanTime);
            String notes = customNotes;
            if (notes == null || notes.isBlank()) {
                LocalTime now = LocalTime.now();
                if (now.isAfter(cleanTime.plusMinutes(15))) {
                    notes = "Taken late - Please take on time!";
                } else {
                    notes = "Taken on time";
                }
            }
            MedicationLog newLog = new MedicationLog(this.id, this.name, cleanTime, true, notes);
            logs.add(newLog);
            System.out.println("Logged: " + newLog);
        } else {
            removeLogForTime(cleanTime);
        }
    }

    public void logDose(LocalTime scheduledTime, boolean isChecked) {
        logDose(scheduledTime, isChecked, null);
    }

    public void logMissedDose(LocalTime scheduledTime, String customNotes) {
        LocalTime cleanTime = scheduledTime != null ? scheduledTime.truncatedTo(ChronoUnit.MINUTES) : LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        removeLogForTime(cleanTime);
        String notes = (customNotes != null && !customNotes.isBlank()) ? customNotes : "Missed dose (Exceeded 6 hours)";
        MedicationLog newLog = new MedicationLog(this.id, this.name, cleanTime, false, notes);
        logs.add(newLog);
        System.out.println("Logged missed: " + newLog);
    }

    public void logMissedDose(LocalTime scheduledTime) {
        logMissedDose(scheduledTime, null);
    }

    private void removeLogForTime(LocalTime scheduledTime) {
        if (scheduledTime == null) return;
        LocalTime targetMinute = scheduledTime.truncatedTo(ChronoUnit.MINUTES);
        logs.removeIf(log -> log.getScheduledTime() != null 
                && log.getScheduledTime().truncatedTo(ChronoUnit.MINUTES).equals(targetMinute) 
                && log.getTakenTimestamp() != null 
                && log.getTakenTimestamp().toLocalDate().equals(LocalDate.now()));
    }

    public List<MedicationLog> getLogs() {
        return new ArrayList<>(logs);
    }

    public void addLog(MedicationLog log) {
        if (log != null) {
            logs.add(log);
        }
    }

    public interface ReminderCallback {
        void onReminder(Medication medication, LocalTime scheduledTime);
    }
}
