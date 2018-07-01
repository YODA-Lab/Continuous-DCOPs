package function.complex;

import function.Function;
import function.Interval;

public class LogarithmicFunction extends Function {

    /**
     * 
     */
    private static final long serialVersionUID = 3774031161796324407L;

    // a log_b{ function }
    double a;
    
    double b;
    
    Function function;
    
    public LogarithmicFunction(String selfAgent, String otherAgent, Interval selfInterval, Interval otherInterval) {
        super(selfAgent, otherAgent, selfInterval, otherInterval);
    }

    @Override
    public Object evaluate(double x) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public double evaluate(double x, double y) {
      // TODO Auto-generated method stub
      return 0;
    }
}
