package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.agents.AmbulanceAgent;
import it.unige.dibris.mas.gui.SimulationLogger;
import java.util.Random;

public class SpawnPatientsBehaviour extends TickerBehaviour {

    private Random random = new Random();
    private static final long MAX_SPAWN_INTERVAL = 20000; // Max 1 secondo
    private long nextSpawnTime;

    public SpawnPatientsBehaviour(Agent agent) {
        super(agent, 500);
        this.nextSpawnTime = System.currentTimeMillis() + random.nextLong(MAX_SPAWN_INTERVAL + 1);
    }

    @Override
    protected void onTick() {
        long now = System.currentTimeMillis();

        if (now >= nextSpawnTime) {
            PatientSeverity severity = getRandomSeverity();

            if (severity == PatientSeverity.HIGH || severity == PatientSeverity.MEDIUM) {
                AmbulanceAgent ambulance = getAvailableAmbulance();
                if (ambulance != null) {
                    spawnWithAmbulance(ambulance, severity);
                } else {
                    spawnNormal(severity);
                }
            } else {
                spawnNormal(severity);
            }

            nextSpawnTime = now + random.nextLong(MAX_SPAWN_INTERVAL + 1);
        }
    }

    private AmbulanceAgent getAvailableAmbulance() {
        for (AmbulanceAgent amb : it.unige.dibris.mas.Main.sharedAmbulances) {
            if (amb.isAvailable()) {
                return amb;
            }
        }
        return null;
    }

    private void spawnWithAmbulance(AmbulanceAgent ambulance, PatientSeverity severity) {
        ambulance.setAvailable(false);

        SimulationLogger.getInstance().log("[Ambulance] Dispatched for HIGH priority patient");

        // Crea il paziente
        it.unige.dibris.mas.Main.createPatientFromSpawn(severity, true);


        new Thread(() -> {
            try {

                Thread.sleep(5000);                
                ambulance.setAvailable(true);
                SimulationLogger.getInstance().log("[Ambulance] Returned and available");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void spawnNormal(PatientSeverity severity) {
        it.unige.dibris.mas.Main.createPatientFromSpawn(severity, false);
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