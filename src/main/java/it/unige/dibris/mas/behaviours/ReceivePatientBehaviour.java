package it.unige.dibris.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.agents.DoctorAgent;
import it.unige.dibris.mas.agents.PatientAgent;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.Main;
import it.unige.dibris.mas.agents.BedManagerAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.Map;
import java.util.Random;

public class ReceivePatientBehaviour extends CyclicBehaviour {

    private long treatmentStartTime = 0;
    private long TREATMENT_DURATION = 20000;
    private QueueManagerAgent queueManager;
    private BedManagerAgent bedManager;
    private Random random = new Random();
    private TriageColor currentPatientColor = null;
    private PatientAgent patientAgent; // Riferimento al paziente attuale (per statistiche)

    public ReceivePatientBehaviour(Agent agent, QueueManagerAgent queueManager, BedManagerAgent bedManager,
            long treatmentDuration) {
        this.queueManager = queueManager;
        this.bedManager = bedManager;
        this.TREATMENT_DURATION = treatmentDuration;
    }

    @Override
    public void action() {
        DoctorAgent doctorAgent = (DoctorAgent) myAgent;

        // Se il dottore non sta curando nessuno, prendi un paziente dalla coda
        if (doctorAgent.getCurrentPatientId() == null) {
            Map.Entry<String, TriageColor> patientEntry = queueManager.getNextPatient();

            if (patientEntry != null) {
                String patientId = patientEntry.getKey();
                TriageColor color = patientEntry.getValue();
                

                // ← NUOVO: Controlla se il paziente era a letto
                Integer bedId = bedManager.getPatientBedId(patientId);

                if (bedId != null) {
                    // Era a letto → libera il letto ADESSO
                    bedManager.dischargePatientFromBed(bedId);
                    SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Released bed " + bedId
                            + " (Patient_" + patientId + " entering treatment)");
                }

                doctorAgent.setCurrentPatientId(patientId);
                currentPatientColor = color;
                treatmentStartTime = System.currentTimeMillis();

                Main.updateDoctorStatus(myAgent.getLocalName(), patientId + "|" + color.name());

                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Received patient: " + patientId
                        + " (color: " + color.getLabel() + ")");
            }
        } else {
            // Se sta curando, controlla se è finito
            if (System.currentTimeMillis() - treatmentStartTime >= TREATMENT_DURATION) {
                String patientId = doctorAgent.getCurrentPatientId();
                TriageColor improvedColor = improvePatientColor(currentPatientColor);

                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Treated " + patientId
                        + ": " + currentPatientColor.getLabel() + " → " + improvedColor.getLabel());

                ACLMessage dischargeMsg = new ACLMessage(ACLMessage.INFORM);
                dischargeMsg.addReceiver(new AID(patientId, AID.ISLOCALNAME));
                dischargeMsg.setContent("DISCHARGE");
                // White dimesso, Green e Blue hanno probabilità di essere dimessi o migliorare,
                // gli altri migliorano sempre
                switch (improvedColor) {
                    case WHITE:
                        // Paziente dimesso

                        doctorAgent.send(dischargeMsg);

                        SimulationLogger.getInstance()
                                .log("[" + myAgent.getLocalName() + "] " + patientId + " discharged (LOW severity)");
                        Main.removePatientFromColorStats(patientId, currentPatientColor.name());


                        break;
                    case GREEN:
                        double rand = random.nextDouble();
                        if (rand < 0.5) {
                            SimulationLogger.getInstance()
                                    .log("[" + myAgent.getLocalName() + "] " + patientId
                                            + " discharged (MEDIUM severity)");

                            Main.removePatientFromColorStats(patientId, currentPatientColor.name());
                            doctorAgent.send(dischargeMsg);
                        } else {
                            bedManager.admitPatient(patientId, improvedColor);
                            Main.updatePatientColor(patientId, improvedColor.name()); // Aggiorna al nuovo
                        }
                        break;

                    case BLUE:
                        double randBlue = random.nextDouble();
                        if (randBlue < 0.3) {
                            SimulationLogger.getInstance()
                                    .log("[" + myAgent.getLocalName() + "] " + patientId
                                            + " discharged (HIGH severity)");
                            Main.removePatientFromColorStats(patientId, currentPatientColor.name());
                            doctorAgent.send(dischargeMsg);
                        } else {
                            bedManager.admitPatient(patientId, improvedColor);
                            Main.updatePatientColor(patientId, improvedColor.name()); // Aggiorna al nuovo
                        }
                        break;
                    default:
                        bedManager.admitPatient(patientId, improvedColor);
                        Main.removePatientFromColorStats(patientId, currentPatientColor.name()); // Decrementa ilvecchio
                        Main.updatePatientColor(patientId, improvedColor.name()); // Incrementa il nuovo
                }

                Main.updateDoctorStatus(myAgent.getLocalName(), "FREE");

                doctorAgent.setCurrentPatientId(null);
                currentPatientColor = null;

                try {
                    Thread.sleep(6000); // 6 secondi (3 min reali)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private TriageColor improvePatientColor(TriageColor color) {
        // Probabilità di migliorare di 1 o 2 colori
        double rand = random.nextDouble();

        if (rand < 0.6) {
            // 60% probabilità di migliorare di 1 colore
            return decreaseColor(color, 1);
        } else if (rand < 0.9) {
            // 30% probabilità di migliorare di 2 colori
            return decreaseColor(color, 2);
        } else {
            // 10% probabilità di non migliorare
            return color;
        }
    }

    private TriageColor decreaseColor(TriageColor color, int steps) {

        if (color == TriageColor.WHITE) {
            return TriageColor.WHITE; // Non può migliorare
        }
        // Usa direttamente l'ordine dell'enum
        TriageColor[] colors = TriageColor.values(); // [WHITE, BLUE, GREEN, ORANGE, RED]
        int currentIndex = color.ordinal(); // Posizione nell'enum
        int newIndex = Math.max(0, currentIndex - steps); // Non andare sotto WHITE

        return colors[newIndex];
    }
}