package it.unige.dibris.mas.behaviours;

import jade.lang.acl.ACLMessage;
import jade.core.AID;
import it.unige.dibris.mas.agents.RegistrationAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;

import java.util.Queue;
import java.util.Map;

public class ProcessRegistrationQueueBehaviour extends ProcessQueueBehaviour {

    // This behaviour processes the registration queue, registers patients, and sends them to triage
    

    public ProcessRegistrationQueueBehaviour(RegistrationAgent agent, long processingTime) {
        super(agent, processingTime);
    }

    @Override
    protected void processPatient(String patientId, ACLMessage originalMsg) {
        RegistrationAgent regAgent = (RegistrationAgent) myAgent;

        regAgent.addRegisteredPatient(patientId);
        SimulationLogger.getInstance().log("[RegistrationAgent] Registered: " + patientId);
        SimulationLogger.getInstance().log("[RegistrationAgent] Queue size: " + regAgent.getPatientQueue().size());

        // Reply to the original sender to confirm registration
        ACLMessage reply = originalMsg.createReply();
        reply.setContent("REGISTERED");
        myAgent.send(reply);

        String content = originalMsg.getContent();
        String[] parts = content.split("\\|");
        PatientSeverity severity = parts.length > 1 ? PatientSeverity.valueOf(parts[1]) : PatientSeverity.MEDIUM;

        // Send to triage agent with the patient ID and severity
        ACLMessage triageMsg = new ACLMessage(ACLMessage.INFORM);
        triageMsg.addReceiver(new AID("TriageAgent", AID.ISLOCALNAME));
        triageMsg.setContent("PATIENT_REGISTERED|" + patientId + "|" + severity);
        myAgent.send(triageMsg);

        SimulationLogger.getInstance().log("[RegistrationAgent] Sent " + patientId + " to Triage");

    }

    @Override
    protected Queue<Map.Entry<String, ACLMessage>> getQueue() {
        return ((RegistrationAgent) myAgent).getPatientQueue();
    }
}