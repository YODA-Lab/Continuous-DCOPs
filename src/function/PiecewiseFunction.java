package function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import agent.DcopInfo;
import agent.DcopInfo.SolvingType;

/**
 * @author khoihd
 *
 */
public class PiecewiseFunction extends Function implements Serializable {
    // function -> intervals
    
    /**
     * 
     */
    private static final long serialVersionUID = 2147073840063782655L;
    private List<Function> functionList;
    
    public PiecewiseFunction(PiecewiseFunction pwf) {
        super(pwf.getSelfAgent(), pwf.getOtherAgent());
        for (Function f : pwf.getFunctionList()) {
            functionList.add(f);
        }
    }
    
    public PiecewiseFunction(String selfAgent, String otherAgent) {
        super(selfAgent, otherAgent);
        functionList = new ArrayList<>();
    }
    
    public Set<Double> getSegmentedRange() {
        Set<Double> range = new TreeSet<>();
        for (Function func:functionList) {
//            Interval intervals = ((UnaryFunction) func).getSelfInterval();
            Interval intervals = func.getSelfInterval();
            range.add(intervals.getLowerBound());
            range.add(intervals.getUpperBound());
        }
        
        return range;
    }
    
    public void addNewFunction(Function function) {
        this.selfAgent = function.getSelfAgent();
        this.otherAgent = function.getOtherAgent();
        functionList.add(function);
    }
    
    public PiecewiseFunction project(SolvingType solType) {
        return solType == SolvingType.APPROXIMATE ? this.approxProject(DcopInfo.NUMBER_OF_INTERVALS)
                : this.analyticallyProject(DcopInfo.DISCRETIZED_VALUE);
    }
    
    public PiecewiseFunction analyticallyProject(double discretizedValue) {
        PiecewiseFunction pwFunc = new PiecewiseFunction(selfAgent, otherAgent);
        for (Function f:functionList) {
            PiecewiseFunction newPiecewise = (((BinaryFunction) f).project(discretizedValue));
            pwFunc.addPiecewiseFunction(newPiecewise);
        }
//        System.out.println("ANATICALLY PROJECTED FUNCTION: " + pwFunc);
        return pwFunc;
    }
    
    // Divide into smaller intervals
    //  + Divide each intervals into k equal intervals
    //  + In each k intervals, pick the middle values
    // Get the final function
    // Find the argmax of that function
    // The max is the orgin
    public PiecewiseFunction approxProject(int numberOfIntervals) {
        PiecewiseFunction pwFunc = new PiecewiseFunction(selfAgent, otherAgent);
        for (Function f:functionList) {
            PiecewiseFunction newPiecewise = (((BinaryFunction) f).approxProject(numberOfIntervals));
            pwFunc.addPiecewiseFunction(newPiecewise);
        }
//        System.out.println("APPROXIMATELY PROJECTED FUNCTION: " + pwFunc);
        return pwFunc;
    }
    
    public void addPiecewiseFunction(PiecewiseFunction f) {
        functionList.addAll(f.getFunctionList());
    }

    public List<Function> getFunctionList() {
        return functionList;
    }

    public void setFunctionList(List<Function> functionList) {
        this.functionList = functionList;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Function f : functionList) {
            builder.append(f);
            builder.append("\n");
        }
        return builder.toString();
    }
}
