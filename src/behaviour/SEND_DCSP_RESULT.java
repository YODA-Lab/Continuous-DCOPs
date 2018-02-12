package behaviour;

import agent.DCOP;
import jade.core.AID;
import jade.core.behaviours.Behaviour;

public class SEND_DCSP_RESULT extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -9137969826179481705L;

	DCOP agent;
	
	public SEND_DCSP_RESULT(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		//looking for each agents in neighbor AID List
		//send message
		//content of each message is list of its values			
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// update 100ms to simulated time
		agent.addupSimulatedTime(100);
		
		agent.getDCSP_result_toSend().set(0, agent.getIsSatisfiedPhase1List().get(0));
		agent.getDCSP_result_toSend().set(1, agent.getIsSatisfiedPhase2List().get(0));
		agent.getDCSP_result_toSend().set(2, agent.getResponseTimes().get(0));
		agent.getDCSP_result_toSend().set(3, agent.getL_processed());

		for (AID neighborAgentAID:agent.getNeighborAIDList()) {
			agent.sendObjectMessageWithTime(neighborAgentAID, agent.getDCSP_result_toSend(), 
					DCSP_RESULT, agent.getSimulatedTime());
			
			System.out.println("===ITERATION " + agent.getLsIteration() + ": Agent " + agent.getIdStr() + " sends "
					 + agent.getDCSP_result_toSend().toString()	+ " results to " + neighborAgentAID.getLocalName());

		}
	}

	@Override
	public boolean done() {
		return agent.getLsIteration() == DCOP.MAX_ITERATION;
	}	
}
