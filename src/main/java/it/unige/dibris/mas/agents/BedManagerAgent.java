package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.TriageColor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import it.unige.dibris.mas.ontology.BedInfo;
import it.unige.dibris.mas.ontology.PatientQueueEntry;

public class BedManagerAgent extends Agent {

    private Map<Integer, BedInfo> beds = new HashMap<>();
    private ReentrantReadWriteLock bedsLock = new ReentrantReadWriteLock();
    private Map<String, Integer> patientToBedMap = new HashMap<>();

    private int totalBeds = 5;

    protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Bed Manager Agent started");

        // Inizializza i letti
        for (int i = 1; i <= totalBeds; i++) {
            BedInfo bed = new BedInfo();
            bed.bedId = i;
            bed.patientId = null;
            beds.put(i, bed);
        }

        it.unige.dibris.mas.Main.sharedBedManager = this; // ← SALVA RIFERIMENTO
        it.unige.dibris.mas.Main.agentReady();
    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Bed Manager Agent shutting down");
    }

    // Thread-safe: ammetti paziente a un letto
    public void admitPatient(String patientId, TriageColor color, PatientQueueEntry queueEntry) {
        bedsLock.writeLock().lock();
        try {
            // Trova il primo letto libero
            for (BedInfo bed : beds.values()) {
                if (bed.patientId == null) {
                    bed.patientId = patientId;
                    bed.color = color;
                    bed.admissionTime = System.currentTimeMillis();
                    bed.queueEntry = queueEntry;  

                    patientToBedMap.put(patientId, bed.bedId);
                    // ← NUOVO: Aggiorna la GUI
                    it.unige.dibris.mas.Main.updateBedUI(bed.bedId, patientId, color.name(), bed.admissionTime);

                    SimulationLogger.getInstance().log("[BedManager] " + patientId + " admitted to bed " + bed.bedId
                            + " (color: " + color.getLabel() + ")");
                    return;
                }
            }

            // Se nessun letto libero, dimetti il miglior paziente
            BedInfo bestBed = findBestPatientToDischarge();
            if (bestBed != null) {
                SimulationLogger.getInstance().log("[BedManager] Discharging " + bestBed.patientId + " from bed "
                        + bestBed.bedId + " to make room");

                // ← NUOVO: Aggiorna la GUI (letto diventa libero)
                it.unige.dibris.mas.Main.updateBedUI(bestBed.bedId, null, null, 0);

                bestBed.patientId = null;
                bestBed.color = null;
                bestBed.admissionTime = 0;

                // Riassegna il nuovo paziente
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

    // Trova il miglior paziente da dimettere (più sano + più tempo a letto)
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

    // Confronta due pazienti: vero se bed1 è migliore da dimettere di bed2
    private boolean isBetterToDischarge(BedInfo bed1, BedInfo bed2) {
        int color1 = bed1.color.ordinal();
        int color2 = bed2.color.ordinal();

        // Se colore diverso, dimetti il più sano (colore più basso)
        if (color1 != color2) {
            return color1 < color2;
        }

        // Se colore uguale, dimetti chi è stato più tempo a letto
        return bed1.admissionTime < bed2.admissionTime;
    }

    // Metodo per ottenere info di un letto specifico
    public BedInfo getBedInfo(int bedId) {
    bedsLock.readLock().lock();
    try {
        BedInfo bed = beds.get(bedId);
        if (bed == null || bed.patientId == null) {
            return null;
        }
        // ← COPIA ANCHE queueEntry
        return new BedInfo(bed.bedId, bed.patientId, bed.color, bed.admissionTime, bed.queueEntry);
    } finally {
        bedsLock.readLock().unlock();
    }
}

    // Metodo per dimettere un paziente da un letto specifico
    public void dischargePatientFromBed(int bedId) {
        bedsLock.writeLock().lock();
        try {
            BedInfo bed = beds.get(bedId);
            if (bed != null && bed.patientId != null) {
                SimulationLogger.getInstance().log("[BedManager] Discharging " + bed.patientId + " from bed " + bedId);

                // ← NUOVO: Aggiorna la GUI (letto diventa libero)
                it.unige.dibris.mas.Main.updateBedUI(bedId, null, null, 0);

                bed.patientId = null;
                bed.color = null;
                bed.admissionTime = 0;
            }
        } finally {
            bedsLock.writeLock().unlock();
        }
    }

    // Metodo per ottenere il QueueManager
    public QueueManagerAgent getQueueManager() {
        return it.unige.dibris.mas.Main.sharedQueueManager;
    }
}