package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Prescription extends Medication {
    private static final long serialVersionUID = 1L;
    public static final int priority = 1;

    public Prescription(int id, String name, String dosage, String unit, int frequencyPerDay,
                        LocalDate startDate, LocalDate endDate, String instructions, boolean isActive, LocalTime... intake) {
        super(id, name, "Prescribed Medicine", dosage, unit, frequencyPerDay, startDate, endDate, instructions, isActive, intake);
    }

    public Prescription(int id, String name, String type, int frequencyPerDay, LocalDate startDate, LocalDate endDate,
                        boolean isActive, LocalTime... intake) {
        super(id, name, "Prescribed Medicine", frequencyPerDay, startDate, endDate, isActive, intake);
    }

    public Prescription() {
        super();
        setType("Prescribed Medicine");
    }

    @Override
    public String getDosageWarning() {
        if ("Supplement".equalsIgnoreCase(getType())) {
            return "🍶 Take your supplements daily with meals for optimal absorption and wellness.";
        }
        return "💊 Make sure to take your medicine on time! It's crucial for your recovery.";
    }
}
