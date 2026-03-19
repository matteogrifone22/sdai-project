package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import it.unige.dibris.mas.agents.DoctorAgent;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.agents.BedManagerAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.Map;
import java.util.Random;

public class ReceivePatientBehaviour extends CyclicBehaviour {

    private long treatmentStartTime = 0;
    private static final long TREATMENT_DURATION = 20000;
    private QueueManagerAgent queueManager;
    private BedManagerAgent bedManager;
    private Random random = new Random();
    private TriageColor currentPatientColor = null;

    public ReceivePatientBehaviour(Agent agent, QueueManagerAgent queueManager, BedManagerAgent bedManager) {
        this.queueManager = queueManager;
        this.bedManager = bedManager;
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
                doctorAgent.setCurrentPatientId(patientId);
                currentPatientColor = color; 
                treatmentStartTime = System.currentTimeMillis();

                it.unige.dibris.mas.Main.updateDoctorStatus(
                        myAgent.getLocalName(),
                        "Treating: " + patientId);

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

                bedManager.admitPatient(patientId, improvedColor);

                it.unige.dibris.mas.Main.updateDoctorStatus(
                        myAgent.getLocalName(),
                        "FREE");

                doctorAgent.setCurrentPatientId(null);
                currentPatientColor = null; // Reset
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
        // Usa direttamente l'ordine dell'enum
        TriageColor[] colors = TriageColor.values(); // [WHITE, BLUE, GREEN, ORANGE, RED]
        int currentIndex = color.ordinal(); // Posizione nell'enum
        int newIndex = Math.max(0, currentIndex - steps); // Non andare sotto WHITE

        return colors[newIndex];
    }
}