package behavior;

import static agent.DcopConstants.DSA_VALUE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import agent.DcopAgent;
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

  private DcopAgent agent;

  public CONTINUOUS_DSA(DcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    // send the current value to neighbors
    for (AID neighborAID : agent.getNeighborAIDList()) {
      ACLMessage msg = new ACLMessage(DSA_VALUE);
      msg.addReceiver(neighborAID);
      try {
        msg.setContentObject(agent.getValue());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      agent.send(msg);
    }

    // initialization
    PiecewiseMultivariateQuadFunction combinedFunction = new PiecewiseMultivariateQuadFunction(); // combinedFunction

    Map<String, Double> neighborValueMap = waitingForMessageFromNeighborWithTime(DSA_VALUE);
    
    for (Entry<String, Double> entry : neighborValueMap.entrySet()) {
      combinedFunction.addPiecewiseFunction(agent.getFunctionMap().get(entry.getKey()));
    }

    double chosenValue = combinedFunction.getArgmax(this.getAgent().getLocalName(), neighborValueMap);
    
    // Found new value
    // Choose which DSA version?
    if (Double.compare(chosenValue, agent.getValue()) != 0) {
      if (Double.compare(new Random().nextDouble(), DcopAgent.DSA_PROBABILITY) <= 0) {
        agent.setValue(chosenValue);
        //TODO: Modify the printout
        System.out.println("Agent " + agent.getLocalName() + " changes to a better value " + chosenValue);
      } else {
        //TODO: Modify the printout
        System.out.println("Agent " + agent.getLocalName() + " could change to a better value " + chosenValue
            + ", but it decides to remain the value " + agent.getValue());
      }
    } 
    // Can't find better value
    else {
      System.out.println("Agent " + agent.getLocalName() + " doesn't find a better value and remains " + agent.getValue());
    }
  }
  
  //TODO: Review the simulated runtime
  private Map<String, Double> waitingForMessageFromNeighborWithTime(int msgCode) {
    // Start of waiting time for the message
    agent.startSimulatedTiming();    
    
    Map<String, Double> valueMap = new HashMap<>();
    
    while (valueMap.size() < agent.getChildrenAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.receive(template);

      if (receivedMessage != null) {
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
        if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        } else {
          agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
        }
        
        try {
          valueMap.put(receivedMessage.getSender().getLocalName(), (Double) receivedMessage.getContentObject());
        } catch (UnreadableException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }        
      } else
        block();
    }
    
    return valueMap;
  }
}
