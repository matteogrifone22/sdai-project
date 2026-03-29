package it.unige.dibris.mas.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConfigurationGui {

    private static int numDoctors = 2;
    private static int numNurses = 2;
    private static int numBeds = 6;
    private static int numAmbulances = 2;
    private static boolean configDone = false;

    public static void showConfigurationDialog(Stage primaryStage, Runnable onConfigComplete) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("ED System Configuration");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // Doctors 
        HBox doctorsBox = new HBox(10);
        doctorsBox.setAlignment(Pos.CENTER_LEFT);
        Label doctorsLabel = new Label("Number of Doctors:");
        doctorsLabel.setPrefWidth(150);
        Spinner<Integer> doctorsSpinner = new Spinner<>(1, 10, 2);
        doctorsSpinner.setPrefWidth(100);
        doctorsBox.getChildren().addAll(doctorsLabel, doctorsSpinner);

        // Beds
        HBox bedsBox = new HBox(10);
        bedsBox.setAlignment(Pos.CENTER_LEFT);
        Label bedsLabel = new Label("Number of Beds:");
        bedsLabel.setPrefWidth(150);
        Spinner<Integer> bedsSpinner = new Spinner<>(1, 30, 6);
        bedsSpinner.setPrefWidth(100);
        bedsBox.getChildren().addAll(bedsLabel, bedsSpinner);

        // Number of Nurses (max = number of beds)
        HBox nursesBox = new HBox(10);
        nursesBox.setAlignment(Pos.CENTER_LEFT);
        Label nursesLabel = new Label("Number of Nurses:");
        nursesLabel.setPrefWidth(150);
        Spinner<Integer> nursesSpinner = new Spinner<>(1, 10, 2);
        nursesSpinner.setPrefWidth(100);
        nursesBox.getChildren().addAll(nursesLabel, nursesSpinner);

        // Number of Ambulances
        HBox ambulancesBox = new HBox(10);
        ambulancesBox.setAlignment(Pos.CENTER_LEFT);
        Label ambulancesLabel = new Label("Number of Ambulances:");
        ambulancesLabel.setPrefWidth(150);
        Spinner<Integer> ambulancesSpinner = new Spinner<>(0, 10, 2);
        ambulancesSpinner.setPrefWidth(100);
        ambulancesBox.getChildren().addAll(ambulancesLabel, ambulancesSpinner);

        // Validation Label
        Label validationLabel = new Label("");
        validationLabel.setStyle("-fx-text-fill: #DC143C; -fx-font-weight: bold;");

        // Listeners to ensure nurses <= beds
        bedsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) nursesSpinner.getValueFactory()).setMax(newVal);
            if (nursesSpinner.getValue() > newVal) {
                nursesSpinner.getValueFactory().setValue(newVal);
            }
            validationLabel.setText("");
        });

        // Start Button
        Button startButton = new Button("Start ED Simulation");
        startButton.setPrefWidth(150);
        startButton.setStyle(
                "-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #228B22; -fx-text-fill: white; -fx-cursor: hand;");

        startButton.setOnAction(e -> {
            int doctors = doctorsSpinner.getValue();
            int beds = bedsSpinner.getValue();
            int nurses = nursesSpinner.getValue();

            // Validation
            if (nurses > beds) {
                validationLabel.setText("ERROR: Nurses cannot exceed number of beds!");
                return;
            }

            // Save configuration and proceed
            numDoctors = doctors;
            numNurses = nurses;
            numBeds = beds;
            numAmbulances = ambulancesSpinner.getValue();
            configDone = true;

            SimulationLogger.getInstance()
                    .log("Configuration: " + doctors + " Doctors, " + beds + " Beds, " + nurses + " Nurses");

            Stage configStage = (Stage) startButton.getScene().getWindow();
            configStage.close();

            onConfigComplete.run();
        });

        root.getChildren().addAll(
                titleLabel,
                new Separator(),
                doctorsBox,
                bedsBox,
                nursesBox,
                ambulancesBox,
                validationLabel,
                new Separator(),
                startButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("ED Configuration");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    public static int getNumDoctors() {
        return numDoctors;
    }

    public static int getNumNurses() {
        return numNurses;
    }

    public static int getNumBeds() {
        return numBeds;
    }

    public static int getNumAmbulances() {
        return numAmbulances;
    }

    public static boolean isConfigDone() {
        return configDone;
    }
}