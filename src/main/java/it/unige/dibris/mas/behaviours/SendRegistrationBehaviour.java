package it.unige.dibris.mas.behaviours;

import it.unige.dibris.mas.agents.PatientAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendRegistrationBehaviour extends OneShotBehaviour {

    // This behaviour is responsible for sending the registration request to the RegistrationAgent and waiting for the response.
    // If the patient arrived by ambulance, it will skip registration and directly inform the TriageAgent.

    @Override
    public void action() {

        PatientAgent patient = (PatientAgent) myAgent;

        SimulationLogger.getInstance()
                .log("[DEBUG] " + patient.getPatientId() + " arrivedByAmbulance: " + patient.isArrivedByAmbulance());
        if (((PatientAgent) myAgent).isArrivedByAmbulance()) {
            ACLMessage triageMsg = new ACLMessage(ACLMessage.INFORM);
            triageMsg.addReceiver(new AID("TriageAgent", AID.ISLOCALNAME));
            triageMsg.setContent("PATIENT_REGISTERED|" + patient.getPatientId() + "|" + patient.getSeverity().name());
            patient.send(triageMsg);
            SimulationLogger.getInstance().log("[DEBUG] " + patient.getPatientId() + " skipping registration (ambulance)");
            return;
        }

        SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Going to registration...");
        PatientSeverity severity = ((it.unige.dibris.mas.agents.PatientAgent) myAgent).getSeverity();

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("RegistrationAgent", AID.ISLOCALNAME));
        msg.setContent("REGISTER|" + severity.name());
        myAgent.send(msg);

        // Loop while not "REGISTERED"
        boolean registered = false;
        while (!registered) {
            ACLMessage reply = myAgent.blockingReceive();

            if (reply.getContent().equals("WAIT")) {
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Waiting in queue...");

            } else if (reply.getContent().equals("REGISTERED")) {
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Sono stato registrato");
                ((it.unige.dibris.mas.agents.PatientAgent) myAgent).setRegistered(true);
                registered = true; // Exit the loop once registered
            }
        }

    }

}
