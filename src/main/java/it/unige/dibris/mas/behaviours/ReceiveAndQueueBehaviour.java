package it.unige.dibris.mas.behaviours;

import java.util.Queue;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.gui.SimulationLogger;
import java.util.AbstractMap;
import java.util.Map;

public abstract class ReceiveAndQueueBehaviour extends CyclicBehaviour {
    
    public void action() {
        ACLMessage msg = myAgent.receive();

        
        if (msg != null) {
            // ← ESTRAI DAL MESSAGGIO, NON DAL SENDER
            String patientId = msg.getSender().getLocalName();
            
            Queue<Map.Entry<String, ACLMessage>> queue = getQueue();
            
            queue.add(new AbstractMap.SimpleEntry<>(patientId, msg));
            
            String agentName = getAgentName();
            SimulationLogger.getInstance().log("[" + agentName + "] " + patientId + " joined queue. Size: " + queue.size());
            
            // Se non è il primo in coda, rispondi WAIT
            if (queue.size() > 1) {
                ACLMessage reply = msg.createReply();
                reply.setContent("WAIT");
                myAgent.send(reply);
            }
        } else {
            block();
        }
    }
    
    // Ogni agente fornisce la sua coda
    protected abstract Queue<Map.Entry<String, ACLMessage>> getQueue();
    
    // Ogni agente fornisce il suo nome
    protected abstract String getAgentName();
}