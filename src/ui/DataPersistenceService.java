package service;

import model.Patient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataPersistenceService {

    private static final String DATA_DIR = "data";
    private static final String FILE_PATH = "data/patients.dat";
    private static final String SESSION_FILE_PATH = "data/session.dat";

    public static void savePatients(List<Patient> patients) {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
                oos.writeObject(patients);
            }
        } catch (IOException e) {
            System.err.println("Failed to save patient data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Patient> loadPatients() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<Patient>) obj;
            }
        } catch (Exception e) {
            System.err.println("Failed to load patient data: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public static void saveSession(String username) {
        if (username == null || username.isBlank()) {
            clearSession();
            return;
        }
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE_PATH))) {
                writer.write(username.trim());
            }
        } catch (IOException e) {
            System.err.println("Failed to save session data: " + e.getMessage());
        }
    }

    public static String loadSession() {
        File file = new File(SESSION_FILE_PATH);
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null && !line.isBlank()) {
                return line.trim();
            }
        } catch (Exception e) {
            System.err.println("Failed to load session data: " + e.getMessage());
        }
        return null;
    }

    public static void clearSession() {
        File file = new File(SESSION_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
}
