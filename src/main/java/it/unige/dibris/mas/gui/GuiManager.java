package it.unige.dibris.mas.gui;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unige.dibris.mas.ontology.TriageColor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class GuiManager {

    // Variabili statiche per la GUI
    private static GridPane bedsGridPane;
    private static Map<Integer, VBox> bedBoxes = new HashMap<>();
    private static Map<Integer, VBox> doctorBoxes = new HashMap<>();

    // Aggiungi in cima alle variabili statiche:
    private static Map<String, List<Long>> dischargeTimesByColor = new HashMap<>();

    // Aggiungi in cima alle variabili statiche:
    private static Map<String, Label> colorCountLabels = new HashMap<>();
    private static int patientCounter = 0; // Contatore per ID pazienti

    private static TextArea logArea;
    private static TextField patientCountField;

    public static void createAndShowGUI(Stage primaryStage, int totalBeds, int numDoctors, int numNurses) {
        // Inizializza le statistiche di dimissione

        for (TriageColor color : TriageColor.values()) {
            dischargeTimesByColor.put(color.name(), new ArrayList<>());
        }

        // ========== BLOCCO (0,0) - LOG + BUTTONS ==========

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
        patientCountField.setPromptText("Number");
        patientCountField.setPrefWidth(80);

        GuiManager.setPatientCountField(patientCountField);

        // Buttons con stile piatto colorato
        Button createLowButton = new Button("Create Low");
        createLowButton.setPrefWidth(120);
        createLowButton.setStyle(
                "-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #228B22; -fx-text-fill: white; -fx-cursor: hand;");
        createLowButton.setOnAction(
                e -> {
                    int count = Integer.parseInt(patientCountField.getText());
                    it.unige.dibris.mas.Main.createMultiplePatients(it.unige.dibris.mas.ontology.PatientSeverity.LOW, count);
                });

        Button createMediumButton = new Button("Create Medium");
        createMediumButton.setPrefWidth(120);
        createMediumButton.setStyle(
                "-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #FF8C00; -fx-text-fill: white; -fx-cursor: hand;");
        createMediumButton.setOnAction(e -> {
            int count = Integer.parseInt(patientCountField.getText());
            it.unige.dibris.mas.Main.createMultiplePatients(it.unige.dibris.mas.ontology.PatientSeverity.MEDIUM, count);
        });

        Button createHighButton = new Button("Create High");
        createHighButton.setPrefWidth(120);
        createHighButton.setStyle(
                "-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #DC143C; -fx-text-fill: white; -fx-cursor: hand;");
        createHighButton.setOnAction(e -> {
            int count = Integer.parseInt(patientCountField.getText());
            it.unige.dibris.mas.Main.createMultiplePatients(it.unige.dibris.mas.ontology.PatientSeverity.HIGH, count);
        });

        Button quitButton = new Button("Quit");
        quitButton.setPrefWidth(100);
        quitButton.setStyle(
                "-fx-font-size: 12; -fx-padding: 10; -fx-background-color: #808080; -fx-text-fill: white; -fx-cursor: hand;");
        quitButton.setOnAction(e -> quitApplication());

        // Layout input (Quit in alto con gli altri)
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        inputBox.getChildren().addAll(
                new Label("Patients:"),
                patientCountField,
                new Separator(),
                createLowButton,
                createMediumButton,
                createHighButton,
                new Separator(),
                quitButton);

        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        logBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        logBox.getChildren().addAll(
                new Label("ED Simulation Log"),
                scrollPane,
                inputBox);

        // ========== BLOCCO (0,1) - LISTE ==========

        // Triage Queue
        ObservableList<String> waitingForTriageItems = FXCollections.observableArrayList();
        ListView<String> waitingTriageListView = new ListView<>(waitingForTriageItems);
        waitingTriageListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-text-fill: #000000;");
            }
        });
        waitingTriageListView.setPrefHeight(300);
        waitingTriageListView.setPrefWidth(200);
        waitingTriageListView.setPlaceholder(new Label("No patients waiting for triage"));
        waitingTriageListView
                .setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-background-color: #F5F5F5;");
        it.unige.dibris.mas.Main.setWaitingForTriageItems(waitingForTriageItems);

        VBox triageBox = new VBox(10);
        triageBox.setPadding(new Insets(10));
        triageBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        triageBox.getChildren().addAll(
                new Label("Triage Queue"),
                waitingTriageListView);

        // Priority Queue
        ObservableList<Map.Entry<String, TriageColor>> queueItems = FXCollections.observableArrayList();
        ListView<Map.Entry<String, TriageColor>> queueListView = new ListView<>(queueItems);
        queueListView.setCellFactory(param -> new ListCell<Map.Entry<String, TriageColor>>() {
            @Override
            protected void updateItem(Map.Entry<String, TriageColor> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKey());
                    setStyle("-fx-text-fill: " + getHexColor(item.getValue()) + ";");
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
        queueListView.setPlaceholder(new Label("No patients in priority queue"));
        queueListView.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-background-color: #F5F5F5;");
        it.unige.dibris.mas.Main.setQueueListItems(queueItems);

        VBox priorityBox = new VBox(10);
        priorityBox.setPadding(new Insets(10));
        priorityBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        priorityBox.getChildren().addAll(
                new Label("Waiting List"),
                queueListView);

        // Doctor Status (verticale, box-based)
        VBox doctorsStatusBox = createDoctorsStatusBox(numDoctors);

        HBox queueBox = new HBox(10);
        queueBox.setPadding(new Insets(10));
        queueBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        queueBox.getChildren().addAll(triageBox, priorityBox, doctorsStatusBox);

        // ========== BLOCCO (1,0) - GRAFICI ==========

        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(10));
        chartBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab1 = new Tab("Color Count", createColorCountView());
        Tab tab2 = new Tab("Discharge Time", createLineChartView());

        tabPane.getTabs().addAll(tab1, tab2);

        chartBox.getChildren().addAll(
                new Label("Charts"),
                tabPane);

        // ========== BLOCCO (1,1) - BED MANAGEMENT ==========

        bedsGridPane = new GridPane();
        bedsGridPane.setHgap(10);
        bedsGridPane.setVgap(10);
        bedsGridPane.setPadding(new Insets(10));
        bedsGridPane.setStyle("-fx-background-color: #F5F5F5;");

        // Crea 5 letti (dinamico dopo)
        for (int i = 1; i <= totalBeds; i++) {
            VBox bedBox = createBedBox(i);
            bedBoxes.put(i, bedBox);

            int row = (i - 1) / 4;
            int col = (i - 1) % 4;

            bedsGridPane.add(bedBox, col, row);
        }
        for (int col = 0; col < 4; col++) {
            javafx.scene.layout.ColumnConstraints colConstraints = new javafx.scene.layout.ColumnConstraints();
            colConstraints.setPercentWidth(25); // Ogni colonna occupa il 25% dello spazio
            bedsGridPane.getColumnConstraints().add(colConstraints);
        }

        ScrollPane bedsScrollPane = new ScrollPane(bedsGridPane);
        bedsScrollPane.setFitToWidth(true);
        bedsScrollPane.setStyle("-fx-control-inner-background: #F5F5F5;");

        VBox bedsContainer = new VBox(10);
        bedsContainer.setPadding(new Insets(10));
        bedsContainer.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
        bedsContainer.getChildren().addAll(
                new Label("Bed Management"),
                new Separator(),
                bedsScrollPane);

        // ========== GRID LAYOUT 2x2 ==========

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        gridPane.add(logBox, 0, 0);
        gridPane.add(queueBox, 1, 0);
        gridPane.add(chartBox, 0, 1);
        gridPane.add(bedsContainer, 1, 1);

        // Configura colonne e righe
        javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
        col.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col, new javafx.scene.layout.ColumnConstraints());
        gridPane.getColumnConstraints().get(1).setPercentWidth(50);

        javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
        row.setPercentHeight(50);
        gridPane.getRowConstraints().addAll(row, new javafx.scene.layout.RowConstraints());
        gridPane.getRowConstraints().get(1).setPercentHeight(50);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(gridPane);

        ScrollPane rootScroll = new ScrollPane(root);
        rootScroll.setFitToWidth(true);
        rootScroll.setFitToHeight(true);

        Scene scene = new Scene(rootScroll);
        primaryStage.setTitle("ED Multi-Agent System Simulation");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            quitApplication();
        });
        primaryStage.show();

        SimulationLogger.getInstance().log("GUI Ready! You can now create patients.");
    }

    // ========== METODI HELPER ==========

    private static VBox createDoctorsStatusBox(int numDoctors) {
        VBox doctorsBox = new VBox(10);
        doctorsBox.setPadding(new Insets(10));
        doctorsBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        Label title = new Label("Doctors Status");
        doctorsBox.getChildren().add(title);

        // ← CREA UN VBOX PER I DOTTORI:
        VBox doctorsList = new VBox(10);
        doctorsList.setPadding(new Insets(5));

        for (int i = 1; i <= numDoctors; i++) {
            VBox doctorBox = createDoctorBox(i);
            doctorBoxes.put(i, doctorBox);
            doctorsList.getChildren().add(doctorBox);
        }

        // ← WRAPPA IN SCROLLPANE:
        ScrollPane doctorsScrollPane = new ScrollPane(doctorsList);
        doctorsScrollPane.setFitToWidth(true);
        doctorsScrollPane.setFitToHeight(true);

        doctorsBox.getChildren().add(doctorsScrollPane);
        doctorsBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(doctorsBox, javafx.scene.layout.Priority.ALWAYS);

        return doctorsBox;
    }

    private static VBox createDoctorBox(int doctorId) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #228B22; -fx-border-width: 2; -fx-background-color: white;");
        box.setAlignment(Pos.CENTER);

        Label doctorLabel = new Label("Doctor_" + doctorId);
        doctorLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        doctorLabel.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("Free");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #228B22; -fx-font-weight: bold;");
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(doctorLabel, statusLabel);
        box.setUserData(new DoctorUIData(statusLabel));

        return box;
    }

    private static VBox createBedBox(int bedId) {
        VBox bedBox = new VBox(5);
        bedBox.setPadding(new Insets(10));
        bedBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        bedBox.setAlignment(Pos.TOP_CENTER);
        bedBox.setPrefWidth(150);
        bedBox.setPrefHeight(150);

        Label bedLabel = new Label("Bed " + bedId);
        bedLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Circle bedCircle = new Circle(30);
        bedCircle.setFill(javafx.scene.paint.Color.web("#E8E8E8"));
        bedCircle.setStroke(javafx.scene.paint.Color.web("#999999"));
        bedCircle.setStrokeWidth(2);

        Label patientLabel = new Label("Free");
        patientLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
        patientLabel.setWrapText(true);
        patientLabel.setMaxWidth(130);

        Label colorLabel = new Label("");
        colorLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");

        Label timeLabel = new Label("");
        timeLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #999999;");

        bedBox.getChildren().addAll(bedLabel, bedCircle, patientLabel, colorLabel, timeLabel);
        bedBox.setUserData(new BedUIData(bedCircle, patientLabel, colorLabel, timeLabel));

        return bedBox;
    }

    private static VBox createColorCountView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Patient Count by Triage Color"); // ← Titolo
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        HBox colorsBox = new HBox(20);
        colorsBox.setAlignment(Pos.CENTER);

        // 5 cerchi colorati con numero
        for (TriageColor color : TriageColor.values()) {
            VBox colorItem = createColorCountItem(color);
            colorsBox.getChildren().add(colorItem);
        }

        container.getChildren().addAll(titleLabel, colorsBox);
        return container;
    }

    private static VBox createColorCountItem(TriageColor color) {
        VBox item = new VBox(10);
        item.setAlignment(Pos.CENTER);

        Label countLabel = new Label("0");
        countLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Circle textCircle = new Circle(50);
        textCircle.setFill(javafx.scene.paint.Color.web(getHexColorForChart(color)));
        textCircle.setStroke(javafx.scene.paint.Color.web("#999999"));
        textCircle.setStrokeWidth(2);

        StackPane stack = new StackPane(textCircle, countLabel);

        Label nameLabel = new Label(color.getLabel());
        nameLabel.setStyle("-fx-font-size: 12;");

        item.getChildren().addAll(stack, nameLabel);

        // ← AGGIUNGI QUESTA RIGA:
        colorCountLabels.put(color.getLabel(), countLabel);

        return item;
    }

    private static String getHexColorForChart(TriageColor color) {
        switch (color) {
            case WHITE:
                return "#898989";
            case BLUE:
                return "#4169E1";
            case GREEN:
                return "#228B22";
            case ORANGE:
                return "#FF8C00";
            case RED:
                return "#DC143C";
            default:
                return "#000000";
        }
    }

    // ========== METODI PUBBLICI PER AGGIORNAMENTI GUI ==========

    public static void updateBedUI(int bedId, String patientId, String colorName, long admissionTime) {
        Platform.runLater(() -> {
            SimulationLogger.getInstance()
                    .log("[GuiManager DEBUG] Trying to update bed " + bedId + ", bedBoxes.size = " + bedBoxes.size());

            VBox bedBox = bedBoxes.get(bedId);
            if (bedBox == null) {
                SimulationLogger.getInstance().log("[GuiManager ERROR] bedBox " + bedId + " NOT FOUND in bedBoxes!");
                return;
            }

            BedUIData data = (BedUIData) bedBox.getUserData();

            if (patientId == null) {
                bedBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
                data.circle.setFill(javafx.scene.paint.Color.web("#E8E8E8"));
                data.patientLabel.setText("Free");
                data.patientLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
                data.colorLabel.setText("");
                data.timeLabel.setText("");
                data.admissionTime = 0;
            } else {
                String hexColor = getHexColorForChart(TriageColor.valueOf(colorName));
                data.circle.setFill(javafx.scene.paint.Color.web(hexColor));
                data.patientLabel.setText(patientId);
                data.patientLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #000000; -fx-font-weight: bold;");
                data.colorLabel.setText("Color: " + colorName);

                long elapsedSeconds = (System.currentTimeMillis() - admissionTime) / 1000;
                data.timeLabel.setText("Stay: " + elapsedSeconds + "s");
                data.admissionTime = admissionTime;
            }
        });
    }

    public static void highlightBedForNurseCheck(int bedId, String nurseName) {
        Platform.runLater(() -> {
            VBox bedBox = bedBoxes.get(bedId);
            if (bedBox == null)
                return;

            BedUIData data = (BedUIData) bedBox.getUserData();

            // Cambia solo opacity e label, NON la grandezza
            bedBox.setOpacity(0.7);
            data.timeLabel.setText(nurseName + " CHECK");
            data.timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #FF8C00; -fx-font-weight: bold;");

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    resetBedHighlight(bedId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private static void resetBedHighlight(int bedId) {
        Platform.runLater(() -> {
            VBox bedBox = bedBoxes.get(bedId);
            if (bedBox == null)
                return;

            BedUIData data = (BedUIData) bedBox.getUserData();

            bedBox.setOpacity(1.0);
            bedBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");

            if (data.admissionTime == 0) {
                data.timeLabel.setText("");
            } else {
                long elapsedSeconds = (System.currentTimeMillis() - data.admissionTime) / 1000;
                data.timeLabel.setText("Stay: " + elapsedSeconds + "s");
            }
        });
    }

    private static TextField patientCountFieldStatic;

    public static void setPatientCountFieldStatic(TextField field) {
        patientCountFieldStatic = field;
    }

    public static String getPatientCountFieldText() {
        return patientCountFieldStatic != null ? patientCountFieldStatic.getText() : "1";
    }

    public static void updateDoctorStatus(String doctorName, String status) {
        Platform.runLater(() -> {
            String[] parts = doctorName.split("_");
            if (parts.length < 2)
                return;

            int doctorId = Integer.parseInt(parts[1]);
            VBox doctorBox = doctorBoxes.get(doctorId);
            if (doctorBox == null)
                return;

            DoctorUIData data = (DoctorUIData) doctorBox.getUserData();

            if (status.equals("FREE") || status.equals("Free")) {
                // FREE: bordo verde, testo verde
                doctorBox.setStyle("-fx-border-color: #228B22; -fx-border-width: 2; -fx-background-color: white;");
                data.statusLabel.setText("Free");
                data.statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #228B22;");
            } else {
                // IN CURA: bordo rosso, estrai patient ID e colore
                doctorBox.setStyle("-fx-border-color: #DC143C; -fx-border-width: 2; -fx-background-color: white;");

                // status formato: "Patient_1|GREEN"
                String[] statusParts = status.split("\\|");
                String patientId = statusParts.length > 0 ? statusParts[0].trim() : status;
                String colorCode = statusParts.length > 1 ? statusParts[1].trim() : "BLACK";

                // Solo il nome del paziente, colorato
                data.statusLabel.setText(patientId);
                String hexColor = getHexColorForChart(TriageColor.valueOf(colorCode));
                data.statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + hexColor + ";");
            }
        });
    }

    private static void quitApplication() {
        SimulationLogger.getInstance().log("Shutting down JADE...");
        new Thread(() -> {
            try {
                Thread.sleep(500);
                jade.core.Runtime.instance().shutDown();
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    // ========== METODI PUBBLICI PER AGGIORNAMENTI GUI (chiamati dagli agenti)
    // ==========

    private static ObservableList<String> waitingForTriageItems;
    private static ObservableList<Map.Entry<String, TriageColor>> queueListItems;

    public static void setWaitingForTriageItems(ObservableList<String> items) {
        waitingForTriageItems = items;
    }

    public static void setQueueListItems(ObservableList<Map.Entry<String, TriageColor>> items) {
        queueListItems = items;
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
                        }
                    } catch (Exception e) {
                        SimulationLogger.getInstance().log("[DEBUG] Error: " + e.getMessage());
                    }
                }
            }
        });
    }

    public static void updateColorStats(String colorName) {
        Platform.runLater(() -> {
            // Prova sia uppercase che il formato originale
            Label label = colorCountLabels.get(colorName);

            if (label == null) {
                // Prova con il formato TriageColor.getLabel() (es. "Orange")
                TriageColor color = TriageColor.valueOf(colorName);
                label = colorCountLabels.get(color.getLabel());
            }

            if (label != null) {
                int currentCount = Integer.parseInt(label.getText());
                label.setText(String.valueOf(currentCount + 1));
                SimulationLogger.getInstance().log("[ColorStats] Updated " + colorName + " → " + label.getText());
            } else {
                SimulationLogger.getInstance().log("[ColorStats ERROR] Color label not found: " + colorName);
            }
        });
    }

    public static void updatePatientColor(String patientId, String newColorName) {
        Platform.runLater(() -> {
            // Incrementa il nuovo colore
            Label newLabel = colorCountLabels.get(TriageColor.valueOf(newColorName).getLabel());
            if (newLabel != null) {
                int count = Integer.parseInt(newLabel.getText());
                newLabel.setText(String.valueOf(count + 1));
                SimulationLogger.getInstance().log("[ColorStats] Added " + patientId + " to " + newColorName);
            }
        });
    }

    public static void removePatientFromColorStats(String patientId, String colorName) {
        Platform.runLater(() -> {
            Label label = colorCountLabels.get(TriageColor.valueOf(colorName).getLabel());
            if (label != null) {
                int count = Integer.parseInt(label.getText());
                label.setText(String.valueOf(Math.max(0, count - 1)));
                SimulationLogger.getInstance().log("[ColorStats] Removed " + patientId + " from " + colorName);
            }
        });
    }

    public static void setPatientCountField(TextField field) {
        patientCountField = field;
    }


    public static void setPatientCounter(int counter) {
        patientCounter = counter;
    }

    public static int getPatientCounter() {
        return patientCounter;
    }

    public static void incrementPatientCounter() {
        patientCounter++;
    }

    // ========== CLASSI INNER ==========

    private static class BedUIData {
        Circle circle;
        Label patientLabel;
        Label colorLabel;
        Label timeLabel;
        long admissionTime;

        BedUIData(Circle circle, Label patientLabel, Label colorLabel, Label timeLabel) {
            this.circle = circle;
            this.patientLabel = patientLabel;
            this.colorLabel = colorLabel;
            this.timeLabel = timeLabel;
            this.admissionTime = 0;
        }
    }

    private static class DoctorUIData {
        Label statusLabel;

        DoctorUIData(Label statusLabel) {
            this.statusLabel = statusLabel;
        }
    }

    // Aggiungi in cima alle variabili statiche:
    private static javafx.scene.chart.BarChart<String, Number> barChart;
    private static javafx.scene.chart.XYChart.Series<String, Number> barChartSeries;

    private static VBox createLineChartView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        xAxis.setLabel("Triage Color");
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Average Time (seconds)");

        barChart = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        barChart.setTitle("Average Discharge Time by Entry Color");
        barChart.setPrefHeight(400);

        barChartSeries = new javafx.scene.chart.XYChart.Series<>();
        barChartSeries.setName("Avg Time");

        for (TriageColor color : TriageColor.values()) {
            barChartSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(color.getLabel(), 0));
        }

        barChart.getData().add(barChartSeries);
        container.getChildren().add(barChart);
        return container;
    }

    public static void updateDischargeStats(String colorName, long treatmentTime) {
        Platform.runLater(() -> {
            // Aggiungi il tempo alla lista
            List<Long> times = dischargeTimesByColor.get(colorName);
            if (times != null) {
                times.add(treatmentTime);

                SimulationLogger.getInstance().log("[DischargeStats] " + colorName + " time: " + treatmentTime + "s");

                // Aggiorna il grafico
                updateBarChart();
            } else {
                SimulationLogger.getInstance().log("[DischargeStats ERROR] Color not found: " + colorName);
            }
        });
    }

    private static void updateBarChart() {
        if (barChartSeries == null) {
            SimulationLogger.getInstance().log("[BarChart ERROR] barChartSeries is null!");
            return;
        }

        // Aggiorna tutti i dati del grafico
        for (javafx.scene.chart.XYChart.Data<String, Number> data : barChartSeries.getData()) {
            String colorLabel = data.getXValue(); // Es. "Red", "Orange", ecc

            // Cerca nella mappa con il nome uppercase (RED, ORANGE, ecc)
            String colorUppercase = colorLabel.toUpperCase();
            List<Long> times = dischargeTimesByColor.get(colorUppercase);

            if (times != null && !times.isEmpty()) {
                long avgTime = times.stream().mapToLong(Long::longValue).sum() / times.size();
                data.setYValue(avgTime);

                SimulationLogger.getInstance().log("[BarChart] Updated " + colorLabel + " → " + avgTime + "s avg");
            }
        }
    }
}