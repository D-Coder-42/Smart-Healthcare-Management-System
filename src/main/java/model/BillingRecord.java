import java.time.LocalDate;
import javafx.beans.property.*;

public class BillingRecord {
    private final StringProperty patientId;
    private final StringProperty patient;
    private final StringProperty service;
    private final DoubleProperty amount;
    private final ObjectProperty<LocalDate> date;

    public BillingRecord(String patientId, String patient, String service, double amount, LocalDate date) {
        this.patientId = new SimpleStringProperty(patientId);
        this.patient = new SimpleStringProperty(patient);
        this.service = new SimpleStringProperty(service);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleObjectProperty<>(date);
    }

    public String getPatientId() {
        return patientId.get();
    }

    public String getPatient() {
        return patient.get();
    }

    public double getAmount() {
        return amount.get();
    }

    public LocalDate getDate() {
        return date.get();
    }

    public StringProperty patientProperty() {
        return patient;
    }

    public StringProperty serviceProperty() {
        return service;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
}
