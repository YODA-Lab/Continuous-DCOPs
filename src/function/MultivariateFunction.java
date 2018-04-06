package function;

import java.io.Serializable;
import java.util.Arrays;

public class MultivariateFunction implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1364981024826339147L;
    
    String selfAgent;
    String[] otherAgents;
    
    int numberOfVars;
    
    double[] a;
    double[] b;
    double[][] c;
    double d;
    
    public MultivariateFunction(int numberOfVars) {
        this.numberOfVars = numberOfVars;
        this.otherAgents = new String[numberOfVars];
        this.a = new double[numberOfVars];
        this.b = new double[numberOfVars];
        this.c = new double[numberOfVars][numberOfVars];
    }
    
    public MultivariateFunction(String seflAgent, String[] otherAgents) {
        this(otherAgents.length + 1);
        this.selfAgent = seflAgent;
        this.otherAgents = Arrays.copyOf(otherAgents, otherAgents.length);
    }
    
    public MultivariateFunction(String seflAgent, String[] otherAgents, double[] a, double[] b, double[][] c, double d) {
        this(seflAgent, otherAgents);
        this.a = Arrays.copyOf(a, a.length);
        this.b = Arrays.copyOf(b, b.length);
        for (int i = 0; i < this.numberOfVars; i++) {
            this.c[i] = Arrays.copyOf(c[i], numberOfVars);
        }
        this.d = d;
    }
    
    public MultivariateFunction(MultivariateFunction o) {
        this(o.getSelfAgent(), o.getOtherAgents(), o.getA(), o.getB(), o.getC(), o.getD());
    }
    
    public String getSelfAgent() {
        return selfAgent;
    }
    public void setSelfAgent(String selfAgent) {
        this.selfAgent = selfAgent;
    }
    public String[] getOtherAgents() {
        return otherAgents;
    }
    public void setOtherAgents(String[] otherAgents) {
        this.otherAgents = otherAgents;
    }
    public double[] getA() {
        return a;
    }
    public void setA(double[] a) {
        this.a = a;
    }
    public double[] getB() {
        return b;
    }
    public void setB(double[] b) {
        this.b = b;
    }
    public double[][] getC() {
        return c;
    }
    public void setC(double[][] c) {
        this.c = c;
    }
    public double getD() {
        return d;
    }
    public void setD(double d) {
        this.d = d;
    }
    
    
    
}
