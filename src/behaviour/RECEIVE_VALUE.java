package behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.awt.SecondaryLoop;
import java.util.ArrayList;
import java.util.concurrent.Phaser;

import agent.DCOP;

public class RECEIVE_VALUE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 3951196053602788669L;

	DCOP agent;
	
	public RECEIVE_VALUE(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		// save oldSimulatedTime here, roll back if stop condition
		long oldSimulatedTime = agent.getSimulatedTime();
		
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
				
		while (messageList.size() < agent.getNeighborAIDList().size()) {
			if (agent.getLsIteration() == DCOP.MAX_ITERATION) {
				agent.setSimulatedTime(oldSimulatedTime);
				return;
			}
			MessageTemplate template = MessageTemplate.MatchPerformative(LS_VALUE);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
//				System.out.println("Agent " + getLocalName() + " receive message "
//						+ msgTypes[LS_VALUE] + " from Agent " + receivedMessage.
//						getSender().getLocalName());
				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
				if (timeFromReceiveMessage > agent.getSimulatedTime())
					agent.setSimulatedTime(timeFromReceiveMessage);
				
				messageList.add(receivedMessage);
			}
			else
				block();
			
		}
		
		
		agent.addupSimulatedTime(DCOP.getDelayMessageTime());
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());		
				
		for (ACLMessage msg:messageList) {
			ArrayList<Double> valuesFromNeighbor = new ArrayList<Double>();
			try {
				valuesFromNeighbor = (ArrayList<Double>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			String sender = msg.getSender().getLocalName();
			System.out.println("===ITERATION " + agent.getLsIteration() + ": Agent " + agent.getIdStr() + " receives "
								+ valuesFromNeighbor.toString() + " loads from " + sender);
						
			String senderAgent = agent.isNextAgent(sender) ? "next" : "further";
			agent.getAgentView_DPOP_TSMap().put(senderAgent, valuesFromNeighbor);
		}
		
		/*TEST CODE
		 * 
		 */
		
		System.out.println("AGENT VIEW OF " + agent.getIdStr() + " about next: " + agent.getAgentView_DPOP_TSMap().get("next"));
		System.out.println("AGENT VIEW OF " + agent.getIdStr() + " about further: " + agent.getAgentView_DPOP_TSMap().get("further"));

		
		double isSatisfiedPhase1 = checkForSatisfiedPhase1(
						agent.getBestValueMap().get("next"), agent.getAgentView_DPOP_TSMap().get("next")) 
						+ checkForSatisfiedPhase1(
						agent.getBestValueMap().get("further"), agent.getAgentView_DPOP_TSMap().get("further"));
		double isSatisfiedPhase2 = checkForSatisfiedPhase2(
				agent.getBestValueMap().get("next"), agent.getAgentView_DPOP_TSMap().get("next")) 
				+ checkForSatisfiedPhase2(
				agent.getBestValueMap().get("further"), agent.getAgentView_DPOP_TSMap().get("further"));

		if (agent.isRoot()) {
			System.out.println("Agent " + agent.getIdStr() + " satisfy phase 1: " + isSatisfiedPhase1);
			System.out.println("Agent " + agent.getIdStr() + " satisfy phase 2: " + isSatisfiedPhase2);
		}
				
		isSatisfiedPhase1 = Double.compare(isSatisfiedPhase1, 2.0) == 0 ? 1.0 : 0.0;
		isSatisfiedPhase2 = Double.compare(isSatisfiedPhase2, 2.0) == 0 ? 1.0 : 0.0;
	

		agent.getIsSatisfiedPhase1List().set(0, isSatisfiedPhase1);
		agent.getIsSatisfiedPhase2List().set(0, isSatisfiedPhase2);

		
		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
//		for (AID neighbor:agent.getNeighborAIDList()) 
//			agent.sendObjectMessageWithTime(neighbor, "", LS_ITERATION_DONE, agent.getSimulatedTime());	
		
//		if (agent.phase == DCOP.FIRST_PHASE) agent.phase = DCOP.SECOND_PHASE;
//		else if (agent.phase == DCOP.SECOND_PHASE) agent.phase = DCOP.FIRST_PHASE
	}

	@Override
	public boolean done() {
		return agent.getLsIteration() == DCOP.MAX_ITERATION;
	}		
	
	public double checkForSatisfiedPhase1(ArrayList<Double> sentValues, ArrayList<Double> agentView) {
		if (Double.compare(sentValues.get(0), agentView.get(1)) != 0) return 0.0;		
		if (Double.compare(sentValues.get(1), agentView.get(0)) != 0) return 0.0;		
		
		return 1.0;	
	}
	
	public double checkForSatisfiedPhase2(ArrayList<Double> sentValues, ArrayList<Double> agentView) {
		if (Double.compare(sentValues.get(2), agentView.get(3)) != 0) return 0.0;		
		if (Double.compare(sentValues.get(3), agentView.get(2)) != 0) return 0.0;		
		
		return 1.0;
	}
}
