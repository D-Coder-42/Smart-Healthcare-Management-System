import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.chart.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AnalyticsView extends VBox {
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

        reportTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            patientSelector.setVisible(newVal.equals("Individual Patient History"));
            updateChart();
        });

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
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        
        lineChart.setTitle("Monthly Patient Visits");
        xAxis.setLabel("Month");
        yAxis.setLabel("Number of Visits");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patient Visits");

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

        String patientId = patientSelector.getValue().split("ID: ")[1].replace(")", "");

        VBox patientStats = new VBox(10);
        patientStats.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> visitChart = new LineChart<>(xAxis, yAxis);
        
        visitChart.setTitle("Visit History");
        XYChart.Series<String, Number> visitSeries = new XYChart.Series<>();
        visitSeries.setName("Visits");

        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        BarChart<String, Number> expenseChart = new BarChart<>(xAxis2, yAxis2);
        
        expenseChart.setTitle("Monthly Expenses");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        Map<YearMonth, Integer> monthlyVisits = new TreeMap<>();
        Map<YearMonth, Double> monthlyExpenses = new TreeMap<>();

        appointments.stream()
            .filter(apt -> apt.patientIdProperty().get().equals(patientId) &&
                          !apt.dateProperty().get().isBefore(startDate) &&
                          !apt.dateProperty().get().isAfter(endDate))
            .forEach(apt -> {
                YearMonth month = YearMonth.from(apt.dateProperty().get());
                monthlyVisits.merge(month, 1, Integer::sum);
            });

        billingRecords.stream()
            .filter(record -> record.getPatientId().equals(patientId) &&
                            !record.dateProperty().get().isBefore(startDate) &&
                            !record.dateProperty().get().isAfter(endDate))
            .forEach(record -> {
                YearMonth month = YearMonth.from(record.dateProperty().get());
                monthlyExpenses.merge(month, record.amountProperty().get(), Double::sum);
            });

        monthlyVisits.forEach((month, count) ->
            visitSeries.getData().add(new XYChart.Data<>(
                month.format(DateTimeFormatter.ofPattern("MMM yyyy")), count)));

        monthlyExpenses.forEach((month, amount) ->
            expenseSeries.getData().add(new XYChart.Data<>(
                month.format(DateTimeFormatter.ofPattern("MMM yyyy")), amount)));

        visitChart.getData().add(visitSeries);
        expenseChart.getData().add(expenseSeries);

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
