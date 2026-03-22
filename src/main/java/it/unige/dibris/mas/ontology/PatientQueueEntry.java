package it.unige.dibris.mas.ontology;


public class PatientQueueEntry {
    public String patientId;
    public TriageColor color;
    public long startTime;
    
    public PatientQueueEntry(String patientId, TriageColor color, long startTime) {
        this.patientId = patientId;
        this.color = color;
        this.startTime = startTime;
    }
}