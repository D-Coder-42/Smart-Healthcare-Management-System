import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DoctorManagementView extends VBox {
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
