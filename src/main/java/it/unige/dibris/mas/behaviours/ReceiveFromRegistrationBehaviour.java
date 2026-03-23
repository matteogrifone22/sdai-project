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

        if (msg != null && msg.getContent().startsWith("PATIENT_REGISTERED")) {
            // Funziona sia da RegistrationAgent che da PatientAgent
            String[] parts = msg.getContent().split("\\|");
            String patientId = parts[1];
            PatientSeverity severity = PatientSeverity.valueOf(parts[2]);

            TriageAgent triageAgent = (TriageAgent) myAgent;

            triageAgent.addWaitingPatient(patientId, severity);
            Main.updateWaitingForTriage(patientId, true);

            SimulationLogger.getInstance().log("[TriageAgent] " + patientId + " waiting for triage. Queue: "
                    + triageAgent.getWaitingPatients().size());

        }
    }
}