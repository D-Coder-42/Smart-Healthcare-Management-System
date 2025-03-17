import java.time.LocalDate;
import javafx.beans.property.*;

public class Patient {
    private final StringProperty name;
    private final ObjectProperty<LocalDate> dateOfBirth;
    private final StringProperty contactInfo;
    private final StringProperty medicalHistory;
    private final StringProperty patientId;

    public Patient(String name, LocalDate dateOfBirth, String contactInfo, String medicalHistory) {
        this.name = new SimpleStringProperty(name);
        this.dateOfBirth = new SimpleObjectProperty<>(dateOfBirth);
        this.contactInfo = new SimpleStringProperty(contactInfo);
        this.medicalHistory = new SimpleStringProperty(medicalHistory);
        this.patientId = new SimpleStringProperty(generatePatientId());
    }

    public Patient(String patientId, String name, LocalDate dateOfBirth, String contactInfo, String medicalHistory) {
        this.patientId = new SimpleStringProperty(patientId);
        this.name = new SimpleStringProperty(name);
        this.dateOfBirth = new SimpleObjectProperty<>(dateOfBirth);
        this.contactInfo = new SimpleStringProperty(contactInfo);
        this.medicalHistory = new SimpleStringProperty(medicalHistory);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirth;
    }

    public StringProperty contactInfoProperty() {
        return contactInfo;
    }

    public StringProperty medicalHistoryProperty() {
        return medicalHistory;
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public String getPatientId() {
        return patientId.get();
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth.get();
    }

    public String getContactInfo() {
        return contactInfo.get();
    }

    public String getMedicalHistory() {
        return medicalHistory.get();
    }

    private String generatePatientId() {
        // Simple ID generation
        return "P" + System.currentTimeMillis() % 10000;
    }

    @Override
    public String toString() {
        return getName();
    }
}
