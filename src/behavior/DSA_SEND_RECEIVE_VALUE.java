package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import agent.DcopAgent;
import static agent.DcopInfo.*;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import table.Row;
import table.Table;

import jade.core.behaviours.OneShotBehaviour;

public class DSA_SEND_RECEIVE_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = 6680449924898094747L;

	private DcopAgent agent;
	
	/**
	 * @param agent
	 */
	public DSA_SEND_RECEIVE_VALUE(DcopAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
    
	  agent.getCurrentValueSet(); // retrieve the domain
    agent.getValue(); // retrieve DSA value which is already initialized in the first DSA iteration
	  
    agent.getTableList(); // list of DCOP table
    
    double chosenValue = 0;
    agent.setValue(chosenValue); // store DSA value 
    
    // The below commented function is used to join to DcopTable and return a new one
    // DPOP_UTIL.joinTable(table1, table2);
    
    // This is used to send message. Ignore the simulatedTime for now
    // agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DSA_VALUE, agent.getSimulatedTime());
    
    // Refer to waitingForMessageFromChildrenWithTime() in DPOP_UTIL and create a similar function here to wait for messages from all neighbors
	}
}
