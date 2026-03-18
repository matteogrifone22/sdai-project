package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class QueueManagerAgent extends Agent {

    private LinkedList<Map.Entry<String, TriageColor>> patientQueue = new LinkedList<>();
    private ReentrantReadWriteLock queueLock = new ReentrantReadWriteLock();

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Queue Manager Agent started");

        it.unige.dibris.mas.Main.sharedQueueManager = this;

        it.unige.dibris.mas.Main.agentReady();
    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Queue Manager Agent shutting down");
    }

    // Thread-safe: aggiungi paziente alla coda
    public void addPatient(String patientId, TriageColor color) {
        queueLock.writeLock().lock();
        try {
            patientQueue.add(new java.util.AbstractMap.SimpleEntry<>(patientId, color));
            SimulationLogger.getInstance().log("[QueueManager] " + patientId + " added to queue with color "
                    + color.getLabel() + ". Queue size: " + patientQueue.size());
            it.unige.dibris.mas.Main.updateQueueListFromManager(getQueueStatus());
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    // Thread-safe: prendi il primo paziente dalla coda (usato dai dottori)
    public Map.Entry<String, TriageColor> getNextPatient() {
        queueLock.writeLock().lock();
        try {
            if (!patientQueue.isEmpty()) {
                Map.Entry<String, TriageColor> patient = patientQueue.pollFirst();
                it.unige.dibris.mas.Main.updateQueueListFromManager(getQueueStatus());
                return patient;
            }
            return null;
        } finally {
            queueLock.writeLock().unlock();
        }
    }

    // Thread-safe: leggi lo status della coda (senza modificarla)
    public String getQueueStatus() {
        queueLock.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            int position = 1;
            for (Map.Entry<String, TriageColor> entry : patientQueue) {
                sb.append(position).append(". ").append(entry.getKey())
                        .append(" (").append(entry.getValue().getLabel()).append(")\n");
                position++;
            }
            return sb.toString();
        } finally {
            queueLock.readLock().unlock();
        }
    }

    // Thread-safe: ottieni la dimensione della coda
    public int getQueueSize() {
        queueLock.readLock().lock();
        try {
            return patientQueue.size();
        } finally {
            queueLock.readLock().unlock();
        }
    }
}