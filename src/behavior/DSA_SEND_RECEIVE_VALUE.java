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
    if (agent.isRunningDiscreteAlg()) {
      createDCOPTableFromFunction(agent.getFunctionMap());
    }
    
    agent.getTableList(); // list of DCOP table
    
    double chosenValue = 0;
    
    agent.setValue(chosenValue); // store DSA value 
    agent.getValue(); // retrieve DSA value
    
    // The below commented function is used to join to DcopTable and return a new one
    // DPOP_UTIL.joinTable(table1, table2);
    
    // This is used to send message. Ignore the simulatedTime for now
    // agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DSA_VALUE, agent.getSimulatedTime());
    
    // Refer to waitingForMessageFromChildrenWithTime() in DPOP_UTIL and create a similar function here to wait for messages from all neighbors
	}
	
  /**
   * Create the DCOP tables from agent.getCurrentValueSet();
   */
  public void createDCOPTableFromFunction(Map<String, PiecewiseMultivariateQuadFunction> functionMap) {
    List<Table> tableList = new ArrayList<>();
    for (PiecewiseMultivariateQuadFunction pwFunction : functionMap.values()) {
      MultivariateQuadFunction func = pwFunction.getTheFirstFunction(); // there is only one function in pw at this time

      List<String> varListLabel = func.getVariableSet().stream().collect(Collectors.toList());
      Table tableFromFunc = new Table(varListLabel);

      // Always binary functions
      String variableOne = varListLabel.get(0);
      String variableTwo = varListLabel.get(1);

      for (Double valueOne : agent.getCurrentValueSet()) {
        Map<String, Double> valueMap = new HashMap<>();
        List<Double> rowValueList = new ArrayList<>();
        rowValueList.add(valueOne);
        valueMap.put(variableOne, valueOne);
        for (double valueTwo : agent.getCurrentValueSet()) {
          rowValueList.add(valueTwo);
          valueMap.put(variableTwo, valueTwo);
          Row newRow = new Row(new ArrayList<>(rowValueList), func.evaluateToValueGivenValueMap(valueMap));
          tableFromFunc.addRow(newRow);
          rowValueList.remove(1);
          valueMap.remove(variableTwo);
        }
        rowValueList.clear();
        valueMap.clear();
      }
      tableList.add(tableFromFunc);
    }
    agent.setTableList(tableList);
  }
}
