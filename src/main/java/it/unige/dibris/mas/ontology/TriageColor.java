package it.unige.dibris.mas.ontology;

public enum TriageColor {
    WHITE("White", 0),
    GREEN("Green", 1),
    BLUE("Blue", 2),
    ORANGE("Orange", 3),
    RED("Red", 4);
    
    private String label;
    private int priority;
    
    TriageColor(String label, int priority) {
        this.label = label;
        this.priority = priority;
    }
    
    public String getLabel() {
        return label;
    }
    
    public int getPriority() {
        return priority;
    }
}