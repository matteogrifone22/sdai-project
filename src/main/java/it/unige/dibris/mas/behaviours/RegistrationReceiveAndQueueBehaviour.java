package it.unige.dibris.mas.behaviours;

import it.unige.dibris.mas.agents.RegistrationAgent;
import java.util.Queue;
import java.util.Map;
import jade.lang.acl.ACLMessage;

public class RegistrationReceiveAndQueueBehaviour extends ReceiveAndQueueBehaviour {
    
    @Override
    protected Queue<Map.Entry<String, ACLMessage>> getQueue() {
        return ((RegistrationAgent) myAgent).getPatientQueue();
    }
    
    @Override
    protected String getAgentName() {
        return "RegistrationAgent";
    }
}