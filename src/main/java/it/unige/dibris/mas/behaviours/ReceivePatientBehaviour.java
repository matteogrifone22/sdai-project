package it.unige.dibris.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.agents.DoctorAgent;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.Main;
import it.unige.dibris.mas.agents.BedManagerAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientQueueEntry;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.Map;
import java.util.Random;

public class ReceivePatientBehaviour extends CyclicBehaviour {

    // This behaviour is responsible for receiving patients from the queue manager,
    // treating them, and then either discharging them or admitting them to a bed based on
    // their improvement and some randomness for green/blue patients

    private long treatmentStartTime = 0;
    private long TREATMENT_DURATION = 20000;
    private QueueManagerAgent queueManager;
    private BedManagerAgent bedManager;
    private Random random = new Random();
    private TriageColor currentPatientColor = null;
    private TriageColor entryColor = null;
    private Long patientArrivalTime = null;
    private PatientQueueEntry currentPatientEntry = null;

    public ReceivePatientBehaviour(Agent agent, QueueManagerAgent queueManager, BedManagerAgent bedManager) {
        this.queueManager = queueManager;
        this.bedManager = bedManager;
    }

    @Override
    public void action() {
        DoctorAgent doctorAgent = (DoctorAgent) myAgent;

        // if not treating anyone, get next patient from queue manager
        if (doctorAgent.getCurrentPatientId() == null) {
            Map.Entry<PatientQueueEntry, TriageColor> patientEntry = queueManager.getNextPatient();

            if (patientEntry != null) {
                String patientId = patientEntry.getKey().patientId;
                TriageColor entryColor = patientEntry.getKey().entryColor;
                long ArrivalTime = patientEntry.getKey().arrivalTime;
                TriageColor currentColor = patientEntry.getValue();
                PatientQueueEntry queueEntry = patientEntry.getKey();

                this.currentPatientColor = currentColor;
                this.entryColor = entryColor;
                this.patientArrivalTime = ArrivalTime;
                this.currentPatientEntry = queueEntry;
                this.TREATMENT_DURATION = calculateTreatmentDuration(currentColor);

                // check if the patient was already in a bed (e.g., was waiting for treatment) and release it if so
                Integer bedId = bedManager.getPatientBedId(patientId);

                if (bedId != null) {
                    // free the bed before starting treatment
                    bedManager.dischargePatientFromBed(bedId);
                    SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Released bed " + bedId
                            + " (Patient_" + patientId + " entering treatment)");
                }

                doctorAgent.setCurrentPatientId(patientId);
                treatmentStartTime = System.currentTimeMillis();

                Main.updateDoctorStatus(myAgent.getLocalName(), patientId + "|" + currentColor.name());

                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Received patient: " + patientId
                        + " (color: " + currentColor.getLabel() + ")");
            }
        } else {
            // if treating a patient, check if treatment is done
            if (System.currentTimeMillis() - treatmentStartTime >= TREATMENT_DURATION) {
                String patientId = doctorAgent.getCurrentPatientId();
                TriageColor improvedColor = improvePatientColor(currentPatientColor);

                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Treated " + patientId
                        + ": " + currentPatientColor.getLabel() + " → " + improvedColor.getLabel());

                ACLMessage dischargeMsg = new ACLMessage(ACLMessage.INFORM);
                dischargeMsg.addReceiver(new AID(patientId, AID.ISLOCALNAME));
                dischargeMsg.setContent("DISCHARGE");
                
                // White patients are always discharged
                // Green patients have a 50% chance of being discharged, 50% chance of being admitted to bed 
                // Blue patients have a 30% chance of being discharged, 70% chance of being admitted to bed
                // Orange and Red patients are always admitted to bed (no improvement possible)
                switch (improvedColor) {
                    case WHITE:

                        SimulationLogger.getInstance()
                                .log("[" + myAgent.getLocalName() + "] " + patientId + " discharged (LOW severity)");
                        Main.removePatientFromColorStats(patientId, currentPatientColor.name());

                        it.unige.dibris.mas.Main.updateDischargeStats(entryColor.name(),
                                (System.currentTimeMillis() - patientArrivalTime) / 1000);
                        doctorAgent.send(dischargeMsg);

                        break;
                    case GREEN:
                        double rand = random.nextDouble();
                        if (rand < 0.5) {
                            SimulationLogger.getInstance()
                                    .log("[" + myAgent.getLocalName() + "] " + patientId
                                            + " discharged (MEDIUM severity)");

                            Main.removePatientFromColorStats(patientId, currentPatientColor.name());

                            it.unige.dibris.mas.Main.updateDischargeStats(entryColor.name(),
                                    (System.currentTimeMillis() - patientArrivalTime) / 1000);
                            doctorAgent.send(dischargeMsg);

                        } else {
                            bedManager.admitPatient(patientId, improvedColor, currentPatientEntry);
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

                            it.unige.dibris.mas.Main.updateDischargeStats(entryColor.name(),
                                    (System.currentTimeMillis() - patientArrivalTime) / 1000);
                            doctorAgent.send(dischargeMsg);

                        } else {
                            bedManager.admitPatient(patientId, improvedColor, currentPatientEntry);
                            Main.updatePatientColor(patientId, improvedColor.name()); 
                        }
                        break;
                    default:
                        bedManager.admitPatient(patientId, improvedColor, currentPatientEntry);
                        Main.removePatientFromColorStats(patientId, currentPatientColor.name()); 
                }

                Main.updateDoctorStatus(myAgent.getLocalName(), "FREE");

                doctorAgent.setCurrentPatientId(null);
                currentPatientColor = null;

                try {
                    // quick pause before treating next patient to simulate time taken for doctor to be ready again
                    Thread.sleep(5000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private long calculateTreatmentDuration(TriageColor color) {
        Random rand = new Random();

        // These durations are just examples and can be adjusted based on how you want the simulation to behave
        // The doctor visit includes also the time taken for an exams (like blood test, x-ray, etc.) 
        // which is why the time are increasing with the severity of the patient.
        // I assume higher color patients generally require more exams and more time for treatment
        switch (color) {
            case WHITE:
                return (10 + rand.nextInt(16)) * 1000; // 10-25 sec
            case BLUE:
                return (15 + rand.nextInt(16)) * 1000; // 15-30 sec
            case GREEN:
                return (25 + rand.nextInt(16)) * 1000; // 25-40 sec
            case ORANGE:
                return (35 + rand.nextInt(16)) * 1000; // 35-50 sec
            case RED:
                return (45 + rand.nextInt(16)) * 1000; // 45-60 sec
            default:
                return 20000; // Default 20 sec
        }
    }

    private TriageColor improvePatientColor(TriageColor color) {
        // Probability of improvement of 1 or 2 colors 
        double rand = random.nextDouble();

        if (rand < 0.6) {
            // 60% probability of improving by 1 color
            return decreaseColor(color, 1);
        } else if (rand < 0.9) {
            // 30% probability of improving by 2 colors
            return decreaseColor(color, 2);
        } else {
            // 10% probability of no improvement
            return color;
        }
    }

    private TriageColor decreaseColor(TriageColor color, int steps) {

        if (color == TriageColor.WHITE) {
            return TriageColor.WHITE; // you can't improve beyond white
        }
        // Use directly the ordinal of the enum to calculate the new color after improvement
        TriageColor[] colors = TriageColor.values(); // [WHITE, BLUE, GREEN, ORANGE, RED]
        int currentIndex = color.ordinal(); // Get the index of the current color
        int newIndex = Math.max(0, currentIndex - steps); // Calculate the new index after improvement, ensuring it doesn't go below 0

        return colors[newIndex];
    }
}