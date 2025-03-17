import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Doctor {
    private final StringProperty name;
    private final StringProperty specialization;
    private final StringProperty contactInfo;

    public Doctor(String name, String specialization, String contactInfo) {
        this.name = new SimpleStringProperty(name);
        this.specialization = new SimpleStringProperty(specialization);
        this.contactInfo = new SimpleStringProperty(contactInfo);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty specializationProperty() {
        return specialization;
    }

    public StringProperty contactInfoProperty() {
        return contactInfo;
    }

    @Override
    public String toString() {
        return getName();
    }
}
