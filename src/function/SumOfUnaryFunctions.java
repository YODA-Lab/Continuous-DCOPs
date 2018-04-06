package function;

import java.util.List;

public class SumOfUnaryFunctions extends Function {

    /**
     * 
     */
    private static final long serialVersionUID = 7650890640047965438L;
    List<Function> unaryFunctionList;
    
    public SumOfUnaryFunctions(String selfAgent, String otherAgent) {
        super(selfAgent, otherAgent);
        // TODO Auto-generated constructor stub
    }
   
    public double evaluate(double x) {
        double value = 0;
        for (Function f : unaryFunctionList) {
            value += f.evaluate(x);
        }
        return value;
    }

    public List<Function> getUnaryFunctionList() {
        return unaryFunctionList;
    }

    public void setUnaryFunctionList(List<Function> unaryFunctionList) {
        this.unaryFunctionList = unaryFunctionList;
    }
}
