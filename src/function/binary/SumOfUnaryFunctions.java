package function.binary;

import java.util.List;

import function.Function;
import function.Interval;

public class SumOfUnaryFunctions extends Function {

    /**
     * 
     */
    private static final long serialVersionUID = 7650890640047965438L;
    List<Function> unaryFunctionList;
    
    public SumOfUnaryFunctions(String selfAgent, String otherAgent, Interval selfInterval, Interval otherInterval) {
        super(selfAgent, otherAgent, selfInterval, otherInterval);
    }

    public List<Function> getUnaryFunctionList() {
        return unaryFunctionList;
    }

    public void setUnaryFunctionList(List<Function> unaryFunctionList) {
        this.unaryFunctionList = unaryFunctionList;
    }
    
    @Override
    public Double evaluate(double x) {
        double value = 0;
        for (Function f : unaryFunctionList) {
            value += (Double) f.evaluate(x);
        }
        return value;
    }

    @Override
    public double evaluate(double x, double y) {
        return evaluate(x);
    }
}
