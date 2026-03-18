package it.unige.dibris.mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;

public class ReceiveTriageResultBehaviour extends CyclicBehaviour {
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        
        if (msg != null && msg.getContent().startsWith("TRIAGE_RESULT")) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 2) {
                String colorName = parts[1];
                TriageColor color = TriageColor.valueOf(colorName);
                
                ((it.unige.dibris.mas.agents.PatientAgent) myAgent).setTriageColor(color);
                ((it.unige.dibris.mas.agents.PatientAgent) myAgent).logPatientInfo();
                
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Received triage result: " + color.getLabel());
            }
        }
    }
}