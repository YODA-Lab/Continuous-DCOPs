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
import com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation.ANONYMOUS;

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

  public void addToFunctionList(MultivariateQuadFunction function) {
    functionList.add(function);
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

    for (MultivariateQuadFunction f : functionList) {
      PiecewiseMultivariateQuadFunction newPiecewise = f.approxProject(numberOfIntervals, agentID, numberOfAgents, isApprox);
      pwFunc.addPiecewiseToList(newPiecewise);
    }
    return pwFunc;
  }
  

  public PiecewiseMultivariateQuadFunction analyticalProject() {
    PiecewiseMultivariateQuadFunction pwFunc = new PiecewiseMultivariateQuadFunction();

    for (MultivariateQuadFunction f : functionList) {
      PiecewiseMultivariateQuadFunction newPiecewise = f.analyticalProject();
      pwFunc.addPiecewiseToList(newPiecewise);
    }
    return pwFunc;
  }

  private void addPiecewiseToList(PiecewiseMultivariateQuadFunction newPiecewise) {
    // TODO Auto-generated method stub
    for (MultivariateQuadFunction entry : newPiecewise.getFunctionList()) {
      functionList.add(entry);
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
   * ADD operator Adding two PiecewiseMultivariateQuadFunction 1. Find the set X
   * of common variables 2. For each variable in X, get the atomic ranges =>
   * Create a map <commonVariable, TreeSet<Double>> 3. For each
   * 
   * @param pmqFunc
   *          which is a PiecewiseMultivariateQuadFunction
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
          for (MultivariateQuadFunction anotherFunc : pmqFunc.getFunctionList()) {
            if (anotherFunc.isTotallyBigger(commonDomainMap)) {
              MultivariateQuadFunction addedFunc = thisFunc.add(anotherFunc, false);

              // concat the common domain with the domain of anotherFunc
              Map<String, Interval> domainMapToAdd = new HashMap<>(commonDomainMap);
              for (Entry<String, Interval> entry : anotherFunc.getIntervals().entrySet()) {
                domainMapToAdd.computeIfAbsent(entry.getKey(), k -> entry.getValue());
              }

              addedFunc.replaceOrUpdateInterval(domainMapToAdd);
              addedFunction.addToFunctionList(addedFunc);
            }
          }
        }
      }
    }

    return addedFunction;
  }

  /**
   * This function is UNIT TESTED
   * 
   * @param thisFunction
   *          the first function
   * @param anotherFunction
   *          the other function
   * @param commonVarSet
   *          the set of common variables of the two functions
   * @return the segmented product of sorted range of each variable
   * 
   */
  public static Set<List<Interval>> cartesianProductIntervalCommonVariables(PiecewiseMultivariateQuadFunction thisFunction,
      PiecewiseMultivariateQuadFunction anotherFunction, Set<String> commonVarSet) {
    // preserve the ordering of variable in commonVarSet
    List<Set<Interval>> listOfSortedIntervals = new ArrayList<>();

    // Get the segmented sorted values in the range of every common variable
    for (String commonVar : commonVarSet) {
      Set<Double> sortedRange = new TreeSet<>();
      for (MultivariateQuadFunction thisF : thisFunction.getFunctionList()) {
        sortedRange.add(thisF.getIntervals().get(commonVar).getLowerBound());
        sortedRange.add(thisF.getIntervals().get(commonVar).getUpperBound());
      }

      for (MultivariateQuadFunction anotherF : anotherFunction.getFunctionList()) {
        sortedRange.add(anotherF.getIntervals().get(commonVar).getLowerBound());
        sortedRange.add(anotherF.getIntervals().get(commonVar).getUpperBound());
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
  
  public Set<String> getVaribleSet() {
    Set<String> variableSet = new HashSet<>();
    for (MultivariateQuadFunction f : functionList) {
      variableSet.addAll(f.getVariables());
    }
    
    return variableSet;
  }

  /**
   * Setter of functionList
   * 
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
      builder.append("\n");
    }
    return builder.toString();
  }

  public void setOwner(String idStr) {
    // TODO Auto-generated method stub
    for (MultivariateQuadFunction entry : functionList) {
      entry.setOwner(idStr);
    }
  }

  public long size() {
    return functionList.size();
  }
}
