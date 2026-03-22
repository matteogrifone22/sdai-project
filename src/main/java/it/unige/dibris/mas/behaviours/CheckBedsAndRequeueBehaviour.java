package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.agents.BedManagerAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.BedInfo;
import it.unige.dibris.mas.ontology.TriageColor;

public class CheckBedsAndRequeueBehaviour extends TickerBehaviour {

    private BedManagerAgent bedManager;
    private int startBed;
    private int endBed;
    private int currentBedIndex;

    public CheckBedsAndRequeueBehaviour(Agent agent, BedManagerAgent bedManager, int startBed, int endBed, long checkInterval) {
        super(agent, checkInterval);
        this.bedManager = bedManager;
        this.startBed = startBed;
        this.endBed = endBed;
        this.currentBedIndex = startBed; // ← NUOVO
    }

    @Override
    protected void onTick() {
        // Controlla UN solo letto per tick
        checkBedAndRequeue(currentBedIndex);

        // Passa al letto successivo
        currentBedIndex++;
        if (currentBedIndex > endBed) {
            currentBedIndex = startBed; // Ricomincia da capo
        }
    }

    private void checkBedAndRequeue(int bedId) {
    BedInfo bed = bedManager.getBedInfo(bedId);
    
    if (bed != null && bed.patientId != null) {
        long stayTime = (System.currentTimeMillis() - bed.admissionTime) / 1000;
        
        SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Checking patient " 
            + bed.patientId + " in bed " + bedId + " (stay time: " + stayTime + "s)");

        it.unige.dibris.mas.Main.highlightBedForNurseCheck(bedId, myAgent.getLocalName());

        
        // Rimetti in coda
        if (bed.queueEntry != null) {  // ← CONTROLLA SE ESISTE
            bedManager.getQueueManager().addPatient(bed.queueEntry, bed.color);  // ← USA queueEntry da BedInfo
            SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Requeued " + bed.patientId 
                + " to priority queue with color " + bed.color.getLabel() + " (still in bed " + bedId + ")");
        } else {
            SimulationLogger.getInstance().log("[ERROR] " + bed.patientId + " has no queueEntry!");
        }
    }
}
}