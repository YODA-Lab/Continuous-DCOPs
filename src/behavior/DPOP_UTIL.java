package behavior;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.Sets;

import static java.lang.System.out;
import static java.lang.Double.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import agent.DCOP;
import static agent.DcopInfo.*;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import table.Row;
import table.Table;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/*
 * This is UTIL phrase of DTREE
 * 1. If X is leaf THEN
 *    WE ASSUME EACH VALUE OF PARENT HAS AT LEAST ONE CORRESPONDING VALUES FROM CHILDREN 
 * 		FOR EACH value from domain(parent)
 * 			Calculate the minimum utility constraint (for each corresponding value of children)
 * 			, then store the minimum pair (parent, children)
 * 		Then combine all the parent_value, utility
 * 		Send this vector to the parent
 * 		STOP;
 * 
 * 2. ELSE (not a leaf)
 * 		Wait until receiving all messages from all the children
 * 		2.1 If X is a root THEN
 * 			FOR EACH value of X
 * 				sum the utility that received from all the children
 * 			pick the value with the minimum utility from all the children.
 * 			STOP;
 * 
 * 		2.2 X is not a root
 * 			FOR EACH value of X
 * 				sum the utility that received from all the children
 * 			So here, we have each pair of value of X, and corresponding utility for this subtree
 * 			FOR EACH value of parent X
 * 				Calculate the minimum utility BASED ON the SUM of (corresponding constraints, and
 * 															utility from this value of X constraints)
 * 				Store this pair of (parent_value, children_value, utility)
 * 			Combine all the value of (parent_value, utility) and send to the parent
 * 			STOP;  
 */
/**
 * @author khoihd
 *
 */
public class DPOP_UTIL extends OneShotBehaviour {

  private static final long serialVersionUID = -2438558665331658059L;

  DCOP agent;

  public DPOP_UTIL(DCOP agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    if (agent.isRunningDiscreteAlg()) {
      createDCOPTableFromFunction();
    }
    // At this point, all three algorithms have the same functions (or
    // transformed to tables)
    out.println("Done removing children!");

    agent.setSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

    if (agent.isRunningDiscreteAlg()) {
      doUtil_TABLE();
    } else if (agent.algorithm == DCOP.ANALYTICAL_DPOP || agent.algorithm == DCOP.APPROX_DPOP) {
      doUtil_FUNC();
    } else if (agent.isRunningHybridAlg()) {
      doUtil_HYBRID();
    }

    // long maxMemory = Runtime.getRuntime().maxMemory() / 10241024;
    // out.println(agent.getIdStr() + " Max memory after done: " + maxMemory);
  }
  
  private void doUtil_FUNC() {
    if (agent.isLeaf())
      leaf_FUNC();
    else if (agent.isRoot())
      root_FUNC();
    else
      internalNode_FUNC();
  }
  
  private void doUtil_TABLE() {
    if (agent.isLeaf())
      leaf_TABLE();
    else if (agent.isRoot())
      root_TABLE();
    else
      internalNode_TABLE();
  }
  
  private void doUtil_HYBRID() {  
    if (agent.isLeaf())
      leaf_HYBRID();
    else if (agent.isRoot())
      root_HYBRID();
    else
      internalNode_HYBRID();
  }

  /**
   * Create the DCOP tables from agent.getCurrentValueSet();
   */
  public void createDCOPTableFromFunction() {
    List<Table> tableListWithParents = new ArrayList<>();
    for (PiecewiseMultivariateQuadFunction pwFunction : agent.getPWFunctionWithPParentMap().values()) {
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
      tableListWithParents.add(tableFromFunc);
    }
    agent.setTableListWithPParents(tableListWithParents);
  }

  private void leaf_TABLE() {
    agent.startSimulatedTiming();
    
    out.println("LEAF " + agent.getID() + " is running");
    // get the first table
    Table combinedTable = agent.getTableListWithPParents().get(0);
    // combinedTable.printDecVar();
    // joining other tables with table 0
    int currentTableListDPOPsize = agent.getTableListWithPParents().size();
    for (int index = 1; index < currentTableListDPOPsize; index++) {
      Table pseudoParentTable = agent.getTableListWithPParents().get(index);
      // pseudoParentTable.printDecVar(); // KHOI PRINT
      combinedTable = joinTable(combinedTable, pseudoParentTable);
    }

    agent.setAgentViewTable(combinedTable);
    Table projectedTable = projectOperator(combinedTable, agent.getLocalName());

    // out.println("Projected table: " ); projectedTable.printDecVar();

    agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

    agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
  }

  /*
   */
  private void leaf_FUNC() { 
    agent.startSimulatedTiming();
  
    out.println("LEAF " + agent.getID() + " is running");

    List<PiecewiseMultivariateQuadFunction> pwFuncList = new ArrayList<>();
    pwFuncList.addAll(agent.getPWFunctionWithPParentMap().values());
    PiecewiseMultivariateQuadFunction combinedFunction = pwFuncList.get(0);
    for (int i = 1; i < pwFuncList.size(); i++) {
      combinedFunction = combinedFunction.addPiecewiseFunction(pwFuncList.get(i));
    }

    combinedFunction.setOwner(agent.getID());

    agent.setAgentViewFunction(combinedFunction);

    PiecewiseMultivariateQuadFunction projectedFunction = null;
    if (agent.algorithm == APPROX_DPOP) {
      projectedFunction = combinedFunction.approxProject(agent.getNumberOfPoints(), agent.getID(),
          agent.getNumberOfApproxAgents(), agent.isApprox());
    } else if (agent.algorithm == ANALYTICAL_DPOP) {
      projectedFunction = combinedFunction.analyticalProject();
    }

    agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

    try {
      agent.sendByteObjectMessageWithTime(agent.getParentAID(), projectedFunction, DPOP_UTIL, agent.getSimulatedTime());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * This function has been REVIEWED
   * 1. Move the values of parent and pseudo-parents 
   *    The values are the numberOfPoints
   * 2. After moving:
   *  - For each value combination, find the max from sum of utility function
   *  - Create UTIL message from the valueCombination and max value
   *  - Agent_view is not needed.
   */
  private void leaf_HYBRID() {
    agent.startSimulatedTiming();

    out.println("LEAF " + agent.getID() + " is running");
    
    // Sum up all functions to create agentViewFunction
    PiecewiseMultivariateQuadFunction sumFunction = new PiecewiseMultivariateQuadFunction();
    for (String ppAgent : agent.getParentAndPseudoStrList()) {
      sumFunction = sumFunction.addPiecewiseFunction(agent.getPWFunctionWithPParentMap().get(ppAgent));
    }
    
    agent.setAgentViewFunction(sumFunction);
    
    /*
     * Move the values of parent and pseudo-parents
     */
    Set<List<Double>> productPPValues = movingPointsUsingTheGradient(null, DONE_AT_LEAF);
    
   /*
    * After moving, find the max utility from all functions with parent and pseudo-parents
    * For each value list
    *  Compute the sum of the utility function at each value of the value list
    *  Find the max
    *  Add to the UTIL messages
    * End
    */
    List<String> label = agent.getParentAndPseudoStrList();
    
    Table utilTable = new Table(label);
    
    for (List<Double> valueList : productPPValues) {
      Map<String, Double> valueMapOfOtherVariables = new HashMap<>();

      for (int parentIndex = 0; parentIndex < agent.getParentAndPseudoStrList().size(); parentIndex++) {
        String pAgent = label.get(parentIndex);
        double pValue = valueList.get(parentIndex);

        valueMapOfOtherVariables.put(pAgent, pValue);
      }
      
      PiecewiseMultivariateQuadFunction unaryFunction = sumFunction.evaluateToUnaryFunction(valueMapOfOtherVariables);
      
      double max = -Double.MAX_VALUE;
      
      for (Map<String, Interval> interval : sumFunction.getTheFirstIntervalSet()) {
        double maxArgmax[] = unaryFunction.getTheFirstFunction().getMaxAndArgMax(interval);
        
        if (compare(maxArgmax[0], max) > 0) {
          max = maxArgmax[0];
        }
      }
      
      utilTable.addRow(new Row(valueList, max));
    }
    
//    out.println("utilTable");
//    out.println(utilTable);
    
    agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
    agent.sendObjectMessageWithTime(agent.getParentAID(), utilTable, DPOP_UTIL, agent.getSimulatedTime());
  }

  public void internalNode_TABLE() {
    out.println("INTERNAL node " + agent.getID() + " is running");

    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);

    // Start of processing 
    agent.startSimulatedTiming();
    
    // After combined, it becomes a unary function
    Table combinedUtilAndConstraintTable = combineMessage(receivedUTILmsgList);

    for (Table pseudoParentTable : agent.getTableListWithPParents()) {
      combinedUtilAndConstraintTable = joinTable(combinedUtilAndConstraintTable, pseudoParentTable);
    }

    agent.setAgentViewTable(combinedUtilAndConstraintTable);

    Table projectedTable = projectOperator(combinedUtilAndConstraintTable, agent.getLocalName());

    agent.pauseSimulatedTiming();
    
    agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
  }

  public void internalNode_FUNC() {
    out.println("INTERNAL node " + agent.getID() + " is running");

    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
    
    agent.startSimulatedTiming();
    
    System.out.println("Agent " + agent.getID() + " has received all UTIL messages");
    
    // UnaryPiecewiseFunction
    // PiecewiseMultivariateQuadFunction combinedFunctionMessage =
    // combineMessageToFunction(receivedUTILmsgList);
    PiecewiseMultivariateQuadFunction combinedFunctionMessage = null;
    try {
      combinedFunctionMessage = combineByteMessageToFunction(receivedUTILmsgList);
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    System.out.println(
        "Agent " + agent.getID() + " Internal node functions counts before joining rewards " + combinedFunctionMessage.size());

    for (PiecewiseMultivariateQuadFunction pseudoParentFunction : agent.getPWFunctionWithPParentMap().values()) {
      combinedFunctionMessage = combinedFunctionMessage.addPiecewiseFunction(pseudoParentFunction);
    }

    out.println("Agent " + agent.getID() + " Internal node number of combined function: "
        + combinedFunctionMessage.getFunctionMap().size());

    combinedFunctionMessage.setOwner(agent.getID());

    agent.setAgentViewFunction(combinedFunctionMessage);

    PiecewiseMultivariateQuadFunction projectedFunction = null;

    out.println("Agent " + agent.getID() + " Internal node number of combined function: "
        + combinedFunctionMessage.getFunctionMap().size());

    if (agent.algorithm == APPROX_DPOP) {
      projectedFunction = combinedFunctionMessage.approxProject(agent.getNumberOfPoints(), agent.getID(),
          agent.getNumberOfApproxAgents(), agent.isApprox());
    } else if (agent.algorithm == ANALYTICAL_DPOP) {
      projectedFunction = combinedFunctionMessage.analyticalProject();
    }

    out.println("Agent " + agent.getID() + " Internal node number of projected function: "
        + projectedFunction.getFunctionMap().size());

    agent.pauseSimulatedTiming();

    try {
      agent.sendByteObjectMessageWithTime(agent.getParentAID(), projectedFunction, DPOP_UTIL, agent.getSimulatedTime());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Join the UTIL tables from children and add up the utility functions to the table
   * This is the agent_view_table
   * 
   * For value combinations of pParents:
   *  Moving their values from the table using the derivative of the corresponding utility function
   *  Given the current combination, all the possbible values of agent {}, create the set of interpolated row
   *  Find the argmax and move that values
   * End
   * 
   * After moving, we need to find the corresponding utility for each value combination.
   * Interpolate the set of row, find the max
   * Send this message up to the 
   * 
   * Interpolate new points using the joined tables
   * Add the corresponding utility functions to the joinedTable
   * Then send this UTIL message to the parent
   */
  public void internalNode_HYBRID() {    
    out.println("INTERNAL node " + agent.getID() + " is running");

    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
    
    agent.startSimulatedTiming();
    
    Set<Table> tableSet = createTableSet(receivedUTILmsgList);
    
    // Interpolate points and join all the tables
    Table joinedTable = interpolateAndJoinTable(tableSet, NOT_ADD_POINTS);
    
    joinedTable = addTheUtilityFunctionsToTheJoinedTable(joinedTable);
    
    agent.setAgentViewTable(joinedTable);
        
    Set<List<Double>> productPPValues = movingPointsUsingTheGradient(joinedTable, DONE_AT_INTERNAL_NODE);
        
    Table utilTable = createUtilTableFromValueSet(joinedTable, productPPValues);
     
    agent.pauseSimulatedTiming();
    
    agent.sendObjectMessageWithTime(agent.getParentAID(), utilTable, DPOP_UTIL, agent.getSimulatedTime());
  }

  /**
   * This function has been REVIEWED
   * Create the utilTable from agentViewTable (which contains this agent) and productPPValues of pParents
   * @param agentViewTable
   * @param productPPValues
   * @return
   */
  private Table createUtilTableFromValueSet(Table agentViewTable, Set<List<Double>> productPPValues) {
    //Now calculate the new UTIL table to send to parent
    List<String> label = agent.getParentAndPseudoStrList();
    Table utilTable = new Table(label);

    // Interpolate the table to get values for each of the valueList
    for (List<Double> valueList : productPPValues) {     
      Map<String, Double> valueMap = new HashMap<>();
      for (int i = 0; i < valueList.size(); i++) {
        valueMap.put(agent.getParentAndPseudoStrList().get(i), valueList.get(i));
      }
      
      double maxUtil = agentViewTable.maxArgmaxHybrid(agent, valueMap)[0];
      
      Row newRow = new Row(valueList, maxUtil);

      // Add the utility of all functions to the row
      utilTable.addRow(newRow);
    }
    return utilTable;
  }

  /*
   * THIS FUNCTION IS REVIEWED
   * For each pp
   *  If joinedTable contains pp
   *    Evaluate the row by the function and update the row
   *  Else if joinedTable doesn't contain pp
   *    Get a list of pp values (interval.discretize())
   *    Create new table with the label.add(pParent)
   *    For each ppValue
   *      For each row 
   *        Get the <agent, value> and <pp, ppValue>, evaluate the function
   *        Create the new row with extending the valueList
   *        Add up to the utility value
   *      End
   *    End
   *  Endif
   * End
   *
   * @param joinedTable
   * @return
   */
  private Table addTheUtilityFunctionsToTheJoinedTable(Table joinedTable) {
    for (String pParent : agent.getParentAndPseudoStrList()) {
      PiecewiseMultivariateQuadFunction pFunction = agent.getPWFunctionWithPParentMap().get(pParent);
      // If containing pParent, update the utility 
      if (joinedTable.containsAgent(pParent)) {
        for (Row row : joinedTable.getRowList()) {
          Map<String, Double> valueMap = new HashMap<>();
          valueMap.put(pParent, row.getValueAtPosition(joinedTable.indexOf(pParent)));
          valueMap.put(agent.getID(), row.getValueAtPosition(joinedTable.indexOf(agent.getID())));
          row.setUtility(row.getUtility() + pFunction.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap));
        }
      }
      // Doesn't contain the pParent
      else {
        Table newTable = new Table(joinedTable.getLabel());
        newTable.extendToTheEndOfLabel(pParent);
        
        // Add values for the new label of pParent
        Set<Double> pValueList = agent.getCurrentValueSet();
        
        for (Double pValue : pValueList) {
          for (Row row : joinedTable.getRowList()) {
            Map<String, Double> valueMap = new HashMap<>();
            valueMap.put(pParent, pValue);
            valueMap.put(agent.getID(), row.getValueAtPosition(joinedTable.indexOf(agent.getID())));
            double newUtility = row.getUtility() + pFunction.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap); 
            
            Row newRow = new Row(row.getValueList(), newUtility);
            newRow.addValueToTheEnd(pValue);
            newTable.addRow(newRow);
          }
        }
        joinedTable = newTable;
      }
    }
    return joinedTable;
  }

  private Set<Table> createTableSet(List<ACLMessage> receivedUTILmsgList) {
    Set<Table> tableSet = new HashSet<>();
    for (ACLMessage msg : receivedUTILmsgList) {
      try {
        tableSet.add((Table) msg.getContentObject());
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    } 
    
    return tableSet;
  }

  /**
   * This function has been REVIEWED
   * Moving the values of parent and pseudo-parent
   * This function is called in both leaves and internal nodes.
   * There is a flag to differentiate between the two.
   * @param joinedTable this table is used in internal nodes. At leaf, it is null.
   * @param flag FUNCTION_ONLY or FUNCTION_AND_TABLE
   */
  private Set<List<Double>> movingPointsUsingTheGradient(Table joinedTable, int flag) {
    Set<List<Double>> immutableProductPPValues;
    // Create a set of PP's value list
    // Create a list of valueSet with the same ordering of PP
    // Then do the Cartesian product to get the set of valueList (same ordering as PP)
    List<Set<Double>> valueSetList = new ArrayList<Set<Double>>();
    for (String pParent : agent.getParentAndPseudoStrList()) {
      if (flag == DONE_AT_LEAF) {
        valueSetList.add(agent.getCurrentValueSet());
      } // The joined table might contain the PP or not
      else if (flag == DONE_AT_INTERNAL_NODE) {
        Set<Double> valueSetOfPParentToAdd = joinedTable.getValueSetOfGivenAgent(pParent, false);
        if (agent.isClustering()) {
          valueSetOfPParentToAdd.addAll(agent.getCurrentValueSet());
        }
        valueSetList.add(valueSetOfPParentToAdd);
      }
    }
    immutableProductPPValues = Sets.cartesianProduct(valueSetList);
    
    // Make the productPPValues to be mutable
    Set<List<Double>> mutableProductPPValues = new HashSet<>();
    for (List<Double> innerList : immutableProductPPValues) {
      List<Double> newList = new ArrayList<>(innerList);
      mutableProductPPValues.add(newList);
    }
    
    // Traverse the valueList
    for (int movingIteration = 0; movingIteration < agent.getGradientIteration(); movingIteration++) {
      for (List<Double> valueList : mutableProductPPValues) {

        // For each ppToMove (direction), take the derivative of the utility function
        for (int ppToMoveIndex = 0; ppToMoveIndex < valueList.size(); ppToMoveIndex++) {
          String ppAgentToMove = agent.getParentAndPseudoStrList().get(ppToMoveIndex);
          double ppValueToMove = valueList.get(ppToMoveIndex);

          PiecewiseMultivariateQuadFunction derivativePw = agent.getPWFunctionWithPParentMap().get(ppAgentToMove)
              .takeFirstPartialDerivative(ppAgentToMove);

          // Now find the arg_max
          Map<String, Double> valueMapOfOtherVariables = new HashMap<>();
          for (int ppIndex = 0; ppIndex < agent.getParentAndPseudoStrList().size(); ppIndex++) {
            String ppAgent = agent.getParentAndPseudoStrList().get(ppIndex);
            double ppValue = valueList.get(ppIndex);
            
            valueMapOfOtherVariables.put(ppAgent, ppValue);
          }
          
          double argMax = -Double.MAX_VALUE;
          if (flag == DONE_AT_LEAF) {
            PiecewiseMultivariateQuadFunction unaryFunction = agent.getAgentViewFunction().evaluateToUnaryFunction(valueMapOfOtherVariables);

            double max = -Double.MAX_VALUE;
            
            for (Map<String, Interval> interval : unaryFunction.getTheFirstIntervalSet()) {
              double maxArgmax[] = unaryFunction.getTheFirstFunction().getMaxAndArgMax(interval);
              
              if (compare(maxArgmax[0], max) > 0) {
                max = maxArgmax[0];
                argMax = maxArgmax[1];
              }
            }
          } else if (flag == DONE_AT_INTERNAL_NODE){
            argMax = agent.getAgentViewTable().maxArgmaxHybrid(agent, valueMapOfOtherVariables)[1];
          }
          
          Map<String, Double> valueMap = new HashMap<>();
          valueMap.put(agent.getID(), argMax);
          valueMap.put(ppAgentToMove, ppValueToMove);

          double gradient = derivativePw.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap);
          
          double movedPpValue = ppValueToMove + agent.getGradientScalingFactor() * gradient;
          
          // only move if the new point is within the interval
          if (agent.getGlobalInterval().contains(movedPpValue)) {         
            valueList.set(ppToMoveIndex, movedPpValue);
          }
        }
      }
    }
    
    if (agent.isClustering()) {
      Set<List<Double>> centroids = kmeanCluster(mutableProductPPValues, agent.getNumberOfPoints());
      return centroids;
    } else {
      return mutableProductPPValues;
    }
  }

  private void root_FUNC() {
    out.println("ROOT node " + agent.getID() + " is running");

    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
    
    // Start of processing time
    agent.startSimulatedTiming();
    
    PiecewiseMultivariateQuadFunction combinedFunctionMessage = null;
    try {
      combinedFunctionMessage = combineByteMessageToFunction(receivedUTILmsgList);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (PiecewiseMultivariateQuadFunction pseudoParentFunction : agent.getPWFunctionWithPParentMap().values()) {
      combinedFunctionMessage = combinedFunctionMessage.addPiecewiseFunction(pseudoParentFunction);
    }

    combinedFunctionMessage.setOwner(agent.getID());

    // choose the maximum
    double argmax = -Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;

    // out.println("Root Combined function: " + combinedFunctionMessage);

    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : combinedFunctionMessage.getFunctionMap()
        .entrySet()) {
      MultivariateQuadFunction function = functionEntry.getKey();
      for (Map<String, Interval> intervalMap : functionEntry.getValue()) {
        double[] maxAndArgMax = function.getMaxAndArgMax(intervalMap);

        if (compare(maxAndArgMax[0], max) > 0) {
          max = maxAndArgMax[0];
          argmax = maxAndArgMax[1];
        }
      }
    }
    
    agent.setChosenValue(argmax);

    out.println("MAX VALUE IS " + max);
    out.println("ARGMAX VALUE IS " + argmax);

    agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
    
    agent.pauseSimulatedTiming();
  }

  private void root_TABLE() {
    out.println("ROOT node " + agent.getID() + " is running");
    
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);

    // Start of processing time
    agent.startSimulatedTiming();

    Table combinedUtilAndConstraintTable = combineMessage(receivedUTILmsgList);
    // combinedUtilAndConstraintTable.printDecVar();
    for (Table pseudoParentTable : agent.getTableListWithPParents()) {
      combinedUtilAndConstraintTable = joinTable(combinedUtilAndConstraintTable, pseudoParentTable);
    }

    out.println("Root is finding max and argmax");

    // pick value with smallest utility
    // since agent 0 is always at the beginning of the row formatted:
    // agent0,agent1,..,agentN -> utility
    double maxUtility = Integer.MIN_VALUE;
    // System.err.println("Timestep " + agent.getCurrentTS() + " Combined
    // messages at root:");
    // combinedUtilAndConstraintTable.printDecVar();
    for (Row row : combinedUtilAndConstraintTable.getRowList()) {
      if (row.getUtility() > maxUtility) {
        maxUtility = row.getUtility();
        agent.setChosenValue(row.getValueAtPosition(0));
      }
    }

    out.println("CHOSEN: " + agent.getChosenValue());

    out.println(DCOP.algTypes[agent.algorithm] + " utility " + maxUtility);

    agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
    
    agent.pauseSimulatedTiming();
  }
  
  /**
   * This function has been REVIEWED
   */
  private void root_HYBRID() {
    out.println("ROOT " + agent.getID() + " is running");
    
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);

    // Start of processing time
    agent.startSimulatedTiming();
    
    Set<Table> tableSet = createTableSet(receivedUTILmsgList);
    
    // Interpolate points and join all the tables
    Table joinedTable = interpolateAndJoinTable(tableSet, ADD_MORE_POINTS);
    
    out.println("Joined table: " + joinedTable);
    
    double maxUtility = -Double.MAX_VALUE;
    
    // Find the maxUtility and argmax from the joinedTable
    for (Row row : joinedTable.getRowList()) {
      if (compare(row.getUtility(), maxUtility) > 0) {
        // only choose the row with value in the interval
        maxUtility = row.getUtility();
        agent.setChosenValue(row.getValueList().get(0));
      }
    }

    out.println("CHOSEN: " + agent.getChosenValue());

    out.println(DCOP.algTypes[agent.algorithm] + " utility " + maxUtility);

    agent.pauseSimulatedTiming();
  }
  
  /**
   * This function has been REVIEWED
   * Interpolate points that are not common among tables <br>
   * Fill in the table with interpolated points <br>
   * @param tableCollection
   * @return the joined table after interpolation
   */
  private Table interpolateAndJoinTable(Collection<Table> tableCollection, boolean isAddingPoints) {    
    // Find the common variables
    Set<String> commonVariables = new HashSet<>();
    commonVariables.addAll(tableCollection.iterator().next().getLabel());
    for (Table utilTable : tableCollection) {
      commonVariables.retainAll(utilTable.getLabel());
    }
            
    /*
     * For each table, find all value combination of the common variables from other tables
     */
    Map<Table, Set<Row>> interpolatedRowSetOfEachTable = new HashMap<>();
    Map<String, Set<Double>> valueFromAllTableMap = new HashMap<>();
    
    /*
     * Traverse every table => create the map <Agent, Set<Double>>
     */
    for (Table utilTable : tableCollection) { 
      for (String commonAgent : commonVariables) {
        Set<Double> valueSetOtherTableGivenAgent = utilTable.getValueSetOfGivenAgent(commonAgent, true);
        
        if (isAddingPoints == ADD_MORE_POINTS) {
          valueSetOtherTableGivenAgent.addAll(agent.getGlobalInterval().getMidPointInIntegerRanges());
        }
        
        if (valueFromAllTableMap.containsKey(commonAgent)) {
          valueFromAllTableMap.get(commonAgent).addAll(valueSetOtherTableGivenAgent);
        } else {
          valueFromAllTableMap.put(commonAgent, new HashSet<>(valueSetOtherTableGivenAgent));
        }
      }
    }
    
    /*
     * For each table => do the interpolation and add them to the list
     */
    for (Table utilTable : tableCollection) {
      interpolatedRowSetOfEachTable.put(utilTable, utilTable.interpolateGivenValueSetMap(valueFromAllTableMap));
    }

//    out.println("Interpolated row set: ");
//    out.println(interpolatedRowSetOfEachTable);
    
    // Add the interpolated row to the corresponding table
    for (Entry<Table, Set<Row>> entry : interpolatedRowSetOfEachTable.entrySet()) {
      entry.getKey().addRowSet(entry.getValue());
    }
    
    // Now joining all the tables
    Table joinedTable = null;
    for (Table table : interpolatedRowSetOfEachTable.keySet()) {
      joinedTable = joinTable(joinedTable, table);
    }
    
    return joinedTable;
  }
  
  private Set<List<Double>> kmeanCluster(Set<List<Double>> dataset, int numClusters) {
    Set<List<Double>> centroids = new HashSet<>();
    SimpleKMeans kmean = new SimpleKMeans();
    
    ArrayList<Attribute> attritbuteList = new ArrayList<Attribute>();
    int numberAttributes = dataset.iterator().next().size();
    for (int i = 1; i <= numberAttributes; i++) {
      attritbuteList.add(new Attribute(String.valueOf(i)));
    }

    Instances instances = new Instances("cluster", attritbuteList, dataset.size());
    for (List<Double> rawData : dataset) {
      Instance pointInstance = new SparseInstance(1.0, rawData.stream().mapToDouble(Double::doubleValue).toArray());
      instances.add(pointInstance);
    }
    
    try {
      kmean.setNumClusters(agent.getNumberOfPoints());
      kmean.buildClusterer(instances);
      
      Instances centroidsInstance = kmean.getClusterCentroids();
      for (Instance pointArray : centroidsInstance) {
        centroids.add(Arrays.stream(pointArray.toDoubleArray()).boxed().collect(Collectors.toList()));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return centroids;
  }

  /**
   * Agent is waiting for and then process received messages from children. <br>
   * The simulated time is computed properly.
   * @param msgCode
   * @return
   */
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
    // Start of waiting time for the message
    agent.startSimulatedTiming();    
    
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getChildrenAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.receive(template);

      if (receivedMessage != null) {
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
        if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        } else {
          agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
        }
        
        messageList.add(receivedMessage);
      } else
        block();
    }
    
    return messageList;
  }

  // constraintTableAtEachTSMap is constructed in collapsing table (decision and
  // random)
  // get table lists from constraintTableAtEachTSMap at currentTS timeStep
  // remove children tables from that list
//  private void removeChildrenTableFromTableList(int currentTimeStep) {
//    /** Remove children and pseudoChildren constraint table */
//    List<AID> childAndPseudoChildrenAIDList = new ArrayList<>();
//    childAndPseudoChildrenAIDList.addAll(agent.getChildrenAIDList());
//    childAndPseudoChildrenAIDList.addAll(agent.getPseudoChildrenAIDList());
//
//    List<String> childAndPseudoChildrenStrList = new ArrayList<>();
//    for (AID pscChildrenAID : childAndPseudoChildrenAIDList) {
//      childAndPseudoChildrenStrList.add(pscChildrenAID.getLocalName());
//    }
//
//    List<Table> constraintTableToBeRemove = new ArrayList<>();
//    // get table list, then remove children table
//    List<Table> tableListAtCurrentTS = new ArrayList<>(agent.getConstraintTableAtEachTSMap().get(currentTimeStep));
//    out.println("Agent " + agent.getAgentStrID() + " size" + agent.getConstraintTableAtEachTSMap().get(currentTimeStep).size());
//
//    // for (Table constraintTable:constraintTableAtEachTSMap.get(currentTS)) {
//    for (Table constraintTable : tableListAtCurrentTS) {
//      List<String> decLabelList = constraintTable.getLabel();
//      boolean hasChildren = false;
//      for (String children : childAndPseudoChildrenStrList) {
//        if (decLabelList.contains(children)) {
//          hasChildren = true;
//          break;
//        }
//      }
//      if (hasChildren)
//        constraintTableToBeRemove.add(constraintTable);
//    }
//
//    for (Table removeTable : constraintTableToBeRemove) {
//      tableListAtCurrentTS.remove(removeTable);
//    }
//
//    agent.setTableListWithPParents(tableListAtCurrentTS);
//  }

  // create indexList1
  // la vi tri cua tung variable (tu nho den lon)
  // o trong row1, ma nam trong commonVariables

  // create indexList2
  // la vi tri cua tung variable (tu nho den lon)
  // o trong row2, ma nam trong commonVariables

  // join(label1, label2) as the same order

  // for each row1 of table1.getTable()
  // for each row2 of table2.getTable()
  // join(row1, row2, indexList1, indexList2)
  public static Table joinTable(Table table1, Table table2) {
    if (null == table1) return table2;
    if (null == table2) return table1;
    
    // get commonVariables
    List<String> commonVariables = getCommonVariables(table1.getLabel(), table2.getLabel());

    // create indexList1, indexList2
    // xet tung variable commonVariables
    // add index of that variable to the indexList
    List<Integer> indexContainedInCommonList1 = new ArrayList<Integer>();
    List<Integer> indexContainedInCommonList2 = new ArrayList<Integer>();
    for (String variable : commonVariables) {
      indexContainedInCommonList1.add(table1.getLabel().indexOf(variable));
      indexContainedInCommonList2.add(table2.getLabel().indexOf(variable));
    }

    // create returnTable
    // join label
    List<String> joinedLabelTable1FirstThenTable2 = getJoinLabel(table1.getLabel(), table2.getLabel(),
        indexContainedInCommonList2);

    Table joinedTable = new Table(joinedLabelTable1FirstThenTable2);
    for (Row row1 : table1.getRowList()) {
      for (Row row2 : table2.getRowList()) {
        Row joinedRow = getJoinRow(row1, row2, indexContainedInCommonList1, indexContainedInCommonList2);
        if (joinedRow != null)
          joinedTable.addRow(joinedRow);
      }
    }

    return joinedTable;
  }

  static List<String> getCommonVariables(List<String> variableList1, List<String> variableList2) {
    List<String> commonVariableList = new ArrayList<String>(variableList1);
    commonVariableList.retainAll(variableList2);

    return commonVariableList;
  }

  // for variable1 from label1, add to joinedLabel
  // for variable2 from label2
  // if index not in indexContainedInCommonList2
  // then add to joinedLabel
  public static List<String> getJoinLabel(List<String> label1, List<String> label2, List<Integer> indexContainedInCommonList2) {

    List<String> joinedLabel = new ArrayList<>();// (label1);
    for (String variable1 : label1) {
      joinedLabel.add(variable1);
    }

    // add variable with index not in indexContainedInCommonList2
    for (int i = 0; i < label2.size(); i++) {
      if (!indexContainedInCommonList2.contains(i))
        joinedLabel.add(label2.get(i));
    }

    return joinedLabel;
  }

  public static Row getJoinRow(Row row1, Row row2, List<Integer> indexList1, List<Integer> indexList2) {

    // check if same size
    if (indexList1.size() != indexList2.size()) {
      System.err.println("Different size from indexList: " + indexList1.size() + " " + indexList2.size());
      return null;
    }

    int listSize = indexList1.size();
    // check if same values
    for (int i = 0; i < listSize; i++) {
      if (row1.getValueList().get(indexList1.get(i)).equals(row2.getValueList().get(indexList2.get(i))) == false) {
        // out.println("Different values here!");
        return null;
      }
    }

    // join two row
    List<Double> joinedValues = new ArrayList<Double>();// (row1.getValueList());
    for (Double value1 : row1.getValueList()) {
      joinedValues.add(value1);
    }

    for (int i = 0; i < row2.getValueList().size(); i++) {
      if (indexList2.contains(i) == false)
        joinedValues.add(row2.getValueList().get(i));
    }

    Row joinedRow = new Row(joinedValues, row1.getUtility() + row2.getUtility());
    return joinedRow;
  }

  // create new TabelDPOP
  // create new Label: eliminate variableToProject
  // create new Table with -1 dimension
  // create checkedList mark already picked tuples
  // for each tuple1 from the table
  // if index(tuple1) already in picked tuple => continue
  // for each tuple2:tuple1->end from the table
  // compare to the minimum , and update
  // add to new Table
  public static Table projectOperator(Table table, String variableToProject) {
    int indexOfVariableToProject = getIndexOfContainedVariable(table.getLabel(), variableToProject);
    if (indexOfVariableToProject == -1) {
      return null;
    }

    // create arrayIndex
    List<Integer> indexesOfOtherVariables = new ArrayList<Integer>();
    for (int i = 0; i < table.getLabel().size(); i++) {
      if (i != indexOfVariableToProject)
        indexesOfOtherVariables.add(i);
    }

    // create checkedList
    List<Integer> listOfComparedRow = new ArrayList<Integer>();

    // create projectedLabel
    List<String> labelOfProjectedTable = createTupleFromList(table.getLabel(), indexesOfOtherVariables);

    // create projectedTable
    Table projectTable = new Table(labelOfProjectedTable);
    for (int rowIndexOfOriginTable = 0; rowIndexOfOriginTable < table.getRowCount(); rowIndexOfOriginTable++) {
      if (listOfComparedRow.contains(rowIndexOfOriginTable) == true)
        continue;
      listOfComparedRow.add(rowIndexOfOriginTable);
      Row row1 = table.getRowList().get(rowIndexOfOriginTable);
      List<Double> tuple1 = createTupleFromRow(row1, indexesOfOtherVariables);
      double maxUtility = row1.getUtility();
      List<Double> maxTuple = tuple1;

      for (int j = rowIndexOfOriginTable + 1; j < table.getRowCount(); j++) {
        Row row2 = table.getRowList().get(j);
        List<Double> tuple2 = createTupleFromRow(row2, indexesOfOtherVariables);
        double row2Utility = row2.getUtility();
        if (isSameTuple(tuple1, tuple2) == true) {
          listOfComparedRow.add(j);
          if (row2Utility > maxUtility) {
            maxUtility = row2Utility;
            maxTuple = tuple2;
          }
        }
      }

      projectTable.addRow(new Row(maxTuple, maxUtility));
    }

    return projectTable;
  }

  /**
   * @param table
   *          The table to project out
   * @param agentToProjectOut
   *          The agent to project out from the table
   * @param valueSet
   *          Value list of the agent
   * @return The Map containing: <value_of_the_agent_to_project, argmax>
   */
  public static Map<Double, Double> findTheArgmaxInHybridMaxSum(Table table, String agentToProjectOut, Set<Double> valueSet) {
    Map<Double, Double> argmaxMap = new HashMap<>();
    int indexToProject = table.getLabel().indexOf(agentToProjectOut);

    Set<Integer> indexesOfComparedRow = new HashSet<>();
    for (double valueToFindArgmax : valueSet) {
      double maxUtility = -MAX_VALUE;
      Row currentBestRow = null;
      for (int rowIndex = 0; rowIndex < table.getRowList().size(); rowIndex++) {
        // This row has been used to compared
        if (indexesOfComparedRow.contains(rowIndex)) {
          continue;
        }

        Row row = table.getRowList().get(rowIndex);
        if (compare(row.getValueList().get(indexToProject), valueToFindArgmax) != 0) {
          continue;
        }

        if (compare(row.getUtility(), maxUtility) > 0) {
          maxUtility = row.getUtility();
          currentBestRow = row;
          indexesOfComparedRow.add(rowIndex);
        }
      }
      List<Double> valuesOfOtherVariables = new ArrayList<>(currentBestRow.getValueList());
      valuesOfOtherVariables.remove(indexToProject);
      // Binary constraints
      argmaxMap.put(valueToFindArgmax, valuesOfOtherVariables.get(0));
    }

    return argmaxMap;
  }

  public static int getIndexOfContainedVariable(List<String> list, String input) {
    return list.indexOf(input);
  }

  // create tuples from Row and arrayIndex
  public static List<String> createTupleFromList(List<String> list, List<Integer> arrayIndex) {
    if (arrayIndex.size() >= list.size()) {
      // System.err.println("Cannot create tuple with size: " + arrayIndex + "
      // from Row size: " +
      // list.size());
      return null;
    }
    List<String> newTuple = new ArrayList<>();
    for (Integer index : arrayIndex) {
      newTuple.add(list.get(index));
    }
    return newTuple;
  }

  // create tuples from Row and arrayIndex
  public static List<Double> createTupleFromRow(Row row, List<Integer> arrayIndex) {
    if (arrayIndex.size() >= row.getNumberOfVariables()) {
      // System.err.println("Cannot create tuple with size: " + arrayIndex + "
      // from Row size: " +
      // row.variableCount);
      return null;
    }
    List<Double> newTuple = new ArrayList<>();
    for (Integer index : arrayIndex) {
      newTuple.add(row.getValueAtPosition(index));
    }
    return newTuple;
  }

  // check if two tuples has the same values with the same ordering
  public static boolean isSameTuple(List<Double> tuple1, List<Double> tuple2) {
    if (tuple1.size() != tuple2.size()) {
      System.err.println("Different size from two tuples: " + tuple1.size() + " and " + tuple2.size());
      return false;
    }
    int size = tuple1.size();
    for (int i = 0; i < size; i++) {
      if (tuple1.get(i).equals(tuple2.get(i)) == false) {
        return false;
      }
    }
    return true;
  }

  // for each value of X
  // for each message received from the children
  // sum the utility that received from the children
  private Table combineMessage(List<ACLMessage> list) {
    List<Table> listTable = new ArrayList<Table>();
    for (ACLMessage msg : list) {
      try {
        listTable.add((Table) msg.getContentObject());
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }

    int size = listTable.size();
    Table table = listTable.get(0);

    for (int i = 1; i < size; i++) {
      table = joinTable(table, listTable.get(i));
    }

    return table;
  }

  @SuppressWarnings("unused")
  private PiecewiseMultivariateQuadFunction combineMessageToFunction(List<ACLMessage> list) {
    List<PiecewiseMultivariateQuadFunction> listFunction = new ArrayList<>();
    for (ACLMessage msg : list) {
      try {
        listFunction.add((PiecewiseMultivariateQuadFunction) msg.getContentObject());
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }

    int size = listFunction.size();
    PiecewiseMultivariateQuadFunction function = listFunction.get(0);

    for (int i = 1; i < size; i++) {
      function = function.addPiecewiseFunction(listFunction.get(i));
    }

    return function;
  }

  private PiecewiseMultivariateQuadFunction combineByteMessageToFunction(List<ACLMessage> list)
      throws IOException, ClassNotFoundException {
    List<PiecewiseMultivariateQuadFunction> listFunction = new ArrayList<>();
    for (ACLMessage msg : list) {
      ByteArrayInputStream bais = new ByteArrayInputStream(msg.getByteSequenceContent());
      GZIPInputStream gzipIn = new GZIPInputStream(bais);
      ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
      PiecewiseMultivariateQuadFunction func = (PiecewiseMultivariateQuadFunction) objectIn.readObject();
      objectIn.close();
      listFunction.add(func);
    }

    int size = listFunction.size();
    PiecewiseMultivariateQuadFunction function = listFunction.get(0);

    for (int i = 1; i < size; i++) {
      function = function.addPiecewiseFunction(listFunction.get(i));
    }

    return function;
  }
}
