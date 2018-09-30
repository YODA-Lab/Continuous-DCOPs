package behaviour;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import agent.DCOP;
import agent.DcopInfo;
import agent.DcopInfo.SolvingType;
//import function.BinaryFunction;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
//import function.binary.PiecewiseFunction;
//import function.binary.QuadraticBinaryFunction;
//import function.binary.QuadraticUnaryFunction;
import table.Row;
import table.Table;

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
public class DPOP_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -2438558665331658059L;

	DCOP agent;
	DcopInfo.SolvingType solvingType;
	
	public DPOP_UTIL(DCOP agent) {
		super(agent);
		this.agent = agent;
		solvingType = SolvingType.ANALYTICALLY;
	}
	
	@Override
	public void action() {
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		agent.setCurrentUTILstartTime(System.currentTimeMillis());
		agent.setCurrentTableListDPOP(null);
		
		if (agent.algorithm == DCOP.DPOP) {
		  System.out.println("Removing functions that contains children or pseudochildren...");
			removeChildrenFunctionFromFunctionList(0);
		}
				
		agent.setSimulatedTime(agent.getSimulatedTime()
						+ agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		if (agent.isRoot()) System.out.println(agent.getIdStr() + ": I am root");
		else if (agent.isLeaf()) System.out.println(agent.getIdStr() + ": I am leaf, my parent is " + agent.getParentAID().getLocalName());
		else {System.out.println(agent.getIdStr() + ": I am internal node, my parent is " + agent.getParentAID().getLocalName());}
		
		if (agent.isLeaf()) {
//			leafDoUtilProcess();
      if (agent.algorithm == DCOP.DPOP) System.out.println("LEAF is running");
		  leafDoFuncUtilProcess();
			if (agent.algorithm == DCOP.DPOP) System.out.println("LEAF done");
		} 
		else if (agent.isRoot()){
//			rootDoUtilProcess();
		  rootDoFuncUtilProcess();
			
//			if (agent.algorithm == DCOP.REACT || agent.algorithm == DCOP.HYBRID) {
//				writeTimeToFile();
//			}
//			if (agent.algorithm == DCOP.FORWARD || agent.algorithm == DCOP.BACKWARD) {
//				if (agent.getCurrentTS() == agent.h)
//					Utilities.writeUtil_Time_FW_BW(agent);
//			}
		}
		else {
//			internalNodeDoUtilProcess();
      if (agent.algorithm == DCOP.DPOP) System.out.println("INTERNAL node is running");
		  internalNodeDoFuncUtilProcess();
			if (agent.algorithm == DCOP.DPOP) System.out.println("INTERNAL node done");
		}
	}		
	
	public void leafDoUtilProcess() {
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		//get the first table
		Table combinedTable = agent.getCurrentTableListDPOP().get(0);
//		combinedTable.printDecVar();
		//joining other tables with table 0
		int currentTableListDPOPsize = agent.getCurrentTableListDPOP().size();
		for (int index = 1; index<currentTableListDPOPsize; index++) {
			Table pseudoParentTable = agent.getCurrentTableListDPOP().get(index);
//			pseudoParentTable.printDecVar(); // KHOI PRINT
			combinedTable = joinTable(combinedTable, pseudoParentTable);
		}
		
		agent.setAgentViewTable(combinedTable);
		Table projectedTable = projectOperator(combinedTable, Double.parseDouble(agent.getLocalName()));

//    System.out.println("Projected table: " ); projectedTable.printDecVar();

		agent.setSimulatedTime(agent.getSimulatedTime()
					+ agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
//		projectedTable.printDecVar();
	}
	
	/*
	 */
  public void leafDoFuncUtilProcess() {
    agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());

    // Combine all functions in the leaf
    PiecewiseMultivariateQuadFunction combinedFunction = agent.getCurrentFunctionListDPOP().get(0);
    for (int i = 1; i < agent.getCurrentFunctionListDPOP().size(); i++) {
      combinedFunction = combinedFunction.addPiecewiseFunction(agent.getFunctionList().get(i));
    }
    
    agent.setAgentViewFunction(combinedFunction);
    
    System.out.println("Leaf Combined function: " + combinedFunction);
    
    PiecewiseMultivariateQuadFunction projectedFunction = combinedFunction.approxProject(agent.getNumberOfIntervals());
    
    System.out.println("Leaf Projected function: " + projectedFunction);
    
    agent.setSimulatedTime(
        agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
    
    agent.sendObjectMessageWithTime(agent.getParentAID(), projectedFunction, DPOP_UTIL, agent.getSimulatedTime());
  }
	
	
	public void internalNodeDoUtilProcess() {			
		List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
			
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		// After combined, it becomes a unary function
		Table combinedUtilAndConstraintTable = combineMessage(receivedUTILmsgList);
	
		for (Table pseudoParentTable : agent.getCurrentTableListDPOP()) {
			combinedUtilAndConstraintTable = joinTable(combinedUtilAndConstraintTable, pseudoParentTable);
		}

		agent.setAgentViewTable(combinedUtilAndConstraintTable);

		Table projectedTable = projectOperator(combinedUtilAndConstraintTable, Double.parseDouble(agent.getLocalName()));
		
		agent.setSimulatedTime(agent.getSimulatedTime() +
					agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
	}
	
  public void internalNodeDoFuncUtilProcess() {
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);

    agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
    
    // UnaryPiecewiseFunction
    PiecewiseMultivariateQuadFunction combinedFunctionMessage = combineMessageToFunction(receivedUTILmsgList);
    
    for (int i = 0; i < agent.getCurrentFunctionListDPOP().size(); i++) {
      combinedFunctionMessage = combinedFunctionMessage.addPiecewiseFunction(agent.getFunctionList().get(i));
    }
     
    agent.setAgentViewFunction(combinedFunctionMessage);

    PiecewiseMultivariateQuadFunction projectedFunction = combinedFunctionMessage.approxProject(agent.getNumberOfIntervals());
    
    System.out.println("Internal node Projected function: " + projectedFunction);
    
    agent.setSimulatedTime(
        agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

    agent.sendObjectMessageWithTime(agent.getParentAID(), projectedFunction, DPOP_UTIL, agent.getSimulatedTime());
  }
	
	public void rootDoFuncUtilProcess() {
	  List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
	  agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
    PiecewiseMultivariateQuadFunction combinedFunctionMessage = combineMessageToFunction(receivedUTILmsgList);
        
    for (PiecewiseMultivariateQuadFunction pseudoParentFunction : agent.getCurrentFunctionListDPOP()) {
      combinedFunctionMessage = combinedFunctionMessage.addPiecewiseFunction(pseudoParentFunction);   
    }
    
    // choose the maximum
    double argmax = -Double.MAX_VALUE; 
    double max = -Double.MAX_VALUE;
    
    System.out.println("Root Combined function: " + combinedFunctionMessage);
    
    for (MultivariateQuadFunction f : combinedFunctionMessage.getFunctionList()) {
      double[] maxAndArgMax = f.getMaxAndArgMax();
      
      if (Double.compare(maxAndArgMax[0], max) > 0) {
        max = maxAndArgMax[0];
        argmax = maxAndArgMax[1];
      }
    }
    
    System.out.println("MAX VALUE IS " + max);
    System.out.println("ARGMAX VALUE IS " + argmax);
	}
	   
  public void rootDoUtilProcess() {
		List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		Table combinedUtilAndConstraintTable = combineMessage(receivedUTILmsgList);
//		combinedUtilAndConstraintTable.printDecVar();
		for (Table pseudoParentTable:agent.getCurrentTableListDPOP()) {
			combinedUtilAndConstraintTable = joinTable(combinedUtilAndConstraintTable, pseudoParentTable);
		}
		
		//pick value with smallest utility
		//since agent 0 is always at the beginning of the row formatted: agent0,agent1,..,agentN -> utility
		double maxUtility = Integer.MIN_VALUE;
//		System.err.println("Timestep " +  agent.getCurrentTS() + " Combined messages at root:");
//		combinedUtilAndConstraintTable.printDecVar();
		for (Row row:combinedUtilAndConstraintTable.getTable()) {
			if (row.getUtility() > maxUtility) {
				maxUtility = row.getUtility();
				agent.setChosenValue(row.getValueAtPosition(0));
			}
		}
			
		System.out.println("CHOSEN: " + agent.getChosenValue());
		

		if (agent.algorithm == DCOP.LS_SDPOP) {
		}
		else if (agent.algorithm == DCOP.SDPOP) {
		}

		else if (agent.algorithm == DCOP.DPOP) {
			System.err.println("C_DPOP utility " + maxUtility);
		}
		
		agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime()
							- agent.getCurrentStartTime());
	}
	
	public List<ACLMessage> waitingForMessageFromChildren(int msgCode) {
		List<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < agent.getChildrenAIDList().size()) {
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
	
	public List<ACLMessage> waitingFromRandChildren(int msgCode, int noRandChild) {
		List<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < noRandChild) {
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
	
	public List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
		List<ACLMessage> messageList = new ArrayList<ACLMessage>();

		while (messageList.size() < agent.getChildrenAIDList().size()) {
			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.receive(template);
			
			if (receivedMessage != null) {
//				System.out.println("Agent " + getLocalName() + " receive message "
//						+ msgTypes[msgCode] + " from Agent " + receivedMessage.
//						getSender().getLocalName());

				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
				if (timeFromReceiveMessage > agent.getSimulatedTime())
					agent.setSimulatedTime(timeFromReceiveMessage);
				messageList.add(receivedMessage);	
			}
			else
				block();
		}
		agent.setSimulatedTime(agent.getSimulatedTime() + DCOP.getDelayMessageTime());
		return messageList;
	}
	
	
	/**
	 * Remove any function that contains children or pseudoChildren.
	 * @param currentTimeStep
	 */
	public void removeChildrenFunctionFromFunctionList(int currentTimeStep) {
	  List<PiecewiseMultivariateQuadFunction> funcList = new ArrayList<>();
	  Set<AID> childAndPseudoChildrenAIDSet = new HashSet<>(agent.getChildrenAIDList());
    childAndPseudoChildrenAIDSet.addAll(agent.getPseudoChildrenAIDList());
    
    Set<String> childAndPseudoChildrenStrList = new HashSet<String>();
    for (AID pscChildrenAID : childAndPseudoChildrenAIDSet) {
      childAndPseudoChildrenStrList.add(pscChildrenAID.getLocalName());
    }
	  
	  for (PiecewiseMultivariateQuadFunction pwFunc : agent.getFunctionList()) {
	    Set<String> variableSet = pwFunc.getVaribleSet();
	    variableSet.retainAll(childAndPseudoChildrenStrList);
	    
	    // There is no children or pseudo-children in this function
	    // Then add to the function list
	    if (variableSet.size() == 0) {
	      funcList.add(pwFunc);
	    }
//  	  for (AID children : agent.getChildrenStrList()) {
//  	    // add if not contain children
////  	    System.out.println("Children " + Double.valueOf(children.getLocalName()));
////  	    System.out.println("Other agent: " + pwFunc.getOtherAgent());
//  	    if (Double.compare(pwFunc.getOtherAgent(), Double.valueOf(children.getLocalName())) != 0) {  	      
//  	      funcList.add(pwFunc);
//  	      if (agent.isRoot()) {
//  	        System.out.println("Added " + pwFunc.getOtherAgent() + " " + Double.valueOf(children.getLocalName()));
//  	      }
//  	    }
//  	  }
	  }
	   	  
	  
//	  /**Remove children and pseudoChildren constraint table*/
//    List<AID> childAndPseudoChildrenAIDList = new List<AID>();
//    childAndPseudoChildrenAIDList.addAll(agent.getChildrenAIDList());
//    childAndPseudoChildrenAIDList.addAll(agent.getPseudoChildrenAIDList());
//
//    List<String> childAndPseudoChildrenStrList = new List<String>();
//    for (AID pscChildrenAID:childAndPseudoChildrenAIDList) {
//      childAndPseudoChildrenStrList.add(pscChildrenAID.getLocalName());
//    }
//    
//    List<PiecewiseFunction> functionToBeRemove = new List<>();
//    List<PiecewiseFunction> currentFunctionList = new List<>();
//    for (PiecewiseFunction f : agent.getFunctionList()) {
//      currentFunctionList.add(new PiecewiseFunction(f));
//    }
//    
//    for (PiecewiseFunction pwf : currentFunctionList) {
////      List<Double> decLabelList = pwf.get();
//      double neighbor = pwf.getFunctionList().get(0).getOtherAgent();
//      
//      boolean hasChildren = false;
//      for (String children:childAndPseudoChildrenStrList) {
//        if (Double.compare(neighbor, Double.parseDouble(children)) == 0) {
//          hasChildren = true;
//          break;
//        }
//      }
//      if (hasChildren)
//        functionToBeRemove.add(pwf);
//    }
//    
//    for (PiecewiseFunction removeFunction:functionToBeRemove) {
//      currentFunctionList.remove(removeFunction);
//    }
//    
//    agent.setCurrentFunctionListDPOP(currentFunctionList);
	  agent.setCurrentFunctionListDPOP(funcList);
	  for (PiecewiseMultivariateQuadFunction entry : funcList) {
	    System.out.println("The function list after removing children of agent " + agent.getIdStr() + " is: " + entry);
	  }
	}
	
	//constraintTableAtEachTSMap is constructed in collapsing table (decision and random)
	//get table lists from constraintTableAtEachTSMap at currentTS timeStep
	//remove children tables from that list
	public void removeChildrenTableFromTableList(int currentTimeStep) {
		/**Remove children and pseudoChildren constraint table*/
		List<AID> childAndPseudoChildrenAIDList = new ArrayList<>();
		childAndPseudoChildrenAIDList.addAll(agent.getChildrenAIDList());
		childAndPseudoChildrenAIDList.addAll(agent.getPseudoChildrenAIDList());

		List<String> childAndPseudoChildrenStrList = new ArrayList<>();
		for (AID pscChildrenAID:childAndPseudoChildrenAIDList) {
			childAndPseudoChildrenStrList.add(pscChildrenAID.getLocalName());
		}
		
		List<Table> constraintTableToBeRemove = new ArrayList<>();
		//get table list, then remove children table
		List<Table> tableListAtCurrentTS = new ArrayList<>(agent.getConstraintTableAtEachTSMap().get(currentTimeStep));
		System.out.println("Agent " + agent.getIdStr() + " size" + agent.getConstraintTableAtEachTSMap().get(currentTimeStep).size());

		//for (Table constraintTable:constraintTableAtEachTSMap.get(currentTS)) {
		for (Table constraintTable:tableListAtCurrentTS) {
			List<Double> decLabelList = constraintTable.getDecVarLabel();
			boolean hasChildren = false;
			for (String children:childAndPseudoChildrenStrList) {
				if (decLabelList.contains(Double.parseDouble(children))) {
					hasChildren = true;
					break;
				}
			}
			if (hasChildren)
				constraintTableToBeRemove.add(constraintTable);
		}
		
		for (Table removeTable:constraintTableToBeRemove) {
			tableListAtCurrentTS.remove(removeTable);
		}
		
		agent.setCurrentTableListDPOP(tableListAtCurrentTS);
		
	}
	
	/**
	 * This function is commented
	 * @param binaryPw1
	 * @param binaryPw2
	 * @return
	 */
//	public PiecewiseFunction addBinaryPiecewiseFunctions(PiecewiseFunction binaryPw1, PiecewiseFunction binaryPw2) {
//    PiecewiseFunction pwFunc = new PiecewiseFunction(null, null);
//    List<Function> binaryList1 = binaryPw1.getFunctionList();
//    List<Function> binaryList2 = binaryPw2.getFunctionList();
//
//    Set<Double> binaryRange1 = binaryPw1.getSortedSegmentedRange();
//    Set<Double> binaryRange2 = binaryPw2.getSortedSegmentedRange();
//    binaryRange1.addAll(binaryRange2);
//    List<Double> rangeList = new ArrayList<>(binaryRange1);
//
//    QuadraticBinaryFunction binaryFunc1 = new QuadraticBinaryFunction(null, null, null, null);
//    QuadraticBinaryFunction binaryFunc2 = new QuadraticBinaryFunction(null, null, null, null);
//	 
//    for (int i = 0; i < rangeList.size() - 1; i++) {
//      double a = rangeList.get(i);
//      double b = rangeList.get(i+1);
//      for (Function f : binaryList1) {
//        if (f.isInRange(a, b)) {
//          binaryFunc1 = (QuadraticBinaryFunction) f;
//          break;
//        }
//        binaryFunc1 = null;
//      }
//      
//      for (Function f : binaryList2) {
//        if (f.isInRange(a, b)) {
//          System.out.println("Unary function: " + f);
//          binaryFunc2 = (QuadraticBinaryFunction) f;
//          break;
//        }
//        binaryFunc2 = null;
//      }
//
//      QuadraticBinaryFunction newBinaryFunction = new QuadraticBinaryFunction(binaryFunc1);
//      newBinaryFunction = newBinaryFunction.addNewBinaryFunction(binaryFunc2, new Interval(a, b),
//          binaryFunc2.getOtherInterval(), binaryFunc2.getSelfAgent(), binaryFunc2.getOtherAgent());
//      pwFunc.addNewFunction(newBinaryFunction);
//    }
//    
//    return pwFunc;
//	  return null;
//	}
	
  /**
   * This function is checked manually
   * @param binaryPw
   * @param unaryPw
   * @return a binary piecewise function
   */
//  public PiecewiseFunction addBinaryAndUnaryPiecewiseFunctions(PiecewiseFunction binaryPw, PiecewiseFunction unaryPw) {
//	  PiecewiseFunction pwFunc = new PiecewiseFunction(null, null);
//	  List<BinaryFunction> binaryList = binaryPw.getFunctionList();
//	  List<BinaryFunction> unaryList = unaryPw.getFunctionList();
//	  
//	  TreeSet<Double> binaryRange = binaryPw.getSortedSegmentedRange();
//    TreeSet<Double> unaryRange = unaryPw.getSortedSegmentedRange();
//    binaryRange.addAll(unaryRange);
//    List<Double> rangeList = new ArrayList<>(binaryRange);
//    
//    QuadraticBinaryFunction binaryFunc = null;
//    QuadraticUnaryFunction unaryFunc = null;
//    
//    for (int i = 0; i < rangeList.size() - 1; i++) {
//      double a = rangeList.get(i);
//      double b = rangeList.get(i+1);
//      for (BinaryFunction f : binaryList) {
//        // find the only binary f that is in the range
//        // if not, return null
//        if (f.isInRange(a, b)) {
//          binaryFunc = (QuadraticBinaryFunction) f;
//          break;
//        }
//        binaryFunc = null;
//      }
//      
//      // find the only unary f that is in the range
//      // if not, return null
//      for (BinaryFunction f : unaryList) {
//        if (f.isInRange(a, b)) {
//          unaryFunc = (QuadraticUnaryFunction) f;
//          break;
//        }
//        unaryFunc = null;
//      }
//
//      // Assume that binaryFunc is not null
//      QuadraticBinaryFunction newBinaryFunction = new QuadraticBinaryFunction(binaryFunc);
//      newBinaryFunction = newBinaryFunction.addUnaryFuncDiffInterval(unaryFunc);
//      newBinaryFunction.setSelfInterval(new Interval(a, b));
//      pwFunc.addNewFunction(newBinaryFunction);
//    }
//    
//    return pwFunc;
//	}
	
	
//	public PiecewiseFunction addUnaryPiecewiseFunctions(PiecewiseFunction func1, PiecewiseFunction func2) {
//	  PiecewiseFunction pwFunc = new PiecewiseFunction(null, null);
//	  List<BinaryFunction> functionList1 = func1.getFunctionList();
//	  List<BinaryFunction> functionList2 = func2.getFunctionList();
//	  

//	  TreeSet<Double> range1 = func1.getSortedSegmentedRange();
//	  TreeSet<Double> range2 = func2.getSortedSegmentedRange();
//	  range1.addAll(range2);
//	  List<Double> rangeList = new ArrayList<>(range1);
//	  
//	  QuadraticUnaryFunction f1 = null;
//	  QuadraticUnaryFunction f2 = null; 
//	  
//	  for (int i = 0; i < rangeList.size() - 1; i++) {
//	    double a = rangeList.get(i);
//	    double b = rangeList.get(i+1);
//	    for (BinaryFunction f : functionList1) {
//	      if (f.isInRange(a, b)) {
//	        f1 = new QuadraticUnaryFunction((QuadraticUnaryFunction) f);
//	        break;
//	      }
//	      f1 = null;
//	    }
//	    
//      for (BinaryFunction f : functionList2) {
//        if (f.isInRange(a, b)) {
//          f2 = new QuadraticUnaryFunction((QuadraticUnaryFunction) f);
//          break;
//        }
//        f2 = null;
//      }
//
//      QuadraticUnaryFunction newFunction = new QuadraticUnaryFunction(f1);
////      newFunction = newFunction.addNewUnaryFunction(f2), new Interval(a, b), f2.getSelfAgent(), -1);
//      newFunction = newFunction.addNewUnaryFunction(f2);
//      pwFunc.addNewFunction(newFunction);
//	  }
//	  
//	  // given a range [a, b], check which function in that range in functionList1 and functionList2
//	  // to check a <= LB and UB <= b
//	  // then sum up two function
//	  
//	  return pwFunc;
//	}
	
	
	
	//create indexList1
	//la vi tri cua tung variable (tu nho den lon)
	//o trong row1, ma nam trong commonVariables
	
	//create indexList2
	//la vi tri cua tung variable (tu nho den lon)
	//o trong row2, ma nam trong commonVariables
	
	//join(label1, label2) as the same order
	
	//for each row1 of table1.getTable()
	//	for each row2 of table2.getTable()
	//		join(row1, row2, indexList1, indexList2)
	public Table joinTable(Table table1, Table table2) {
		//get commonVariables
		List<Double> commonVariables = getCommonVariables(table1.getDecVarLabel(), table2.getDecVarLabel());
				
		//create indexList1, indexList2
		//xet tung variable commonVariables
		//add index of that variable to the indexList
		List<Integer> indexContainedInCommonList1 = new ArrayList<Integer>();
		List<Integer> indexContainedInCommonList2 = new ArrayList<Integer>();
		for (Double variable:commonVariables) {
			indexContainedInCommonList1.add(table1.getDecVarLabel().indexOf(variable));
			indexContainedInCommonList2.add(table2.getDecVarLabel().indexOf(variable));
		}
		
		//create returnTable
		//join label
		List<Double> joinedLabelTable1FirstThenTable2 = getJoinLabel(table1.getDecVarLabel(), table2.getDecVarLabel()
																			,indexContainedInCommonList2);
		
		Table joinedTable = new Table(joinedLabelTable1FirstThenTable2);
		for (Row row1:table1.getTable()) {
			for (Row row2:table2.getTable()) {
				Row joinedRow = getJoinRow(row1, row2, indexContainedInCommonList1, 
						indexContainedInCommonList2);
				if (joinedRow != null)
					joinedTable.addRow(joinedRow);
			}
		}
		
		return joinedTable;
	}
	
	List<Double> getCommonVariables(List<Double> variableList1, List<Double> variableList2) {
		List<Double> commonVariableList = new ArrayList<Double>(variableList1);
		commonVariableList.retainAll(variableList2);
		
		return commonVariableList;
	}
	
	//for variable1 from label1, add to joinedLabel
	//for variable2 from label2
	//	if index not in indexContainedInCommonList2
	//	then add to joinedLabel
	public List<Double> getJoinLabel(List<Double> label1, List<Double> label2,
											List<Integer> indexContainedInCommonList2) {
												
		List<Double> joinedLabel = new ArrayList<>();// (label1);
		for (Double variable1:label1) {
			joinedLabel.add(variable1);
		}
		
		//add variable with index not in indexContainedInCommonList2
		for (int i=0; i<label2.size(); i++) {
			if (!indexContainedInCommonList2.contains(i))
				joinedLabel.add(label2.get(i));
		}
	
		return joinedLabel;
	}
	
	public Row getJoinRow(Row row1, Row row2, List<Integer> indexList1, 
			  List<Integer> indexList2) {
	  
	  //check if same size
		if (indexList1.size() != indexList2.size()) {
			System.err.println("Different size from indexList: " + indexList1.size() +
										 " " + indexList2.size());
			return null;
		}
		
		int listSize = indexList1.size();
		//check if same values
		for (int i=0; i<listSize; i++) {
			if (row1.getValueList().get(indexList1.get(i)).equals
			(row2.getValueList().get(indexList2.get(i))) == false) {
//				System.out.println("Different values here!");
				return null;
			}
		}
		
		//join two row
		List<Double> joinedValues = new ArrayList<Double>();//(row1.getValueList());
		for (Double value1:row1.getValueList()) {
			joinedValues.add(value1);
		}
		
		for (int i=0; i<row2.getValueList().size(); i++) {
			if (indexList2.contains(i) == false)
				joinedValues.add(row2.getValueList().get(i));
		}
		
		Row joinedRow = new Row(joinedValues, row1.getUtility() + row2.getUtility());
		return joinedRow;				
	}
	
	//create new TabelDPOP
	//create new Label: eliminate variableToProject
	//create new Table with -1 dimension
	//create checkedList mark already picked tuples
	//for each tuple1 from the table
	//	if index(tuple1) already in picked tuple => continue
	//	for each tuple2:tuple1->end from the table
	//		compare to the minimum , and update
	//	add to new Table
	public Table projectOperator(Table table, Double variableToProject) {
		int indexEliminated = getIndexOfContainedVariable(table.getDecVarLabel(), variableToProject);
		if (indexEliminated == -1) {
			return null;
		}
		
		//create arrayIndex
		List<Integer> arrayIndex = new ArrayList<Integer>();
		for (int i=0; i<table.getDecVarLabel().size(); i++) {
			if (i != indexEliminated)
				arrayIndex.add(i);
		}
		
		//create checkedList
		List<Integer> checkedList = new ArrayList<Integer>();
		
		//create projectedLabel
		List<Double> projectedLabel = createTupleFromList(table.getDecVarLabel(), arrayIndex);
		
		//create projectedTable
		Table projectTable = new Table(projectedLabel);
		for (int i=0; i<table.getRowCount(); i++) {
			if (checkedList.contains(i) == true)
				continue;
			checkedList.add(i);
			Row row1 = table.getTable().get(i);
			List<Double> tuple1 = createTupleFromRow(row1, arrayIndex);
			double maxUtility = row1.getUtility();
			List<Double> maxTuple = tuple1;
			
			for (int j=i+1; j<table.getRowCount(); j++) {
				Row row2 = table.getTable().get(j);
				List<Double> tuple2 = createTupleFromRow(row2, arrayIndex);
				double row2Utility = row2.getUtility();
				if (isSameTuple(tuple1, tuple2) == true) {
					checkedList.add(j);
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
	
	int getIndexOfContainedVariable(List<Double> list, Double input) {
		return list.indexOf(input);
	}
	
	//create tuples from Row and arrayIndex
	public List<Double> createTupleFromList(List<Double> list, List<Integer> arrayIndex) {
		if (arrayIndex.size() >= list.size()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									list.size());
			return null;
		}
		List<Double> newTuple = new ArrayList<>();
		for (Integer index:arrayIndex) {
			newTuple.add(list.get(index));
		}
		return newTuple;
	}
	
	//create tuples from Row and arrayIndex
	public List<Double> createTupleFromRow(Row row, List<Integer> arrayIndex) {
		if (arrayIndex.size() >= row.getVariableCount()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									row.variableCount);
			return null;
		}
		List<Double> newTuple = new ArrayList<>();
		for (Integer index:arrayIndex) {
			newTuple.add(row.getValueAtPosition(index));
		}
		return newTuple;
	}
	
	//check if two tuples has the same values
	public boolean isSameTuple(List<Double> tuple1, List<Double> tuple2) {
		if (tuple1.size() != tuple2.size()) {
			System.err.println("Different size from two tuples: " + tuple1.size() + " and "
																	+ tuple2.size());
			return false;
		}
		int size = tuple1.size();
		for (int i=0; i<size; i++) {
			if (tuple1.get(i).equals(tuple2.get(i)) == false) {
				return false;
			}
		}
		return true;
	}
	
	
	
	
	//for each value of X
	//for each message received from the children
	//sum the utility that received from the children
	Table combineMessage(List<ACLMessage> list) {
		List<Table> listTable = new ArrayList<Table>();
		for (ACLMessage msg:list) {
			try {
				listTable.add((Table) msg.getContentObject());
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		
		int size = listTable.size();
		Table table = listTable.get(0);

		for (int i=1; i<size; i++) {
			table = joinTable(table, listTable.get(i));
		}

		return table;
	}
	
	PiecewiseMultivariateQuadFunction combineMessageToFunction(List<ACLMessage> list) {
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
	
//	public void writeTimeToFile() {
//		if (agent.algorithm != DCOP.REACT)
//			return;
//		
//		String line = "";
//		String alg = DCOP.algTypes[agent.algorithm];
//		if (agent.getCurrentTS() == 0)
//			line = line + alg + "\t" + agent.inputFileName + "\n";
//		
//		DecimalFormat df = new DecimalFormat("##.##");
//		df.setRoundingMode(RoundingMode.DOWN);
//		long runTime = System.currentTimeMillis() - agent.getCurrentUTILstartTime();
//		
//		line = line + "ts=" + agent.getCurrentTS() + "\t" + df.format(runTime) + " ms" + "\n";
//
//		String fileName = "id=" + agent.instanceD + "/sw=" + (int) agent.switchingCost + "/react_runtime.txt";
//		byte data[] = line.getBytes();
//	  Path p = Paths.get(fileName);
//
//	  try (OutputStream out = new BufferedOutputStream(
//	    Files.newOutputStream(p, CREATE, APPEND))) {
//	    out.write(data, 0, data.length);
//	  } catch (IOException x) {
//	    System.err.println(x);
//	  }
//	}
	
//	public double swCost(String curValue, String preValue, int typeOfFunction) {
//		if (typeOfFunction == DCOP.CONSTANT)
//			return curValue.equals(preValue) ? 0 : agent.switchingCost;
//		else if (typeOfFunction == DCOP.LINEAR)
//			return curValue.equals(preValue) ? 0 : agent.switchingCost * absoluteDistance(curValue, preValue);
//		else if (typeOfFunction == DCOP.EXP_2)
//			return curValue.equals(preValue) ? 0 : Math.pow(agent.scExponentialBase, absoluteDistance(curValue, preValue));
//		else
//			return -Double.MAX_VALUE;
//	}
	
	public double absoluteDistance(String a, String b) {
		return Math.abs(Double.parseDouble(a) - Double.parseDouble(b));
	}
}
