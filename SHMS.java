import java.util.*;
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.collections.*;
import javafx.geometry.*;
import java.time.*;
import java.time.format.*;
import javafx.beans.property.*;

public class SHMS extends Application {
    // Shared data stores
    private static final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private static final ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private static final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private static final ObservableList<BillingRecord> billingRecords = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        
        // Initialize views with shared data stores
        Tab patientTab = new Tab("Patient Management");
        patientTab.setContent(new PatientManagementView(patients));
        patientTab.setClosable(false);

        Tab appointmentTab = new Tab("Appointment Scheduling");
        appointmentTab.setContent(new AppointmentSchedulingView(patients, doctors, appointments));
        appointmentTab.setClosable(false);

        Tab doctorTab = new Tab("Doctor Management");
        doctorTab.setContent(new DoctorManagementView(doctors));
        doctorTab.setClosable(false);

        Tab billingTab = new Tab("Billing");
        billingTab.setContent(new BillingView(patients, billingRecords));
        billingTab.setClosable(false);

        Tab analyticsTab = new Tab("Analytics");
        analyticsTab.setContent(new AnalyticsView(patients, doctors, appointments, billingRecords));
        analyticsTab.setClosable(false);

        tabPane.getTabs().addAll(patientTab, appointmentTab, doctorTab, billingTab, analyticsTab);

        Scene scene = new Scene(tabPane, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Smart Healthcare Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Patient {
    private final javafx.beans.property.StringProperty name;
    private final javafx.beans.property.ObjectProperty<LocalDate> dateOfBirth;
    private final javafx.beans.property.StringProperty contactInfo;
    private final javafx.beans.property.StringProperty medicalHistory;
    private final javafx.beans.property.StringProperty patientId; // Changed to StringProperty

    public Patient(String name, LocalDate dateOfBirth, String contactInfo, String medicalHistory) {
        this.name = new javafx.beans.property.SimpleStringProperty(name);
        this.dateOfBirth = new javafx.beans.property.SimpleObjectProperty<>(dateOfBirth);
        this.contactInfo = new javafx.beans.property.SimpleStringProperty(contactInfo);
        this.medicalHistory = new javafx.beans.property.SimpleStringProperty(medicalHistory);
        this.patientId = new javafx.beans.property.SimpleStringProperty(generatePatientId());
    }

    public Patient(String patientId, String name, LocalDate dateOfBirth, String contactInfo, String medicalHistory) {
        this.patientId = new javafx.beans.property.SimpleStringProperty(patientId);
        this.name = new javafx.beans.property.SimpleStringProperty(name);
        this.dateOfBirth = new javafx.beans.property.SimpleObjectProperty<>(dateOfBirth);
        this.contactInfo = new javafx.beans.property.SimpleStringProperty(contactInfo);
        this.medicalHistory = new javafx.beans.property.SimpleStringProperty(medicalHistory);
    }

    public String getName() {
        return name.get();
    }

    public javafx.beans.property.StringProperty nameProperty() {
        return name;
    }

    public javafx.beans.property.ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirth;
    }

    public javafx.beans.property.StringProperty contactInfoProperty() {
        return contactInfo;
    }

    public javafx.beans.property.StringProperty medicalHistoryProperty() {
        return medicalHistory;
    }

    public javafx.beans.property.StringProperty patientIdProperty() {
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
        return getName(); // For ComboBox display
    }
}

class PatientManagementView extends VBox {
    private final TextField nameField;
    private final DatePicker dateOfBirthPicker;
    private final TextField contactInfoField;
    private final TextArea medicalHistoryArea;
    private final TextField patientIdField;
    private final TableView<Patient> patientTable;
    private final ObservableList<Patient> patients;
    private final String SYSTEM_PASSWORD = "javaFX_24";
    
    private final TextField searchField;
    private final ComboBox<String> searchCriteriaBox;
    private Patient currentEditingPatient;
    private Button addUpdateButton;
    
    public PatientManagementView(ObservableList<Patient> patients) {
        this.patients = patients;
        
        // Initialize search components
        searchField = new TextField();
        searchField.setPromptText("Enter search term...");
        searchCriteriaBox = new ComboBox<>();
        searchCriteriaBox.getItems().addAll("ID", "Name", "Contact Info");
        searchCriteriaBox.setValue("Name");
        
        // Initialize input components
        patientIdField = new TextField();
        patientIdField.setPromptText("Leave empty for auto-generated ID");
        nameField = new TextField();
        dateOfBirthPicker = new DatePicker();
        contactInfoField = new TextField();
        medicalHistoryArea = new TextArea();
        medicalHistoryArea.setPrefRowCount(3);
        
        patientTable = new TableView<>();
        setupPatientTable();
        
        addUpdateButton = new Button("Add Patient");
        addUpdateButton.setOnAction(e -> handleAdd());
        
        // Layout
        HBox searchBox = createSearchBox();
        GridPane inputGrid = createInputGrid();
        HBox buttonBox = new HBox(10, addUpdateButton);
        
        getChildren().addAll(searchBox, inputGrid, buttonBox, patientTable);
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Add search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch());
    }
    
    private void viewPatientInformation(Patient patient) {
        Stage infoStage = new Stage();
        infoStage.initModality(Modality.APPLICATION_MODAL);
        infoStage.setTitle("Patient Information");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        TextField nameField = new TextField(patient.getName());
        DatePicker dobPicker = new DatePicker(patient.getDateOfBirth());
        TextField contactField = new TextField(patient.getContactInfo());
        TextArea historyArea = new TextArea(patient.getMedicalHistory());
        historyArea.setPrefRowCount(5);
        historyArea.setWrapText(true);
        
        // Initially set fields as non-editable
        nameField.setEditable(false);
        dobPicker.setEditable(false);
        contactField.setEditable(false);
        historyArea.setEditable(false);
        
        Button editButton = new Button("Edit");
        Button saveButton = new Button("Save Changes");
        Button closeButton = new Button("Close");
        
        // Create button box first so we can reference it
        HBox buttonBox = new HBox(10, editButton, saveButton, closeButton);
        
        // Set up edit button
        editButton.setOnAction(e -> showPasswordDialogForEdit(nameField, dobPicker, 
                                                            contactField, historyArea, 
                                                            saveButton));
        
        // Set up save button
        saveButton.setDisable(true);
        saveButton.setOnAction(e -> {
            updatePatient(patient, nameField.getText(), dobPicker.getValue(),
                        contactField.getText(), historyArea.getText());
            infoStage.close();
        });
        
        // Set up close button
        closeButton.setOnAction(e -> infoStage.close());
        
        content.getChildren().addAll(
            new Label("Patient ID: " + patient.getPatientId()),
            new Label("Name:"), nameField,
            new Label("Date of Birth:"), dobPicker,
            new Label("Contact:"), contactField,
            new Label("Medical History:"), historyArea,
            buttonBox
        );
        
        Scene scene = new Scene(content);
        infoStage.setScene(scene);
        infoStage.showAndWait();
    }
    
    private void setupPatientTable() {
        // ID column
        TableColumn<Patient, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().patientIdProperty());
        
        // Name column
        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        // Date of Birth column
        TableColumn<Patient, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(cellData -> cellData.getValue().dateOfBirthProperty());
        
        // Contact Info column
        TableColumn<Patient, String> contactCol = new TableColumn<>("Contact Info");
        contactCol.setCellValueFactory(cellData -> cellData.getValue().contactInfoProperty());
        
        // Actions column - Fixed to always show View button regardless of ID type
        TableColumn<Patient, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    showPasswordDialog(patient);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        
        patientTable.getColumns().addAll(idCol, nameCol, dobCol, contactCol, actionsCol);
        patientTable.setItems(patients);
    }
    
    private void handleAdd() {
        String name = nameField.getText().trim();
        LocalDate dob = dateOfBirthPicker.getValue();
        String contactInfo = contactInfoField.getText().trim();
        String medicalHistory = medicalHistoryArea.getText().trim();
        String customId = patientIdField.getText().trim();
        
        if (name.isEmpty() || dob == null || contactInfo.isEmpty()) {
            showAlert("Error", "Please fill in all required fields.", Alert.AlertType.ERROR);
            return;
        }
        
        // Check for duplicate patient
        Optional<Patient> existingPatient = findExistingPatient(name, dob);
        if (existingPatient.isPresent()) {
            showAlert("Duplicate Patient", 
                     "A patient with this name and date of birth already exists.",
                     Alert.AlertType.WARNING);
            return;
        }
        
        // Check if custom ID is already in use
        if (!customId.isEmpty() && patients.stream()
                .anyMatch(p -> p.getPatientId().equals(customId))) {
            showAlert("Error", "This Patient ID is already in use.", Alert.AlertType.ERROR);
            return;
        }
        
        // Add new patient with custom or generated ID
        Patient newPatient = customId.isEmpty() ? 
            new Patient(name, dob, contactInfo, medicalHistory) :
            new Patient(customId, name, dob, contactInfo, medicalHistory);
        
        patients.add(newPatient);
        showAlert("Success", "Patient added successfully.", Alert.AlertType.INFORMATION);
        clearInputFields();
    }
    
    private void showPasswordDialogForEdit(TextField nameField, DatePicker dobPicker,
                                         TextField contactField, TextArea historyArea,
                                         Button saveButton) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Authentication Required");
        dialog.setHeaderText("Please enter password to edit patient information");
        
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Password:"), passwordField);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (password.equals(SYSTEM_PASSWORD)) {
                enableEditing(nameField, dobPicker, contactField, historyArea, saveButton);
            } else {
                showAlert("Error", "Incorrect password!", Alert.AlertType.ERROR);
            }
        });
    }
    
    private void enableEditing(TextField nameField, DatePicker dobPicker,
                             TextField contactField, TextArea historyArea,
                             Button saveButton) {
        nameField.setEditable(true);
        dobPicker.setEditable(true);
        contactField.setEditable(true);
        historyArea.setEditable(true);
        saveButton.setDisable(false);
    }
    
    private void updatePatient(Patient patient, String name, LocalDate dob,
                             String contactInfo, String medicalHistory) {
        patient.nameProperty().set(name);
        patient.dateOfBirthProperty().set(dob);
        patient.contactInfoProperty().set(contactInfo);
        patient.medicalHistoryProperty().set(medicalHistory);
        
        showAlert("Success", "Patient information updated successfully.", 
                 Alert.AlertType.INFORMATION);
    }
    
    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Patient ID (Optional):"), patientIdField);
        grid.addRow(1, new Label("Name:"), nameField);
        grid.addRow(2, new Label("Date of Birth:"), dateOfBirthPicker);
        grid.addRow(3, new Label("Contact Info:"), contactInfoField);
        grid.addRow(4, new Label("Medical History:"), medicalHistoryArea);
        return grid;
    }
    
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(
            new Label("Search by:"),
            searchCriteriaBox,
            searchField
        );
        return searchBox;
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().toLowerCase();
        String criteria = searchCriteriaBox.getValue();
        
        if (searchTerm.isEmpty()) {
            patientTable.setItems(patients);
            return;
        }
        
        ObservableList<Patient> filteredList = FXCollections.observableArrayList();
        for (Patient patient : patients) {
            boolean matches = switch (criteria) {
                case "ID" -> patient.getPatientId().toLowerCase().contains(searchTerm);
                case "Name" -> patient.getName().toLowerCase().contains(searchTerm);
                case "Contact Info" -> patient.getContactInfo().toLowerCase().contains(searchTerm);
                default -> false;
            };
            
            if (matches) {
                filteredList.add(patient);
            }
        }
        
        patientTable.setItems(filteredList);
    }
    
    private void startEditing(Patient patient) {
        currentEditingPatient = patient;
        nameField.setText(patient.getName());
        dateOfBirthPicker.setValue(patient.getDateOfBirth());
        contactInfoField.setText(patient.getContactInfo());
        medicalHistoryArea.setText(patient.getMedicalHistory());
        
        addUpdateButton.setText("Update Patient");
        addUpdateButton.getScene().lookup("Button:contains('Cancel')").setVisible(true);
    }
    
    private void cancelEditing() {
        currentEditingPatient = null;
        clearInputFields();
        addUpdateButton.setText("Add Patient");
        addUpdateButton.getScene().lookup("Button:contains('Cancel')").setVisible(false);
    }
    
    private void handleAddUpdate() {
        String name = nameField.getText().trim();
        LocalDate dob = dateOfBirthPicker.getValue();
        String contactInfo = contactInfoField.getText().trim();
        String medicalHistory = medicalHistoryArea.getText().trim();
        
        if (name.isEmpty() || dob == null || contactInfo.isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }
        
        if (currentEditingPatient != null) {
            // Update existing patient
            currentEditingPatient.nameProperty().set(name);
            currentEditingPatient.dateOfBirthProperty().set(dob);
            currentEditingPatient.contactInfoProperty().set(contactInfo);
            currentEditingPatient.medicalHistoryProperty().set(medicalHistory);
            
            showAlert("Success", "Patient information updated successfully.");
            cancelEditing();
        } else {
            // Check for duplicate patient
            Optional<Patient> existingPatient = findExistingPatient(name, dob);
            if (existingPatient.isPresent()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Duplicate Patient");
                alert.setHeaderText("A patient with this name and date of birth already exists.");
                alert.setContentText("Would you like to update the existing patient record?");
                
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        startEditing(existingPatient.get());
                    }
                });
                return;
            }
            
            // Add new patient
            patients.add(new Patient(name, dob, contactInfo, medicalHistory));
            showAlert("Success", "Patient added successfully.");
            clearInputFields();
        }
    }
    
    private Optional<Patient> findExistingPatient(String name, LocalDate dob) {
        return patients.stream()
            .filter(p -> p.getName().equalsIgnoreCase(name) && p.getDateOfBirth().equals(dob))
            .findFirst();
    }
    
    private void showPasswordDialog(Patient patient) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Authentication Required");
        dialog.setHeaderText("Please enter password to view patient information");
        
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        
        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Password:"), passwordField);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (password.equals(SYSTEM_PASSWORD)) {
                viewPatientInformation(patient);
            } else {
                showAlert("Error", "Incorrect password!");
            }
        });
    }
    
    private void addPatient() {
        String name = nameField.getText().trim();
        LocalDate dob = dateOfBirthPicker.getValue();
        String contactInfo = contactInfoField.getText().trim();
        String medicalHistory = medicalHistoryArea.getText().trim();
        
        if (name.isEmpty() || dob == null || contactInfo.isEmpty()) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }
        
        patients.add(new Patient(name, dob, contactInfo, medicalHistory));
        clearInputFields();
    }
    
    private void clearInputFields() {
        nameField.clear();
        dateOfBirthPicker.setValue(null);
        contactInfoField.clear();
        medicalHistoryArea.clear();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

class AppointmentSchedulingView extends VBox {
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
        
        // Initialize components
        patientComboBox = new ComboBox<>(patients);
        doctorComboBox = new ComboBox<>(doctors);
        appointmentDatePicker = new DatePicker();
        appointmentTimeComboBox = new ComboBox<>(createTimeSlots());
        
        appointmentTable = new TableView<>();
        setupAppointmentTable();
        
        // Layout
        GridPane inputGrid = createInputGrid();
        Button scheduleButton = new Button("Schedule Appointment");
        scheduleButton.setOnAction(e -> scheduleAppointment());
        
        getChildren().addAll(inputGrid, scheduleButton, appointmentTable);
        setSpacing(10);
        setPadding(new Insets(10));

        // Add listeners for data changes
        patients.addListener((ListChangeListener<Patient>) c -> patientComboBox.setItems(FXCollections.observableArrayList(patients)));
        doctors.addListener((ListChangeListener<Doctor>) c -> doctorComboBox.setItems(FXCollections.observableArrayList(doctors)));
        
        // Set default date to today
        appointmentDatePicker.setValue(LocalDate.now());
        
        // Add date validation
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
        
        // Add delete button column
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

class DoctorManagementView extends VBox {
    private final TextField nameField;
    private final TextField specializationField;
    private final TextField contactInfoField;
    private final TableView<Doctor> doctorTable;
    private final ObservableList<Doctor> doctors;

    public DoctorManagementView(ObservableList<Doctor> doctors) {
        this.doctors = doctors;
        
        // Initialize components
        nameField = new TextField();
        specializationField = new TextField();
        contactInfoField = new TextField();
        
        doctorTable = new TableView<>();
        setupDoctorTable();
        
        // Layout
        GridPane inputGrid = createInputGrid();
        Button addUpdateButton = new Button("Add Doctor");
        addUpdateButton.setOnAction(e -> addDoctor());
        
        getChildren().addAll(inputGrid, addUpdateButton, doctorTable);
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void setupDoctorTable() {
        TableColumn<Doctor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        TableColumn<Doctor, String> specializationCol = new TableColumn<>("Specialization");
        specializationCol.setCellValueFactory(cellData -> cellData.getValue().specializationProperty());
        
        TableColumn<Doctor, String> contactCol = new TableColumn<>("Contact Info");
        contactCol.setCellValueFactory(cellData -> cellData.getValue().contactInfoProperty());
        
        TableColumn<Doctor, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Remove");
            {
                deleteButton.setOnAction(event -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    handleDoctorRemoval(doctor);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
        
        doctorTable.getColumns().addAll(nameCol, specializationCol, contactCol, actionCol);
        doctorTable.setItems(doctors);
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Specialization:"), specializationField);
        grid.addRow(2, new Label("Contact Info:"), contactInfoField);

        return grid;
    }

    private void addDoctor() {
        String name = nameField.getText().trim();
        String specialization = specializationField.getText().trim();
        String contactInfo = contactInfoField.getText().trim();

        if (name.isEmpty() || specialization.isEmpty() || contactInfo.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }

        Doctor newDoctor = new Doctor(name, specialization, contactInfo);
        doctors.add(newDoctor);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor added successfully.");
        clearInputFields();
    }

    private void handleDoctorRemoval(Doctor doctor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Doctor");
        alert.setHeaderText("Remove " + doctor.getName());
        alert.setContentText("Are you sure you want to remove this doctor?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                doctors.remove(doctor);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor removed successfully.");
            }
        });
    }

    private void clearInputFields() {
        nameField.clear();
        specializationField.clear();
        contactInfoField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

class Doctor {
    private final javafx.beans.property.StringProperty name;
    private final javafx.beans.property.StringProperty specialization;
    private final javafx.beans.property.StringProperty contactInfo;

    public Doctor(String name, String specialization, String contactInfo) {
        this.name = new javafx.beans.property.SimpleStringProperty(name);
        this.specialization = new javafx.beans.property.SimpleStringProperty(specialization);
        this.contactInfo = new javafx.beans.property.SimpleStringProperty(contactInfo);
    }

    // Add getter for ComboBox display
    public String getName() {
        return name.get();
    }

    public javafx.beans.property.StringProperty nameProperty() {
        return name;
    }

    public javafx.beans.property.StringProperty specializationProperty() {
        return specialization;
    }

    public javafx.beans.property.StringProperty contactInfoProperty() {
        return contactInfo;
    }

    @Override
    public String toString() {
        return getName(); // For ComboBox display
    }
}

class BillingView extends VBox {
    private final ComboBox<Patient> patientComboBox;
    private final TextField serviceField;
    private final TextField amountField;
    private final DatePicker billingDatePicker;
    private final TableView<BillingRecord> billingTable;
    private final ObservableList<BillingRecord> billingRecords;
    private final ObservableList<Patient> patients;

    public BillingView(ObservableList<Patient> patients, ObservableList<BillingRecord> billingRecords) {
        this.patients = patients;
        this.billingRecords = billingRecords;
        
        // Initialize components
        patientComboBox = new ComboBox<>(patients);
        serviceField = new TextField();
        amountField = new TextField();
        billingDatePicker = new DatePicker(LocalDate.now());
        
        billingTable = new TableView<>();
        setupBillingTable();
        
        // Layout
        GridPane inputGrid = createInputGrid();
        Button addUpdateButton = new Button("Add Billing Record");
        addUpdateButton.setOnAction(e -> addBillingRecord());
        
        getChildren().addAll(inputGrid, addUpdateButton, billingTable);
        setSpacing(10);
        setPadding(new Insets(10));

        // Add listener for patient data changes
        patients.addListener((ListChangeListener<Patient>) c -> 
            patientComboBox.setItems(FXCollections.observableArrayList(patients)));
    }

    private void setupBillingTable() {
        TableColumn<BillingRecord, String> patientCol = new TableColumn<>("Patient");
        patientCol.setCellValueFactory(cellData -> cellData.getValue().patientProperty());
        
        TableColumn<BillingRecord, String> serviceCol = new TableColumn<>("Service");
        serviceCol.setCellValueFactory(cellData -> cellData.getValue().serviceProperty());
        
        TableColumn<BillingRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        TableColumn<BillingRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        
        TableColumn<BillingRecord, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    BillingRecord record = getTableView().getItems().get(getIndex());
                    handleBillingRecordDeletion(record);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        billingTable.getColumns().addAll(patientCol, serviceCol, amountCol, dateCol, actionCol);
        billingTable.setItems(billingRecords);
    }

    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Patient:"), patientComboBox);
        grid.addRow(1, new Label("Service:"), serviceField);
        grid.addRow(2, new Label("Amount:"), amountField);
        grid.addRow(3, new Label("Date:"), billingDatePicker);

        return grid;
    }

    private void addBillingRecord() {
        Patient patient = patientComboBox.getValue();
        String service = serviceField.getText().trim();
        String amountText = amountField.getText().trim();
        LocalDate date = billingDatePicker.getValue();

        if (patient == null || service.isEmpty() || amountText.isEmpty() || date == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot create billing records for future dates.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Amount must be greater than zero.");
                return;
            }
            
            BillingRecord newRecord = new BillingRecord(patient.getPatientId(), patient.getName(), service, amount, date);
            billingRecords.add(newRecord);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Billing record added successfully.");
            clearInputFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount. Please enter a valid number.");
        }
    }

    private void handleBillingRecordDeletion(BillingRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Billing Record");
        alert.setHeaderText("Delete billing record for " + record.patientProperty().get());
        alert.setContentText("Are you sure you want to delete this billing record?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                billingRecords.remove(record);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Billing record deleted successfully.");
            }
        });
    }

    private void clearInputFields() {
        patientComboBox.setValue(null);
        serviceField.clear();
        amountField.clear();
        billingDatePicker.setValue(LocalDate.now());
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

class BillingRecord {
    private final javafx.beans.property.StringProperty patientId;
    private final javafx.beans.property.StringProperty patient;
    private final javafx.beans.property.StringProperty service;
    private final javafx.beans.property.DoubleProperty amount;
    private final javafx.beans.property.ObjectProperty<LocalDate> date;

    public BillingRecord(String patientId, String patient, String service, double amount, LocalDate date) {
        this.patientId = new javafx.beans.property.SimpleStringProperty(patientId);
        this.patient = new javafx.beans.property.SimpleStringProperty(patient);
        this.service = new javafx.beans.property.SimpleStringProperty(service);
        this.amount = new javafx.beans.property.SimpleDoubleProperty(amount);
        this.date = new javafx.beans.property.SimpleObjectProperty<>(date);
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

    public javafx.beans.property.StringProperty patientProperty() {
        return patient;
    }

    public javafx.beans.property.StringProperty serviceProperty() {
        return service;
    }

    public javafx.beans.property.DoubleProperty amountProperty() {
        return amount;
    }

    public javafx.beans.property.ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
}

class AnalyticsView extends VBox {
    private final ComboBox<String> reportTypeComboBox;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final ComboBox<String> patientSelector;
    private final VBox chartContainer;
    private final ObservableList<Patient> patients;
    private final ObservableList<Doctor> doctors;
    private final ObservableList<Appointment> appointments;
    private final ObservableList<BillingRecord> billingRecords;

    public AnalyticsView(ObservableList<Patient> patients, 
                        ObservableList<Doctor> doctors,
                        ObservableList<Appointment> appointments,
                        ObservableList<BillingRecord> billingRecords) {
        this.patients = patients;
        this.doctors = doctors;
        this.appointments = appointments;
        this.billingRecords = billingRecords;

        // Initialize components
        reportTypeComboBox = new ComboBox<>();
        reportTypeComboBox.getItems().addAll(
            "Monthly Patient Visits",
            "Doctor Workload Distribution",
            "Monthly Revenue Trend",
            "Service Type Distribution",
            "Individual Patient History"
        );
        reportTypeComboBox.setValue("Monthly Patient Visits");
        
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(6));
        endDatePicker = new DatePicker(LocalDate.now());
        
        // Patient selector for individual analytics
        patientSelector = new ComboBox<>();
        updatePatientSelector();
        patientSelector.setVisible(false);
        
        chartContainer = new VBox();
        chartContainer.setMinHeight(400);

        initializeLayout();
        setupEventHandlers();
        updateChart();
    }

    private void initializeLayout() {
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(10));

        inputGrid.add(new Label("Report Type:"), 0, 0);
        inputGrid.add(reportTypeComboBox, 1, 0);
        inputGrid.add(new Label("Start Date:"), 0, 1);
        inputGrid.add(startDatePicker, 1, 1);
        inputGrid.add(new Label("End Date:"), 0, 2);
        inputGrid.add(endDatePicker, 1, 2);
        inputGrid.add(new Label("Patient:"), 0, 3);
        inputGrid.add(patientSelector, 1, 3);

        Button generateButton = new Button("Generate Report");
        generateButton.setOnAction(e -> updateChart());
        inputGrid.add(generateButton, 1, 4);

        this.getChildren().addAll(inputGrid, chartContainer);
        this.setSpacing(20);
        this.setPadding(new Insets(10));
    }

    private void setupEventHandlers() {
        // Date validation
        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisabled(empty || date.isBefore(startDatePicker.getValue()));
            }
        });

        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisabled(empty || date.isAfter(LocalDate.now()));
            }
        });

        // Show/hide patient selector based on report type
        reportTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            patientSelector.setVisible(newVal.equals("Individual Patient History"));
            updateChart();
        });

        // Update on data changes
        patients.addListener((ListChangeListener<Patient>) c -> {
            updatePatientSelector();
            updateChart();
        });
        doctors.addListener((ListChangeListener<Doctor>) c -> updateChart());
        appointments.addListener((ListChangeListener<Appointment>) c -> updateChart());
        billingRecords.addListener((ListChangeListener<BillingRecord>) c -> updateChart());
    }

    private void updatePatientSelector() {
        patientSelector.getItems().clear();
        patients.forEach(patient -> 
            patientSelector.getItems().add(patient.getName() + " (ID: " + patient.getPatientId() + ")"));
        if (!patientSelector.getItems().isEmpty()) {
            patientSelector.setValue(patientSelector.getItems().get(0));
        }
    }

    private void updateChart() {
        String reportType = reportTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a valid date range.");
            return;
        }

        chartContainer.getChildren().clear();

        switch (reportType) {
            case "Monthly Patient Visits":
                generateMonthlyPatientVisitsChart(startDate, endDate);
                break;
            case "Doctor Workload Distribution":
                generateDoctorWorkloadChart(startDate, endDate);
                break;
            case "Monthly Revenue Trend":
                generateMonthlyRevenueChart(startDate, endDate);
                break;
            case "Service Type Distribution":
                generateServiceDistributionChart(startDate, endDate);
                break;
            case "Individual Patient History":
                generatePatientHistoryChart(startDate, endDate);
                break;
        }
    }

    private void generateMonthlyPatientVisitsChart(LocalDate startDate, LocalDate endDate) {
        // Create line chart for trending
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        
        lineChart.setTitle("Monthly Patient Visits");
        xAxis.setLabel("Month");
        yAxis.setLabel("Number of Visits");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patient Visits");

        // Group appointments by month
        Map<YearMonth, Integer> monthlyVisits = new TreeMap<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!current.isAfter(end)) {
            monthlyVisits.put(current, 0);
            current = current.plusMonths(1);
        }

        appointments.stream()
            .filter(apt -> !apt.dateProperty().get().isBefore(startDate) &&
                          !apt.dateProperty().get().isAfter(endDate))
            .forEach(apt -> {
                YearMonth month = YearMonth.from(apt.dateProperty().get());
                monthlyVisits.merge(month, 1, Integer::sum);
            });

        monthlyVisits.forEach((month, count) ->
            series.getData().add(new XYChart.Data<>(month.format(DateTimeFormatter.ofPattern("MMM yyyy")), count)));

        lineChart.getData().add(series);
        chartContainer.getChildren().add(lineChart);
    }

    private void generateDoctorWorkloadChart(LocalDate startDate, LocalDate endDate) {
        // Create pie chart for workload distribution
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Doctor Workload Distribution");

        Map<String, Integer> doctorWorkload = new HashMap<>();
        
        appointments.stream()
            .filter(apt -> !apt.dateProperty().get().isBefore(startDate) &&
                          !apt.dateProperty().get().isAfter(endDate))
            .forEach(apt -> doctorWorkload.merge(apt.doctorProperty().get(), 1, Integer::sum));

        doctorWorkload.forEach((doctor, count) ->
            pieChart.getData().add(new PieChart.Data(doctor + " (" + count + " appointments)", count)));

        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        chartContainer.getChildren().add(pieChart);
    }

    private void generateMonthlyRevenueChart(LocalDate startDate, LocalDate endDate) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        StackedBarChart<String, Number> stackedChart = new StackedBarChart<>(xAxis, yAxis);
        
        stackedChart.setTitle("Monthly Revenue by Service Type");
        xAxis.setLabel("Month");
        yAxis.setLabel("Revenue ($)");

        // Create series for each service type
        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        
        billingRecords.stream()
            .filter(record -> !record.dateProperty().get().isBefore(startDate) &&
                            !record.dateProperty().get().isAfter(endDate))
            .forEach(record -> {
                String service = record.serviceProperty().get();
                String month = YearMonth.from(record.dateProperty().get())
                    .format(DateTimeFormatter.ofPattern("MMM yyyy"));
                double amount = record.amountProperty().get();

                seriesMap.computeIfAbsent(service, k -> {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(k);
                    return series;
                }).getData().add(new XYChart.Data<>(month, amount));
            });

        stackedChart.getData().addAll(seriesMap.values());
        chartContainer.getChildren().add(stackedChart);
    }

    private void generateServiceDistributionChart(LocalDate startDate, LocalDate endDate) {
        // Create donut chart for service distribution
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Service Type Distribution");

        Map<String, Double> serviceRevenue = new HashMap<>();
        
        billingRecords.stream()
            .filter(record -> !record.dateProperty().get().isBefore(startDate) &&
                            !record.dateProperty().get().isAfter(endDate))
            .forEach(record -> 
                serviceRevenue.merge(record.serviceProperty().get(), 
                                   record.amountProperty().get(), 
                                   Double::sum));

        serviceRevenue.forEach((service, amount) ->
            pieChart.getData().add(new PieChart.Data(
                service + " ($" + String.format("%.2f", amount) + ")", 
                amount)));

        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        chartContainer.getChildren().add(pieChart);
    }

    private void generatePatientHistoryChart(LocalDate startDate, LocalDate endDate) {
        if (patientSelector.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a patient.");
            return;
        }

        // Extract patient ID from selector value
        String patientId = patientSelector.getValue().split("ID: ")[1].replace(")", "");

        VBox patientStats = new VBox(10);
        patientStats.setPadding(new Insets(10));

        // Create timeline chart for patient visits
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> visitChart = new LineChart<>(xAxis, yAxis);
        
        visitChart.setTitle("Visit History");
        XYChart.Series<String, Number> visitSeries = new XYChart.Series<>();
        visitSeries.setName("Visits");

        // Create bar chart for expenses
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        BarChart<String, Number> expenseChart = new BarChart<>(xAxis2, yAxis2);
        
        expenseChart.setTitle("Monthly Expenses");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        // Process patient data
        Map<YearMonth, Integer> monthlyVisits = new TreeMap<>();
        Map<YearMonth, Double> monthlyExpenses = new TreeMap<>();

        // Get patient's appointments
        appointments.stream()
            .filter(apt -> apt.patientIdProperty().get().equals(patientId) &&
                          !apt.dateProperty().get().isBefore(startDate) &&
                          !apt.dateProperty().get().isAfter(endDate))
            .forEach(apt -> {
                YearMonth month = YearMonth.from(apt.dateProperty().get());
                monthlyVisits.merge(month, 1, Integer::sum);
            });

        // Get patient's billing records
        billingRecords.stream()
            .filter(record -> record.getPatientId().equals(patientId) &&
                            !record.dateProperty().get().isBefore(startDate) &&
                            !record.dateProperty().get().isAfter(endDate))
            .forEach(record -> {
                YearMonth month = YearMonth.from(record.dateProperty().get());
                monthlyExpenses.merge(month, record.amountProperty().get(), Double::sum);
            });

        // Add data to charts
        monthlyVisits.forEach((month, count) ->
            visitSeries.getData().add(new XYChart.Data<>(
                month.format(DateTimeFormatter.ofPattern("MMM yyyy")), count)));

        monthlyExpenses.forEach((month, amount) ->
            expenseSeries.getData().add(new XYChart.Data<>(
                month.format(DateTimeFormatter.ofPattern("MMM yyyy")), amount)));

        visitChart.getData().add(visitSeries);
        expenseChart.getData().add(expenseSeries);

        // Add summary statistics
        Label summaryLabel = new Label(String.format(
            "Summary Statistics:\n" +
            "Total Visits: %d\n" +
            "Total Expenses: $%.2f\n" +
            "Average Monthly Visits: %.1f\n" +
            "Average Monthly Expense: $%.2f",
            monthlyVisits.values().stream().mapToInt(Integer::intValue).sum(),
            monthlyExpenses.values().stream().mapToDouble(Double::doubleValue).sum(),
            monthlyVisits.values().stream().mapToInt(Integer::intValue).average().orElse(0),
            monthlyExpenses.values().stream().mapToDouble(Double::doubleValue).average().orElse(0)
        ));
        summaryLabel.setStyle("-fx-font-weight: bold;");

        patientStats.getChildren().addAll(summaryLabel, visitChart, expenseChart);
        chartContainer.getChildren().add(patientStats);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

class Appointment {
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

    // Property methods
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
