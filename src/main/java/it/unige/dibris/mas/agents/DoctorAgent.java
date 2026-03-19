package it.unige.dibris.mas.agents;

import it.unige.dibris.mas.Main;
import it.unige.dibris.mas.behaviours.ReceivePatientBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import jade.core.Agent;

public class DoctorAgent extends Agent {

    private String currentPatientId;

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Doctor Agent started");

        Main.updateDoctorStatus(getLocalName(), "FREE");

        // Aggiungi comportamenti per gestire i pazienti (da implementare)
        addBehaviour(new ReceivePatientBehaviour(this, Main.sharedQueueManager, Main.sharedBedManager));
        Main.agentReady();
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
