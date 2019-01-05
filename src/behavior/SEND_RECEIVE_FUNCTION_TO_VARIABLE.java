package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Sets;

import java.util.Set;

import agent.DCOP;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import maxsum.MaxSumMessage;
import table.Row;
import table.Table;

import static agent.DcopInfo.*;


/**
 * @author khoihd
 *
 */
public class SEND_RECEIVE_FUNCTION_TO_VARIABLE extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = 796364337476910372L;
  
  DCOP agent;
  
  public SEND_RECEIVE_FUNCTION_TO_VARIABLE(DCOP agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    /* PSEUDO-CODE
     * For each receiver in getFunctionIOwn
     *   Get the utility function
     *   Retrieve VARIABLE_TO_FUNCTION message from stored_VARIABLE_TO_FUNCTION with the receiver
     *   Add the function to the message 
     *   Project out the receiver and send this message to the receiver
     *   
     *   Retrieve VARIABLE_TO_FUNCTION message from received_VARIABLE_TO_FUNCTION with the receiver
     *   Add the function to the message
     *   Project out the receiver and store this message to stored_FUNCTION_TO_VARIABLE
     * End
     * For each agent in functionOwnedByOther
     *  Waiting for FUNCTION_TO_VARIABLE messages
     *  Store the FUNCTION_TO_VARIABLE messages to received_FUNCTION_TO_VARIABLE map
     * End
     */
    
    for (AID functionAgent : agent.getFunctionIOwn()) {
      PiecewiseMultivariateQuadFunction function = agent.getMSFunctionMap().get(functionAgent.getLocalName());
      
      MaxSumMessage var2FuncMsgStored = agent.getStored_VARIABLE_TO_FUNCTION().get(functionAgent);
      MaxSumMessage FUNC_TO_VARmsg_to_send = createFUNC_TO_VAR(function, var2FuncMsgStored, functionAgent, FUNC_TO_VAR_TO_SEND_OUT);
      long time = 0;
      
      agent.sendObjectMessageWithTime(functionAgent, FUNC_TO_VARmsg_to_send, FUNC_TO_VAR, time);
      System.out.println(FUNC_TO_VARmsg_to_send);
      
      MaxSumMessage var2FuncMsgReceived = agent.getReceived_VARIABLE_TO_FUNCTION().get(functionAgent);
      MaxSumMessage FUNC_TO_VARmsg_to_store = createFUNC_TO_VAR(function, var2FuncMsgReceived, functionAgent, FUNC_TO_VAR_TO_STORE);
      agent.getStored_FUNCTION_TO_VARIABLE().put(functionAgent, FUNC_TO_VARmsg_to_store);
    }
    
    waiting_store_FUNC_TO_VAR_message(FUNC_TO_VAR);
    
    double bestValue = calculateTheBestValue();
    System.out.println("Agent " + agent.getID() + " at iteration " + agent.getLsIteration() + " choose the best value: " + bestValue);
    
    agent.setChosenValue(bestValue);
    
    agent.incrementLsIteration();
  }
  
  /**
   * Aggregate all the FUNC_TO_VAR messages
   */
  private double calculateTheBestValue() {
    MaxSumMessage msg = new MaxSumMessage(agent.getCurrentValueSet());
    for (AID functionAgent : agent.getFunctionOwnedByOther()) {
      msg = msg.addMessage(agent.getReceived_FUNCTION_TO_VARIABLE().get(functionAgent));
    }
    
    for (AID functionAgent : agent.getFunctionIOwn()) {
      msg = msg.addMessage(agent.getStored_FUNCTION_TO_VARIABLE().get(functionAgent));
    }
    
    return msg.getBestValue();
  }

  /**
   * This function creates the vanilla Max-sum message and the first derivative in the Hybrid Max-Sum
   * @param function
   * @param var2FuncMsg
   * @param functionAgent
   * @param toSendOrStore
   * @return
   */
  private MaxSumMessage createFUNC_TO_VAR(PiecewiseMultivariateQuadFunction function, MaxSumMessage var2FuncMsg, AID functionAgent, int toSendOrStore) {
    // Self agent first
    List<Set<Double>> listOfValueSet = new ArrayList<Set<Double>>();
    
    String agentToKeep;
    Set<Double> agentToKeepValueSet = null;
    
    String agentToProject;
    Set<Double> agentToProjectValueSet = null;
    
    if (toSendOrStore == FUNC_TO_VAR_TO_SEND_OUT) {
      agentToKeepValueSet = agent.getCurrentValueSet();
      agentToKeep = agent.getID();
      
      agentToProjectValueSet = var2FuncMsg.getNewValueSet();
      agentToProject = functionAgent.getLocalName();
    }
    else { // if (toSendOrStore == FUNC_TO_VAR_TO_STORE) {
      agentToKeep = functionAgent.getLocalName();
      agentToKeepValueSet = var2FuncMsg.getNewValueSet();

      agentToProject = agent.getID();
      agentToProjectValueSet = agent.getCurrentValueSet();
    }

    listOfValueSet.add(agentToKeepValueSet);
    listOfValueSet.add(agentToProjectValueSet);
    Set<List<Double>> setOfScopeAgentValues = Sets.cartesianProduct(listOfValueSet);

    List<String> agentLabel = new ArrayList<>();
    agentLabel.add(agentToKeep);
    agentLabel.add(agentToProject);
    Table discretizedFunction = new Table(agentLabel);

    // Creating the table by adding up the messages
    for (List<Double> listValueEntry : setOfScopeAgentValues) {
      Map<String, Double> mapValue = new HashMap<>();
      double agentToKeepValue = listValueEntry.get(0);
      double agentToProjectValue = listValueEntry.get(1);

      mapValue.put(agentToKeep, agentToKeepValue);
      mapValue.put(agentToProject, agentToProjectValue);

      double evalutedValue = function.getTheFirstFunction().evaluateToValueGivenValueMap(mapValue);
      evalutedValue += var2FuncMsg.getValueUtilityMap().get(agentToKeepValue);
      discretizedFunction.addRow(new Row(listValueEntry, evalutedValue));
    }
    // Project out the DCOP
    Table projectedTable = behavior.DPOP_UTIL.projectOperator(discretizedFunction, agentToProject);
//    Map<Double, Double> argmaxes = behaviour.DPOP_UTIL.findTheArgmaxInHybridMaxSum(discretizedFunction, agentToProject, agentToProjectValueSet);
    
    // Convert the Table to Map<Double, Double>
    Map<Double, Double> valueUtilMap = new HashMap<>();

    for (Row row : projectedTable.getRowList()) {
      valueUtilMap.put(row.getValueAtPosition(0), row.getUtility());
    }

    return new MaxSumMessage(valueUtilMap);
  }
  
  private void waiting_store_FUNC_TO_VAR_message(int msgCode) {
    int msgCount = 0;
    while (msgCount < agent.getFunctionOwnedByOther().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.receive(template);
      
      if (receivedMessage != null) {
        MaxSumMessage maxsumMsg = null;
        try {
          maxsumMsg = (MaxSumMessage) receivedMessage.getContentObject();
        } catch (UnreadableException e) {
          e.printStackTrace();
        }
        agent.getReceived_FUNCTION_TO_VARIABLE().put(receivedMessage.getSender(), maxsumMsg);
        msgCount++;
      }
      else
        block();
    }
  }
}
