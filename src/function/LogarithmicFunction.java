package function;

public class LogarithmicFunction extends Function {

    /**
     * 
     */
    private static final long serialVersionUID = 3774031161796324407L;

    // a log_b{ function }
    double a;
    
    double b;
    
    Function function;
    
    public LogarithmicFunction(String selfAgent, String otherAgent) {
        super(selfAgent, otherAgent);
    }
    
    double evaluateUnary(double x) {
        return a * Math.log10(function.evaluate(x)) / Math.log10(b);
    }
}
