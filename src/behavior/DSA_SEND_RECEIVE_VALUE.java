package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import agent.DcopAgent;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.behaviours.OneShotBehaviour;
import table.Row;
import table.Table;

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
