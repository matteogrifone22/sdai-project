package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import it.unige.dibris.mas.agents.DoctorAgent;
import it.unige.dibris.mas.agents.QueueManagerAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import java.util.Map;

public class ReceivePatientFromQueueBehaviour extends CyclicBehaviour {

    private long treatmentStartTime = 0;
    private static final long TREATMENT_DURATION = 20000;
    private QueueManagerAgent queueManager;  // ← NUOVO

    public ReceivePatientFromQueueBehaviour(Agent agent, QueueManagerAgent queueManager) {  // ← MODIFICATO
        this.queueManager = queueManager;  // ← NUOVO
    }

    @Override
    public void action() {
        DoctorAgent doctorAgent = (DoctorAgent) myAgent;
        
        // Se il dottore non sta curando nessuno, prendi un paziente dalla coda
        if (doctorAgent.getCurrentPatientId() == null) {
            // NUOVO: Prendi il paziente direttamente dalla coda del QueueManager
            Map.Entry<String, ?> patientEntry = queueManager.getNextPatient();
            
            if (patientEntry != null) {
                String patientId = patientEntry.getKey();
                
                doctorAgent.setCurrentPatientId(patientId);
                treatmentStartTime = System.currentTimeMillis();
                
                it.unige.dibris.mas.Main.updateDoctorStatus(
                    myAgent.getLocalName(), 
                    "Treating: " + patientId);
                
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Received patient: " + patientId);
            }
        } else {
            // Se sta curando, controlla se è finito
            if (System.currentTimeMillis() - treatmentStartTime >= TREATMENT_DURATION) {
                String patientId = doctorAgent.getCurrentPatientId();
                
                it.unige.dibris.mas.Main.updateDoctorStatus(
                    myAgent.getLocalName(), 
                    "FREE");
                
                SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Finished treating: " + patientId);
                doctorAgent.setCurrentPatientId(null);
            }
        }
    }
}