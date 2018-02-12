package function;

import java.io.Serializable;

/**
 * @author khoihd
 *
 */
public class UnaryFunction extends Function implements Serializable {
    // ax^2 + bx + c
    
    /**
     * 
     */
    private static final long serialVersionUID = -902091853584620558L;
    /**
     * 
     */
    private double a;
    private double b;
    private double c;
        
    private Interval selfInterval;

    public UnaryFunction(double selfAgent, double otherAgent) {
        super(selfAgent, otherAgent);
        a = 0;
        b = 0;
        c = 0;
        selfInterval = new Interval(0, 0);
    }
    
    public UnaryFunction(UnaryFunction f) {
        super(f.getSelfAgent(), f.getOtherAgent());
        a = f.getA();
        b = f.getB();
        c = f.getC();
        selfInterval = new Interval(f.getSelfInterval().getLowerBound(), f.getSelfInterval().getUpperBound());
    }
    
    public boolean isInRange(double a, double b) {
        return Double.compare(a,selfInterval.getLowerBound()) <= 0 
                && Double.compare(selfInterval.getUpperBound(), b) <= 0;
    }

    public UnaryFunction(UnaryFunction f1, Interval interval, double selfAgent, double otherAgent) {
        super(selfAgent, otherAgent);
        this.a = f1.getA();
        this.b = f1.getB();
        this.c = f1.getC();
        this.selfInterval = new Interval(interval.getLowerBound(), interval.getUpperBound());
    }
    
    public UnaryFunction(double a, double b, double c, Interval interval, double selfAgent, double otherAgent) {
        super(selfAgent, otherAgent);
        this.a = a;
        this.b = b;
        this.c = c;
        this.selfInterval = new Interval(interval.getLowerBound(), interval.getUpperBound());
    }

    public void addNewUnaryFunction(double a, double b, double c) {
        this.a += a;
        this.b += b;
        this.c += c;
    }
    
    public UnaryFunction addNewUnaryFunction(UnaryFunction f, Interval interval, double agentOwner, double dummyAgent) {
        return new UnaryFunction(a + f.getA(), b + f.getB(), c + f.getC(), interval, agentOwner, -1.0);
    }
    
    public double getMax() {
        double v1 = evaluate(selfInterval.getLowerBound());
        double v2 = evaluate(selfInterval.getUpperBound());
        double v3 = evaluate(-b / (2 * a));
        
        double max = Math.max(v1, Math.max(v2, v3));
        if (Double.compare(v1, max) == 0) return v1;
        else if (Double.compare(v2, max) == 0) return v2;
        else return v3;
    }
    
    public double getArgMax() {
        double v1 = evaluate(selfInterval.getLowerBound());
        double v2 = evaluate(selfInterval.getUpperBound());
        double v3 = evaluate(-b / (2 * a));
        
        double max = Math.max(v1, Math.max(v2, v3));
        if (Double.compare(v1, max) == 0) return selfInterval.getLowerBound();
        else if (Double.compare(v2, max) == 0) return selfInterval.getUpperBound();
        else return (-b / (2 * a));
    }
    
    public double evaluate(double x) {
        return a * Math.pow(x, 2) + b * x + c;
    }
    
    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public Interval getSelfInterval() {
        return selfInterval;
    }

    public void setSelfInterval(Interval selfInterval) {
        this.selfInterval = selfInterval;
    }
    
    @Override
    public String toString() {
        return a + " X^2 " + b + " X " + c + "\t" + selfInterval;
    }
}
