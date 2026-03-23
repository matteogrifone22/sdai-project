package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.gui.SimulationLogger;
import java.util.Random;

public class SpawnPatientsBehaviour extends TickerBehaviour {
    
    private Random random = new Random();
    private static final long MAX_SPAWN_INTERVAL = 20000;  // Max 1 secondo
    private long nextSpawnTime;
    
    public SpawnPatientsBehaviour(Agent agent) {
        super(agent, 500); 
        this.nextSpawnTime = System.currentTimeMillis() + random.nextLong(MAX_SPAWN_INTERVAL + 1);
    }
    
    @Override
    protected void onTick() {
        long now = System.currentTimeMillis();
        
        if (now >= nextSpawnTime) {
            SimulationLogger.getInstance().log("[AutomaticSpawn] SPAWNING patient...");
            
            PatientSeverity severity = getRandomSeverity();
            it.unige.dibris.mas.Main.createPatientFromSpawn(severity);
            
            // Calcola il prossimo spawn
            nextSpawnTime = now + random.nextLong(MAX_SPAWN_INTERVAL + 1);
            SimulationLogger.getInstance().log("[AutomaticSpawn] Next spawn in " + (nextSpawnTime - now) + "ms");
        }
    }
    
    private PatientSeverity getRandomSeverity() {
        double rand = random.nextDouble();
        
        if (rand < 0.5) {
            return PatientSeverity.LOW;
        } else if (rand < 0.8) {
            return PatientSeverity.MEDIUM;
        } else {
            return PatientSeverity.HIGH;
        }
    }
}