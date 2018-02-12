package behaviour;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import agent.DCOP;
import agent.DCOP_INFO;
import function.BinaryFunction;
import function.Function;
import function.Interval;
import function.PiecewiseFunction;
import function.UnaryFunction;
import table.Row;
import table.Table;
import utilities.Utilities;


/*
 * This is UTIL phrase of DTREE
 * 1. If X is leaf THEN
 *      WE ASSUME EACH VALUE OF PARENT HAS AT LEAST ONE CORRESPONDING VALUES FROM CHILDREN 
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
public class DPOP_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -2438558665331658059L;

	DCOP agent;
	
	public DPOP_UTIL(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		agent.setCurrentUTILstartTime(System.currentTimeMillis());
		agent.setCurrentTableListDPOP(null);
		//TODO: adjust remove children table
		//TODO: join all tables at once, instead of pair-wise
		
		if (agent.algorithm == DCOP.DPOP) {
			removeChildrenTableFromTableList(0);
			removeChildrenFunctionFromFunctionList(0);
//			createCollapsedUnarySwitchingCostTable(agent.h);
		}
		else if (agent.algorithm == DCOP.DSA) {
			removeChildrenTableFromTableList(0);
			System.out.println("DSA fix this error here!!!");
		}
//		else if (agent.algorithm == DCOP.HYBRID) {
//			agent.addExpectedRandomTableToList(agent.getCurrentTS());
//			agent.addConstraintTableToList(agent.getCurrentTS());
//			removeChildrenTableFromTableList(agent.getCurrentTS());
//			if (agent.getCurrentTS() != 0)
//				createUnarySwitchingCostTableAtATimeStep(agent.getCurrentTS(), DCOP.FORWARD_BOOL);
//		}
//		else if (agent.algorithm == DCOP.MULTI_CDPOP) {
//			removeChildrenTableFromTableList(agent.getCurrentTS());
//			createCollapsedUnarySwitchingCostTable(agent.hybridTS);
//			//consider with previous solution
//		}
//		//Done: forward is correct
//		else if (agent.algorithm == DCOP.FORWARD) {
//			//solve at currentTS with actual distribution
//			if (agent.getCurrentTS() < agent.h -1) {
//				agent.addExpectedRandomTableToList(agent.getCurrentTS());
//				agent.addConstraintTableToList(agent.getCurrentTS());
//				removeChildrenTableFromTableList(agent.getCurrentTS());
//				if (agent.getCurrentTS() != 0)
//					createUnarySwitchingCostTableAtATimeStep(agent.getCurrentTS(), DCOP.FORWARD_BOOL);
//			}
//			//when t = h-1, solve h first
//			//no switching cost
//			else if (agent.getCurrentTS() == agent.h-1) {
//				agent.addExpectedRandomTableToList(agent.h);
//				agent.addConstraintTableToList(agent.h);
//				removeChildrenTableFromTableList(agent.h);
//			}
//			//when t = h, solve h-1 with 2 switching cost
//			else if (agent.getCurrentTS() == agent.h) {
//				agent.addExpectedRandomTableToList(agent.h-1);
//				agent.addConstraintTableToList(agent.h-1);
//				removeChildrenTableFromTableList(agent.h-1);
//				createDoubleUnarySwitchingBeforeLastTimeStep(agent.h-1);
//			}
//		}
//		else if (agent.algorithm == DCOP.BACKWARD) {
//			//solve from h -> 0
//			//ERROR with solving DCOP more than necessary steps not happen,
//			//since number of behaviors is set correctly
//			agent.addExpectedRandomTableToList(agent.h - agent.getCurrentTS());
//			agent.addConstraintTableToList(agent.h - agent.getCurrentTS());
//			removeChildrenTableFromTableList(agent.h - agent.getCurrentTS());
//			//last time step: not need to add switching cost table
//			if (agent.getCurrentTS() != 0)
//				createUnarySwitchingCostTableAtATimeStep(agent.h - agent.getCurrentTS()
//															, DCOP.BACKWARD_BOOL);
//		}
//		else if (agent.algorithm == DCOP.REACT) {
//			agent.addConstraintTableToList(agent.getCurrentTS());
//			agent.addReactRandomTableToList(agent.getCurrentTS());
//			removeChildrenTableFromTableList(agent.getCurrentTS());
//			if (agent.getCurrentTS() != 0)
//				createUnarySwitchingCostTableAtATimeStep(agent.getCurrentTS(), DCOP.FORWARD_BOOL);
//		}
//		//Done: SDPOP, LS_SDPOP are correct
//		else if (agent.algorithm == DCOP.LS_SDPOP || agent.algorithm == DCOP.SDPOP) {
//			agent.addExpectedRandomTableToList(agent.getCurrentTS());
//			agent.addConstraintTableToList(agent.getCurrentTS());
//			removeChildrenTableFromTableList(agent.getCurrentTS());
//		}
		
		agent.setSimulatedTime(agent.getSimulatedTime()
						+ agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		if (agent.isRoot()) System.out.println(agent.getIdStr() + ": I am root");
		else if (agent.isLeaf()) System.out.println(agent.getIdStr() + ": I am leaf, my parent is " + agent.getParentAID().getLocalName());
		else {System.out.println(agent.getIdStr() + ": I am internal node, my parent is " + agent.getParentAID().getLocalName());}
		
		if (agent.isLeaf()) {
//			leafDoUtilProcess();
		    leafDoFuncUtilProcess();
			if (agent.algorithm == DCOP.DPOP) System.out.println("leaf done");
		} 
		else if (agent.isRoot()){
//			rootDoUtilProcess();
		    rootFuncDoUtilProcess();
			
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
		    internalNodeFuncDoUtilProcess();
			
			if (agent.algorithm == DCOP.DPOP) System.out.println("internal node done");
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

//        System.out.println("Projected table: " ); projectedTable.printDecVar();

		agent.setSimulatedTime(agent.getSimulatedTime()
					+ agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
//		projectedTable.printDecVar();
	}
	
    public void leafDoFuncUtilProcess() {
        agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());

        PiecewiseFunction combinedFunction = agent.getFunctionList().get(0); 

        agent.setAgentViewFunction(combinedFunction);
        
//        System.out.println("Combined function " + combinedFunction);

        PiecewiseFunction projectedFunction = combinedFunction.project(DCOP_INFO.DISCRETIZED_VALUE);

        agent.setSimulatedTime(
                agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
        
//        System.out.println("Projected function " + projectedFunction);

        agent.sendObjectMessageWithTime(agent.getParentAID(), projectedFunction, DPOP_UTIL, agent.getSimulatedTime());
    }
	
	
	public void internalNodeDoUtilProcess() {			
		ArrayList<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
			
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		Table combinedUtilAndConstraintTable = combineMessage(receivedUTILmsgList);
	
		for (Table pseudoParentTable:agent.getCurrentTableListDPOP()) {
			combinedUtilAndConstraintTable = joinTable(combinedUtilAndConstraintTable, pseudoParentTable);
		}

		agent.setAgentViewTable(combinedUtilAndConstraintTable);

		Table projectedTable = projectOperator(combinedUtilAndConstraintTable, Double.parseDouble(agent.getLocalName()));
		
		agent.setSimulatedTime(agent.getSimulatedTime() +
					agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
	}
	
    public void internalNodeFuncDoUtilProcess() {
        ArrayList<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);

        agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());

        PiecewiseFunction combinedFunctionMessage = combineMessageToFunction(receivedUTILmsgList);

        for (PiecewiseFunction pseudoParentFunction : agent.getCurrentFunctionListDPOP()) {
            combinedFunctionMessage = addPiecewiseFunction(combinedFunctionMessage, pseudoParentFunction);
        }

        agent.setAgentViewFunction(combinedFunctionMessage);

        PiecewiseFunction projectedFunction = combinedFunctionMessage.project(DCOP_INFO.DISCRETIZED_VALUE);

        agent.setSimulatedTime(
                agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());

        agent.sendObjectMessageWithTime(agent.getParentAID(), projectedFunction, DPOP_UTIL, agent.getSimulatedTime());
    }
	
	public void rootFuncDoUtilProcess() {
	    ArrayList<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
	    agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
        PiecewiseFunction combinedFunctionMessage = combineMessageToFunction(receivedUTILmsgList);
        
        System.out.println("COMBINED " + combinedFunctionMessage);
        
        for (PiecewiseFunction pseudoParentFunction : agent.getCurrentFunctionListDPOP()) {
            combinedFunctionMessage = addPiecewiseFunction(combinedFunctionMessage, pseudoParentFunction);
        }
        
        // choose the maximum
        double argmax = -Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (Function f : combinedFunctionMessage.getFunctionList()) {
            if (Double.compare(((UnaryFunction) f).getMax(), max) > 0) {
                max = ((UnaryFunction) f).getMax();
                argmax = ((UnaryFunction) f).getArgMax();
            }
        }
        
        System.out.println("MAX VALUE IS " + max);
        System.out.println("ARGMAX VALUE IS " + argmax);
	}
	   
    public void rootDoUtilProcess() {
		ArrayList<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
		
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
	
	public ArrayList<ACLMessage> waitingForMessageFromChildren(int msgCode) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
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
	
	public ArrayList<ACLMessage> waitingFromRandChildren(int msgCode, int noRandChild) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
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
	
	public ArrayList<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();

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
	
	
	public void removeChildrenFunctionFromFunctionList(int currentTimeStep) {
//	    /**Remove children and pseudoChildren constraint table*/
//        ArrayList<AID> childAndPseudoChildrenAIDList = new ArrayList<AID>();
//        childAndPseudoChildrenAIDList.addAll(agent.getChildrenAIDList());
//        childAndPseudoChildrenAIDList.addAll(agent.getPseudoChildrenAIDList());
//
//        ArrayList<String> childAndPseudoChildrenStrList = new ArrayList<String>();
//        for (AID pscChildrenAID:childAndPseudoChildrenAIDList) {
//            childAndPseudoChildrenStrList.add(pscChildrenAID.getLocalName());
//        }
//        
//        List<PiecewiseFunction> functionToBeRemove = new ArrayList<>();
//        List<PiecewiseFunction> currentFunctionList = new ArrayList<>();
//        for (PiecewiseFunction f : agent.getFunctionList()) {
//            currentFunctionList.add(new PiecewiseFunction(f));
//        }
//        
//        for (PiecewiseFunction pwf : currentFunctionList) {
////            ArrayList<Double> decLabelList = pwf.get();
//            double neighbor = pwf.getFunctionList().get(0).getOtherAgent();
//            
//            boolean hasChildren = false;
//            for (String children:childAndPseudoChildrenStrList) {
//                if (Double.compare(neighbor, Double.parseDouble(children)) == 0) {
//                    hasChildren = true;
//                    break;
//                }
//            }
//            if (hasChildren)
//                functionToBeRemove.add(pwf);
//        }
//        
//        for (PiecewiseFunction removeFunction:functionToBeRemove) {
//            currentFunctionList.remove(removeFunction);
//        }
//        
//        agent.setCurrentFunctionListDPOP(currentFunctionList);
	    agent.setCurrentFunctionListDPOP(agent.getFunctionList());
	}
	
	//constraintTableAtEachTSMap is constructed in collapsing table (decision and random)
	//get table lists from constraintTableAtEachTSMap at currentTS timeStep
	//remove children tables from that list
	public void removeChildrenTableFromTableList(int currentTimeStep) {
		/**Remove children and pseudoChildren constraint table*/
		ArrayList<AID> childAndPseudoChildrenAIDList = new ArrayList<AID>();
		childAndPseudoChildrenAIDList.addAll(agent.getChildrenAIDList());
		childAndPseudoChildrenAIDList.addAll(agent.getPseudoChildrenAIDList());

		ArrayList<String> childAndPseudoChildrenStrList = new ArrayList<String>();
		for (AID pscChildrenAID:childAndPseudoChildrenAIDList) {
			childAndPseudoChildrenStrList.add(pscChildrenAID.getLocalName());
		}
		
		ArrayList<Table> constraintTableToBeRemove = new ArrayList<Table>();
		@SuppressWarnings("unchecked")
		//get table list, then remove children table
		ArrayList<Table> tableListAtCurrentTS = (ArrayList<Table>) agent.getConstraintTableAtEachTSMap().get(currentTimeStep).clone();
		System.out.println("Agent " + agent.getIdStr() + " size" + agent.getConstraintTableAtEachTSMap().get(currentTimeStep).size());

		//for (Table constraintTable:constraintTableAtEachTSMap.get(currentTS)) {
		for (Table constraintTable:tableListAtCurrentTS) {
			ArrayList<Double> decLabelList = constraintTable.getDecVarLabel();
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
	
//	void createCollapsedUnarySwitchingCostTable(int numberOfTimeStep) {
//		//loop over each decision variable
//		//for each decision variable,
//		
//		String decVar = agent.getIdStr();
//		ArrayList<String> newLabel = new ArrayList<String>();
//		newLabel.add(decVar);
//		agent.setCollapsedSwitchingCostTable(new Table(newLabel));
//		int domainSize = agent.getDecisionVariableDomainMap().get(decVar).size();
//		//0 -> numberOfTimeStep
////		int noTimeStep = agent.solveTimeStep + 1;
//		int noTimeStep = numberOfTimeStep + 1;
//		int totalSize = (int) Math.pow(domainSize, noTimeStep);
//		//create all possible value tuple, calculate switching cost, add to row of switchingCostTable
//		for (int count=0; count<totalSize; count++) {
//			String valueTuple = "";
//			int quotient = count;
//			for (int tS=noTimeStep-1; tS>=0; tS--) {
//				int remainder = quotient%domainSize;
//				quotient = quotient/domainSize;
//				valueTuple = agent.getDecisionVariableDomainMap().get(decVar).get(remainder) + "," + valueTuple;
//			}
//			valueTuple = valueTuple.substring(0, valueTuple.length()-1);
//				
//			//create row and switchingCost
//			ArrayList<String> row = new ArrayList<String>();
//			row.add(valueTuple);
//			double sC = 0;
//			String[] valueList = valueTuple.split(",");
//			//consider case with h >= 1, so that there is switching cost
//			if (valueList.length != 1) {
//				for (int i=1; i<valueList.length; i++) {
////					sC += swCost(valueList[i], valueList[i-1], agent.scType);
//					sC += agent.sc_func(valueList[i], valueList[i-1]);
//				}
//				//compare value at allowedTimeStep with valueAtStableState
////				if (valueList[valueList.length-1].equals(valueAtStableState))
////					sC += agent.switchingCost;
//			}
//	
//			agent.getCollapsedSwitchingCostTable().addRow(new Row(row, -sC));
//		}
//		
//		if (agent.algorithm == DCOP.C_DPOP)
//			agent.getCurrentTableListDPOP().add(agent.getCollapsedSwitchingCostTable());
//	}
	
	//compare values with next timeStep
//	void createUnarySwitchingCostTableAtATimeStep(int timeStep, boolean FWorBW) {
//		//correct
//		String valueToBeCompared = null;
//		//compare with previous time step
//		if (FWorBW == DCOP.FORWARD_BOOL)
//			valueToBeCompared = agent.getValueAtEachTSMap().get(timeStep-1);
//		//compare with next time step
//		else if (FWorBW == DCOP.BACKWARD_BOOL)
//			valueToBeCompared = agent.getValueAtEachTSMap().get(timeStep+1);
//		
//		ArrayList<String> newLabel = new ArrayList<String>();
//		newLabel.add(agent.getIdStr());
//		Table switchingCostTable = new Table(newLabel);
//
//		//traverse all value from domain, compare with valueToBeCompared
//		for (String value:agent.getDecisionVariableDomainMap().get(agent.getIdStr())) {
//			ArrayList<String> row = new ArrayList<String>();
//			row.add(value);
////			switchingCostTable.addRow(new Row(row, -swCost(value, valueToBeCompared, agent.scType)));
//			switchingCostTable.addRow(new Row(row, -agent.sc_func(value, valueToBeCompared)));
//		}
////		switchingCostTable.printDecVar();
//		agent.getCurrentTableListDPOP().add(switchingCostTable);
//	}
	
	public PiecewiseFunction addPiecewiseFunction(PiecewiseFunction func1, PiecewiseFunction func2) {
	    PiecewiseFunction pwFunc = new PiecewiseFunction(-1, -1);
	    List<Function> functionList1 = func1.getFunctionList();
	    List<Function> functionList2 = func2.getFunctionList();
	    
	    Set<Double> range1 = func1.getSegmentedRange();
	    Set<Double> range2 = func2.getSegmentedRange();
	    range1.addAll(range2);
	    List<Double> rangeList = new ArrayList<>(range1);
	    
	    UnaryFunction f1 = new UnaryFunction(-1, -1);
	    UnaryFunction f2 = new UnaryFunction(-1, -1);
	    
	    for (int i = 0; i < rangeList.size() - 1; i++) {
	        double a = rangeList.get(i);
	        double b = rangeList.get(i+1);
	        for (Function f:functionList1) {
	            if (((UnaryFunction) f).isInRange(a, b)) {
	                f1 = (UnaryFunction) f;
	                break;
	            }
	            f1 = null;
	        }
	        
            for (Function f : functionList2) {
                if (((UnaryFunction) f).isInRange(a, b)) {
                    f2 = (UnaryFunction) f;
                    break;
                }
                f2 = null;
            }

            UnaryFunction newFunction = new UnaryFunction(f1);
            newFunction = newFunction.addNewUnaryFunction(f2, new Interval(a, b), f2.getSelfAgent(), -1);
            pwFunc.addNewFunction(newFunction);
	    }
	    
	    // given a range [a, b], check which function in that range in functionList1 and functionList2
	    // to check a <= LB and UB <= b
	    // then sum up two function
	    
	    return pwFunc;
	}
	
	
	
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
		ArrayList<Double> commonVariables = getCommonVariables(table1.getDecVarLabel(), table2.getDecVarLabel());
				
		//create indexList1, indexList2
		//xet tung variable commonVariables
		//add index of that variable to the indexList
		ArrayList<Integer> indexContainedInCommonList1 = new ArrayList<Integer>();
		ArrayList<Integer> indexContainedInCommonList2 = new ArrayList<Integer>();
		for (Double variable:commonVariables) {
			indexContainedInCommonList1.add(table1.getDecVarLabel().indexOf(variable));
			indexContainedInCommonList2.add(table2.getDecVarLabel().indexOf(variable));
		}
		
//		for (int i=0; i<table1.getDecVarLabel().size(); i++) {
//			if (commonVariables.contains(table1.getDecVarLabel().get(i)))
//				indexContainedInCommonList1.add(i);
//		}
//		
//		for (int i=0; i<table2.getDecVarLabel().size(); i++) {
//			if (commonVariables.contains(table2.getDecVarLabel().get(i)))
//				indexContainedInCommonList2.add(i);
//		}
		
		//create returnTable
		//join label
		ArrayList<Double> joinedLabelTable1FirstThenTable2 = getJoinLabel(table1.getDecVarLabel(), table2.getDecVarLabel()
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
	
	ArrayList<Double> getCommonVariables(ArrayList<Double> variableList1, ArrayList<Double> variableList2) {
//		ArrayList<String> commonVariableList = new ArrayList<String>();
//		
//		for (String variable1:variableList1)
//			for (String variable2:variableList2) {
//				//check if equal agents, and if list contains agent or not
//				//if (variable1.equals(variable2) && isContainVariable(commonVariableList, variable1) == false)
//				if (variable1.equals(variable2) && !commonVariableList.contains(variable1)) {	
//					commonVariableList.add(variable1);
//					break;
//				}
//			}
//		
//		return commonVariableList;
		
		ArrayList<Double> commonVariableList = new ArrayList<Double>(variableList1);
		commonVariableList.retainAll(variableList2);
		
		return commonVariableList;
	}
	
	//for variable1 from label1, add to joinedLabel
	//for variable2 from label2
	//	if index not in indexContainedInCommonList2
	//	then add to joinedLabel
	public ArrayList<Double> getJoinLabel(ArrayList<Double> label1, ArrayList<Double> label2,
											ArrayList<Integer> indexContainedInCommonList2) {
												
		ArrayList<Double> joinedLabel = new ArrayList<>();// (label1);
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
	
	public Row getJoinRow(Row row1, Row row2, ArrayList<Integer> indexList1, 
			  ArrayList<Integer> indexList2) {
//		if (!agent.isLeaf()) {
//		    System.out.println(row1.getValueList());
//	        System.out.println(indexList1);
//            System.out.println(row2.getValueList());
//            System.out.println(indexList2);
//            System.out.println("====================");
//		}
	    
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
		ArrayList<Double> joinedValues = new ArrayList<Double>();//(row1.getValueList());
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
		ArrayList<Integer> arrayIndex = new ArrayList<Integer>();
		for (int i=0; i<table.getDecVarLabel().size(); i++) {
			if (i != indexEliminated)
				arrayIndex.add(i);
		}
		
		//create checkedList
		ArrayList<Integer> checkedList = new ArrayList<Integer>();
		
		//create projectedLabel
		ArrayList<Double> projectedLabel = createTupleFromList(table.getDecVarLabel(), arrayIndex);
		
		//create projectedTable
		Table projectTable = new Table(projectedLabel);
		for (int i=0; i<table.getRowCount(); i++) {
			if (checkedList.contains(i) == true)
				continue;
			checkedList.add(i);
			Row row1 = table.getTable().get(i);
			ArrayList<Double> tuple1 = createTupleFromRow(row1, arrayIndex);
			double maxUtility = row1.getUtility();
			ArrayList<Double> maxTuple = tuple1;
			
			for (int j=i+1; j<table.getRowCount(); j++) {
				Row row2 = table.getTable().get(j);
				ArrayList<Double> tuple2 = createTupleFromRow(row2, arrayIndex);
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
	
	int getIndexOfContainedVariable(ArrayList<Double> list, Double input) {
		return list.indexOf(input);
	}
	
	//create tuples from Row and arrayIndex
	public ArrayList<Double> createTupleFromList(ArrayList<Double> list, ArrayList<Integer> arrayIndex) {
		if (arrayIndex.size() >= list.size()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									list.size());
			return null;
		}
		ArrayList<Double> newTuple = new ArrayList<>();
		for (Integer index:arrayIndex) {
			newTuple.add(list.get(index));
		}
		return newTuple;
	}
	
	//create tuples from Row and arrayIndex
	public ArrayList<Double> createTupleFromRow(Row row, ArrayList<Integer> arrayIndex) {
		if (arrayIndex.size() >= row.getVariableCount()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									row.variableCount);
			return null;
		}
		ArrayList<Double> newTuple = new ArrayList<>();
		for (Integer index:arrayIndex) {
			newTuple.add(row.getValueAtPosition(index));
		}
		return newTuple;
	}
	
	//check if two tuples has the same values
	public boolean isSameTuple(ArrayList<Double> tuple1, ArrayList<Double> tuple2) {
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
	Table combineMessage(ArrayList<ACLMessage> list) {
		ArrayList<Table> listTable = new ArrayList<Table>();
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
	
    PiecewiseFunction combineMessageToFunction(ArrayList<ACLMessage> list) {
        List<PiecewiseFunction> listFunction = new ArrayList<>();
        for (ACLMessage msg : list) {
            try {
                listFunction.add((PiecewiseFunction) msg.getContentObject());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

        int size = listFunction.size();
        PiecewiseFunction function = listFunction.get(0);

        for (int i = 1; i < size; i++) {
            function = addPiecewiseFunction(function, listFunction.get(i));
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
//	    Path p = Paths.get(fileName);
//
//	    try (OutputStream out = new BufferedOutputStream(
//	      Files.newOutputStream(p, CREATE, APPEND))) {
//	      out.write(data, 0, data.length);
//	    } catch (IOException x) {
//	      System.err.println(x);
//	    }
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
