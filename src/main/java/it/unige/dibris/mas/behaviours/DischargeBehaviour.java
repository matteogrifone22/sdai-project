package it.unige.dibris.mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.agents.PatientAgent;

public class DischargeBehaviour extends CyclicBehaviour {

    private boolean done = false;

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();

        if (msg != null && msg.getContent().equals("DISCHARGE")) {
            PatientAgent patient = (PatientAgent) myAgent;

            long treatmentTime = System.currentTimeMillis() - patient.getArrivalTime();

            SimulationLogger.getInstance()
                    .log("[" + myAgent.getLocalName() + "] Discharged! Total time: " + (treatmentTime / 1000) + "s");

            
            // Aggiorna le stats
            if (patient.getEntryColor() != null) {
                it.unige.dibris.mas.Main.updateDischargeStats(patient.getEntryColor().name(), treatmentTime / 1000);
                SimulationLogger.getInstance()
                        .log("[DischargeBehaviour] Called updateDischargeStats with " + patient.getEntryColor().name());
            } else {
                SimulationLogger.getInstance().log("[DischargeBehaviour ERROR] entryColor is null!");
            }

            done = true;
            // ← RIMUOVI: myAgent.doDelete();
        } else if (msg == null) {
            block(); // Aspetta un messaggio
        }
    }

    @Override
    public int onEnd() {
        myAgent.doDelete(); // ← ELIMINALO QUI, dopo che done() ritorna true
        return super.onEnd();
    }

}