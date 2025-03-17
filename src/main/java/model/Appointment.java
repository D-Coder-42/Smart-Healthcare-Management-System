import java.time.LocalDate;
import java.time.LocalTime;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Appointment {
    private final SimpleStringProperty patientId;
    private final SimpleStringProperty patientName;
    private final SimpleStringProperty doctorName;
    private final SimpleObjectProperty<LocalDate> date;
    private final SimpleObjectProperty<LocalTime> time;

    public Appointment(String patientId, String patientName, String doctorName, LocalDate date, LocalTime time) {
        this.patientId = new SimpleStringProperty(patientId);
        this.patientName = new SimpleStringProperty(patientName);
        this.doctorName = new SimpleStringProperty(doctorName);
        this.date = new SimpleObjectProperty<>(date);
        this.time = new SimpleObjectProperty<>(time);
    }

    public StringProperty patientIdProperty() {
        return patientId;
    }

    public SimpleStringProperty patientProperty() {
        return patientName;
    }

    public SimpleStringProperty doctorProperty() {
        return doctorName;
    }

    public SimpleObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public SimpleObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public String getPatientName() {
        return patientName.get();
    }

    public String getDoctorName() {
        return doctorName.get();
    }

    public LocalDate getDate() {
        return date.get();
    }

    public LocalTime getTime() {
        return time.get();
    }
}
