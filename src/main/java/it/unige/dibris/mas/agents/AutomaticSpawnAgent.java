package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.behaviours.SpawnPatientsBehaviour;

public class AutomaticSpawnAgent extends Agent {
    
    protected void setup() {
        SimulationLogger.getInstance().log("[AutomaticSpawnAgent] Automatic Patient Spawn started");
        
        addBehaviour(new SpawnPatientsBehaviour(this));
    }
    
    protected void takeDown() {
        SimulationLogger.getInstance().log("[AutomaticSpawnAgent] Automatic spawn shutting down");
    }
}