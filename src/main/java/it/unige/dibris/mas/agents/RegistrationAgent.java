package it.unige.dibris.mas.agents;

import java.util.List;
import java.util.Queue;

import jade.core.Agent;
import it.unige.dibris.mas.behaviours.RegistrationReceiveAndQueueBehaviour;
import it.unige.dibris.mas.gui.SimulationLogger;
import it.unige.dibris.mas.Main;
import it.unige.dibris.mas.behaviours.ProcessRegistrationQueueBehaviour;

import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;
import jade.lang.acl.ACLMessage;

public class RegistrationAgent extends Agent {
    private Queue<Map.Entry<String, ACLMessage>> patientQueue = new LinkedList<>();
    private List<String> registeredPatients = new ArrayList<>();
    private static final long REGISTRATION_DURATION = 2000;  // 2 secs 

     protected void setup() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Registration Agent started");
        addBehaviour(new RegistrationReceiveAndQueueBehaviour()); // take patients from the outside and put them in the queue
        addBehaviour(new ProcessRegistrationQueueBehaviour(this, REGISTRATION_DURATION)); // process the queue
        Main.agentReady();
    }
    
    
    protected void takeDown() {
        SimulationLogger.getInstance().log("[" + getLocalName() + "] Registration Agent shutting down");
    }

    public Queue<Map.Entry<String, ACLMessage>> getPatientQueue() {
        return patientQueue;
    }

    public void addRegisteredPatient(String patientId) {
        registeredPatients.add(patientId);
    }

    public List<String> getRegisteredPatients() {
        return registeredPatients;
    }
}