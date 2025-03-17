import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.time.LocalDate;
import java.util.Optional;

public class PatientManagementView extends VBox {
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
        addUpdateButton.setOnAction(e -> handleAddUpdate());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> cancelEditing());
        cancelButton.setVisible(false);
        
        // Layout
        HBox searchBox = createSearchBox();
        GridPane inputGrid = createInputGrid();
        HBox buttonBox = new HBox(10, addUpdateButton, cancelButton);
        
        getChildren().addAll(searchBox, inputGrid, buttonBox, patientTable);
        setSpacing(10);
        setPadding(new Insets(10));
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch());
    }
    
    private void setupPatientTable() {
        TableColumn<Patient, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().patientIdProperty());
        
        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        TableColumn<Patient, LocalDate> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(cellData -> cellData.getValue().dateOfBirthProperty());
        
        TableColumn<Patient, String> contactCol = new TableColumn<>("Contact Info");
        contactCol.setCellValueFactory(cellData -> cellData.getValue().contactInfoProperty());
        
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
    
    private void handleAddUpdate() {
        String name = nameField.getText().trim();
        LocalDate dob = dateOfBirthPicker.getValue();
        String contactInfo = contactInfoField.getText().trim();
        String medicalHistory = medicalHistoryArea.getText().trim();
        String customId = patientIdField.getText().trim();
        
        if (name.isEmpty() || dob == null || contactInfo.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }
        
        if (currentEditingPatient != null) {
            // Update existing patient
            currentEditingPatient.nameProperty().set(name);
            currentEditingPatient.dateOfBirthProperty().set(dob);
            currentEditingPatient.contactInfoProperty().set(contactInfo);
            currentEditingPatient.medicalHistoryProperty().set(medicalHistory);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient information updated successfully.");
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

            // Check if custom ID is already in use
            if (!customId.isEmpty() && patients.stream().anyMatch(p -> p.getPatientId().equals(customId))) {
                showAlert(Alert.AlertType.ERROR, "Error", "This Patient ID is already in use.");
                return;
            }
            
            Patient newPatient = customId.isEmpty() ? 
                new Patient(name, dob, contactInfo, medicalHistory) :
                new Patient(customId, name, dob, contactInfo, medicalHistory);
            
            patients.add(newPatient);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient added successfully.");
            clearInputFields();
        }
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
                showAlert(Alert.AlertType.ERROR, "Error", "Incorrect password!");
            }
        });
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
        
        HBox buttonBox = new HBox(10, editButton, saveButton, closeButton);
        
        editButton.setOnAction(e -> showPasswordDialogForEdit(nameField, dobPicker, 
                                                            contactField, historyArea, 
                                                            saveButton));
        
        saveButton.setDisable(true);
        saveButton.setOnAction(e -> {
            updatePatient(patient, nameField.getText(), dobPicker.getValue(),
                        contactField.getText(), historyArea.getText());
            infoStage.close();
        });
        
        closeButton.setOnAction(e -> infoStage.close());
        
        content.getChildren().addAll(
            new Label("Patient ID: " + patient.getPatientId()),
            new Label("Name:"), nameField,
            new Label("Date of Birth:"), dobPicker,
            new Label("Contact:"), contactField,
            new Label("Medical History:"), historyArea,
            buttonBox
        );
        
        infoStage.setScene(new javafx.scene.Scene(content));
        infoStage.showAndWait();
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
                showAlert(Alert.AlertType.ERROR, "Error", "Incorrect password!");
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
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Patient information updated successfully.");
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
    
    private Optional<Patient> findExistingPatient(String name, LocalDate dob) {
        return patients.stream()
            .filter(p -> p.getName().equalsIgnoreCase(name) && p.getDateOfBirth().equals(dob))
            .findFirst();
    }
    
    private void clearInputFields() {
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