package it.unige.dibris.mas.agents;

import it.unige.dibris.mas.behaviours.ReceivePatientFromQueueBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import jade.core.Agent;

public class DoctorAgent extends Agent {

    private String currentPatientId;

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Doctor Agent started");

        it.unige.dibris.mas.Main.updateDoctorStatus(getLocalName(), "FREE");

        // Aggiungi comportamenti per gestire i pazienti (da implementare)
        addBehaviour(new ReceivePatientFromQueueBehaviour(this, it.unige.dibris.mas.Main.sharedQueueManager));
        it.unige.dibris.mas.Main.agentReady();
    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Doctor Agent shutting down");
    }

    public String getCurrentPatientId() {
        return currentPatientId;
    }

    public void setCurrentPatientId(String currentPatientId) {
        this.currentPatientId = currentPatientId;
    }
    
}
