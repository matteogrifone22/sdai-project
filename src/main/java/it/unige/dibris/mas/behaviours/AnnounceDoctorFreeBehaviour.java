package it.unige.dibris.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import it.unige.dibris.mas.agents.DoctorAgent;
import it.unige.dibris.mas.gui.SimulationLogger;

public class AnnounceDoctorFreeBehaviour extends TickerBehaviour {
    
    public AnnounceDoctorFreeBehaviour(Agent agent) {
        super(agent, 1000);  // Ogni 2 secondi (più frequente!)
    }

    @Override
    protected void onTick() {
        DoctorAgent doctorAgent = (DoctorAgent) myAgent;
        
        if (doctorAgent.getCurrentPatientId() == null) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("QueueManagerAgent", AID.ISLOCALNAME));
            msg.setContent("DOCTOR_FREE");
            myAgent.send(msg);
            
            SimulationLogger.getInstance().log("[" + myAgent.getLocalName() + "] Announced: DOCTOR_FREE");
        }
    }
}