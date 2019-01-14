package behavior;

import agent.DCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SEARCH_NEIGHBORS extends OneShotBehaviour {

	private static final long serialVersionUID = 6680449924898094747L;

	DCOP agent;
	
	/**
	 * @param agent
	 */
	public SEARCH_NEIGHBORS(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		DFAgentDescription templateDF = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agent.getID());
		templateDF.addServices(serviceDescription);
				
		while (agent.getNeighborAIDList().size() < agent.getNeighborStrSet().size()) {
			try {
			  DFAgentDescription[] foundAgentList = DFService.search(myAgent, templateDF);
				agent.getNeighborAIDList().clear();

				for (DFAgentDescription neighbor : foundAgentList) {
				  agent.getNeighborAIDList().add(neighbor.getName());
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
		
		// Add agents to AgentKeepMyFunctionAID and AgentNotOwningFunctionAID
		if (agent.isRunningMaxsum()) {
  		for (AID agentID : agent.getNeighborAIDList()) {
  		  if (agent.getMSFunctionMapIOwn().keySet().contains(agentID.getLocalName())) {
  		    agent.addAgentToFunctionIOwn(agentID);
  		  } else {
          agent.addAgentToFunctionOwnedByOther(agentID);
  		  }
  		}
		}
	}
}
