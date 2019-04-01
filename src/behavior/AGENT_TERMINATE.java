package behavior;

import java.io.IOException;

import agent.DcopAgent;
import jade.core.behaviours.OneShotBehaviour;

public class AGENT_TERMINATE extends OneShotBehaviour {

	private static final long serialVersionUID = -5079656778610995797L;

	private DcopAgent agent;
	
	public AGENT_TERMINATE(DcopAgent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.doDelete();
		if (agent.isRoot()) {
		  try {
        @SuppressWarnings("unused")
        Process p = new ProcessBuilder(
            "killall", 
            "-9", 
            "java").start();
      } catch (IOException e) {
        e.printStackTrace();
      }
		}
	}
}
