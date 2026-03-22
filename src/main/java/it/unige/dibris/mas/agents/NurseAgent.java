package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.behaviours.CheckBedsAndRequeueBehaviour;

public class NurseAgent extends Agent {
    
    private BedManagerAgent bedManager;
    private int startBed;
    private int endBed;
    private static final long CHECK_INTERVAL = 10000;
    
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 3) {
            bedManager = (BedManagerAgent) args[0];
            startBed = (Integer) args[1];
            endBed = (Integer) args[2];
        }
        
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Nurse Agent started (beds " + startBed + "-" + endBed + ")");
        
        addBehaviour(new CheckBedsAndRequeueBehaviour(this, bedManager, startBed, endBed, CHECK_INTERVAL));
        
        it.unige.dibris.mas.Main.agentReady();
    }
    
    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Nurse Agent shutting down");
    }
}