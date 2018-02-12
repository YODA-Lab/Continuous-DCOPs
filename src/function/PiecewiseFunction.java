package function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import agent.DCOP_INFO;

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
    
    public PiecewiseFunction(double selfAgent, double otherAgent) {
        super(selfAgent, otherAgent);
        functionList = new ArrayList<>();
    }
    
    public Set<Double> getSegmentedRange() {
        Set<Double> range = new TreeSet<>();
        for (Function func:functionList) {
            Interval intervals = ((UnaryFunction) func).getSelfInterval();
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
    
    // TODO
    public PiecewiseFunction project(double discretizedValue) {
        PiecewiseFunction pwFunc = new PiecewiseFunction(selfAgent, otherAgent);
        for (Function f:functionList) {
            PiecewiseFunction newPiecewise = (((BinaryFunction) f).project(discretizedValue));
            pwFunc.addPiecewiseFunction(newPiecewise);
        }
        
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
        }
        return builder.toString();
    }
}
