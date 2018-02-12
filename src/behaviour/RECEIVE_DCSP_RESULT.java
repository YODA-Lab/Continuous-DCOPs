package behaviour;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import table.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import agent.DCOP;

public class RECEIVE_DCSP_RESULT extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -2879055736536273274L;

	DCOP agent;
	
	public RECEIVE_DCSP_RESULT(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		
		ArrayList<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborsWithTime(DCSP_RESULT);
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		for (ACLMessage receivedMessage:receivedMessageFromNeighborList) {
			String sender = receivedMessage.getSender().getLocalName();
			ArrayList<Double> DCSP_results_from_neighbors = new ArrayList<Double>();
			try {
				DCSP_results_from_neighbors = (ArrayList<Double>) receivedMessage.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			System.out.println("===ITERATION " + agent.getLsIteration() + ": Agent " + agent.getIdStr() + " receives "
					 + DCSP_results_from_neighbors	+ " from " + sender);


			//put values to isSatisfiedList and responseTimes
			if (agent.isNextAgent(sender)) {
				agent.getIsSatisfiedPhase1List().set(1, DCSP_results_from_neighbors.get(0));
				agent.getIsSatisfiedPhase2List().set(1, DCSP_results_from_neighbors.get(1));
				agent.getResponseTimes().set(1, DCSP_results_from_neighbors.get(2));
				agent.getProcesssedLoads().set(1, DCSP_results_from_neighbors.get(3));
			} else {
				agent.getIsSatisfiedPhase1List().set(2, DCSP_results_from_neighbors.get(0));
				agent.getIsSatisfiedPhase2List().set(2, DCSP_results_from_neighbors.get(1));
				agent.getResponseTimes().set(2, DCSP_results_from_neighbors.get(2));
				agent.getProcesssedLoads().set(2, DCSP_results_from_neighbors.get(3));
			}	
		}
		
		if (agent.getIdStr().equals("1")) {
			double sumSatisfiedPhase1 = agent.getIsSatisfiedPhase1List().get(0) + agent.getIsSatisfiedPhase1List().get(1) + agent.getIsSatisfiedPhase1List().get(2);
			double sumSatisfiedPhase2 = agent.getIsSatisfiedPhase2List().get(0) + agent.getIsSatisfiedPhase2List().get(1) + agent.getIsSatisfiedPhase2List().get(2);
			double averageTime = agent.getResponseTimes().get(0) + agent.getResponseTimes().get(1) + agent.getResponseTimes().get(2);
			boolean satisfyPhase1 = Double.compare(sumSatisfiedPhase1, 3.0) == 0 ? true : false;
			boolean satisfyPhase2 = Double.compare(sumSatisfiedPhase2, 3.0) == 0 ? true : false;

			System.out.println("===ITERATION " + agent.getLsIteration() + 
					" has PHASE 1: " + sumSatisfiedPhase1 + ", and PHASE 2: " + sumSatisfiedPhase2 +" and average time = " + averageTime);

			System.out.println("===ITERATION " + agent.getLsIteration() + 
					" has PHASE 1: " + satisfyPhase1 + ", and PHASE 2: " + satisfyPhase2 +" and average time = " + averageTime);
//			System.out.println("===ITERATION " + agent.getLsIteration() + ": Agent " + agent.getIdStr() + " receives " + valueFromNeighbor + " loads from " + sender);
			
			try {
			    PrintWriter writer = new PrintWriter(new FileOutputStream(new File("result.txt"),true));
			    if (satisfyPhase1 && satisfyPhase2)
			    	writer.println("Average response time: " + averageTime);
//			    	writer.println(satisfyPhase1 + " " + satisfyPhase2 + " " + averageTime);

			    writer.close();
			} catch (IOException e) {
			   // do something
			}
		}
		
		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		agent.incrementLsIteration();
		
		if (agent.getLsIteration() < DCOP.MAX_ITERATION)
			agent.sendImproveValue();

	}
	
//	public ArrayList<ACLMessage> waitingForMessageFromNeighbors(int msgCode) {
//		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
//		while (messageList.size() < agent.getNeighborAIDList().size()) {
//			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
//			ACLMessage receivedMessage = myAgent.receive(template);
//			if (receivedMessage != null) {
//				messageList.add(receivedMessage);
//			}
//			else
//				block();
//		}
//		return messageList;
//	}
	
	public ArrayList<ACLMessage> waitingForMessageFromNeighborsWithTime(int msgCode) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < agent.getNeighborAIDList().size()) {
			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
//				System.out.println("Agent " + getLocalName() + " receive message "
//						+ msgTypes[msgCode] + " from Agent " + receivedMessage.
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
		return messageList;
	}

	@Override
	public boolean done() {
		return agent.getLsIteration() == DCOP.MAX_ITERATION;
	}	
}
