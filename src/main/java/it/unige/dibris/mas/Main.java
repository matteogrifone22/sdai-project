package it.unige.dibris.mas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import java.util.Map;
import java.util.AbstractMap;
import java.util.HashMap;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.ontology.TriageColor;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.gui.ColorStats;
import java.util.concurrent.CountDownLatch;

public class Main extends Application {

    private static Map<String, ColorStats> colorStatsMap = new HashMap<>();
    private static ContainerController container;
    private static int patientCounter = 0;
    private static CountDownLatch edInitLatch = new CountDownLatch(5); // 5 ED Agents: Registration, Triage,
                                                                       // QueueManager, Doctor_1, Doctor_2

    private TextArea logArea;
    private TextField patientCountField;
    // private Button createPatientsButton;
    // private Button addPatientButton;
    private Button quitButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Crea il profilo JADE
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");

            Runtime runtime = Runtime.instance();
            container = runtime.createMainContainer(profile);

            SimulationLogger.getInstance().log("JADE container started!");

            // Inizialize ED in un thread separato
            Thread edInitThread = new Thread(() -> {
                initializeED();
            });
            edInitThread.setDaemon(false);
            edInitThread.start();

            try {
                edInitLatch.await();
                SimulationLogger.getInstance().log("ED initialized! All ED Agents have been created");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Crea la GUI
        createGUI(primaryStage);
    }

    private void createGUI(Stage primaryStage) {
        // Log Area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(20);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11;");

        ScrollPane scrollPane = new ScrollPane(logArea);
        scrollPane.setFitToWidth(true);

        // Aggiungi listener al logger
        SimulationLogger.getInstance().addListener(() -> {
            logArea.setText(SimulationLogger.getInstance().getFullLog());
            logArea.setScrollTop(Double.MAX_VALUE);
        });

        // Patient Count Input
        patientCountField = new TextField("1");
        patientCountField.setPromptText("Insert number of patients");
        patientCountField.setPrefWidth(150);

        // Buttons
        Button createLowButton = new Button("Create Low");
        createLowButton.setPrefWidth(100);
        createLowButton.setOnAction(e -> createMultiplePatients(PatientSeverity.LOW));

        Button createMediumButton = new Button("Create Medium");
        createMediumButton.setPrefWidth(100);
        createMediumButton.setOnAction(e -> createMultiplePatients(PatientSeverity.MEDIUM));

        Button createHighButton = new Button("Create High");
        createHighButton.setPrefWidth(100);
        createHighButton.setOnAction(e -> createMultiplePatients(PatientSeverity.HIGH));

        quitButton = new Button("Quit");
        quitButton.setPrefWidth(120);
        quitButton.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        quitButton.setOnAction(e -> quitApplication());

        // Layout input
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        inputBox.getChildren().addAll(
                new Label("Number of patients:"),
                patientCountField,
                new Separator(),
                createLowButton,
                createMediumButton,
                createHighButton,
                quitButton);

        // ========== BLOCCO (0,0) - LOG + BUTTONS ==========
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        logBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        logBox.getChildren().addAll(
                new Label("📋 ED Simulation Log"),
                scrollPane,
                inputBox);

        // ========== BLOCCO (0,1) - QUEUE LIST VIEW ==========

        // Priority Queue
        javafx.collections.ObservableList<Map.Entry<String, TriageColor>> queueItems = javafx.collections.FXCollections
                .observableArrayList();

        javafx.scene.control.ListView<Map.Entry<String, TriageColor>> queueListView = new javafx.scene.control.ListView<>(
                queueItems);

        queueListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Map.Entry<String, TriageColor> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getKey());
                    setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 12));
                    String textColor = getHexColor(item.getValue());
                    setStyle("-fx-text-fill: " + textColor + ";");
                }
            }

            private String getHexColor(TriageColor color) {
                switch (color) {
                    case RED:
                        return "#DC143C";
                    case ORANGE:
                        return "#FF8C00";
                    case GREEN:
                        return "#228B22";
                    case BLUE:
                        return "#4169E1";
                    default:
                        return "#000000";
                }
            }
        });

        queueListView.setPrefHeight(300);
        queueListView.setPrefWidth(200);
        queueListView.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-background-color: #F5F5F5;");
        Main.setQueueListItems(queueItems);

        // Waiting for Triage
        javafx.collections.ObservableList<String> waitingForTriageItems = javafx.collections.FXCollections
                .observableArrayList();

        javafx.scene.control.ListView<String> waitingTriageListView = new javafx.scene.control.ListView<>(
                waitingForTriageItems);

        waitingTriageListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #000000;");
                    setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 12));
                }
            }
        });

        waitingTriageListView.setPrefHeight(300);
        waitingTriageListView.setPrefWidth(200); // ← AGGIUNGI
        waitingTriageListView
                .setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-background-color: #F5F5F5;");
        Main.setWaitingForTriageItems(waitingForTriageItems);

        // Doctors Status
        javafx.collections.ObservableList<String> doctorStatusList = javafx.collections.FXCollections
                .observableArrayList();

        javafx.scene.control.ListView<String> doctorsListView = new javafx.scene.control.ListView<>(
                doctorStatusList);

        doctorsListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Estrai il nome del dottore e lo stato
                    String[] parts = item.split(" : ");
                    String doctorName = parts.length > 0 ? parts[0] : "";
                    String status = parts.length > 1 ? parts[1] : "";

                    // Crea il testo su due righe
                    String displayText = doctorName + "\n" + status;

                    setText(displayText);
                    setWrapText(true);

                    if (status.contains("FREE")) {
                        SimulationLogger.getInstance().log("[DEBUG] Doctor FREE: " + status);
                        setStyle(
                                "-fx-text-fill: #228B22; -fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-font-weight: bold;");
                    } else {
                        SimulationLogger.getInstance().log("[DEBUG] Doctor NOT FREE: '" + status + "'");
                        setStyle(
                                "-fx-text-fill: #DC143C; -fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-font-weight: bold;");
                    }
                }
            }
        });

        doctorsListView.setPrefHeight(300);
        doctorsListView.setPrefWidth(200);
        doctorsListView.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-background-color: #F5F5F5;");
        doctorsListView.setFixedCellSize(60); // ← AGGIUNGI QUESTO per dare spazio alle due righe

        Main.setDoctorStatusList(doctorStatusList);
        // ← CAMBIO: VBox → HBox con due colonne
        VBox triageBox = new VBox(10);
        triageBox.setPadding(new Insets(10));
        triageBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        triageBox.getChildren().addAll(
                new Label("⏳ Triage Queue"),
                waitingTriageListView);

        VBox priorityBox = new VBox(10);
        priorityBox.setPadding(new Insets(10));
        priorityBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        priorityBox.getChildren().addAll(
                new Label("🏥 Waiting List"),
                queueListView);

        VBox doctorsBox = new VBox(10);
        doctorsBox.setPadding(new Insets(10));
        doctorsBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        doctorsBox.getChildren().addAll(
                new Label("👨‍⚕️ Doctors Status"),
                doctorsListView);

        Main.setDoctorStatusList(doctorStatusList);

        Main.setPatientsInTreatment(patientsInTreatment);

        HBox queueBox = new HBox(10); // ← CAMBIA DA VBox A HBox
        queueBox.setPadding(new Insets(10));
        queueBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        queueBox.getChildren().addAll(triageBox, priorityBox, doctorsBox); // ← AGGIUNGI treatmentBox

        GridPane gridPane = new GridPane();// Nel GridPane:
        // ========== BLOCCO (1,0) - BAR CHART STATISTICHE ==========

        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Count");

        javafx.scene.chart.BarChart<String, Number> barChart = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        barChart.setTitle("Patients by Color");
        barChart.setPrefHeight(250);
        barChart.setStyle("-fx-font-size: 12;");

        // Serie di dati
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Count");

        // Aggiungi i dati (in ordine: WHITE, BLUE, GREEN, ORANGE, RED)
        javafx.scene.chart.XYChart.Data<String, Number> whiteData = new javafx.scene.chart.XYChart.Data<>("WHITE", 0);
        javafx.scene.chart.XYChart.Data<String, Number> greenData = new javafx.scene.chart.XYChart.Data<>("GREEN", 0);
        javafx.scene.chart.XYChart.Data<String, Number> blueData = new javafx.scene.chart.XYChart.Data<>("BLUE", 0);
        javafx.scene.chart.XYChart.Data<String, Number> orangeData = new javafx.scene.chart.XYChart.Data<>("ORANGE", 0);
        javafx.scene.chart.XYChart.Data<String, Number> redData = new javafx.scene.chart.XYChart.Data<>("RED", 0);

        series.getData().addAll(whiteData, greenData, blueData, orangeData, redData);
        barChart.getData().add(series);

        // Crea la mappa per accedere velocemente ai dati
        colorStatsMap.put("WHITE", new ColorStats("WHITE", whiteData));
        colorStatsMap.put("GREEN", new ColorStats("GREEN", greenData));
        colorStatsMap.put("BLUE", new ColorStats("BLUE", blueData));
        colorStatsMap.put("ORANGE", new ColorStats("ORANGE", orangeData));
        colorStatsMap.put("RED", new ColorStats("RED", redData));

        whiteData.getNode().setStyle("-fx-bar-fill: #F5F5F5; -fx-border-color: #999999;");
        greenData.getNode().setStyle("-fx-bar-fill: #228B22;");
        blueData.getNode().setStyle("-fx-bar-fill: #4169E1;");
        orangeData.getNode().setStyle("-fx-bar-fill: #FF8C00;");
        redData.getNode().setStyle("-fx-bar-fill: #DC143C;");

        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(10));
        chartBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        chartBox.getChildren().addAll(
                new Label("📊 Patients by Color"),
                barChart);

        // ========== BLOCCO (1,1) - SOON ==========

        VBox comingSoonBox = new VBox(20);
        comingSoonBox.setAlignment(javafx.geometry.Pos.CENTER);
        comingSoonBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: #F0F0F0;");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("🚧 Under Construction 🚧");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #FF8C00;");

        javafx.scene.control.Label subtitleLabel = new javafx.scene.control.Label("Bed Management coming soon...");
        subtitleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #999999;");

        comingSoonBox.getChildren().addAll(titleLabel, subtitleLabel);

        // ========== FINE PAZIENTI IN CURA ==========
        // ========== GRID LAYOUT 2x2 ==========

        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        // Aggiungi i blocchi
        gridPane.add(logBox, 0, 0); // (0,0) - Log + Buttons
        gridPane.add(queueBox, 1, 0); // (0,1) - Queue List
        gridPane.add(chartBox, 0, 1); // (1,0) - Bar Chart
        gridPane.add(comingSoonBox, 1, 1); // (1,1) - Placeholder

        // Configura le colonne e righe per distribuire equamente lo spazio
        javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
        col.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col, new javafx.scene.layout.ColumnConstraints());
        gridPane.getColumnConstraints().get(1).setPercentWidth(50);

        javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
        row.setPercentHeight(50);
        gridPane.getRowConstraints().addAll(row, new javafx.scene.layout.RowConstraints());
        gridPane.getRowConstraints().get(1).setPercentHeight(50);

        // Root con GridPane
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(gridPane);

        // ScrollPane per il root (se la GUI è troppo grande)
        ScrollPane rootScroll = new ScrollPane(root);
        rootScroll.setFitToWidth(true);
        rootScroll.setFitToHeight(true);

        Scene scene = new Scene(rootScroll, 1400, 900);
        primaryStage.setTitle("ED Multi-Agent System Simulation");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            quitApplication();
        });
        primaryStage.show();

        SimulationLogger.getInstance().log("GUI Ready! You can now create patients.");
    }

    private static javafx.collections.ObservableList<String> waitingForTriageItems;

    public static void setWaitingForTriageItems(javafx.collections.ObservableList<String> items) {
        waitingForTriageItems = items;
    }

    public static void updateWaitingForTriage(String patientId, boolean isWaiting) {
        Platform.runLater(() -> {
            if (waitingForTriageItems != null) {
                if (isWaiting) {
                    if (!waitingForTriageItems.contains(patientId)) {
                        waitingForTriageItems.add(patientId);
                    }
                } else {
                    waitingForTriageItems.remove(patientId);
                }
            }
        });
    }

    private static javafx.collections.ObservableList<String> patientsInTreatment;

    public static void setPatientsInTreatment(javafx.collections.ObservableList<String> patients) {
        patientsInTreatment = patients;
    }

    public static void updatePatientsInTreatment(String doctorName, String patientId, boolean isTreating) {
        Platform.runLater(() -> {
            if (patientsInTreatment != null) {
                String entry = doctorName + ": " + patientId;

                if (isTreating) {
                    // Aggiungi il paziente
                    if (!patientsInTreatment.contains(entry)) {
                        patientsInTreatment.add(entry);
                    }
                } else {
                    // Rimuovi il paziente
                    patientsInTreatment.remove(entry);
                }
            }
        });
    }

    private static javafx.collections.ObservableList<String> doctorStatusList;

    public static void setDoctorStatusList(javafx.collections.ObservableList<String> list) {
        doctorStatusList = list;
    }

    public static void updateDoctorStatus(String doctorName, String status) {
        Platform.runLater(() -> {
            if (doctorStatusList != null) {
                // Cerca se il dottore è già nella lista
                int index = -1;
                for (int i = 0; i < doctorStatusList.size(); i++) {
                    if (doctorStatusList.get(i).startsWith(doctorName)) {
                        index = i;
                        break;
                    }
                }

                String entry = doctorName + " : " + status;

                if (index >= 0) {
                    // Aggiorna il dottore esistente
                    doctorStatusList.set(index, entry);
                } else {
                    // Aggiungi nuovo dottore
                    doctorStatusList.add(entry);
                }
            }
        });
    }

    private static javafx.collections.ObservableList<Map.Entry<String, TriageColor>> queueListItems;

    public static void setQueueListItems(javafx.collections.ObservableList<Map.Entry<String, TriageColor>> items) {
        queueListItems = items;

    }

    public static void updateQueueListFromManager(String queueStatus) {
        Platform.runLater(() -> {

            if (queueListItems != null) {
                queueListItems.clear();

                String[] lines = queueStatus.split("\n");

                for (String line : lines) {

                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        int dotIndex = line.indexOf(".");
                        int openParen = line.indexOf("(");
                        int closeParen = line.indexOf(")");

                        if (dotIndex >= 0 && openParen >= 0 && closeParen >= 0) {
                            String patientId = line.substring(dotIndex + 1, openParen).trim();
                            String colorName = line.substring(openParen + 1, closeParen).trim().toUpperCase();

                            TriageColor color = TriageColor.valueOf(colorName);
                            queueListItems.add(new AbstractMap.SimpleEntry<>(patientId, color));
                        } else {
                            SimulationLogger.getInstance().log("[DEBUG] Indices not valid, skipping");
                        }
                    } catch (Exception e) {
                        SimulationLogger.getInstance().log("[DEBUG] Error: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void createMultiplePatients(PatientSeverity severity) {
        try {
            int count = Integer.parseInt(patientCountField.getText());
            if (count <= 0) {
                SimulationLogger.getInstance().log("⚠️ Please enter a positive number");
                return;
            }

            new Thread(() -> {
                for (int i = 0; i < count; i++) {
                    try {
                        createPatient(severity);
                        Thread.sleep(100); // Delay tra creazioni
                    } catch (Exception e) {
                        SimulationLogger.getInstance().log("ERROR: " + e.getMessage());
                    }
                }
                SimulationLogger.getInstance().log("✅ Created " + count + " patients");
            }).start();
            patientCountField.setText("1");

        } catch (NumberFormatException e) {
            SimulationLogger.getInstance().log("⚠️ ERROR: Invalid number");
        }
    }

    public static void updateColorStats(String colorName) {
        SimulationLogger.getInstance().log("[DEBUG] Updating color: " + colorName);

        Platform.runLater(() -> {
            if (colorStatsMap.containsKey(colorName)) {
                ColorStats stats = colorStatsMap.get(colorName);
                stats.setCount(stats.getCount() + 1);
                SimulationLogger.getInstance().log("[DEBUG] " + colorName + " count: " + stats.getCount());
            } else {
                SimulationLogger.getInstance().log("[DEBUG] Color not found in map: " + colorName);
            }
        });
    }

    private void createPatient(PatientSeverity severity) {
        try {
            patientCounter++;
            String patientName = "Patient_" + patientCounter;

            // Passa la severity come argomento
            Object[] args = new Object[] { severity.name() };

            AgentController agentController = container.createNewAgent(
                    patientName,
                    "it.unige.dibris.mas.agents.PatientAgent",
                    args);

            agentController.start();
            SimulationLogger.getInstance().log("✨ Created: " + patientName + " (" + severity.getLabel() + ")");

        } catch (Exception e) {
            SimulationLogger.getInstance().log("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Nel Main.java, aggiungi una variabile statica
    public static QueueManagerAgent sharedQueueManager = null;

    private void initializeED() {
        try {
            AgentController regAgentController = container.createNewAgent(
                    "RegistrationAgent",
                    "it.unige.dibris.mas.agents.RegistrationAgent",
                    null);
            regAgentController.start();

            // Crea il QueueManagerAgent
            AgentController queueManagerAgentController = container.createNewAgent(
                    "QueueManagerAgent",
                    "it.unige.dibris.mas.agents.QueueManagerAgent",
                    null);
            queueManagerAgentController.start();

            // Aspetta che sia inizializzato
            Thread.sleep(1000);

            // IMPORTANTE: Ottieni il QueueManagerAgent dal setup() stesso
            // (Lo faremo nel QueueManagerAgent.setup())

            // Crea il TriageAgent
            Object[] triageArgs = new Object[] { Main.sharedQueueManager };
            AgentController triageAgentController = container.createNewAgent(
                    "TriageAgent",
                    "it.unige.dibris.mas.agents.TriageAgent",
                    triageArgs);
            triageAgentController.start();

            // Crea i DoctorAgent
            for (int i = 1; i <= 2; i++) {
                Object[] doctorArgs = new Object[] { Main.sharedQueueManager };
                AgentController doctorController = container.createNewAgent(
                        "Doctor_" + i,
                        "it.unige.dibris.mas.agents.DoctorAgent",
                        doctorArgs);
                doctorController.start();
            }

            SimulationLogger.getInstance().log("ED initialized! All ED Agents have been created");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void quitApplication() {
        SimulationLogger.getInstance().log("🛑 Shutting down JADE...");
        new Thread(() -> {
            try {
                Thread.sleep(500);
                Runtime.instance().shutDown();
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void agentReady() {
        edInitLatch.countDown();
    }
}