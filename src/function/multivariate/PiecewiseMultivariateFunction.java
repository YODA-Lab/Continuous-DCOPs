package function.multivariate;

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
public class PiecewiseMultivariateFunction implements Serializable {
    // function -> intervals
    
    /**
     * 
     */
    private static final long serialVersionUID = -377336921618263783L;
    /**
     * 
     */
    
    private List<MultivariateQuadFunction> functionList;

    public PiecewiseMultivariateFunction() {
        functionList = new ArrayList<>();
    }
    
//    public Set<Double> getSegmentedRange() {
//        Set<Double> range = new TreeSet<>();
//        for (MultivariateFunction func : functionList) {
////            Interval intervals = ((UnaryFunction) func).getSelfInterval();
//            Interval intervals = func.getSelfInterval();
//            range.add(intervals.getLowerBound());
//            range.add(intervals.getUpperBound());
//        }
//        
//        return range;
//    }
    
    public void addNewFunction(MultivariateQuadFunction function) {
        functionList.add(function);
    }
    
    public PiecewiseMultivariateFunction approxProject() {
        return this.approxProject(DcopInfo.NUMBER_OF_INTERVALS);
    }
    
    // Divide into smaller intervals
    //  + Divide each intervals into k equal intervals
    //  + In each k intervals, pick the middle values
    // Get the final function
    // Find the argmax of that function
    // The max is the orgin
    public PiecewiseMultivariateFunction approxProject(int numberOfIntervals) {
        PiecewiseMultivariateFunction pwFunc = new PiecewiseMultivariateFunction();
        for (MultivariateQuadFunction f : functionList) {
//            PiecewiseMultivariateFunction newPiecewise = f.approxProject(numberOfIntervals);
//            pwFunc.addPiecewiseFunction(newPiecewise);
        }
        return pwFunc;
    }
    
    public void addPiecewiseFunction(PiecewiseMultivariateFunction f) {
        functionList.addAll(f.getFunctionList());
    }

    public List<MultivariateQuadFunction> getFunctionList() {
        return functionList;
    }

    public void setFunctionList(List<MultivariateQuadFunction> functionList) {
        this.functionList = functionList;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (MultivariateQuadFunction f : functionList) {
            builder.append(f);
            builder.append("=====\n");
        }
        return builder.toString();
    }
}
