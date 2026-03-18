package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.behaviours.ReceiveFromRegistrationBehaviour;
import it.unige.dibris.mas.behaviours.ProcessTriageQueueBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;

public class TriageAgent extends Agent {
    
    private Queue<Map.Entry<String, PatientSeverity>> waitingPatients = new LinkedList<>();
    private QueueManagerAgent queueManager;  // ← NUOVO
    
    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Triage Agent started");
        
        // NUOVO: Ricevi il QueueManager dagli argomenti
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            queueManager = (QueueManagerAgent) args[0];
        }
        
        addBehaviour(new ReceiveFromRegistrationBehaviour());
        addBehaviour(new ProcessTriageQueueBehaviour(this, queueManager));  // ← Passa il QueueManager
        
        it.unige.dibris.mas.Main.agentReady();
    }
    
    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Triage Agent shutting down");
    }
    
    public Queue<Map.Entry<String, PatientSeverity>> getWaitingPatients() {
        return waitingPatients;
    }
    
    public void addWaitingPatient(String patientId, PatientSeverity severity) {
        waitingPatients.add(new java.util.AbstractMap.SimpleEntry<>(patientId, severity));
    }
    
    public Map.Entry<String, PatientSeverity> getNextPatient() {
        return waitingPatients.poll();
    }
}