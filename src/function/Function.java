package function;

import java.io.Serializable;

public class Function implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8675857466946038096L;
    /**
     * 
     */
    protected double selfAgent;
    protected double otherAgent;
    
    public Function(double selfAgent, double otherAgent) {
        this.selfAgent = selfAgent;
        this.otherAgent = otherAgent;
    }

    public double getSelfAgent() {
        return selfAgent;
    }

    public void setSelfAgent(double selfAgent) {
        this.selfAgent = selfAgent;
    }

    public double getOtherAgent() {
        return otherAgent;
    }

    public void setOtherAgent(double otherAgent) {
        this.otherAgent = otherAgent;
    }
}
