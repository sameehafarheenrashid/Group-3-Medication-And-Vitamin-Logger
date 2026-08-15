package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Supplement extends Medication {
    private static final long serialVersionUID = 1L;
    public static final int priority = 2;

    public Supplement(int id, String name, String dosage, String unit, int frequencyPerDay,
                      LocalDate startDate, LocalDate endDate, String instructions, boolean isActive, LocalTime... intake) {
        super(id, name, "Supplement", dosage, unit, frequencyPerDay, startDate, endDate, instructions, isActive, intake);
    }

    public Supplement(int id, String name, String type, int frequencyPerDay, LocalDate startDate, LocalDate endDate,
                      boolean isActive, LocalTime... intake) {
        super(id, name, "Supplement", frequencyPerDay, startDate, endDate, isActive, intake);
    }

    public Supplement() {
        super();
        setType("Supplement");
    }

    @Override
    public String getDosageWarning() {
        if ("Prescribed Medicine".equalsIgnoreCase(getType()) || "Medicine".equalsIgnoreCase(getType()) || "Prescription".equalsIgnoreCase(getType())) {
            return "💊 Make sure to take your medicine on time! It's crucial for your recovery.";
        }
        return "🍶 Take your supplements daily with meals for optimal absorption and wellness.";
    }
}
