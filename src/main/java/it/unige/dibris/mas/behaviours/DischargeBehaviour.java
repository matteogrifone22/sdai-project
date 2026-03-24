package it.unige.dibris.mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.gui.SimulationLogger;

public class DischargeBehaviour extends CyclicBehaviour {

    // This behaviour listens for a "DISCHARGE" message to terminate the patient agent
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        
        if (msg != null && msg.getContent().equals("DISCHARGE")) {
            
            SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Discharged");
            
            myAgent.doDelete();
        }
    }
}