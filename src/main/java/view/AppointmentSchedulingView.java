import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentSchedulingView extends VBox {
    private final ComboBox<Patient> patientComboBox;
    private final ComboBox<Doctor> doctorComboBox;
    private final DatePicker appointmentDatePicker;
    private final ComboBox<LocalTime> appointmentTimeComboBox;
    private final TableView<Appointment> appointmentTable;
    private final ObservableList<Appointment> appointments;
    private final ObservableList<Patient> patients;
    private final ObservableList<Doctor> doctors;

    public AppointmentSchedulingView(ObservableList<Patient> patients, 
                                   ObservableList<Doctor> doctors, 
                                   ObservableList<Appointment> appointments) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
        
        patientComboBox = new ComboBox<>(patients);
        doctorComboBox = new ComboBox<>(doctors);
        appointmentDatePicker = new DatePicker();
        appointmentTimeComboBox = new ComboBox<>(createTimeSlots());
        
        appointmentTable = new TableView<>();
        setupAppointmentTable();
        
        GridPane inputGrid = createInputGrid();
        Button scheduleButton = new Button("Schedule Appointment");
        scheduleButton.setOnAction(e -> scheduleAppointment());
        
        getChildren().addAll(inputGrid, scheduleButton, appointmentTable);
        setSpacing(10);
        setPadding(new Insets(10));

        patients.addListener((ListChangeListener<Patient>) c -> patientComboBox.setItems(FXCollections.observableArrayList(patients)));
        doctors.addListener((ListChangeListener<Doctor>) c -> doctorComboBox.setItems(FXCollections.observableArrayList(doctors)));
        
        appointmentDatePicker.setValue(LocalDate.now());
        
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisabled(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });
    }

    private ObservableList<LocalTime> createTimeSlots() {
        ObservableList<LocalTime> timeSlots = FXCollections.observableArrayList();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        while (!startTime.isAfter(endTime)) {
            timeSlots.add(startTime);
            startTime = startTime.plusMinutes(30);
        }
        return timeSlots;
    }

    private void setupAppointmentTable() {
        TableColumn<Appointment, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(cellData -> cellData.getValue().patientProperty());
        
        TableColumn<Appointment, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(cellData -> cellData.getValue().doctorProperty());
        
        TableColumn<Appointment, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        
        TableColumn<Appointment, LocalTime> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Cancel");
            {
                deleteButton.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    handleAppointmentCancellation(appointment);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        appointmentTable.getColumns().addAll(patientCol, doctorCol, dateCol, timeCol, actionCol);
        appointmentTable.setItems(appointments);
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Patient:"), patientComboBox);
        grid.addRow(1, new Label("Doctor:"), doctorComboBox);
        grid.addRow(2, new Label("Date:"), appointmentDatePicker);
        grid.addRow(3, new Label("Time:"), appointmentTimeComboBox);

        return grid;
    }

    private void scheduleAppointment() {
        Patient patient = patientComboBox.getValue();
        Doctor doctor = doctorComboBox.getValue();
        LocalDate date = appointmentDatePicker.getValue();
        LocalTime time = appointmentTimeComboBox.getValue();

        if (patient == null || doctor == null || date == null || time == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot schedule appointments in the past.");
            return;
        }

        if (isTimeSlotTaken(doctor, date, time)) {
            showAlert(Alert.AlertType.ERROR, "Error", "This time slot is already taken for the selected doctor.");
            return;
        }

        Appointment newAppointment = new Appointment(patient.getPatientId(), patient.getName(), doctor.getName(), date, time);
        appointments.add(newAppointment);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment scheduled successfully.");
        clearInputFields();
    }

    private boolean isTimeSlotTaken(Doctor doctor, LocalDate date, LocalTime time) {
        return appointments.stream()
            .anyMatch(apt -> 
                apt.doctorProperty().get().equals(doctor.getName()) &&
                apt.dateProperty().get().equals(date) &&
                apt.timeProperty().get().equals(time)
            );
    }

    private void handleAppointmentCancellation(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Appointment");
        alert.setHeaderText("Cancel appointment for " + appointment.patientProperty().get());
        alert.setContentText("Are you sure you want to cancel this appointment?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                appointments.remove(appointment);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment cancelled successfully.");
            }
        });
    }

    private void clearInputFields() {
        patientComboBox.setValue(null);
        doctorComboBox.setValue(null);
        appointmentDatePicker.setValue(LocalDate.now());
        appointmentTimeComboBox.setValue(null);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
