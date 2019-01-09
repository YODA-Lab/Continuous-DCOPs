package behavior;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import table.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import agent.DCOP;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;

import static agent.DcopInfo.*;
import static java.lang.Double.*;
import static java.lang.System.*;
/*	1. IF X is a root
 * 		Send the value of root to all the children
 *		PRINT OUT the value picked
 *		STOP
 * 
 *  2. ELSE (not a root)
 *  	Waiting from message from the parent
 *  	From the received parent_value, pick X_value from the store (parent_value, X_value)
 *  	//which is the corresponding X_value to parent_value with the minimum utility
 *  	2.1 IF (X is not a leaf)
 *  		Send the value to all the children
 *  	PRINT_OUT the picked value
 *  	STOP 
 */
/**
 * @author khoihd
 *
 */
public class DPOP_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = 4288241761322913640L;
	
	DCOP agent;
	
	public DPOP_VALUE(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
	  // Root do the same thing for all algorithms
	  if (agent.isRoot()) {
	    rootSendChosenValue();
	    return ;
	  }
	  
	  nonRootChooseAndSendValue();
	}

  /**
   * Non-root agent chooses and sends value to its children
   */
  private void nonRootChooseAndSendValue() {
    HashMap<String, Double> valuesFromParent = waitingForValuesFromParent(DPOP_VALUE);
    agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
    
    agent.getNeighborChosenValueMap().putAll(valuesFromParent);
    
    // Only choose value if running DPOP-like algorithm
    if (agent.algorithm == DCOP.DISCRETE_DPOP) {
      agent.setChosenValue(chooseValue_TABLE(valuesFromParent));
    } else if (agent.algorithm == DCOP.ANALYTICAL_DPOP || agent.algorithm == DCOP.APPROX_DPOP) {
      agent.setChosenValue(chooseValue_FUNCTION(valuesFromParent));
    } else if (agent.algorithm == DCOP.MOVING_DPOP | agent.algorithm == DCOP.CLUSTERING_DPOP) {
      agent.setChosenValue(chooseValue_HYBRID(valuesFromParent));
    }    
   
    //add its chosen value to the map to send to its children
    agent.setValuesToSendInVALUEPhase(valuesFromParent);
    agent.addValuesToSendInVALUEPhase(agent.getID(), agent.getChosenValue());     
    
    System.out.println("Agent " + agent.getID() + " has CHOSEN VALUE is " + agent.getChosenValue());
    
    agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
    
    if (agent.isLeaf() == false) {      
      for (AID children : agent.getChildrenAIDList()) {
        agent.sendObjectMessageWithTime(children, agent.getValuesToSendInVALUEPhase(), DPOP_VALUE, agent.getSimulatedTime());
      }
    } 
  }

  /**
   * This function has been REVIEWED
   * @param valuesFromParent
   * @return
   */
  private double chooseValue_FUNCTION(HashMap<String, Double> valuesFromParent) {
    Map<String, Double> valueMapOfOtherVariables = new HashMap<>();
    
    PiecewiseMultivariateQuadFunction agentViewFunction = agent.getAgentViewFunction();
    
    for (String agent : agent.getParentAndPseudoStrList()) {
      double value = valuesFromParent.get(agent);
      valueMapOfOtherVariables.put(agent, value);
    }
    
    agentViewFunction = agentViewFunction.evaluateToUnaryFunction(valueMapOfOtherVariables);
    
    double currentChosenValue = -Double.MAX_VALUE;
    double currentMax = -Double.MAX_VALUE;
    
    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> entry : agentViewFunction.getFunctionMap().entrySet()) {
      MultivariateQuadFunction function = entry.getKey();
      Set<Map<String, Interval>> intervalSet = entry.getValue();
      
      double[] maxArgmax = function.getMaxAndArgMax(intervalSet.iterator().next());
            
      if (Double.compare(maxArgmax[0], currentMax) > 0) {
        currentMax = maxArgmax[0];
        currentChosenValue = maxArgmax[1];
      }
    }
    
    return currentChosenValue;
  }

  /**
   * This function has been REVIEWED
   * Choose value from the agent view table
   * The agent view table surely has the values from the parent
   * @param valuesFromParent
   * @return
   */
  private double chooseValue_TABLE(HashMap<String, Double> valuesFromParent) {
    return agent.getAgentViewTable().getArgmaxGivenVariableAndValueMap(agent.getID(), valuesFromParent);
  }

  private double chooseValue_HYBRID(HashMap<String, Double> valuesFromParent) {
    if (agent.isLeaf()) {
      return leafValue_HYBRID(valuesFromParent);
    }
    else {
      return nonLeafValue_HYBRID(valuesFromParent);
    }
  }
  
  /**
   * Call the chooseValue_FUNCTION(valuesFromParent)
   * @param valuesFromParent
   * @return
   */
  private double leafValue_HYBRID(HashMap<String, Double> valuesFromParent) {
    return chooseValue_FUNCTION(valuesFromParent);
  }
  
  /**
   * This is called by a non-leaf agent in the HYBRID DPOP
   * 
   * @param valuesFromParent
   * @return
   */
  private double nonLeafValue_HYBRID(HashMap<String, Double> valuesFromParent) {
    return agent.getAgentViewTable().maxArgmaxHybrid(agent, valuesFromParent)[1];
  }

  /**
   * Send value to the children. The value is already chosen in the UTIL phase
   */
  private void rootSendChosenValue() {
    agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());

    System.out.println(agent.getID() + " choose value " + agent.getChosenValue());

    agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
    agent.addValuesToSendInVALUEPhase(agent.getID(), agent.getChosenValue());
    for (AID childrenAgentAID : agent.getChildrenAIDList()) {
      agent.sendObjectMessageWithTime(childrenAgentAID, agent.getValuesToSendInVALUEPhase(), DPOP_VALUE,
          agent.getSimulatedTime());
    }
  }

  public ArrayList<ACLMessage> waitingForMessageFromPseudoParent(int msgCode) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
		//no of messages are no of pseudoParent + 1 (parent)
		while (messageList.size() < agent.getPseudoParentAIDList().size() + 1) {
			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
				messageList.add(receivedMessage);
			}
			else
				block();
		}
		return messageList;
	}
	
	@SuppressWarnings("unchecked")
  public HashMap<String, Double> waitingForValuesFromParent(int msgCode) {
		ACLMessage receivedMessage = null;
    HashMap<String, Double> valuesFromParent = new HashMap<String, Double>();

		while (true) {
  		MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
  		receivedMessage = myAgent.receive(template);
  		if (receivedMessage != null) {
  			long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
  			if (timeFromReceiveMessage > agent.getSimulatedTime()) {
  				agent.setSimulatedTime(timeFromReceiveMessage);
  			}
  			break;
  		}
  		else
  			block();
		}
		
		agent.addupSimulatedTime(DCOP.getDelayMessageTime());
		
    try {
      valuesFromParent = (HashMap<String, Double>) receivedMessage.getContentObject();
    } catch (UnreadableException e) {
      e.printStackTrace();
    }
		
		return valuesFromParent;
	}

}
