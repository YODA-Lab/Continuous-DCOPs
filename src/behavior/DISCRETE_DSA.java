package behavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import agent.ContinuousDcopAgent;
import static agent.DcopConstants.*;
import static utilities.Utilities.compareDouble;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import table.Row;
import table.Table;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class DISCRETE_DSA extends OneShotBehaviour {

  private static final long serialVersionUID = 6680449924898094747L;

  private ContinuousDcopAgent agent;

  public DISCRETE_DSA(ContinuousDcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    // send the current value to neighbors
    for (AID neighborAID : agent.getNeighborAIDSet()) {
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
    
    Map<String, Double> neighborValueMap = waitingForMessageFromNeighborWithTime(DSA_VALUE);
    
    for (Entry<String, PiecewiseMultivariateQuadFunction> entry : agent.getFunctionMap().entrySet()) {
      System.out.println(entry.getKey());
      System.out.println(entry.getValue());
      
      PiecewiseMultivariateQuadFunction func = entry.getValue();
      
      Map<String, Double> valueMap = new HashMap<>();
      valueMap.put(entry.getKey(), 3.0);
      
      System.out.println("Argmax is: " + func.getArgmaxGivenVariableAndValueMap(agent.getID(), valueMap));
    }
    
    // create a map to hold the sum utility of all neighbors combined for each
    // value in the domain of this agent
    Set<Double> domain = agent.getCurrentValueSet();
    Map<Double, Double> utilities = new HashMap<>();
    for (Double i : domain) {
      utilities.put(i, 0.0);
    }

    // send the current value to neighbors
    for (AID neighbor : agent.getNeighborAIDSet()) {
      ACLMessage msg = new ACLMessage(DSA_VALUE);
      msg.addReceiver(new AID(neighbor.getLocalName(), AID.ISLOCALNAME));
      // msg.setContent(String.valueOf(this.agent.getValue()));
      msg.setContent(Double.toString(this.agent.getValue()));
      agent.send(msg);
      // System.out.println(" "
      // +agent.getLocalName()+" has value "+agent.getValue()+", sent to "
      // +neighbor.getLocalName());

    }
    // agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable,
    // DSA_VALUE, agent.getSimulatedTime());

    // receive the values sent by neighbors and update the utilities map
    int messageCount = 0;
    while (messageCount < agent.getNeighborAIDSet().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(DSA_VALUE);
      ACLMessage receivedMessage = agent.blockingReceive(template);
//      if (receivedMessage != null) {
        messageCount++;
        String sender = receivedMessage.getSender().getLocalName();
        double senderValue = Double.parseDouble(receivedMessage.getContent());
        utilities = updateUtilities(utilities, sender, senderValue);

//      } else {
//        block();
//      }
    }

    // change the current value to the optimal value with probability of 0.6
    for (Entry<Double, Double> entry : utilities.entrySet()) {
      System.out.println("agent " + agent.getLocalName() + ": if value = " + entry.getKey() + ", then utilities = "
          + entry.getValue().toString());
    }
    
    double chosenValue = Collections.max(utilities.entrySet(), Map.Entry.comparingByValue()).getKey();
    
    // System.out.println("agent "+agent.getLocalName()+" choses the value "+
    // chosenValue);
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
          myIndex = 1;
          senderIndex = 0;
        } else {
          senderIndex = 1;
          myIndex = 0;
        }
        // update the utilities for each value of my domain
        List<Row> rowSet = table.getRowSet();

        for (Row row : rowSet) {
          if (compareDouble(row.getValueAtPosition(senderIndex), senderValue) == 0) {
            double updatedUtility = utilities.get(row.getValueAtPosition(myIndex)) + row.getUtility();
            utilities.put(row.getValueAtPosition(myIndex), updatedUtility);
          }
        }
        break;
      }
    }
    return utilities;
  }
  
  //TODO: Review the simulated runtime
  private Map<String, Double> waitingForMessageFromNeighborWithTime(int msgCode) {
    // Start of waiting time for the message
    agent.startSimulatedTiming();    
    
    Map<String, Double> valueMap = new HashMap<>();
    
    while (valueMap.size() < agent.getChildrenAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);

//      if (receivedMessage != null) {
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
//      } else
//        block();
    }
    
    return valueMap;
  }
}
