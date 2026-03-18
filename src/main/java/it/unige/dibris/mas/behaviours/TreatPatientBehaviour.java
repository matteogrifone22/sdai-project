package it.unige.dibris.mas.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.agents.DoctorAgent;
import it.unige.dibris.mas.gui.SimulationLogger;

public class TreatPatientBehaviour extends CyclicBehaviour {

    private long treatmentStartTime = 0;
    private static final long TREATMENT_DURATION = 20000; // 20 secondi

    @Override
    public void action() {
        DoctorAgent doctorAgent = (DoctorAgent) myAgent;

        // Se il dottore non sta curando nessuno, ricevi il paziente
        if (doctorAgent.getCurrentPatientId() == null) {
            ACLMessage msg = myAgent.receive();

            if (msg != null && msg.getContent().startsWith("PATIENT_ASSIGNED")) {
                String[] parts = msg.getContent().split("\\|");
                if (parts.length >= 2) {
                    String patientId = parts[1];

                    doctorAgent.setCurrentPatientId(patientId);
                    treatmentStartTime = System.currentTimeMillis();

                    it.unige.dibris.mas.Main.updateDoctorStatus(
                            myAgent.getLocalName(),
                            "Treating: " + patientId);

                    SimulationLogger.getInstance()
                            .log("[" + myAgent.getLocalName() + "] Received patient: " + patientId);
                    it.unige.dibris.mas.Main.updatePatientsInTreatment(myAgent.getLocalName(), patientId, true);
                }
            }
        } else {
            // Se sta curando, controlla se è finito
            if (System.currentTimeMillis() - treatmentStartTime >= TREATMENT_DURATION) {
                String patientId = doctorAgent.getCurrentPatientId();

                it.unige.dibris.mas.Main.updateDoctorStatus(
                        myAgent.getLocalName(),
                        "FREE");

                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Finished treating: " + patientId);
                it.unige.dibris.mas.Main.updatePatientsInTreatment(myAgent.getLocalName(), patientId, false);

                doctorAgent.setCurrentPatientId(null);
            }
        }
    }
}