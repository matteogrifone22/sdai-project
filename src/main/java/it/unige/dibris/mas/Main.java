package it.unige.dibris.mas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Stage;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import java.util.Map;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.ontology.TriageColor;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.agents.BedManagerAgent;
import it.unige.dibris.mas.gui.ConfigurationGui;
import it.unige.dibris.mas.gui.GuiManager;

import java.util.concurrent.CountDownLatch;

public class Main extends Application {

    private static ContainerController container;
    private static int patientCounter = 0;
    private static CountDownLatch edInitLatch; // 5 ED Agents: Registration, Triage,
                                               // QueueManager, Doctor_1, Doctor_2

    private static TextField patientCountField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Mostra la configurazione GUI
        ConfigurationGui.showConfigurationDialog(primaryStage, () -> {
            // Quando la configurazione è completata, continua
            try {
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

            // ← CAMBIA QUI:
            Platform.runLater(() -> {
                Stage edStage = new Stage();
                edStage.setTitle("ED Multi-Agent System Simulation");
                createGUI(edStage,
                        ConfigurationGui.getNumBeds(),
                        ConfigurationGui.getNumDoctors(),
                        ConfigurationGui.getNumNurses()
                ); // Passa la nuova Stage
            });
        });
    }

    private void createGUI(Stage primaryStage, int totalBeds, int numDoctors, int numNurses) {
        GuiManager.createAndShowGUI(primaryStage, totalBeds, numDoctors, numNurses);
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

    public static void createMultiplePatients(PatientSeverity severity) {
        try {
            String countText = GuiManager.getPatientCountFieldText();

            int count = Integer.parseInt(countText);
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

    public static void createPatient(PatientSeverity severity) {
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
    public static BedManagerAgent sharedBedManager = null;

    private void initializeED() {
        try {
            // Leggi dalla configurazione
            int totalBeds = ConfigurationGui.getNumBeds();
            int numDoctors = ConfigurationGui.getNumDoctors();
            int numNurses = ConfigurationGui.getNumNurses();

            // ← AGGIUNGI QUI: Calcola il numero totale di agenti
            // Registration, Triage, QueueManager, BedManager, AutomaticSpawn + Doctors +
            // Nurses
            int totalAgents = 5 + numDoctors + numNurses;
            edInitLatch = new CountDownLatch(totalAgents);

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

            Object[] triageArgs = new Object[] { Main.sharedQueueManager };
            AgentController triageAgentController = container.createNewAgent(
                    "TriageAgent",
                    "it.unige.dibris.mas.agents.TriageAgent",
                    triageArgs);
            triageAgentController.start();
            // Crea il BedManagerAgent
            AgentController bedManagerAgentController = container.createNewAgent(
                    "BedManagerAgent",
                    "it.unige.dibris.mas.agents.BedManagerAgent",
                    null);
            bedManagerAgentController.start();

            Thread.sleep(1000);

            // Crea le Nurse (5 letti / 2 = 2-3 nurse)
            int bedsPerNurse = (int) Math.ceil((double) totalBeds / numNurses);

            for (int i = 1; i <= numNurses; i++) {
                int startBed = (i - 1) * bedsPerNurse + 1;
                int endBed = Math.min(i * bedsPerNurse, totalBeds);

                Object[] nurseArgs = new Object[] { Main.sharedBedManager, startBed, endBed };
                AgentController nurseController = container.createNewAgent(
                        "Nurse_" + i,
                        "it.unige.dibris.mas.agents.NurseAgent",
                        nurseArgs);
                nurseController.start();

                SimulationLogger.getInstance()
                        .log("[Main] Created " + "Nurse_" + i + " (beds " + startBed + "-" + endBed + ")");
            }

            // Crea i DoctorAgent
            for (int i = 1; i <= numDoctors; i++) {
                Object[] doctorArgs = new Object[] { Main.sharedQueueManager, Main.sharedBedManager };
                AgentController doctorController = container.createNewAgent(
                        "Doctor_" + i,
                        "it.unige.dibris.mas.agents.DoctorAgent",
                        doctorArgs);
                doctorController.start();
            }

            AgentController spawnAgentController = container.createNewAgent(
                    "AutomaticSpawnAgent",
                    "it.unige.dibris.mas.agents.AutomaticSpawnAgent",
                    null);
            spawnAgentController.start();

            SimulationLogger.getInstance().log("ED initialized! All ED Agents have been created");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Cambia questi metodi nel Main:

    public static void setWaitingForTriageItems(ObservableList<String> items) {
        GuiManager.setWaitingForTriageItems(items);
    }

    public static void updateWaitingForTriage(String patientId, boolean isWaiting) {
        GuiManager.updateWaitingForTriage(patientId, isWaiting);
    }

    public static void setQueueListItems(ObservableList<Map.Entry<String, TriageColor>> items) {
        GuiManager.setQueueListItems(items);
    }

    public static void updateQueueListFromManager(String queueStatus) {
        GuiManager.updateQueueListFromManager(queueStatus);
    }

    public static void updateBedUI(int bedId, String patientId, String colorName, long admissionTime) {
        GuiManager.updateBedUI(bedId, patientId, colorName, admissionTime);
    }

    public static void highlightBedForNurseCheck(int bedId, String nurseName) {
        GuiManager.highlightBedForNurseCheck(bedId, nurseName);
    }

    public static void updateDoctorStatus(String doctorName, String status) {
        GuiManager.updateDoctorStatus(doctorName, status);
    }

    public static void updateColorStats(String colorName) {
        GuiManager.updateColorStats(colorName);
    }

    public static void agentReady() {
        edInitLatch.countDown();
    }

    public static void createPatientFromSpawn(PatientSeverity severity) {
        new Thread(() -> {
            try {
                createPatient(severity);
            } catch (Exception e) {
                SimulationLogger.getInstance().log("ERROR: " + e.getMessage());
            }
        }).start();
    }

    public static void updatePatientColor(String patientId, String newColorName) {
        GuiManager.updatePatientColor(patientId, newColorName);
    }

    public static void removePatientFromColorStats(String patientId, String colorName) {
        GuiManager.removePatientFromColorStats(patientId, colorName);
    }

    public static void updateDischargeStats(String name, long l) {
        GuiManager.updateDischargeStats(name, l);
    }
}