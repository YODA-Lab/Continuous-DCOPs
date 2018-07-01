package function.complex;

import function.Function;
import function.Interval;

import static java.lang.Math.*;

public class ExponentialFunction extends Function {

    /**
     * 
     */
    private static final long serialVersionUID = 4935985356252658512L;

    // a * b^function
    double a;
    double b;
    Function function;
    
    public ExponentialFunction(String selfAgent, String otherAgent, Interval selfInterval, Interval otherInterval) {
        super(selfAgent, otherAgent, selfInterval, otherInterval);
    }
    
    public double evaluate(double a, double b, double x) {
      return a * pow(b, (Double) function.evaluate(x));
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
