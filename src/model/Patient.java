package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private int age;
    private LocalDate birthdate;
    private List<Medication> meds;

    public Patient(String username, String password, String firstName, String lastName, int age, LocalDate birthdate, Medication... meds) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.birthdate = birthdate;
        this.meds = new ArrayList<>(Arrays.asList(meds));
    }

    public Patient(String username, String password) {
        this(username, password, username, "", 0, null);
    }

    public Patient() {
        this.meds = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public List<Medication> getMeds() {
        if (meds == null) meds = new ArrayList<>();
        return meds;
    }

    public void setMeds(List<Medication> meds) {
        this.meds = new ArrayList<>(meds);
    }

    public void addMedication(Medication medication) {
        if (medication != null) {
            if (this.meds == null) this.meds = new ArrayList<>();
            this.meds.add(medication);
        }
    }

    public boolean hasMedicationNamed(String name) {
        if (meds == null || name == null) return false;
        String trimmed = name.trim();
        for (Medication m : meds) {
            if (m.getName() != null && m.getName().equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMedicationNamedExcept(String name, int excludeId) {
        if (meds == null || name == null) return false;
        String trimmed = name.trim();
        for (Medication m : meds) {
            if (m.getId() != excludeId && m.getName() != null && m.getName().equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeMedication(int medicationId) {
        if (this.meds == null) return false;
        return this.meds.removeIf(med -> med.getId() == medicationId);
    }

    public List<Medication> getMedications() {
        if (this.meds == null) return new ArrayList<>();
        return new ArrayList<>(this.meds);
    }

    public List<Medication> getActiveMedications() {
        LocalDate today = LocalDate.now();
        List<Medication> activeMeds = new ArrayList<>();

        if (meds == null) return activeMeds;

        for (Medication med : meds) {
            if (med.isActive() && (med.getStartDate() == null || !today.isBefore(med.getStartDate())) && 
               (med.getEndDate() == null || !today.isAfter(med.getEndDate()))) {
                activeMeds.add(med);
            }
        }
        return activeMeds;
    }

    public void logMedicationDose(Medication medication, LocalTime doseTime, boolean isTaken, String notes) {
        if (medication != null) {
            medication.logDose(doseTime, isTaken, notes);
        }
    }

    public void logMedicationDose(int medicationId, LocalTime doseTime, boolean isTaken, String notes) {
        if (meds == null) return;
        for (Medication med : meds) {
            if (med.getId() == medicationId) {
                med.logDose(doseTime, isTaken, notes);
                break;
            }
        }
    }

    public void logMedicationDose(int medicationId, LocalTime doseTime, boolean isTaken) {
        logMedicationDose(medicationId, doseTime, isTaken, null);
    }

    public void logMissedDose(Medication medication, LocalTime doseTime, String notes) {
        if (medication != null) {
            medication.logMissedDose(doseTime, notes);
        }
    }

    public void logMissedDose(int medicationId, LocalTime doseTime, String notes) {
        if (meds == null) return;
        for (Medication med : meds) {
            if (med.getId() == medicationId) {
                med.logMissedDose(doseTime, notes);
                break;
            }
        }
    }

    public List<MedicationLog> getAllLogs() {
        List<MedicationLog> allLogs = new ArrayList<>();
        if (meds != null) {
            for (Medication med : meds) {
                allLogs.addAll(med.getLogs());
            }
        }
        return allLogs;
    }
}
