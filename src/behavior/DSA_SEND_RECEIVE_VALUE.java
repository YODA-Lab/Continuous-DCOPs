package behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import agent.DcopAgent;
import static agent.DcopInfo.*;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import table.Row;
import table.Table;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DSA_SEND_RECEIVE_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = 6680449924898094747L;

	private DcopAgent agent;
	
	public DSA_SEND_RECEIVE_VALUE(DcopAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	// update the utilities map for a give neighbor's value
	public Map<Double, Double> updateUtilities(Map<Double, Double> utilities, String sender, Double senderValue) {
		
		List<Table> tables = agent.getTableList();
		// iterate to locate the table of this neighbor and me
		for (Table table : tables) {
		    if (table.getLabel().contains(sender)) {
		    	// get the order of this neighbor and me in the rowList.
		    	int senderIndex, myIndex;
		    	if (table.getLabel().equals((new ArrayList<>(Arrays.asList(sender, agent.getLocalName()))))) {
		    		myIndex = 1; senderIndex = 0;
		    		
		    	}
		    	else {
		    		senderIndex = 1; myIndex = 0;
		    	}
		    	// update the utilities for each value of my domain 
		    	List<Row> rowSet = table.getRowSet();
				//System.out.println("							 "+ "I'm agent "+agent.getLocalName()+"  sender is "+sender+", sender's value is "+senderValue);

	    		for (Row row: rowSet) {
    				//System.out.println("I'm agent "+agent.getLocalName()+" the sender is "+sender+". senderValue is "+senderValue);
    				//System.out.println("utility is "+row.getUtility());
    				//System.out.println("row.getValueAtPosition is "+row.getValueAtPosition(senderIndex));
	    			
    				//System.out.println("I'm agent "+agent.getLocalName()+". When "+sender+" is "+row.getValueAtPosition(senderIndex)+", utility is "+row.getUtility());
    				
	    			//System.out.println("compare row.getValueAtPosition(senderIndex) and sendervalue: "+ 1==2);

	    			if (((Double) row.getValueAtPosition(senderIndex)).equals(((Double) senderValue))) {
	    				//System.out.println("found the row"+", utility is "+row.getUtility());
	    				//System.out.println("I'm agent "+agent.getLocalName()+". When "+sender+" is "+senderValue+", utility is "+row.getUtility());
	    				double updatedUtility = utilities.get(row.getValueAtPosition(myIndex)) + row.getUtility();
						utilities.put(row.getValueAtPosition(myIndex), updatedUtility);
	    			}
	    		}
		    	break;
		    }
		}
		return utilities;
	}
	
	@Override
	public void action() {
		
		// create a map to hold the sum utility of all neighbors combined for each value in the domain of this agent
		Set<Double> domain = agent.getCurrentValueSet(); 
		Map<Double, Double> utilities = new HashMap<Double, Double>();
		for (Double i : domain) utilities.put(i,(double) 0);
		
		
		// send the current value to neighbors
		for (AID neighbor:agent.getNeighborAIDList()) {
		    ACLMessage msg = new ACLMessage(DSA_VALUE);
		    msg.addReceiver(new AID(neighbor.getLocalName(), AID.ISLOCALNAME));
//		    msg.setContent(String.valueOf(this.agent.getValue()));
		    msg.setContent(Double.toString(this.agent.getValue()));
		    agent.send(msg);
//		    System.out.println("										"
//		    		+agent.getLocalName()+" has value "+agent.getValue()+", sent to " +neighbor.getLocalName());

		}
		//agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DSA_VALUE, agent.getSimulatedTime());


		// receive the values sent by neighbors and update the utilities map
		int messageCount = 0;
		while (messageCount < agent.getNeighborAIDList().size()) {
			MessageTemplate template = MessageTemplate.MatchPerformative(DSA_VALUE);
			ACLMessage receivedMessage = agent.receive(template);
			if (receivedMessage != null) {
				messageCount++;
				String sender = receivedMessage.getSender().getLocalName();
				double senderValue = Double.parseDouble(receivedMessage.getContent());
				utilities = updateUtilities(utilities, sender, senderValue);
//			    System.out.println("																"
//			    		+agent.getLocalName()+" has received "+sender+"'s value "+senderValue);

			}
			else {
				block();
			}
		}
		
		// change the current value to the optimal value with probability of 0.6
		for (Entry<Double, Double> entry : utilities.entrySet()) {
		    System.out.println("agent "+agent.getLocalName()+": if value = "+entry.getKey() + ", then utilities = " + entry.getValue().toString());
		}
		double chosenValue =  Collections.max(utilities.entrySet(), Map.Entry.comparingByValue()).getKey();
		//System.out.println("agent "+agent.getLocalName()+" choses the value "+ chosenValue);
		if ( chosenValue != agent.getValue()) {
			if (new Random().nextDouble() <= 0.66) {
			    System.out.println("									agent "+agent.getLocalName()+" changes its value from "+agent.getValue()+" to " + chosenValue);
				agent.setValue(chosenValue); 
			}
			else {
			    System.out.println("									agent "+agent.getLocalName()+" could change to a better value " + chosenValue+", but it decides to remain the value "+agent.getValue());
			}
		}
		else {
		    System.out.println("									agent "+agent.getLocalName()+" doesn't find a better value and remains "+agent.getValue());
		}
	}
}
