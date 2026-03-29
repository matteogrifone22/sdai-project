package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientQueueEntry;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class QueueManagerArtifact extends Agent {

    private LinkedList<Map.Entry<PatientQueueEntry, TriageColor>> patientQueue = new LinkedList<>();
    private ReentrantReadWriteLock queueLock = new ReentrantReadWriteLock();

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Queue Manager Agent started");

        it.unige.dibris.mas.Main.sharedQueueManager = this;

        it.unige.dibris.mas.Main.agentReady();
    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Queue Manager Agent shutting down");
    }

    // Add a patient to the queue in a thread-safe way, maintaining order based on triage color
    public void addPatient(PatientQueueEntry patient, TriageColor color) {
        queueLock.writeLock().lock();
        try {

            if (isPatientInQueue(patient.patientId)) {
                SimulationLogger.getInstance().log("[QueueManager] " + patient.patientId
                        + " is already in the queue, skipping add.");
                return;
            }
            
            Map.Entry<PatientQueueEntry, TriageColor> newEntry = new java.util.AbstractMap.SimpleEntry<>(patient, color);

        
            int insertPosition = patientQueue.size();

            for (int i = 0; i < patientQueue.size(); i++) {
                TriageColor existingColor = patientQueue.get(i).getValue();

                // Higher priority colors should be placed before lower priority ones
                if (color.getPriority() > existingColor.getPriority()) {
                    insertPosition = i;
                    break;
                }
            }

            patientQueue.add(insertPosition, newEntry);

            SimulationLogger.getInstance().log("[QueueManager] " + patient.patientId + " added to queue with color "
                    + color.getLabel() + ". Queue size: " + patientQueue.size());
            it.unige.dibris.mas.Main.updateQueueListFromManager(getQueueStatus());
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    // Remove and return the next patient from the queue in a thread-safe way (use by doctors)
    public Map.Entry<PatientQueueEntry, TriageColor> getNextPatient() {
        queueLock.writeLock().lock();
        try {
            if (!patientQueue.isEmpty()) {
                Map.Entry<PatientQueueEntry, TriageColor> patient = patientQueue.pollFirst();
                it.unige.dibris.mas.Main.updateQueueListFromManager(getQueueStatus());
                return patient;
            }
            return null;
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    
    public String getQueueStatus() {
        queueLock.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            int position = 1;
            for (Map.Entry<PatientQueueEntry, TriageColor> entry : patientQueue) {
                sb.append(position).append(". ").append(entry.getKey().patientId)
                        .append(" (").append(entry.getValue().getLabel()).append(")\n");
                position++;
            }
            return sb.toString();
        } finally {
            queueLock.readLock().unlock();
        }
    }


    public int getQueueSize() {
        queueLock.readLock().lock();
        try {
            return patientQueue.size();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    boolean isPatientInQueue(String patientId) {
        queueLock.readLock().lock();
        try {
            for (Map.Entry<PatientQueueEntry, TriageColor> entry : patientQueue) {
                if (entry.getKey().patientId.equals(patientId)) {
                    return true;
                }
            }
            return false;
        } finally {
            queueLock.readLock().unlock();
        }
    }
}