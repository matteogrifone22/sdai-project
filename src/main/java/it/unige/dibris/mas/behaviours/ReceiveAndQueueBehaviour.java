package it.unige.dibris.mas.behaviours;

import java.util.Queue;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.gui.SimulationLogger;
import java.util.AbstractMap;
import java.util.Map;

public abstract class ReceiveAndQueueBehaviour extends CyclicBehaviour {

    // This behaviour listens for incoming patient messages and adds them to the appropriate queue, sending WAIT if not first in line
    
    public void action() {
        ACLMessage msg = myAgent.receive();

        
        if (msg != null) {
            String patientId = msg.getSender().getLocalName();
            
            Queue<Map.Entry<String, ACLMessage>> queue = getQueue();
            
            queue.add(new AbstractMap.SimpleEntry<>(patientId, msg));
            
            String agentName = getAgentName();
            SimulationLogger.getInstance().log("[" + agentName + "] " + patientId + " joined queue. Size: " + queue.size());
            
            if (queue.size() > 1) {
                ACLMessage reply = msg.createReply();
                reply.setContent("WAIT");
                myAgent.send(reply);
            }
        } else {
            block();
        }
    }
    
    protected abstract Queue<Map.Entry<String, ACLMessage>> getQueue();
    
    protected abstract String getAgentName();
}