package it.unige.dibris.mas.ontology;

import it.unige.dibris.mas.ontology.TriageColor;

public class BedInfo {
    public int bedId;
    public String patientId;
    public TriageColor color;
    public long admissionTime;
    
    public BedInfo() {
    }
    
    public BedInfo(int bedId, String patientId, TriageColor color, long admissionTime) {
        this.bedId = bedId;
        this.patientId = patientId;
        this.color = color;
        this.admissionTime = admissionTime;
    }
}