package behavior;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import agent.ContinuousDcopAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import maxsum.MaxSumMessage;

import static agent.DcopConstants.*;

/**
 * @author khoihd
 *
 */
public class MAXSUM_VARIABLE_TO_FUNCTION extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -6435195074924409292L;
  
  private ContinuousDcopAgent agent;
  
  public MAXSUM_VARIABLE_TO_FUNCTION(ContinuousDcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {    
    /* PSEUDO-CODE
     * *****************
     * If the iteration == 0:
     *  Initialize the VAR_TO_FUNC message to 0s
     *  Initialize the newValueSet with currentValueSet
     *  Send VARIABLE_TO_FUNCTION messages to agents in functionOwnedByOther
     *  Store VARIABLE_TO_FUNCTION messages to agents in stored_VARIABLE_TO_FUNCTION
     * ElseIf the iteration != 0:
     *  Update the valueSet based on the firstDerivative of all FUNCTION_TO_VARIABLE messages
     *  
     *  Now agents create the VARIABLE_TO_FUNCTION message
     *  For each neighbor
     *    Retrieve FUNCTION_TO_VARIABLE messages from received_FUNCTION_TO_VARIABLE (except the neighbor)
     *    Retrieve FUNCTION_TO_VARIABLE messages from stored_FUNCTION_TO_VARIABLE (except the neighbor)
     *    Calculate the alpha for that function_to_send
     *    Create the VARIABLE_TO_FUNCTION messages, and update alpha values
     *    If (neighbor is in getFunctionOwnedByOther)
     *      Send the VARIABLE_TO_FUNCTION message
     *    Else if (neighbor is in getFunctionIOwn)
     *      Store the VARIABLE_TO_FUNCTION message
     *    End
     *  End
     * End
     * For each agent in functionIOwn
     *  Waiting for VARIABLE_TO_FUNCTION messages from getFunctionIOwn
     *  Store the VARIABLE_TO_FUNCTION messages to RECEIVED_VARIABLE_TO_FUNCTION map
     * End
     * 
     * TODO: clear the function to message map
     */
    
    // Initialize the message to 0 for all agent in agentKeepMyFunctionAIDSet
    // Set newValues same to old values at the first iteration
    if (agent.getLsIteration() == 0) {
      agent.startSimulatedTiming();
      
      MaxSumMessage msgVAR_TO_FUNC = new MaxSumMessage(agent.getCurrentValueSet());
      msgVAR_TO_FUNC.setNewValueSet(agent.getCurrentValueSet());
      
      agent.pauseSimulatedTiming();
      
      for (AID receiver : agent.getFunctionOwnedByOther()) {
        agent.sendObjectMessage(receiver, msgVAR_TO_FUNC, VAR_TO_FUNC, agent.getSimulatedTime());
      }
      for (AID store_agent : agent.getFunctionIOwn()) {
        agent.getStored_VARIABLE_TO_FUNCTION().put(store_agent, msgVAR_TO_FUNC);
      }
    } // end the if (iteration == 0)
    else {
     /*
      * For each neighbor
      *   Retrieve FUNCTION_TO_VARIABLE messages from received_FUNCTION_TO_VARIABLE (except the neighbor)
      *   Retrieve FUNCTION_TO_VARIABLE messages from stored_FUNCTION_TO_VARIABLE (except the neighbor)
      *   Calculate the alpha for that function_to_send
      *   Create the VARIABLE_TO_FUNCTION messages, and update alpha values
      *   If (neighbor is in getFunctionOwnedByOther)
      *     Send the VARIABLE_TO_FUNCTION message
      *   Else if (neighbor is in getFunctionIOwn)
      *     Store the VARIABLE_TO_FUNCTION message
      *   End
      * End
      */
      agent.startSimulatedTiming();
      
      MaxSumMessage msgVAR_TO_FUNC = new MaxSumMessage(agent.getCurrentValueSet());
      
      if (ContinuousDcopAgent.getAlgorithm() == HYBRID_MAXSUM) {
        modifyMSValuesUsingGradient(); 
      }
      
      agent.pauseSimulatedTiming();
      
      for (AID neighbor : agent.getNeighborAIDSet()) {
        // process the function that is OWNED BY OTHER AGENTS
        if (agent.getFunctionOwnedByOther().contains(neighbor)) {
          agent.startSimulatedTiming();
          
          // Add all messages from getReceived_FUNCTION_TO_VARIABLE except for the neighbor
          for (Entry<AID, MaxSumMessage> msgEntry : agent.getReceived_FUNCTION_TO_VARIABLE().entrySet()) {
            if (neighbor.equals(msgEntry.getKey())) {continue;}
            msgVAR_TO_FUNC = msgVAR_TO_FUNC.addMessage(msgEntry.getValue());
          }
          
          // add all messages from getStored_FUNCTION_TO_VARIABLE
          msgVAR_TO_FUNC = msgVAR_TO_FUNC.addAllMessages(agent.getStored_FUNCTION_TO_VARIABLE().values());
          msgVAR_TO_FUNC.updateAlphaAndValues();
          
          msgVAR_TO_FUNC.setNewValueSet(agent.getCurrentValueSet());

          agent.pauseSimulatedTiming();

          agent.sendObjectMessage(neighbor, msgVAR_TO_FUNC, VAR_TO_FUNC, agent.getSimulatedTime());
        } 
        // process the function that I owned
        else if (agent.getFunctionIOwn().contains(neighbor)) {
          agent.startSimulatedTiming();
          
          // add all messages from getReceived_FUNCTION_TO_VARIABLE
          msgVAR_TO_FUNC = msgVAR_TO_FUNC.addAllMessages(agent.getReceived_FUNCTION_TO_VARIABLE().values());

          // Except for the neighbor, add messages from getStored_FUNCTION_TO_VARIABLE
          for (Entry<AID, MaxSumMessage> msgEntry : agent.getStored_FUNCTION_TO_VARIABLE().entrySet()) {
            if (neighbor.equals(msgEntry.getKey())) {continue;}
            msgVAR_TO_FUNC = msgVAR_TO_FUNC.addMessage(msgEntry.getValue());
          }
          
          msgVAR_TO_FUNC.updateAlphaAndValues();
          msgVAR_TO_FUNC.setNewValueSet(agent.getCurrentValueSet());
          
          agent.getStored_VARIABLE_TO_FUNCTION().put(neighbor, msgVAR_TO_FUNC);
          
          agent.pauseSimulatedTiming();
        }
      }
    }
    waiting_store_VAR_TO_FUNC_message_with_time(VAR_TO_FUNC);
    
//    agent.resetFunc2VarMessageMap();
  }

  /**
   *  From all first derivative from FUNCTION_TO_VALUES messages
   *  Add those messages up and modify the values accordingly
   */
  private void modifyMSValuesUsingGradient() { 
    System.out.println("Before: " + agent.getCurrentValueSet());
    
    double scalingFactor = ContinuousDcopAgent.GRADIENT_SCALING_FACTOR;
    
    Set<Double> newValueSet = new HashSet<>();
    
    for (Double oldValue : agent.getCurrentValueSet()) {
      double sumGradient = 0;
      for (AID neighbor : agent.getNeighborAIDSet()) {
        if (agent.getFunctionOwnedByOther().contains(neighbor)) {
          sumGradient = sumGradient + agent.getReceived_FUNCTION_TO_VARIABLE().get(neighbor).getFirstDerivativeMap().get(oldValue);
        }
        else if (agent.getFunctionIOwn().contains(neighbor)) {
          sumGradient = sumGradient + agent.getStored_FUNCTION_TO_VARIABLE().get(neighbor).getFirstDerivativeMap().get(oldValue);
        }
      }
      
      double newValue = oldValue + scalingFactor * sumGradient;
      
      // Add new value only if it's in the interval
      if (ContinuousDcopAgent.getGlobalInterval().contains(newValue)) {
        newValueSet.add(newValue);
      } else {
        newValueSet.add(oldValue);
      }
    }
    
    agent.setCurrentValueSet(newValueSet);
           
    System.out.println("After: " + agent.getCurrentValueSet());
  }

  private void waiting_store_VAR_TO_FUNC_message_with_time(int msgCode) {
    agent.startSimulatedTiming();    
        
    int msgCount = 0;    
    while (msgCount < agent.getFunctionIOwn().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);
      
//      if (receivedMessage != null) {
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
        agent.getReceived_VARIABLE_TO_FUNCTION().put(receivedMessage.getSender(), maxsumMsg);
        msgCount++;
//      }
//      else
//        block();
    }
  }
}
