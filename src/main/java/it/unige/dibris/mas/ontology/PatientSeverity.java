package it.unige.dibris.mas.ontology;

public enum PatientSeverity {
    LOW("Low"),       
    MEDIUM("Medium"), 
    HIGH("High");
    
    private String label;
    
    PatientSeverity(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}