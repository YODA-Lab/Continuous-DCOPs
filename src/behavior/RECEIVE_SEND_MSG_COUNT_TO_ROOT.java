package behavior;

import agent.DCOP;
import static agent.DcopInfo.*;
import utilities.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class RECEIVE_SEND_MSG_COUNT_TO_ROOT extends OneShotBehaviour {

	private static final long serialVersionUID = 4766760189659187968L;

	DCOP agent;
	
	public RECEIVE_SEND_MSG_COUNT_TO_ROOT(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {		
		int totalDCOPmsgCountSentToParent = Integer.MAX_VALUE;
		
		if (agent.isRunningDPOP()) {
		  totalDCOPmsgCountSentToParent = agent.isLeaf() ? 0 : totalDcopMessageCountFromChildren();
		} 
		else if (agent.isRunningMaxsum()) {
      int totalLocalMsgCount = agent.getParentAndPseudoStrList().size() * 4;
		  
      totalDCOPmsgCountSentToParent = agent.isLeaf() ? totalLocalMsgCount
          : totalLocalMsgCount + totalDcopMessageCountFromChildren();
		}
				
		System.out.println("Agent " +  agent.getID() + " has received DCOP message count from children: " + totalDCOPmsgCountSentToParent);
								 
		if (!agent.isRoot()) {
		  // In DPOP, add the edge of the agent with the parent
		  // In MS, no need to add because it's counted as parent and pseudo-parent
		  if (agent.isRunningDPOP()) {
		    totalDCOPmsgCountSentToParent++;
		  }
		  
		  agent.sendObjectMessageWithTime(agent.getParentAID(), totalDCOPmsgCountSentToParent, MSG_COUNT_TO_THE_ROOT, agent.getSimulatedTime());
		}
		else {
		  if (agent.isRunningDPOP()) {
		    totalDCOPmsgCountSentToParent = totalDCOPmsgCountSentToParent * 2; // DPOP has two phases
		  }
		  
		  agent.setMessageCount(totalDCOPmsgCountSentToParent);
		  
			System.out.println("AGGREGATED UTILITY: " + agent.getAggregatedUtility());
			System.out.println("RUNNING TIME: " + (agent.getEndTime() - agent.getStartTime()) + "ms");
			System.out.println("SIMULATED TIME: " + agent.getSimulatedTime()/1000000 + "ms");
	    Utilities.writeToFile(agent);
		}
	}

  private int totalDcopMessageCountFromChildren() {
    int numberOfReceivedMsg = 0;
    
    int dcopMsgCount = 0;
    
    while (numberOfReceivedMsg < agent.getChildrenAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(MSG_COUNT_TO_THE_ROOT);
      ACLMessage receivedMessage = myAgent.receive(template);
      if (receivedMessage != null) {
        numberOfReceivedMsg++;         
        
        try {
          dcopMsgCount += (Integer) receivedMessage.getContentObject();
          
        } catch (NumberFormatException e) {
          e.printStackTrace();
        } catch (UnreadableException e) {
          e.printStackTrace();
        }
      }
      else
        block();
    }
    
    return dcopMsgCount;
  }
}
