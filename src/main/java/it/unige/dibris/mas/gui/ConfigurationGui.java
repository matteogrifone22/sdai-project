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
    private static int numBeds = 5;
    private static boolean configDone = false;

    public static void showConfigurationDialog(Stage primaryStage, Runnable onConfigComplete) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Titolo
        Label titleLabel = new Label("ED System Configuration");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // Numero Dottori
        HBox doctorsBox = new HBox(10);
        doctorsBox.setAlignment(Pos.CENTER_LEFT);
        Label doctorsLabel = new Label("Number of Doctors:");
        doctorsLabel.setPrefWidth(150);
        Spinner<Integer> doctorsSpinner = new Spinner<>(1, 10, 2);
        doctorsSpinner.setPrefWidth(100);
        doctorsBox.getChildren().addAll(doctorsLabel, doctorsSpinner);

        // Numero Letti
        HBox bedsBox = new HBox(10);
        bedsBox.setAlignment(Pos.CENTER_LEFT);
        Label bedsLabel = new Label("Number of Beds:");
        bedsLabel.setPrefWidth(150);
        Spinner<Integer> bedsSpinner = new Spinner<>(1, 20, 5);
        bedsSpinner.setPrefWidth(100);
        bedsBox.getChildren().addAll(bedsLabel, bedsSpinner);

        // Numero Nurse (max = numero letti)
        HBox nursesBox = new HBox(10);
        nursesBox.setAlignment(Pos.CENTER_LEFT);
        Label nursesLabel = new Label("Number of Nurses:");
        nursesLabel.setPrefWidth(150);
        Spinner<Integer> nursesSpinner = new Spinner<>(1, 10, 2);
        nursesSpinner.setPrefWidth(100);
        nursesBox.getChildren().addAll(nursesLabel, nursesSpinner);

        // Label di validazione
        Label validationLabel = new Label("");
        validationLabel.setStyle("-fx-text-fill: #DC143C; -fx-font-weight: bold;");

        // Listener per validare nurse <= letti
        bedsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) nursesSpinner.getValueFactory()).setMax(newVal);
            if (nursesSpinner.getValue() > newVal) {
                nursesSpinner.getValueFactory().setValue(newVal);
            }
            validationLabel.setText("");
        });

        // Bottone Start
        Button startButton = new Button("Start ED Simulation");
        startButton.setPrefWidth(150);
        startButton.setStyle(
                "-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #228B22; -fx-text-fill: white; -fx-cursor: hand;");

        startButton.setOnAction(e -> {
            int doctors = doctorsSpinner.getValue();
            int beds = bedsSpinner.getValue();
            int nurses = nursesSpinner.getValue();

            // Validazione
            if (nurses > beds) {
                validationLabel.setText("ERROR: Nurses cannot exceed number of beds!");
                return;
            }

            // Salva i valori
            numDoctors = doctors;
            numNurses = nurses;
            numBeds = beds;
            configDone = true;

            SimulationLogger.getInstance()
                    .log("Configuration: " + doctors + " Doctors, " + beds + " Beds, " + nurses + " Nurses");

            // ← CAMBIA QUI: Chiudi la finestra corrente
            Stage configStage = (Stage) startButton.getScene().getWindow();
            configStage.close();

            // Chiama il callback DOPO aver chiuso
            onConfigComplete.run();
        });

        root.getChildren().addAll(
                titleLabel,
                new Separator(),
                doctorsBox,
                bedsBox,
                nursesBox,
                validationLabel,
                new Separator(),
                startButton);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("ED Configuration");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();
    }

    // Getter per i valori
    public static int getNumDoctors() {
        return numDoctors;
    }

    public static int getNumNurses() {
        return numNurses;
    }

    public static int getNumBeds() {
        return numBeds;
    }

    public static boolean isConfigDone() {
        return configDone;
    }
}