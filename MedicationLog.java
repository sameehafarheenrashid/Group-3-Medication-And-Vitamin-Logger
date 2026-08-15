package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MedicationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int medicationId;
    private final String medicationName;
    private final LocalTime scheduledTime; // The dose time (e.g., 08:00)
    private final LocalDateTime takenTimestamp; // The exact time clicked
    private final boolean wasTaken;
    private final String notes;

    public MedicationLog(int medicationId, String medicationName, LocalTime scheduledTime, boolean wasTaken) {
        this(medicationId, medicationName, scheduledTime, wasTaken, wasTaken ? "Taken on time" : "Missed dose");
    }

    public MedicationLog(int medicationId, String medicationName, LocalTime scheduledTime, boolean wasTaken, String notes) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.scheduledTime = scheduledTime;
        this.takenTimestamp = LocalDateTime.now();
        this.wasTaken = wasTaken;
        this.notes = notes != null ? notes : "";
    }

    // Getters
    public int getMedicationId() { return medicationId; }
    public String getMedicationName() { return medicationName; }
    public LocalTime getScheduledTime() { return scheduledTime; }
    public LocalDateTime getTakenTimestamp() { return takenTimestamp; }
    public boolean isWasTaken() { return wasTaken; }
    public String getNotes() { return notes; }

    public String getStatus() {
        if (!wasTaken) {
            return "Missed";
        }
        if (notes != null && notes.toLowerCase().contains("late")) {
            return "Late";
        }
        return "Taken";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Scheduled for %s) - Taken at %s", 
                takenTimestamp.toLocalDate(), medicationName, scheduledTime, takenTimestamp.toLocalTime());
    }
}
