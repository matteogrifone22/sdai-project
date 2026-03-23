package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.gui.SimulationLogger;

public class AmbulanceAgent extends Agent {

    private boolean isAvailable = true;

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Ambulance Agent started");
        it.unige.dibris.mas.Main.sharedAmbulances.add(this);
        it.unige.dibris.mas.Main.agentReady();
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;

        int ambulanceId = Integer.parseInt(getLocalName().split("_")[1]);
        it.unige.dibris.mas.Main.updateAmbulanceStatus(ambulanceId, available);
    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Ambulance Agent shutting down");
    }
}