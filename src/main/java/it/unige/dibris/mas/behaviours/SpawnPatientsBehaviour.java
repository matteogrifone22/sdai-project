package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import java.util.Random;

public class SpawnPatientsBehaviour extends TickerBehaviour {
    
    private Random random = new Random();
    private static final long SPAWN_INTERVAL = 10000;  // 10 secondi (5 min reali)
    
    public SpawnPatientsBehaviour(Agent agent) {
        super(agent, SPAWN_INTERVAL);
    }
    
    @Override
    protected void onTick() {
        // Genera una severity random
        PatientSeverity severity = getRandomSeverity();
        
        // Chiama il metodo del Main per creare il paziente
        it.unige.dibris.mas.Main.createPatientFromSpawn(severity);
    }
    
    private PatientSeverity getRandomSeverity() {
        double rand = random.nextDouble();
        
        // 50% LOW, 30% MEDIUM, 20% HIGH
        if (rand < 0.5) {
            return PatientSeverity.LOW;
        } else if (rand < 0.8) {
            return PatientSeverity.MEDIUM;
        } else {
            return PatientSeverity.HIGH;
        }
    }
}