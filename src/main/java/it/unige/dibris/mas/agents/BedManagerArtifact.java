package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.Main;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import it.unige.dibris.mas.ontology.BedInfo;
import it.unige.dibris.mas.ontology.PatientQueueEntry;

public class BedManagerArtifact extends Agent {

    private Map<Integer, BedInfo> beds = new HashMap<>(); //Map of bed
    private ReentrantReadWriteLock bedsLock = new ReentrantReadWriteLock(); //Lock for bed access
    private Map<String, Integer> patientToBedMap = new HashMap<>(); //Map to track which patient is in which bed

    private int totalBeds;

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Bed Manager Agent started");

        // Innitialize beds
        totalBeds = Main.getTotalBeds();
        for (int i = 1; i <= totalBeds; i++) {
            BedInfo bed = new BedInfo();
            bed.bedId = i;
            bed.patientId = null;
            beds.put(i, bed);
        }

        Main.sharedBedManager = this; // Let other agents access this instance
        Main.agentReady();
    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Bed Manager Agent shutting down");
    }

    // admit a patient to a bed, if no bed is available, discharge the best patient to make room
    public void admitPatient(String patientId, TriageColor color, PatientQueueEntry queueEntry) {
        bedsLock.writeLock().lock();
        try {
            // find a free bed
            for (BedInfo bed : beds.values()) {
                if (bed.patientId == null) {
                    bed.patientId = patientId;
                    bed.color = color;
                    bed.admissionTime = System.currentTimeMillis();
                    bed.queueEntry = queueEntry;  

                    patientToBedMap.put(patientId, bed.bedId);
                    Main.updateBedUI(bed.bedId, patientId, color.name(), bed.admissionTime);

                    SimulationLogger.getInstance().log("[BedManager] " + patientId + " admitted to bed " + bed.bedId
                            + " (color: " + color.getLabel() + ")");
                    return;
                }
            }

            // discharge the best patient to make room
            BedInfo bestBed = findBestPatientToDischarge();
            if (bestBed != null) {
                SimulationLogger.getInstance().log("[BedManager] Discharging " + bestBed.patientId + " from bed "
                        + bestBed.bedId + " to make room");

                String currentPatientId = bestBed.patientId;
                TriageColor currentPatientColor = bestBed.color;
                TriageColor entryColor = bestBed.queueEntry.entryColor;
                Long patientArrivalTime = bestBed.queueEntry.arrivalTime;
                Main.removePatientFromColorStats(currentPatientId, currentPatientColor.name()); 
                Main.updateDischargeStats(entryColor.name(), (System.currentTimeMillis() - patientArrivalTime) / 1000);


                Main.updateBedUI(bestBed.bedId, null, null, 0);


                bestBed.patientId = null;
                bestBed.color = null;
                bestBed.admissionTime = 0;

                admitPatient(patientId, color, queueEntry);
            }
        } finally {
            bedsLock.writeLock().unlock();
        }
    }

    public Integer getPatientBedId(String patientId) {
        bedsLock.readLock().lock();
        try {
            return patientToBedMap.get(patientId);
        } finally {
            bedsLock.readLock().unlock();
        }
    }

    // find the best patient to discharge based on color and admission time
    public BedInfo findBestPatientToDischarge() {
        BedInfo best = null;

        for (BedInfo bed : beds.values()) {
            if (bed.patientId != null) {
                if (best == null || isBetterToDischarge(bed, best)) {
                    best = bed;
                }
            }
        }

        return best;
    }

    // compare two beds
    private boolean isBetterToDischarge(BedInfo bed1, BedInfo bed2) {
        int color1 = bed1.color.ordinal();
        int color2 = bed2.color.ordinal();

        // First look at color
        if (color1 != color2) {
            return color1 < color2;
        }
        // If color is the same, look at admission time (discharge the one that has been admitted longer)
        return bed1.admissionTime < bed2.admissionTime;
    }

    
    public BedInfo getBedInfo(int bedId) {
    bedsLock.readLock().lock();
    try {
        BedInfo bed = beds.get(bedId);
        if (bed == null || bed.patientId == null) {
            return null;
        }
        // Return a copy of the BedInfo to avoid exposing internal state
        return new BedInfo(bed.bedId, bed.patientId, bed.color, bed.admissionTime, bed.queueEntry);
    } finally {
        bedsLock.readLock().unlock();
    }
}

    // discharge a patient from a bed (called when a doctor takes a patient for treatment)
    public void dischargePatientFromBed(int bedId) {
        bedsLock.writeLock().lock();
        try {
            BedInfo bed = beds.get(bedId);
            if (bed != null && bed.patientId != null) {
                SimulationLogger.getInstance().log("[BedManager] Discharging " + bed.patientId + " from bed " + bedId);

                it.unige.dibris.mas.Main.updateBedUI(bedId, null, null, 0);

                bed.patientId = null;
                bed.color = null;
                bed.admissionTime = 0;
            }
        } finally {
            bedsLock.writeLock().unlock();
        }
    }

    // get the queue manager agent
    public QueueManagerArtifact getQueueManager() {
        return it.unige.dibris.mas.Main.sharedQueueManager;
    }
}