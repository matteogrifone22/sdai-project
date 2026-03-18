package it.unige.dibris.mas.behaviours;

import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendRegistrationBehaviour extends OneShotBehaviour {
    public void action() {

        SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Going to registration...");
        PatientSeverity severity = ((it.unige.dibris.mas.agents.PatientAgent) myAgent).getSeverity();

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("RegistrationAgent", AID.ISLOCALNAME));
        msg.setContent("REGISTER|" + severity.name());  
        myAgent.send(msg);

        // Loop finché non ricevi "REGISTERED"
        boolean registered = false;
        while (!registered) {
            ACLMessage reply = myAgent.blockingReceive();

            if (reply.getContent().equals("WAIT")) {
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Waiting in queue...");
                // Continua il loop, aspetta il prossimo messaggio

            } else if (reply.getContent().equals("REGISTERED")) {
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Sono stato registrato");
                ((it.unige.dibris.mas.agents.PatientAgent) myAgent).setRegistered(true);
                registered = true; // ← Esci dal loop
            }
        }

    }

}
