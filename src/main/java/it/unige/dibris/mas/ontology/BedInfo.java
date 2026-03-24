package it.unige.dibris.mas.ontology;

public class BedInfo {

    // This class represents the information about a bed in the hospital,
    // including which patient is occupying it, their triage color, and admission time.

    public int bedId;
    public String patientId;
    public TriageColor color;
    public long admissionTime;
     public PatientQueueEntry queueEntry;
    
    public BedInfo() {
    }
    
    public BedInfo(int bedId, String patientId, TriageColor color, long admissionTime, PatientQueueEntry queueEntry) {
        this.bedId = bedId;
        this.patientId = patientId;
        this.color = color;
        this.admissionTime = admissionTime;
        this.queueEntry = queueEntry;
    }
}