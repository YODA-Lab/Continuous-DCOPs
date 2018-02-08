package behaviour;

import agent.DCOP;
import jade.core.behaviours.OneShotBehaviour;

public class SEND_IMPROVE extends OneShotBehaviour {
	private static final long serialVersionUID = 6159093695904595420L;

	DCOP agent;
	
	public SEND_IMPROVE(DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.sendImprove();
	}
}
