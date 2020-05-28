package behavior;

import agent.ContinuousDcopAgent;
import static agent.DcopConstants.*;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class PROPAGATE_RECEIVE_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = -9137969826179481705L;

	private ContinuousDcopAgent agent;
	
	public PROPAGATE_RECEIVE_VALUE(ContinuousDcopAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		//looking for each agents in neighbor AID List
		//send message
		//content of each message is list of its values			
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// update 100ms to simulated time
		agent.addupSimulatedTime(100);
		
		// SEND VALUE TO NEIGHBORS
		for (AID neighborAgentAID : agent.getNeighborAIDSet()) {
			agent.sendObjectMessage(neighborAgentAID, agent.getValue(), PROPAGATE_VALUE, agent.getSimulatedTime());
		}
		

    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborsWithTime(PROPAGATE_VALUE);
    
    agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
    
    for (ACLMessage receivedMessage:receivedMessageFromNeighborList) {
      String sender = receivedMessage.getSender().getLocalName();
      Double valueFromNeighbor = null;
      try {
        valueFromNeighbor = (Double) receivedMessage.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }

      agent.getNeighborValueMap().put(sender, valueFromNeighbor);
    }
    
    agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
	}
	
	 public List<ACLMessage> waitingForMessageFromNeighborsWithTime(int msgCode) {
	    List<ACLMessage> messageList = new ArrayList<ACLMessage>();
	    while (messageList.size() < agent.getNeighborAIDSet().size()) {
	      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
	      ACLMessage receivedMessage = myAgent.blockingReceive(template);
//	      if (receivedMessage != null) {
//	        System.out.println("Agent " + getLocalName() + " receive message "
//	            + msgTypes[msgCode] + " from Agent " + receivedMessage.
//	            getSender().getLocalName());
	        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
	        if (timeFromReceiveMessage > agent.getSimulatedTime())
	          agent.setSimulatedTime(timeFromReceiveMessage);
	        messageList.add(receivedMessage);
//	      }
//	      else
//	        block();
	    }
	    agent.addupSimulatedTime(ContinuousDcopAgent.getDelayMessageTime());
	    return messageList;
	  }
}
