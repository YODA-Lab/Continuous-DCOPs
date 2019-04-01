package behavior;

import agent.DcopAgent;
import static agent.DcopInfo.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utilities.Utilities;

public class RECEIVE_SEND_UTIL_TO_ROOT extends OneShotBehaviour {

	private static final long serialVersionUID = 4766760189659187968L;

	private DcopAgent agent;
	
	public RECEIVE_SEND_UTIL_TO_ROOT(DcopAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {		
	  	  
		double totalUtilityFromChildren = sumUtilityFromChildrenWithTime();
				
		System.out.println("Agent " +  agent.getID() + " has utility from children is: " + totalUtilityFromChildren);
				
		agent.setAggregatedUtility(totalUtilityFromChildren + agent.utilityFrom_FUNCTION_WithParentAndPseudo());   
		
		System.out.println("Agent " +  agent.getID() + " send utility: " + agent.getAggregatedUtility());
		 
		if (!agent.isRoot())
			agent.sendObjectMessageWithTime(agent.getParentAID(), agent.getAggregatedUtility(), UTILITY_TO_THE_ROOT, agent.getSimulatedTime());
		else {
			agent.setEndTime(System.currentTimeMillis());
			
			System.out.println("AGGREGATED UTILITY: " + agent.getAggregatedUtility());
			System.out.println("RUNNING TIME: " + (agent.getEndTime() - agent.getStartTime()) + "ms");
			System.out.println("SIMULATED TIME: " + agent.getSimulatedTime()/1000000 + "ms");
	    Utilities.writeToFile(agent);
		}
	}

  private double sumUtilityFromChildrenWithTime() {
    agent.startSimulatedTiming();    
    
    double totalUtility = 0;
    int noMessageCount = 0;
    
    while (noMessageCount < agent.getChildrenAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(UTILITY_TO_THE_ROOT);
      ACLMessage receivedMessage = myAgent.receive(template);
      if (receivedMessage != null) {
        noMessageCount++;         
        try {
          totalUtility += (Double) receivedMessage.getContentObject();
        } catch (NumberFormatException e) {
          e.printStackTrace();
        } catch (UnreadableException e) {
          e.printStackTrace();
        }
        
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
        if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        } else {
          agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
        }
      }
      else
        block();
    }
    
    return totalUtility;
  }
}
