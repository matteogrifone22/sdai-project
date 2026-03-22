package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.agents.TriageAgent;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.ontology.TriageColor;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.Main;

import java.util.Map;
import java.util.Random;

public class ProcessTriageQueueBehaviour extends TickerBehaviour {

    private Random random = new Random();
    private String currentPatientId = null;
    private PatientSeverity currentPatientSeverity = null;
    private long triageStartTime = 0;
    private long TRIAGE_DURATION = 5000;
    private QueueManagerAgent queueManager;  // ← NUOVO

    public ProcessTriageQueueBehaviour(Agent agent, QueueManagerAgent queueManager, long triageDuration) {  // ← MODIFICATO
        super(agent, 1000);
        this.queueManager = queueManager;  // ← NUOVO
        this.TRIAGE_DURATION = triageDuration;
    }

    @Override
    protected void onTick() {
        TriageAgent triageAgent = (TriageAgent) myAgent;

        if (currentPatientId == null) {
            if (!triageAgent.getWaitingPatients().isEmpty()) {
                Map.Entry<String, PatientSeverity> patientEntry = triageAgent.getNextPatient();

                currentPatientId = patientEntry.getKey();
                currentPatientSeverity = patientEntry.getValue();
                triageStartTime = System.currentTimeMillis();
                it.unige.dibris.mas.Main.updateWaitingForTriage(currentPatientId, false);

                SimulationLogger.getInstance().log("[TriageAgent] Starting triage for " + currentPatientId + " with severity " + currentPatientSeverity);
            }
        } else {
            if (System.currentTimeMillis() - triageStartTime >= TRIAGE_DURATION) {
                PatientSeverity severity = currentPatientSeverity;
                TriageColor color = assignColor(severity);
               

                SimulationLogger.getInstance()
                        .log("[TriageAgent] Triaging " + currentPatientId + " → " + color.getLabel());

                // Manda il colore al paziente
                jade.lang.acl.ACLMessage patientMsg = new jade.lang.acl.ACLMessage(jade.lang.acl.ACLMessage.INFORM);
                patientMsg.addReceiver(new jade.core.AID(currentPatientId, jade.core.AID.ISLOCALNAME));
                patientMsg.setContent("TRIAGE_RESULT|" + color.name());
                myAgent.send(patientMsg);

                // ← NUOVO: Aggiungi il paziente direttamente alla coda del QueueManager
                queueManager.addPatient(new it.unige.dibris.mas.ontology.PatientQueueEntry(currentPatientId, color, System.currentTimeMillis()), color);

                Main.updatePatientColor(currentPatientId, color.name());

                currentPatientId = null;
            }
        }
    }

    private TriageColor assignColor(PatientSeverity severity) {
        switch (severity) {
            case LOW:
                double rand = random.nextDouble();
                if (rand < 0.5) {
                    return TriageColor.WHITE;
                } else if (rand < 0.8) {
                    return TriageColor.GREEN;
                } else {
                    return TriageColor.BLUE;
                }
            case MEDIUM:
                return (random.nextDouble() < 0.6) ? TriageColor.BLUE : TriageColor.ORANGE;
            case HIGH:
                return (random.nextDouble() < 0.6) ? TriageColor.ORANGE : TriageColor.RED;
            default:
                return TriageColor.GREEN;
        }
    }
}