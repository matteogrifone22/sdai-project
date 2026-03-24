package it.unige.dibris.mas.ontology;


public class PatientQueueEntry {

    // This class represents an entry in the patient queue, containing the patient's ID, their triage color, and their arrival time.
    // it is useful to keep track of patients entering colors to update the discharge time chart

    public String patientId;
    public TriageColor entryColor;
    public long arrivalTime;
    
    public PatientQueueEntry(String patientId, TriageColor entryColor, long arrivalTime) {
        this.patientId = patientId;
        this.entryColor = entryColor;
        this.arrivalTime = arrivalTime;
    }
}