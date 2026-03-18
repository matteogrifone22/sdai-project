package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Queue;
import java.util.Map;

public abstract class ProcessQueueBehaviour extends TickerBehaviour {
    
    public ProcessQueueBehaviour(Agent agent, long period) {
        super(agent, period);
    }
    
    @Override
    protected final void onTick() {
        // Questo è il template - non cambia mai
        Queue<Map.Entry<String, ACLMessage>> queue = getQueue();
        
        if (!queue.isEmpty()) {
            Map.Entry<String, ACLMessage> entry = queue.poll();
            String patientId = entry.getKey();
            ACLMessage originalMsg = entry.getValue();
            
            // Chiama il metodo specifico dell'agente
            processPatient(patientId, originalMsg);
        }
    }
    
    // Metodo template - ogni agente lo implementa
    protected abstract void processPatient(String patientId, ACLMessage originalMsg);
    
    // Ogni agente deve fornire la sua coda
    protected abstract Queue<Map.Entry<String, ACLMessage>> getQueue();
}