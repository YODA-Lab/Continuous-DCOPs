package function;

public class ExponentialFunction extends Function {

    /**
     * 
     */
    private static final long serialVersionUID = 4935985356252658512L;

    // a * b^function
    double a;
    
    double b;
    
    Function function;
    
    public ExponentialFunction(String selfAgent, String otherAgent) {
        super(selfAgent, otherAgent);
    }
    
    double evaluateUnary(double x) {
        return a * Math.pow(b, function.evaluate(x));
    }
}
