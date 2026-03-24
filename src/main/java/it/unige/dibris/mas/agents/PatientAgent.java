package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.behaviours.ReceiveTriageResultBehaviour;
import it.unige.dibris.mas.behaviours.SendRegistrationBehaviour;
import it.unige.dibris.mas.behaviours.DischargeBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.ontology.TriageColor;

public class PatientAgent extends Agent {

    private String patientId;
    private boolean isRegistered;
    private PatientSeverity severity;
    private TriageColor entryColor;
    private TriageColor color;
    private long arrivalTime;
    private boolean arrivedByAmbulance = false;

    protected void setup() {
        
        this.patientId = getLocalName(); // obtain name from agent name (e.g., "Patient_1")
        this.entryColor = null;
        this.color = null;
        this.isRegistered = false;


        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            
            if (args[0] instanceof String) {
                this.severity = PatientSeverity.valueOf((String) args[0]);
            } else if (args[0] instanceof PatientSeverity) {
                this.severity = (PatientSeverity) args[0];
            }
        } else {
            this.severity = PatientSeverity.MEDIUM; // Default
        }
        this.arrivedByAmbulance = (args != null && args.length > 1) ? (Boolean) args[1] : false;
        this.isRegistered = this.arrivedByAmbulance; // if arrived by ambulance, consider already registered

        try {
            Thread.sleep(1000); // 1 sec delay to simulate time taken to arrive and be ready for registration
            this.arrivalTime = System.currentTimeMillis();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SimulationLogger.getInstance().log("[" + patientId + "] Patient arrived at ED");

        addBehaviour(new SendRegistrationBehaviour());
        addBehaviour(new ReceiveTriageResultBehaviour());
        addBehaviour(new DischargeBehaviour());  

    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + patientId + "] Patient discharged");
    }

    // getters and setters

    public boolean isArrivedByAmbulance() {
        return arrivedByAmbulance;
    }

    public void setArrivedByAmbulance(boolean arrivedByAmbulance) {
        this.arrivedByAmbulance = arrivedByAmbulance;
    }

    public String getPatientId() {
        return patientId;
    }

    public TriageColor getColor() {
        return color;
    }

    public TriageColor getEntryColor() {
        return entryColor;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setEntryColor(TriageColor entryColor) {
        this.entryColor = entryColor;
    }

    public void setColor(TriageColor color) {
        this.color = color;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
        SimulationLogger.getInstance()
                .log("[" + patientId + "] Registration status: " + (registered ? "Registered" : "Not Registered"));
    }

    public void setTriageColor(TriageColor colorCode) {
        color = colorCode;
        SimulationLogger.getInstance().log("[" + patientId + "] Triage color: " + color);
    }

    public void setSeverity(PatientSeverity severity) {
        this.severity = severity;
    }

    public PatientSeverity getSeverity() {
        return severity;
    }

    public void logPatientInfo() {
        SimulationLogger.getInstance().log(
                "[" + patientId + "] Severity: " + severity.getLabel() +
                        " | Color: " + (color != null ? color.getLabel() : "NONE"));
    }
}