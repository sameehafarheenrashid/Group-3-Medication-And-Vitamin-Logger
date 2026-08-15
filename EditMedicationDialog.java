package ui;

import java.awt.Window;

public class EditMedicationDialog extends AddMedicationDialog {

    private static final long serialVersionUID = 1L;

    public EditMedicationDialog(
            Window owner,
            String name,
            String type,
            String dosage,
            String unit,
            int frequencyPerDay,
            String scheduledTimes,
            String startDate,
            String endDate,
            String instructions,
            boolean active
    ) {
        super(owner);

        setTitle("Edit Medication");

        setExistingMedication(
                name,
                type,
                dosage,
                unit,
                frequencyPerDay,
                scheduledTimes,
                startDate,
                endDate,
                instructions,
                active
        );
    }
}
