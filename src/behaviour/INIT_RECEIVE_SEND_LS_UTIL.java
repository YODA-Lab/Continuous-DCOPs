//package behaviour;
//
//import utilities.Utilities;
//import agent.DCOP;
//import jade.core.behaviours.OneShotBehaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.UnreadableException;
//
//public class INIT_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {
//
//	private static final long serialVersionUID = 6619734019693007342L;
//
//	DCOP agent;
//	
//	public INIT_RECEIVE_SEND_LS_UTIL(DCOP agent) {
//		super(agent);
//		this.agent = agent;
//	}
//	
//	@Override
//	public void action() {
//		int noMessageCount = 0;
//		agent.setUtilFromChildrenLS(0);
//		while (noMessageCount < agent.getChildrenAIDList().size()) {
//			MessageTemplate template = MessageTemplate.MatchPerformative(INIT_LS_UTIL);
//			ACLMessage receivedMessage = myAgent.receive(template);
//			if (receivedMessage != null) {
//				noMessageCount++;					
//				try {
//					agent.addtoUtilFromChildrenLS((Double) receivedMessage.getContentObject());
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//				} catch (UnreadableException e) {
//					e.printStackTrace();
//				}
//				
//				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
//				if (timeFromReceiveMessage > agent.getSimulatedTime())
//					agent.setSimulatedTime(timeFromReceiveMessage);
//			}
//			else
//				block();
//		}
//		
//		agent.addupSimulatedTime(DCOP.getDelayMessageTime());
//		
////		agent.setUtilityAndCost(agent.getUtilFromChildrenLS() + 
////				agent.utilityWithParentAndPseudoAndUnary() - agent.calculcatingSwitchingCost());
//		
//		agent.setUtilityAndCost(agent.getUtilFromChildrenLS() + agent.utilityWithParentAndPseudoAndUnary());
//
//		if (!agent.isRoot())
//			agent.sendObjectMessageWithTime(agent.getParentAID(), Double.valueOf(agent.getUtilityAndCost())
//								, INIT_LS_UTIL, agent.getSimulatedTime());
//
//		/*TODO: write result to file here */
//		else {
////			//runtime for SDPOP should be calculated at the end of DPOP_VALUE
////			if (agent.algorithm != DCOP.LS_SDPOP) {
////				agent.setEndTime(System.currentTimeMillis());
////				agent.setOldLSRunningTime(agent.getEndTime() - agent.getStartTime());
////			}
////			agent.setOldLSUtility(agent.getUtilityAndCost());
////			if (agent.algorithm == DCOP.LS_SDPOP)
////				Utilities.writeUtil_Time_BeforeLS(agent);
////			else if (agent.algorithm == DCOP.LS_RAND)
////				Utilities.writeUtil_Time_BeforeLS_Rand(agent);
////			
////			System.out.println("RUNNING TIME: " + (agent.getEndTime() - agent.getStartTime()) + "ms");
////			System.out.println("SIMULATED TIME: " + agent.getSimulatedTime()/1000000 + "ms");
////			
////			if (agent.algorithm == DCOP.LS_SDPOP)
////				System.err.println("Utility of Local-search DPOP at iteration 0: " + agent.getUtilityAndCost());
////			else if (agent.algorithm == DCOP.LS_RAND)
////				System.err.println("Utility of Local-search RAND at iteration 0: " + agent.getUtilityAndCost());
//			System.out.println("==========Utility of DSA at iteration 0: " + agent.getUtilityAndCost());
//		}
//	}
//}
