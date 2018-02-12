package function;

import java.io.Serializable;

public class BinaryFunction extends Function implements Serializable {
    // a1x1^2 + b1x1
    // a2x2^2 + b2x2
    // a3x1x2 + b3
    
    /**
     * 
     */
    private static final long serialVersionUID = 917101260312191808L;
    private Interval selfInterval;
    private Interval otherInterval;
    
    private double a1;
    private double b1;
    private double a2;
    private double b2;
    private double a3;
    private double b3;

    public BinaryFunction(double selfAgent, double otherAgent) {
        super(selfAgent, otherAgent);
        this.otherAgent = otherAgent;
        a1 = 0;
        b1 = 0;
        a2 = 0;
        b2 = 0;
        a3 = 0;
        b3 = 0;
    }

    public BinaryFunction(double a1, double b1, double a2, double b2, double a3, double b3, double selfAgent, double otherAgent) {
        super(selfAgent, otherAgent);
        this.otherAgent = otherAgent;
        this.a1 = a1;
        this.b1 = b1;
        this.a2 = a2;
        this.b2 = b2;
        this.a3 = a3;
        this.b3 = b3;
    }

    public void addNewBinaryFunction(double a1, double b1, double a2, double b2, double a3, double b3) {
        this.a1 += a1;
        this.b1 += b1;
        this.a2 += a2;
        this.b2 += b2;
        this.a3 += a3;
        this.b3 += b3;
    }
    
    // TODO
    public PiecewiseFunction project(double discretizedValue) {
        // After project, the function become Piecewise of UnaryFunction
        // Also change the owner to the otherAgent
        
        PiecewiseFunction pwFunction = new PiecewiseFunction(otherAgent, -1); //projected into a unary function
        
        UnaryFunction f1 = getUnaryFunctionAtCriticalPoint();
        UnaryFunction f2 = evaluteBinaryFunction(selfInterval.getLowerBound());
        UnaryFunction f3 = evaluteBinaryFunction(selfInterval.getUpperBound());
        
//        System.out.println("f1 " + f1);
//        System.out.println("f2 " + f2);
//        System.out.println("f3 " + f3);
        
        double currentLB = otherInterval.getLowerBound();
        int currentMaxFunction = -1;
                
        for (double val2 = otherInterval.getLowerBound(); Double.compare(val2, otherInterval.getUpperBound()) <= 0;
                val2 += discretizedValue) {
            double eval1 = f1.evaluate(val2);
            double eval2 = f2.evaluate(val2);
            double eval3 = f3.evaluate(val2);
            int maxFunc = maxFunctionAmong(eval1, eval2, eval3);
            
            if (currentMaxFunction == -1) { //initial
                currentMaxFunction = maxFunc;
            }
            else {
                if (maxFunc != currentMaxFunction || Double.compare(val2, otherInterval.getUpperBound()) == 0) {
                    // create a function here
                    // update the lowerBound  
                    UnaryFunction newFoundUnary = null;
                    if (maxFunc == 1) {
                        newFoundUnary = new UnaryFunction(f1, new Interval(currentLB, val2), otherAgent, -1.0);
                    }
                    else if (maxFunc == 2) {
                        newFoundUnary = new UnaryFunction(f2, new Interval(currentLB, val2), otherAgent, -1.0);
                    }
                    else {
                        newFoundUnary = new UnaryFunction(f3, new Interval(currentLB, val2), otherAgent, -1.0);
                    }
                    pwFunction.addNewFunction(newFoundUnary);
                    currentLB = val2;
                }
            }
        }
        
        System.out.println("pwFunction "  + pwFunction);
        
        return pwFunction;
    }
    
    public int maxFunctionAmong(double eval1, double eval2, double eval3) {
        double maximum = Math.max(eval1, Math.max(eval2, eval3));
        if (Double.compare(eval1, maximum) == 0) return 1;
        else if (Double.compare(eval2, maximum) == 0) return 2;
        else return 3;
    }
    
    public UnaryFunction getUnaryFunctionAtCriticalPoint() {
        UnaryFunction newUnaryFunction = new UnaryFunction(otherAgent, -1.0);
        double newA1 = - Math.pow(a3, 2) / (4 * a1) + a2;
        double newB1 = b2 - b1 * a3 / (2 * a1);
        double newC1 = - Math.pow(b1, 2) / (4 * a1) + b3;
        newUnaryFunction.addNewUnaryFunction(newA1, newB1, newC1);
        newUnaryFunction.setSelfInterval(otherInterval);
        return newUnaryFunction;
    }
    
    public UnaryFunction evaluteBinaryFunction(double x) {
        return new UnaryFunction(a2,
                b2 + a3 * x,
                a1 * Math.pow(x, 2) + b1 * x + b3,
                otherInterval,
                otherAgent, -1.0);
    }

    public double getA1() {
        return a1;
    }

    public void setA1(double a1) {
        this.a1 = a1;
    }

    public double getB1() {
        return b1;
    }

    public void setB1(double b1) {
        this.b1 = b1;
    }

    public double getA2() {
        return a2;
    }

    public void setA2(double a2) {
        this.a2 = a2;
    }

    public double getB2() {
        return b2;
    }

    public void setB2(double b2) {
        this.b2 = b2;
    }

    public double getA3() {
        return a3;
    }

    public void setA3(double a3) {
        this.a3 = a3;
    }

    public double getB3() {
        return b3;
    }

    public void setB3(double b3) {
        this.b3 = b3;
    }

    public double getOtherAgent() {
        return otherAgent;
    }

    public void setOtherAgent(double otherAgent) {
        this.otherAgent = otherAgent;
    }

    public Interval getSelfInterval() {
        return selfInterval;
    }

    public void setSelfInterval(Interval selfInterval) {
        this.selfInterval = selfInterval;
    }

    public Interval getOtherInterval() {
        return otherInterval;
    }

    public void setOtherInterval(Interval otherInterval) {
        this.otherInterval = otherInterval;
    }
    
    @Override
    public String toString() {
        return a1 + " X1^2 " + b1 + " X1 " + "\n"
                + a2 + " X2^2 " + b2 + " X2 " + "\n"
                + a3 + " X1X2 " + b3
                + "\t" + selfInterval + "\t" + otherInterval;
    }
}
