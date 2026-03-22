package it.unige.dibris.mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;
import it.unige.dibris.mas.agents.PatientAgent;

public class ReceiveTriageResultBehaviour extends CyclicBehaviour {
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        
        if (msg != null && msg.getContent().startsWith("TRIAGE_RESULT")) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 2) {
                String colorName = parts[1];
                TriageColor color = TriageColor.valueOf(colorName);
                
                ((PatientAgent) myAgent).setTriageColor(color);
                ((PatientAgent) myAgent).setEntryColor(color);

                ((PatientAgent) myAgent).logPatientInfo();
                
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Received triage result: " + color.getLabel());
            }
        }
    }
}