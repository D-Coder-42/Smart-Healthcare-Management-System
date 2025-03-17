import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

public class BillingView extends VBox {
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
        
        patientComboBox = new ComboBox<>(patients);
        serviceField = new TextField();
        amountField = new TextField();
        billingDatePicker = new DatePicker(LocalDate.now());
        
        billingTable = new TableView<>();
        setupBillingTable();
        
        GridPane inputGrid = createInputGrid();
        Button addUpdateButton = new Button("Add Billing Record");
        addUpdateButton.setOnAction(e -> addBillingRecord());
        
        getChildren().addAll(inputGrid, addUpdateButton, billingTable);
        setSpacing(10);
        setPadding(new Insets(10));

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
