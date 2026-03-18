package it.unige.dibris.mas.agents;

import jade.core.Agent;
import it.unige.dibris.mas.behaviours.ReceiveTriageResultBehaviour;
import it.unige.dibris.mas.behaviours.SendRegistrationBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.ontology.PatientSeverity;
import it.unige.dibris.mas.ontology.TriageColor;

public class PatientAgent extends Agent {

    private String patientId;
    private boolean isRegistered;
    private PatientSeverity severity;
    private TriageColor color;

    protected void setup() {
        // Ricevi il nome dal container
        this.patientId = getLocalName();
        this.color = null;
        this.isRegistered = false;

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Se è una stringa, convertila
            if (args[0] instanceof String) {
                this.severity = PatientSeverity.valueOf((String) args[0]);
            } else if (args[0] instanceof PatientSeverity) {
                this.severity = (PatientSeverity) args[0];
            }
        } else {
            this.severity = PatientSeverity.MEDIUM; // Default
        }
        try {
            Thread.sleep(1000); // ← Delay di 1 secondo prima di iniziare
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SimulationLogger.getInstance().log("[" + patientId + "] Patient arrived at ED");

        // Aggiungi un comportamento (creeremo dopo)
        addBehaviour(new SendRegistrationBehaviour());
        addBehaviour(new ReceiveTriageResultBehaviour());

    }

    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + patientId + "] Patient discharged");
    }

    // Getter
    public String getPatientId() {
        return patientId;
    }

    public TriageColor getColor() {
        return color;
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