package function.multivariate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;

import agent.DcopInfo;
import agent.DcopInfo.SolvingType;
import function.Interval;

/**
 * @author khoihd
 *
 */
public class PiecewiseMultivariateQuadFunction implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -377336921618263783L;
    /**
     * 
     */
    
    private List<MultivariateQuadFunction> functionList;

    public PiecewiseMultivariateQuadFunction() {
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
    
    public Set<List<Interval>> cartesianProductInterval() {
      // List of {intervalSetVar1,...,intervalSetVarN}
      List<Set<Interval>> intervalsSetList = new ArrayList<Set<Interval>>();
      for (Map.Entry<String, Interval> entry : intervals.entrySet()) {
        String entryVariable = entry.getKey();
        if (entryVariable.equals(owner))
          continue;

        Interval entryInterval = entry.getValue();

        // Use LinkedHashSet to preserve the ordering of inserted intervals
        Set<Interval> intervalsSet = new LinkedHashSet<Interval>(entryInterval.separateIntoAListOfIncreasingIntervals(numberOfIntervals));
        intervalsSetList.add(intervalsSet);
      }

      // The ordering of intervals is preserved in the ordering of variables from intervals
      Set<List<Interval>> productIntervals = Sets.cartesianProduct(intervalsSetList);
      return productIntervals;
    }
    
    // Divide into smaller intervals
    //  + Divide each intervals into k equal intervals
    //  + In each k intervals, pick the middle values
    // Get the final function
    // Find the argmax of that function
    // The max is the orgin
    
    /**
     * Approximately project the PiecewiseMultivariateQuadFunction
     * @param numberOfIntervals
     * @return a piecewise function of projected functions
     */
    public PiecewiseMultivariateQuadFunction approxProject(final int numberOfIntervals) {
        PiecewiseMultivariateQuadFunction pwFunc = new PiecewiseMultivariateQuadFunction();
        for (MultivariateQuadFunction f : functionList) {
            PiecewiseMultivariateQuadFunction newPiecewise = f.approxProject(numberOfIntervals);
            pwFunc.addPiecewiseFunction(newPiecewise);
        }
        return pwFunc;
    }
    
    public Set<String> commonVarSet(PiecewiseMultivariateQuadFunction function1, PiecewiseMultivariateQuadFunction function2) {      
      Set<String> thisVarSet = new HashSet<>();
      // Get common variable set
      for (MultivariateQuadFunction f : function1.getFunctionList()) {
        thisVarSet.addAll(f.getVariables());
      }
      
      Set<String> anotherVarSet = new HashSet<>();
      for (MultivariateQuadFunction f : function2.getFunctionList()) {
        anotherVarSet.addAll(f.getVariables());
      }
      
      Set<String> commonVarSet = new HashSet<>(thisVarSet);
      commonVarSet.retainAll(anotherVarSet);
      
      return commonVarSet;
    }
    
    /**
     * ADD operator
     * Adding two PiecewiseMultivariateQuadFunction
     * 1. Find the set X of common variables
     * 2. For each variable in X, get the atomic ranges => Create a map <commonVariable, TreeSet<Double>>
     * 3. For each 
     * @param pmqFunc 
     *            which is a PiecewiseMultivariateQuadFunction
     * @return a summation of two functions. The ranges are modified.
     */
    public PiecewiseMultivariateQuadFunction addPiecewiseFunction(PiecewiseMultivariateQuadFunction pmqFunc) {
      PiecewiseMultivariateQuadFunction addedFunction = new PiecewiseMultivariateQuadFunction();
      
      Set<String> commonVarSet = commonVarSet(this, pmqFunc);
      Set<List<Interval>> productIntervals = cartesianProductIntervalCommonVariables(this, pmqFunc, commonVarSet);
      
      // Get the Interval of common variables
      for (List<Interval> intervalOfCommon : productIntervals) {
        Map<String, Interval> commonDomainMap = new HashMap<>();
        int index = 0;
        for (String commonVar : commonVarSet) {
          Interval interval = intervalOfCommon.get(index);
          commonDomainMap.put(commonVar, interval);
          index++;
        }

        for (MultivariateQuadFunction thisFunc : this.getFunctionList()) {
          if (thisFunc.isTotallyBigger(commonDomainMap)) {
            for (MultivariateQuadFunction anotherFunc : this.getFunctionList()) {
              if (anotherFunc.isTotallyBigger(commonDomainMap)) {
                MultivariateQuadFunction addedFunc = thisFunc.add(anotherFunc);
                addedFunc.replaceOrUpdateInterval(commonDomainMap);
                addedFunction.addNewFunction(addedFunc);
                break;
              }
            }
          }
        }
      }
      
      return addedFunction;
    }

  private Set<List<Interval>> cartesianProductIntervalCommonVariables(
      PiecewiseMultivariateQuadFunction piecewiseMultivariateQuadFunction, PiecewiseMultivariateQuadFunction pmqFunc,
      Set<String> commonVarSet) {

    // Get the segmented sorted values in the range of every common variable
    List<Set<Interval>> listOfSortedIntervals = new ArrayList<>();
    for (String commonVar : commonVarSet) {
      Set<Double> sortedRange = new TreeSet<>();
      for (MultivariateQuadFunction f : this.getFunctionList()) {
        sortedRange.add(f.getIntervals().get(commonVar).getLowerBound());
        sortedRange.add(f.getIntervals().get(commonVar).getUpperBound());
      }

      for (MultivariateQuadFunction f : pmqFunc.getFunctionList()) {
        sortedRange.add(f.getIntervals().get(commonVar).getLowerBound());
        sortedRange.add(f.getIntervals().get(commonVar).getUpperBound());
      }

      List<Double> sortedRangeInListFormat = new ArrayList<>(sortedRange);
      Set<Interval> intervalSet = new LinkedHashSet<>();
      for (int index = 0; index < sortedRangeInListFormat.size() - 1; index++) {
        intervalSet.add(new Interval(sortedRangeInListFormat.get(index), sortedRangeInListFormat.get(index + 1)));
      }

      listOfSortedIntervals.add(intervalSet);
    }

    return Sets.cartesianProduct(listOfSortedIntervals);
  }

    /**
     * @return a list of functions from this PiecewiseMultivariateQuadFunction
     */
    public List<MultivariateQuadFunction> getFunctionList() {
        return functionList;
    }

    /**
     * Setter of functionList
     * @param functionList
     */
    public void setFunctionList(final List<MultivariateQuadFunction> functionList) {
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
