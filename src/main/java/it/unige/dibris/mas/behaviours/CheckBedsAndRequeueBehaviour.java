package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.agents.BedManagerArtifact;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.BedInfo;

public class CheckBedsAndRequeueBehaviour extends TickerBehaviour {

    //NURSE CHECK: every CheckInterval seconds check one bed,
    // if occupied requeue the patient (without removing them from the bed)
    // to give them another chance to be treated by a doctor

    private BedManagerArtifact bedManager;
    private int startBed;
    private int endBed;
    private int currentBedIndex;

    public CheckBedsAndRequeueBehaviour(Agent agent, BedManagerArtifact bedManager, int startBed, int endBed, long checkInterval) {
        super(agent, checkInterval);
        this.bedManager = bedManager;
        this.startBed = startBed;
        this.endBed = endBed;
        this.currentBedIndex = startBed;
    }

    @Override
    protected void onTick() {
        // check one bed at a time
        checkBedAndRequeue(currentBedIndex);

        // go to the next bed for the next tick
        currentBedIndex++;
        if (currentBedIndex > endBed) {
            currentBedIndex = startBed; // loop back to the first bed after reaching the last one
        }
    }

    private void checkBedAndRequeue(int bedId) {
    BedInfo bed = bedManager.getBedInfo(bedId);
    
    if (bed != null && bed.patientId != null) {
        long stayTime = (System.currentTimeMillis() - bed.admissionTime) / 1000;
        
        SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Checking patient " 
            + bed.patientId + " in bed " + bedId + " (stay time: " + stayTime + "s)");

        it.unige.dibris.mas.Main.highlightBedForNurseCheck(bedId, myAgent.getLocalName());

        
        // requeue the patient to give them another chance to be treated by a doctor
        if (bed.queueEntry != null) {  
            bedManager.getQueueManager().addPatient(bed.queueEntry, bed.color);  
            SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Requeued " + bed.patientId 
                + " to priority queue with color " + bed.color.getLabel() + " (still in bed " + bedId + ")");
        } else {
            SimulationLogger.getInstance().log("[ERROR] " + bed.patientId + " has no queueEntry!");
        }
    }
}
}