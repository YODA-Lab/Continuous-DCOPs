package behavior;

import static agent.DcopInfo.DSA_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import agent.DcopAgent;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CONTINUOUS_DSA extends OneShotBehaviour {
  /**
   * 
   */
  private static final long serialVersionUID = 5573433680877118333L;
  
  private DcopAgent agent;

  public CONTINUOUS_DSA(DcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {	
	  
    // send the current value to neighbors
    for (AID neighbor : agent.getNeighborAIDList()) {
      ACLMessage msg = new ACLMessage(DSA_VALUE);
      msg.addReceiver(new AID(neighbor.getLocalName(), AID.ISLOCALNAME));
      msg.setContent(Double.toString(this.agent.getValue()));
      agent.send(msg);
    }
    
    // initialization
    PiecewiseMultivariateQuadFunction combinedFunction = new PiecewiseMultivariateQuadFunction(); //combinedFunction     
	Map<String,PiecewiseMultivariateQuadFunction>  functionMap = agent.getFunctionMap(); //map for  neighbor->function
	Map<String, Double> neighborValues = new HashMap<String, Double>(); //map for neighbor->value 
     
    // receive the values sent by neighbors
    int messageCount = 0;
    while (messageCount < agent.getNeighborAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(DSA_VALUE);
      ACLMessage receivedMessage = agent.receive(template);
      if (receivedMessage != null) {
        messageCount++;
        String sender = receivedMessage.getSender().getLocalName();
        double senderValue = Double.parseDouble(receivedMessage.getContent());
      	combinedFunction = combinedFunction.addPiecewiseFunction(functionMap.get(sender));  //update the function
      	neighborValues.put(sender, senderValue); // add new entry into the map for neighbor->value 
      } else {
        block();
      }
    }
	
    // changes to the argmax
    System.out.println(combinedFunction);
    System.out.println(neighborValues);
    System.out.println(this.getAgent().getLocalName());

    
    double chosenValue = combinedFunction.getArgmaxGivenVariableAndValueMap(this.getAgent().getLocalName(), neighborValues);
    if (chosenValue != agent.getValue()) {
      if (new Random().nextDouble() <= 0.66) {
        System.out.println("									agent " + agent.getLocalName() + " changes its value from " + agent.getValue()
            + " to " + chosenValue);
        agent.setValue(chosenValue);
      } else {
        System.out.println("									agent " + agent.getLocalName() + " could change to a better value " + chosenValue
            + ", but it decides to remain the value " + agent.getValue());
      }
    } else {
      System.out.println(
          "									agent " + agent.getLocalName() + " doesn't find a better value and remains " + agent.getValue());
    }
	//combinedFunction.getArgmaxGivenVariableAndValueMap(variableID, valuesOfOtherVariables)
    // Add a PMQ function with another PWQ function
    
	
//	PiecewiseMultivariateQuadFunction combinedFunction = functionMap.get(0);
//	for (int i = 1; i < functionMap.size(); i++) {
//	  combinedFunction = combinedFunction.addPiecewiseFunction(functionMap.get(i));
//	}
    // To find the argmax of a function f
    // First you need to create a map String -> Double containing the values of all other variables
    // except for the variable you're finding the argmax
    // Look at the below function from PiecewiseMultivariateQuadFunction 
    // public double getArgmaxGivenVariableAndValueMap(String variableID, Map<String, Double> valuesOfOtherVariables)
  }
}
