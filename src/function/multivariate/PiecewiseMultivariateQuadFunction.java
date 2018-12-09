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

import function.Interval;
import static java.lang.Double.*;
import static agent.DcopInfo.*;

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

//  private List<MultivariateQuadFunction> functionList;
  
  Map<MultivariateQuadFunction, Set<Map<String, Interval>>> functionMap;
  

  public PiecewiseMultivariateQuadFunction() {
    functionMap = new HashMap<>();
  }

  /**
   * The isOptimized boolean flag is used to reduced the number of intervals when adding functions in analytical 
   * @param function
   * @param intervalMap
   * @param isOptimized
   */
  public void addToFunctionMapWithInterval(MultivariateQuadFunction function, Map<String, Interval> intervalMap, boolean isOptimized) {
    Set<Map<String, Interval>> setOfIntervalMap = null;
    if (functionMap.containsKey(function)) {
      setOfIntervalMap = functionMap.get(function);
      
      if (isOptimized == false) {
        setOfIntervalMap.add(intervalMap);
      } else if (isOptimized == true) {
        String inputAgent = new ArrayList<>(intervalMap.keySet()).get(0);
        Interval inputInterval = intervalMap.get(inputAgent);
        
        // traverse the setOfIntervalMap
        for (Map<String, Interval> entry : setOfIntervalMap) {
          Interval funcInterval = entry.get(inputAgent);
          // Now comparing the functInterval with the inputInterval
          // If match the criteria, then modify funcInterval
          // 1. funcInterval <= inputInterval
          if (compare(funcInterval.getUpperBound(), inputInterval.getLowerBound()) == 0) {
            Interval newInterval = new Interval(funcInterval.getLowerBound(), inputInterval.getUpperBound());
            entry.put(inputAgent, newInterval);
          } else if (compare(inputInterval.getUpperBound(), funcInterval.getLowerBound()) == 0) {
            // 2. inputInterval <= funcInterval 
            Interval newInterval = new Interval(inputInterval.getLowerBound(), funcInterval.getUpperBound());
            entry.put(inputAgent, newInterval);
          }
        }
      }
      // no need to re-add the function with interval map
    } else {
      setOfIntervalMap = new HashSet<>();
      setOfIntervalMap.add(intervalMap);
      functionMap.put(function, setOfIntervalMap);
    }
  }
  
  /**
   * Approximately project the PiecewiseMultivariateQuadFunction
   * 
   * @param numberOfIntervals
   * @return a piecewise function of projected functions
   */
  public PiecewiseMultivariateQuadFunction approxProject(final int numberOfIntervals, String agentID, int numberOfAgents,
      boolean isApprox) {
    
    PiecewiseMultivariateQuadFunction pwFunc = new PiecewiseMultivariateQuadFunction();

    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : functionMap.entrySet()) {
      MultivariateQuadFunction function = functionEntry.getKey();
      Set<Map<String, Interval>> setOfIntervalMap = functionEntry.getValue();
      for (Map<String, Interval> intervalMap : setOfIntervalMap) {
        PiecewiseMultivariateQuadFunction newPiecewise = function.approxProject(intervalMap, numberOfIntervals, agentID, numberOfAgents, isApprox);

        pwFunc.addPiecewiseToMap(newPiecewise, false);
      }
    }
    return pwFunc;
  }
  

  public PiecewiseMultivariateQuadFunction analyticalProject() {
    PiecewiseMultivariateQuadFunction pwFunc = new PiecewiseMultivariateQuadFunction();

    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : functionMap.entrySet()) {
      MultivariateQuadFunction function = functionEntry.getKey();
      
      for (Map<String, Interval> intervalMap : functionEntry.getValue()) {
        PiecewiseMultivariateQuadFunction newPiecewise = function.analyticalProject(intervalMap);
        pwFunc.addPiecewiseToMap(newPiecewise, true);
      }
    }
    return pwFunc;
  }
  
  public PiecewiseMultivariateQuadFunction takeFirstPartialDerivative(String agentInPartialDerivative) {
    PiecewiseMultivariateQuadFunction pwFunc = new PiecewiseMultivariateQuadFunction();

    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : functionMap.entrySet()) {
      MultivariateQuadFunction function = functionEntry.getKey();
      for (Map<String, Interval> interval : functionEntry.getValue()) {
        pwFunc.addToFunctionMapWithInterval(function.takeFirstPartialDerivative(agentInPartialDerivative), interval, NOT_TO_OPTIMIZE_INTERVAL);
      }
    }
    return pwFunc;
  }

  /**
   * Add the newPiecewise function to map
   * @param newPiecewise
   * @param isOptimized
   */
  public void addPiecewiseToMap(PiecewiseMultivariateQuadFunction newPiecewise, boolean isOptimized) {
    // TODO Reduce the for loop here by addAll
    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : newPiecewise.getFunctionMap().entrySet()) {
      for (Map<String, Interval> interval : functionEntry.getValue())
        addToFunctionMapWithInterval(functionEntry.getKey(), interval, isOptimized);
    }
  }

  /**
   * This function is UNIT TESTED
   * 
   * @param function1
   * @param function2
   * @return the set of common variables from the two functions
   */
  public static Set<String> commonVarSet(PiecewiseMultivariateQuadFunction function1,
    PiecewiseMultivariateQuadFunction function2) {
    // Get common variable set
    Set<String> commonVarSet = new HashSet<>(function1.getVariableSet());
    commonVarSet.retainAll(function2.getVariableSet());

    return commonVarSet;
  }

  /**
   * ADD operator Adding two PiecewiseMultivariateQuadFunction 1. Find the set X
   * of common variables 2. For each variable in X, get the atomic ranges =>
   * Create a map <commonVariable, TreeSet<Double>> 3. For each
   * 
   * @param pmqFunc
   *          which is a PiecewiseMultivariateQuadFunction
   * @return a summation of two functions. The ranges are modified.
   */
  public PiecewiseMultivariateQuadFunction addPiecewiseFunction(PiecewiseMultivariateQuadFunction pmqFunc) {
    PiecewiseMultivariateQuadFunction addedPwFunction = new PiecewiseMultivariateQuadFunction();

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
      
      for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> thisFuncEntry : this.getFunctionMap().entrySet()) {
        MultivariateQuadFunction thisFunc = thisFuncEntry.getKey();        
        for (Map<String, Interval> thisInterval : thisFuncEntry.getValue()) {
          if (isTotallyBigger(thisInterval, commonDomainMap)) {
            
            for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> anotherFuncEntry : pmqFunc.getFunctionMap().entrySet()) {
              MultivariateQuadFunction anotherFunc = anotherFuncEntry.getKey();
              for (Map<String, Interval> anotherInterval : anotherFuncEntry.getValue()) {
                
                // Found the two functions with two intervals that is totallyBigger than the commonDomainMap
                if (isTotallyBigger(anotherInterval, commonDomainMap)) {
                  MultivariateQuadFunction addedFunc = thisFunc.add(anotherFunc);
                  Map<String, Interval> domainMapToAdd = new HashMap<>(commonDomainMap);
                  
                  for (Entry<String, Interval> entry : thisInterval.entrySet()) {
                    domainMapToAdd.computeIfAbsent(entry.getKey(), k -> entry.getValue());
                  }
                  for (Entry<String, Interval> entry : anotherInterval.entrySet()) {
                    domainMapToAdd.computeIfAbsent(entry.getKey(), k -> entry.getValue());
                  }
                  
                  addedPwFunction.addToFunctionMapWithInterval(addedFunc, domainMapToAdd, NOT_TO_OPTIMIZE_INTERVAL);
                }
              }
            }
          }
        }
      }
    }

    return addedPwFunction;
  }
  
  public boolean isTotallyBigger(Map<String, Interval> biggerIntervalMap, Map<String, Interval> smallerIntervalMap) {
    for (Entry<String, Interval> smallerMapEntry : smallerIntervalMap.entrySet()) {
      if (!biggerIntervalMap.get(smallerMapEntry.getKey()).isTotallyBigger(smallerMapEntry.getValue()))
          return false;
    }
    
    return true;
  }

  /**
   * This function is UNIT TESTED
   * 
   * @param thisPwFunction
   *          the first function
   * @param anotherPwFunction
   *          the other function
   * @param commonVarSet
   *          the set of common variables of the two functions
   * @return the segmented product of sorted range of each variable
   * 
   */
  public static Set<List<Interval>> cartesianProductIntervalCommonVariables(PiecewiseMultivariateQuadFunction thisPwFunction,
      PiecewiseMultivariateQuadFunction anotherPwFunction, Set<String> commonVarSet) {
    // preserve the ordering of variable in commonVarSet
    List<Set<Interval>> listOfSortedIntervals = new ArrayList<>();

    // Get the segmented sorted values in the range of every common variable
    for (String commonVar : commonVarSet) {
      Set<Double> sortedRange = new TreeSet<>();
      for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> thisFunctionEntry : thisPwFunction.getFunctionMap().entrySet()) {
        for (Map<String, Interval> thisInterval : thisFunctionEntry.getValue()) {
          sortedRange.add(thisInterval.get(commonVar).getLowerBound());
          sortedRange.add(thisInterval.get(commonVar).getUpperBound());
        }
      }

      for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> anotherFunctionEntry : anotherPwFunction.getFunctionMap().entrySet()) {
        for (Map<String, Interval> anotherInterval : anotherFunctionEntry.getValue()) {
          sortedRange.add(anotherInterval.get(commonVar).getLowerBound());
          sortedRange.add(anotherInterval.get(commonVar).getUpperBound());
        }
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
  public  Map<MultivariateQuadFunction, Set<Map<String, Interval>>> getFunctionMap() {
    return functionMap;
  }
  
  public Set<String> getVariableSet() {    
    return getTheFirstFunction().getVariableSet();
  }

  /**
   * Setter of functionMap
   * 
   * @param functionMap
   */
  public void setFunctionList(final  Map<MultivariateQuadFunction, Set<Map<String, Interval>>> functionMap) {
    this.functionMap = functionMap;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : functionMap.entrySet()) {
      builder.append(functionEntry.getKey());
      builder.append(", Interval=");
      builder.append(functionEntry.getValue());
      builder.append("\n");
    }
    return builder.toString();
  }

  public void setOwner(String idStr) {
    // TODO Auto-generated method stub
    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : functionMap.entrySet()) {
      functionEntry.getKey().setOwner(idStr);
    }
  }
  
  // Return the shallow copy of the first function without interval
  public MultivariateQuadFunction getTheFirstFunction() {
    List<MultivariateQuadFunction> funcList = new ArrayList<>(functionMap.keySet());
    return funcList.get(0);
  }

  public long size() {
    return functionMap.size();
  }
}
