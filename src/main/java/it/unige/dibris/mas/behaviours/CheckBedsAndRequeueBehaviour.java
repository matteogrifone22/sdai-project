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
    private static final long MIN_STAY_TIME = 30000;  // 30 secondi
    private int currentBedIndex;  // Quale letto stiamo controllando
    
    public CheckBedsAndRequeueBehaviour(Agent agent, BedManagerAgent bedManager, int startBed, int endBed) {
        super(agent, 10000);  // ← CAMBIA: Ogni 10 secondi
        this.bedManager = bedManager;
        this.startBed = startBed;
        this.endBed = endBed;
        this.currentBedIndex = startBed;  // ← NUOVO
    }
    
    @Override
    protected void onTick() {
        // Controlla UN solo letto per tick
        checkBedAndRequeue(currentBedIndex);
        
        // Passa al letto successivo
        currentBedIndex++;
        if (currentBedIndex > endBed) {
            currentBedIndex = startBed;  // Ricomincia da capo
        }
    }
    
    private void checkBedAndRequeue(int bedId) {
        BedInfo bedInfo = bedManager.getBedInfo(bedId);
        
        if (bedInfo == null || bedInfo.patientId == null) {
            return;
        }
        
        long stayTime = System.currentTimeMillis() - bedInfo.admissionTime;
        
        if (stayTime >= MIN_STAY_TIME) {
            String patientId = bedInfo.patientId;
            TriageColor color = bedInfo.color;
            
            // ← MODIFICATO: Passa il nome della nurse
            it.unige.dibris.mas.Main.highlightBedForNurseCheck(bedId, myAgent.getLocalName());
            
            SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Checking patient " + patientId 
                + " in bed " + bedId + " (stay time: " + (stayTime / 1000) + "s)");
            
            bedManager.getQueueManager().addPatient(patientId, color);
            
            SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Requeued " + patientId 
                + " to priority queue with color " + color.getLabel() + " (still in bed " + bedId + ")");
        }
    }
}