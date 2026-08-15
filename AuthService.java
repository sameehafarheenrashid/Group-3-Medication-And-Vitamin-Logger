package service;

import model.Patient;
import java.io.File;

import java.time.LocalDate;
import java.util.List;

public class AuthService {

    private final List<Patient> patients;
    private Patient currentPatient;

    public AuthService() {
        this.patients = DataPersistenceService.loadPatients();
        File dataFile = new File("data/patients.dat");
        if (this.patients.isEmpty() && !dataFile.exists()) {
            createDefaultPatients();
        }
        String lastUser = DataPersistenceService.loadSession();
        if (lastUser != null && !lastUser.isBlank()) {
            for (Patient p : patients) {
                if (p.getUsername().equals(lastUser)) {
                    this.currentPatient = p;
                    break;
                }
            }
        }
    }

    private void createDefaultPatients() {
        // User 1: emon / emon
        Patient emon = new Patient("emon", "emon", "Emon", "User", 24, LocalDate.of(2002, 1, 1));

        // User 2: sameeha / sameeha
        Patient sameeha = new Patient("sameeha", "sameeha", "Sameeha", "User", 22, LocalDate.of(2004, 5, 15));

        // User 3: niladry / niladry
        Patient niladry = new Patient("niladry", "niladry", "Niladry", "User", 25, LocalDate.of(2001, 10, 10));

        patients.add(emon);
        patients.add(sameeha);
        patients.add(niladry);

        saveData();
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        for (Patient p : patients) {
            if (p.getUsername().equals(username) && p.getPassword().equals(password)) {
                currentPatient = p;
                DataPersistenceService.saveSession(p.getUsername());
                return true;
            }
        }
        return false;
    }

    public boolean signUp(String username, String password) {
        for (Patient p : patients) {
            if (p.getUsername().equalsIgnoreCase(username)) {
                return false; // Username taken
            }
        }
        Patient newPatient = new Patient(username, password);
        patients.add(newPatient);
        currentPatient = newPatient;
        saveData();
        DataPersistenceService.saveSession(newPatient.getUsername());
        return true;
    }

    public boolean updateUsername(String newUsername) {
        if (currentPatient == null || newUsername == null || newUsername.isBlank()) return false;
        if (currentPatient.getUsername().equals(newUsername)) return true;

        for (Patient p : patients) {
            if (!p.equals(currentPatient) && p.getUsername().equalsIgnoreCase(newUsername)) {
                return false; // Username taken by another patient
            }
        }

        currentPatient.setUsername(newUsername);
        saveData();
        DataPersistenceService.saveSession(newUsername);
        return true;
    }

    public boolean verifyCurrentPassword(String currentPassword) {
        if (currentPatient == null) return false;
        return currentPatient.getPassword().equals(currentPassword);
    }

    public boolean updatePassword(String currentPassword, String newPassword) {
        if (!verifyCurrentPassword(currentPassword)) {
            return false;
        }
        currentPatient.setPassword(newPassword);
        saveData();
        return true;
    }

    public boolean updateAccount(String newUsername, String newPassword) {
        if (currentPatient == null) return false;

        for (Patient p : patients) {
            if (!p.equals(currentPatient) && p.getUsername().equalsIgnoreCase(newUsername)) {
                return false;
            }
        }

        currentPatient.setUsername(newUsername);
        currentPatient.setPassword(newPassword);
        saveData();
        DataPersistenceService.saveSession(newUsername);
        return true;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public String getCurrentUsername() {
        return currentPatient != null ? currentPatient.getUsername() : "";
    }

    public void logout() {
        currentPatient = null;
        DataPersistenceService.clearSession();
    }

    public void saveData() {
        DataPersistenceService.savePatients(patients);
    }
}
