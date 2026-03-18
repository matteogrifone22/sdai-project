package it.unige.dibris.mas.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;

public class SimulationGUI extends Application {
    
    private TextArea logArea;
    private TextField patientCountField;
    private Button createPatientsButton;
    private Button addPatientButton;
    private Button quitButton;
    
    private ContainerController container;
    private int patientCounter = 0;
    
    // Setter per il container (chiamato dal Main)
    public void setContainer(ContainerController container) {
        this.container = container;
    }
    
    @Override
    public void start(Stage primaryStage) {
        // Log Area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(20);
        
        // Patient Count Input
        patientCountField = new TextField();
        patientCountField.setPromptText("Number of patients");
        patientCountField.setPrefWidth(150);
        
        // Buttons
        createPatientsButton = new Button("Create Patients");
        createPatientsButton.setOnAction(e -> createMultiplePatients());
        
        addPatientButton = new Button("Add Patient");
        addPatientButton.setOnAction(e -> createSinglePatient());
        
        quitButton = new Button("Quit");
        quitButton.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        quitButton.setOnAction(e -> quitApplication());
        
        // Layout
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.getChildren().addAll(
            new Label("Patients:"),
            patientCountField,
            createPatientsButton,
            addPatientButton,
            quitButton
        );
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(
            new Label("ED Simulation Log"),
            logArea,
            inputBox
        );
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("ED Simulation");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> quitApplication());
        primaryStage.show();
        
        appendLog("JADE Simulation started!");
    }
    
    private void createMultiplePatients() {
        try {
            int count = Integer.parseInt(patientCountField.getText());
            for (int i = 0; i < count; i++) {
                createPatient();
                Thread.sleep(100);  // Piccolo delay tra creazioni
            }
            appendLog("Created " + count + " patients");
        } catch (NumberFormatException e) {
            appendLog("ERROR: Invalid number");
        } catch (Exception e) {
            appendLog("ERROR: " + e.getMessage());
        }
    }
    
    private void createSinglePatient() {
        try {
            createPatient();
        } catch (Exception e) {
            appendLog("ERROR: " + e.getMessage());
        }
    }
    
    private void createPatient() throws Exception {
        patientCounter++;
        String patientName = "Patient_" + patientCounter;
        
        AgentController agentController = container.createNewAgent(
            patientName,
            "it.unige.dibris.mas.agents.PatientAgent",
            null
        );
        agentController.start();
        appendLog("Created: " + patientName);
    }
    
    private void quitApplication() {
        appendLog("Shutting down...");
        System.exit(0);
    }
    
    public void appendLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}