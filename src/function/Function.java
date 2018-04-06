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
    protected String selfAgent;
    protected String otherAgent;
    protected Interval selfInterval;
    
    public Function(String selfAgent, String otherAgent) {
        this.selfAgent = selfAgent;
        this.otherAgent = otherAgent;
    }

    public double evaluate(double x) {
        return 0;
    }
    
    public double evaluate (double x1, double x2) {
        return 0;
    }
    
    public String getSelfAgent() {
        return selfAgent;
    }

    public void setSelfAgent(String selfAgent) {
        this.selfAgent = selfAgent;
    }

    public String getOtherAgent() {
        return otherAgent;
    }

    public void setOtherAgent(String otherAgent) {
        this.otherAgent = otherAgent;
    }

    public Interval getSelfInterval() {
        return selfInterval;
    }

    public void setSelfInterval(Interval selfInterval) {
        this.selfInterval = selfInterval;
    }
    
    public boolean isInRange(double a, double b) {
        return Double.compare(selfInterval.getLowerBound(), a) <= 0 
                && Double.compare(b, selfInterval.getUpperBound()) <= 0;
    }    
}
