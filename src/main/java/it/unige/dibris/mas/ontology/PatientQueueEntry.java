package it.unige.dibris.mas.ontology;


public class PatientQueueEntry {
    public String patientId;
    public TriageColor entryColor;
    public long arrivalTime;
    
    public PatientQueueEntry(String patientId, TriageColor entryColor, long arrivalTime) {
        this.patientId = patientId;
        this.entryColor = entryColor;
        this.arrivalTime = arrivalTime;
    }
}