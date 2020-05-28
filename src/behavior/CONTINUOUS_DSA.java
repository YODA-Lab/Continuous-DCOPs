package behavior;

import static agent.DcopConstants.DSA_VALUE;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import agent.ContinuousDcopAgent;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CONTINUOUS_DSA extends OneShotBehaviour {
  /**
   * 
   */
  private static final long serialVersionUID = 5573433680877118333L;

  private ContinuousDcopAgent agent;

  public CONTINUOUS_DSA(ContinuousDcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {    
    // send the current value to neighbors
    for (AID neighborAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithIteration(neighborAID, agent.getValue(), DSA_VALUE, agent.getLsIteration(), agent.getSimulatedTime());
    }

    // initialization
    PiecewiseMultivariateQuadFunction combinedFunction = new PiecewiseMultivariateQuadFunction(); // combinedFunction

    Map<String, Double> neighborValueMap = waitingForMessageFromNeighborWithTime(DSA_VALUE, agent.getLsIteration());
    
    for (PiecewiseMultivariateQuadFunction function : agent.getFunctionMap().values()) {
      combinedFunction = combinedFunction.addPiecewiseFunction(function);
    }
        
    double chosenValue = combinedFunction.getArgmax(this.getAgent().getLocalName(), neighborValueMap);
    
    // Found new value
    // Choose which DSA version?
    if (Double.compare(chosenValue, agent.getValue()) != 0) {
      if (Double.compare(new Random().nextDouble(), ContinuousDcopAgent.DSA_PROBABILITY) <= 0) {
        agent.setValue(chosenValue);
        System.out.println("Iteration " + agent.getLsIteration() + " Agent " + agent.getLocalName() + " changes to a better value " + chosenValue);
      } else {
        System.out.println("Iteration " + agent.getLsIteration() + " Agent " + agent.getLocalName() + " could change to a better value " + chosenValue
            + ", but it decides to remain the value " + agent.getValue());
      }
    } 
    // Can't find better value
    else {
      System.out.println("Iteration " + agent.getLsIteration() + " Agent " + agent.getLocalName() + " doesn't find a better value and remains " + agent.getValue());
    }
    
    agent.incrementLsIteration();
  }
  
  //TODO: Review the simulated runtime
  private Map<String, Double> waitingForMessageFromNeighborWithTime(int msgCode, int iteration) {
    // Start of waiting time for the message
    agent.startSimulatedTiming();    
    
    Map<String, Double> valueMap = new HashMap<>();
    
    while (valueMap.size() < agent.getNeighborAIDSet().size()) {
//      System.out.println("Iteration " + agent.getLsIteration() + " Agent " + agent.getID() + " is waiting for message from neighbor: " + valueMap);
      
      MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(msgCode), MessageTemplate.MatchConversationId(String.valueOf(iteration)));      
      ACLMessage receivedMessage = myAgent.blockingReceive(template);

//      if (receivedMessage != null) {
//        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
//        if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
//          agent.setSimulatedTime(timeFromReceiveMessage);
//        } else {
//          agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
//        }
        
        try {
          String sender = receivedMessage.getSender().getLocalName();
          Double content = (Double) receivedMessage.getContentObject();
          
          if (!valueMap.containsKey(sender)) {
            valueMap.put(sender, content);
          }
          
          System.out.println("Iteration " + agent.getLsIteration() + " Agent " + agent.getID() + " receives " + receivedMessage.getContentObject() + " from "
              + receivedMessage.getSender().getLocalName());

        } catch (UnreadableException e) {
          e.printStackTrace();
        }        
//      } else
//        block();
    }
    
    return valueMap;
  }
}
