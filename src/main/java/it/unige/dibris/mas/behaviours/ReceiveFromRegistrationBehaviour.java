package it.unige.dibris.mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.agents.TriageAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.Main;

public class ReceiveFromRegistrationBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();

        if (msg != null && msg.getSender().getLocalName().equals("RegistrationAgent")) {
            String content = msg.getContent();
            String[] parts = content.split("\\|");
            if (parts.length >= 2 && parts[0].equals("PATIENT_REGISTERED")) {
                String patientId = parts[1];
                PatientSeverity severity = parts.length > 2 ? PatientSeverity.valueOf(parts[2]) : PatientSeverity.MEDIUM;
                TriageAgent triageAgent = (TriageAgent) myAgent;

                triageAgent.addWaitingPatient(patientId, severity);
                Main.updateWaitingForTriage(patientId, true);

                SimulationLogger.getInstance().log("[TriageAgent] " + patientId + " waiting for triage. Queue: "
                        + triageAgent.getWaitingPatients().size());
            }
        }
    }
}