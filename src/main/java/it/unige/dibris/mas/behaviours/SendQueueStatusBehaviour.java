package it.unige.dibris.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import it.unige.dibris.mas.agents.QueueManagerAgent;

public class SendQueueStatusBehaviour extends TickerBehaviour {
    
    public SendQueueStatusBehaviour(Agent agent) {
        super(agent, 500);  // Aggiorna ogni 500ms
    }
    
    @Override
    protected void onTick() {
        QueueManagerAgent queueAgent = (QueueManagerAgent) myAgent;
        
        // Ottieni lo stato attuale della coda
        String queueStatus = queueAgent.getQueueStatus();
        
        // Invia al Main per aggiornare la GUI
        it.unige.dibris.mas.Main.updateQueueListFromManager(queueStatus);
    }
}