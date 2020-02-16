package behavior;

import agent.ContinuousDcopAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SEARCH_NEIGHBORS extends OneShotBehaviour {

	private static final long serialVersionUID = 6680449924898094747L;

	private ContinuousDcopAgent agent;
	
	/**
	 * @param agent
	 */
	public SEARCH_NEIGHBORS(ContinuousDcopAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {	  
		DFAgentDescription templateDF = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agent.getID());
		templateDF.addServices(serviceDescription);
				
		while (agent.getNeighborAIDSet().size() < agent.getNeighborStrSet().size()) {
			try {
			  DFAgentDescription[] foundAgentList = DFService.search(myAgent, templateDF);
				agent.getNeighborAIDSet().clear();

				for (DFAgentDescription neighbor : foundAgentList) {
				  agent.getNeighborAIDSet().add(neighbor.getName());
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
		
		// Add agents to AgentKeepMyFunctionAID and AgentNotOwningFunctionAID
		if (agent.isRunningMaxsum()) {
  		for (AID agentID : agent.getNeighborAIDSet()) {
  		  if (agent.getMSFunctionMapIOwn().keySet().contains(agentID.getLocalName())) {
  		    agent.addAgentToFunctionIOwn(agentID);
  		  } else {
          agent.addAgentToFunctionOwnedByOther(agentID);
  		  }
  		}
		}		
	}
}
