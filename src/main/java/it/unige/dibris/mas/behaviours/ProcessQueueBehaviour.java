package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Queue;
import java.util.Map;

public abstract class ProcessQueueBehaviour extends TickerBehaviour {

    // abstract behaviour to process a queue of patients, used by both triage and registration agents
    
    public ProcessQueueBehaviour(Agent agent, long period) {
        super(agent, period);
    }
    
    @Override
    protected final void onTick() {

        Queue<Map.Entry<String, ACLMessage>> queue = getQueue();
        
        if (!queue.isEmpty()) {
            Map.Entry<String, ACLMessage> entry = queue.poll();
            String patientId = entry.getKey();
            ACLMessage originalMsg = entry.getValue();
            
            processPatient(patientId, originalMsg);
        }
    }
    
    protected abstract void processPatient(String patientId, ACLMessage originalMsg);
    
    protected abstract Queue<Map.Entry<String, ACLMessage>> getQueue();
}