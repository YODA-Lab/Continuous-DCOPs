package behaviour;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import agent.DCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import maxsum.MaxSumMessage;

import static agent.DcopInfo.*;

/**
 * @author khoihd
 *
 */
public class SEND_RECEIVE_VARIABLE_TO_FUNCTION extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -6435195074924409292L;
  
  DCOP agent;
  
  public SEND_RECEIVE_VARIABLE_TO_FUNCTION(DCOP agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {    
    /* PSEUDO-CODE
     * *****************
     * If the iteration == 0:
     *  Initialize the VAR_TO_FUNC message to 0s
     *  Send VARIABLE_TO_FUNCTION messages to agents in functionOwnedByOther
     *  Store VARIABLE_TO_FUNCTION messages to agents in stored_VARIABLE_TO_FUNCTION
     * ElseIf the iteration != 0:
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
    if (agent.getLsIteration() == 0) {
      MaxSumMessage msgVAR_TO_FUNC = new MaxSumMessage(agent.getMSvalueSet());
      msgVAR_TO_FUNC.setNewValueSet(agent.getMSvalueSet());
      
      long time = 0;
      for (AID receiver : agent.getFunctionOwnedByOther()) {
        agent.sendObjectMessageWithTime(receiver, msgVAR_TO_FUNC, VAR_TO_FUNC, time);
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
      //TODO: Test Hybrid MaxSum later
//      modifyTheAgentMSValueSet(); 
      
      MaxSumMessage msgVAR_TO_FUNC = new MaxSumMessage(agent.getMSvalueSet());
      for (AID neighbor : agent.getNeighborAIDList()) {
        // process the function that is owned by other agents
        if (agent.getFunctionOwnedByOther().contains(neighbor)) {
          // Add all messages from getReceived_FUNCTION_TO_VARIABLE except for the neighbor
          for (Entry<AID, MaxSumMessage> msgEntry : agent.getReceived_FUNCTION_TO_VARIABLE().entrySet()) {
            if (neighbor.equals(msgEntry.getKey())) {continue;}
            msgVAR_TO_FUNC = msgVAR_TO_FUNC.addMessage(msgEntry.getValue());
          }
          
          // add all messages from getStored_FUNCTION_TO_VARIABLE
          msgVAR_TO_FUNC = msgVAR_TO_FUNC.addAllMessages(agent.getStored_FUNCTION_TO_VARIABLE().values());
          msgVAR_TO_FUNC.updateAlpha();
          msgVAR_TO_FUNC.setNewValueSet(agent.getMSvalueSet());

          long time = 0;
          agent.sendObjectMessageWithTime(neighbor, msgVAR_TO_FUNC, VAR_TO_FUNC, time);
        } 
        // process the function that I owned
        else if (agent.getFunctionIOwn().contains(neighbor)) {
          // add all messages from getReceived_FUNCTION_TO_VARIABLE
          msgVAR_TO_FUNC = msgVAR_TO_FUNC.addAllMessages(agent.getReceived_FUNCTION_TO_VARIABLE().values());

          // Except for the neighbor, add messages from getStored_FUNCTION_TO_VARIABLE
          for (Entry<AID, MaxSumMessage> msgEntry : agent.getStored_FUNCTION_TO_VARIABLE().entrySet()) {
            if (neighbor.equals(msgEntry.getKey())) {continue;}
            msgVAR_TO_FUNC = msgVAR_TO_FUNC.addMessage(msgEntry.getValue());
          }
          
          msgVAR_TO_FUNC.updateAlpha();
          msgVAR_TO_FUNC.setNewValueSet(agent.getMSvalueSet());
          
          agent.getStored_VARIABLE_TO_FUNCTION().put(neighbor, msgVAR_TO_FUNC);
        }
      }
    }
    waiting_store_VAR_TO_FUNC_message(VAR_TO_FUNC);
    
//    agent.resetFunc2VarMessageMap();
  }

  /**
   *  From all first derivative from FUNCTION_TO_VALUES messages
   *  Add those messages up and modify the values accordingly
   */
  private void modifyMSValuesUsingGradient() {
    double scalingFactor = agent.getGradientScalingFactor();
    
    Set<Double> newValueSet = new HashSet<>();
    
    for (Double oldValue : agent.getMSvalueSet()) {
      double sumGradient = 0;
      for (AID neighbor : agent.getNeighborAIDList()) {
        if (agent.getFunctionOwnedByOther().contains(neighbor)) {
          sumGradient = sumGradient + agent.getReceived_FUNCTION_TO_VARIABLE().get(neighbor).getFirstDerivativeMap().get(oldValue);
        }
        else if (agent.getFunctionIOwn().contains(neighbor)) {
          sumGradient = sumGradient + agent.getStored_FUNCTION_TO_VARIABLE().get(neighbor).getFirstDerivativeMap().get(oldValue);
        }
        newValueSet.add(oldValue + scalingFactor * sumGradient);
      }
    }
    
    agent.setMSvalueSet(newValueSet);
  }

  private void waiting_store_VAR_TO_FUNC_message(int msgCode) {
    int msgCount = 0;
    while (msgCount < agent.getFunctionIOwn().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.receive(template);
      
      if (receivedMessage != null) {
        MaxSumMessage maxsumMsg = null;
        try {
          maxsumMsg = (MaxSumMessage) receivedMessage.getContentObject();
        } catch (UnreadableException e) {
          e.printStackTrace();
        }
        agent.getReceived_VARIABLE_TO_FUNCTION().put(receivedMessage.getSender(), maxsumMsg);
        msgCount++;
      }
      else
        block();
    }
  }
}
