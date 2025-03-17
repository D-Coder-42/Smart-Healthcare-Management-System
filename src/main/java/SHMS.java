import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
