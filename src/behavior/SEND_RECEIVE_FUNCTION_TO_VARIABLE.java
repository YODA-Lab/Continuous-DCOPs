package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Sets;

import java.util.Set;

import agent.DcopAgent;
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
  
  private DcopAgent agent;
  
  public SEND_RECEIVE_FUNCTION_TO_VARIABLE(DcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    /* 
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
      agent.startSimulatedTiming();
      
      PiecewiseMultivariateQuadFunction function = agent.getMSFunctionMapIOwn().get(functionAgent.getLocalName());
      
      MaxSumMessage var2FuncMsgStored = agent.getStored_VARIABLE_TO_FUNCTION().get(functionAgent);
      MaxSumMessage var2FuncMsgReceived = agent.getReceived_VARIABLE_TO_FUNCTION().get(functionAgent);

      // Given that agent owns the function, create the message to send out to external variable nodes
      MaxSumMessage FUNC_TO_VARmsg_to_send = createFUNC_TO_VAR(function, var2FuncMsgStored, functionAgent,
          var2FuncMsgReceived.getNewValueSet(), FUNC_TO_VAR_TO_SEND_OUT);
      long time = 0;
      
      agent.pauseSimulatedTiming();
      
      agent.sendObjectMessageWithTime(functionAgent, FUNC_TO_VARmsg_to_send, FUNC_TO_VAR, time);
      
      agent.startSimulatedTiming();
      
      // Agent creates the message to store (sent from function owned to variable owned)
      MaxSumMessage FUNC_TO_VARmsg_to_store = createFUNC_TO_VAR(function, var2FuncMsgReceived, functionAgent, null, FUNC_TO_VAR_TO_STORE);
      agent.getStored_FUNCTION_TO_VARIABLE().put(functionAgent, FUNC_TO_VARmsg_to_store);
      
      agent.pauseSimulatedTiming();
    }
    
    waiting_store_FUNC_TO_VAR_message_with_time(FUNC_TO_VAR);
    
    double bestValue = calculateTheBestValue();
    System.out.println("Agent " + agent.getID() + " at iteration " + agent.getLsIteration() + " choose the best value: " + bestValue);
    
    agent.setValue(bestValue);
    
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
   * This function creates the vanilla Max-sum message
   * The vanilla Max-sum message is created by two agents (agent to keep, agent to be projected out)
   * Values of agent to keep is retrieved from message.getNewValueSet()
   * Values of agent to be projected out is retrieved from message.getValueMap.keys();
   * 
   * Then calculate the gradient for values of functions to keep for those values in message.getNewValueSet()
   * Instead of finding the max as the vanila Maxsum message, now find the argmax and plug them to the first derivative
   *  
   * @param function
   * @param var2FuncMsg
   * @param functionAgent
   * @param toSendOrStore
   * @return
   */
  private MaxSumMessage createFUNC_TO_VAR(PiecewiseMultivariateQuadFunction function, MaxSumMessage var2FuncMsg,
      AID functionAgent, Set<Double> functionAgentValues, int toSendOrStore) {
    List<Set<Double>> listOfValueSet = new ArrayList<Set<Double>>();
    
    String agentToKeep;
    Set<Double> agentToKeepValueSet = null;
    
    String agentToProject;
    Set<Double> agentToProjectValueSet = null;
    

    if (toSendOrStore == FUNC_TO_VAR_TO_SEND_OUT) {
      agentToKeep = functionAgent.getLocalName();
      agentToKeepValueSet = functionAgentValues;

      agentToProject = agent.getID();
      agentToProjectValueSet = var2FuncMsg.getValueUtilityMap().keySet();
    }
    else { // (toSendOrStore == FUNC_TO_VAR_TO_STORE) {
      agentToKeep = agent.getID();
      agentToKeepValueSet = agent.getCurrentValueSet();
      
      agentToProject = functionAgent.getLocalName();      
      agentToProjectValueSet = var2FuncMsg.getValueUtilityMap().keySet();
    }
    
    List<String> agentLabel = new ArrayList<>();
    agentLabel.add(agentToKeep);
    agentLabel.add(agentToProject);
    Table discretizedFunction = new Table(agentLabel);

    listOfValueSet.add(agentToKeepValueSet);
    listOfValueSet.add(agentToProjectValueSet);
    Set<List<Double>> setOfScopeAgentValues = Sets.cartesianProduct(listOfValueSet);

    // Creating the table by adding up the messages
    for (List<Double> listValueEntry : setOfScopeAgentValues) {
      Map<String, Double> mapValue = new HashMap<>();
      double agentToKeepValue = listValueEntry.get(0);
      double agentToProjectValue = listValueEntry.get(1);

      mapValue.put(agentToKeep, agentToKeepValue);
      mapValue.put(agentToProject, agentToProjectValue);

      double evaluatedValue = function.getTheFirstFunction().evaluateToValueGivenValueMap(mapValue);
//      evaluatedValue += var2FuncMsg.getValueUtilityMap().get(agentToKeepValue);
      evaluatedValue += var2FuncMsg.getValueUtilityMap().get(agentToProjectValue);
      discretizedFunction.addRow(new Row(listValueEntry, evaluatedValue));
    }
    // Project out the DCOP
    Table projectedTable = behavior.DPOP_UTIL.projectOperator(discretizedFunction, agentToProject);
//    Map<Double, Double> argmaxes = behaviour.DPOP_UTIL.findTheArgmaxInHybridMaxSum(discretizedFunction, agentToProject, agentToProjectValueSet);
    
    // Convert the Table to Map<Double, Double>
    Map<Double, Double> valueUtilMap = new HashMap<>();
    
    for (Row row : projectedTable.getRowSet()) {
      valueUtilMap.put(row.getValueAtPosition(0), row.getUtility());
    }
    
    Map<Double, Double> firstDerivative = new HashMap<>();
    PiecewiseMultivariateQuadFunction firstDerivativeFunc = function.takeFirstPartialDerivative(agentToKeep);
    for (double agentToKeepValue : agentToKeepValueSet) {
      Map<String, Double> valueMap = new HashMap<>();
      valueMap.put(agentToKeep, agentToKeepValue);
      
      // find the argmax
      double argmax = discretizedFunction.getArgmaxGivenVariableAndValueMap(agentToProject, valueMap);
      valueMap.put(agentToProject, argmax);
      
      firstDerivative.put(agentToKeepValue, firstDerivativeFunc.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap));
    }
    
    return new MaxSumMessage(valueUtilMap, new HashSet<Double>(), firstDerivative);
  }
  
  private void waiting_store_FUNC_TO_VAR_message_with_time(int msgCode) {
    agent.startSimulatedTiming();    
    
    int msgCount = 0;
    while (msgCount < agent.getFunctionOwnedByOther().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.receive(template);
      
      if (receivedMessage != null) {
        MaxSumMessage maxsumMsg = null;
        try {
          maxsumMsg = (MaxSumMessage) receivedMessage.getContentObject();

          long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
          if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
            agent.setSimulatedTime(timeFromReceiveMessage);
          } else {
            agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
          }
          
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
