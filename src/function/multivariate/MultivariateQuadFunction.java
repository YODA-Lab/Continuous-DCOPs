package function.multivariate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import function.Interval;
import utilities.Utilities;
import zexception.FunctionException;

import com.google.common.collect.Sets;
import static java.lang.Math.*;
import static java.lang.Double.*;

/**
 * This is the list of function that needs to be tested: ADD, EVALUATE, PROJECT
 * 
 * @author khoihd
 */
public class MultivariateQuadFunction implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 2751671064195985634L;

  private String owner = new String();
  private Table<String, String, Double> coefficients = HashBasedTable.create();
  private Map<String, Interval> intervals = new HashMap<>();

  /**
   * Constructor with no parameter <br>
   * Initialize the constant coefficient to 0.0
   * 
   */
  public MultivariateQuadFunction() {
    coefficients.put("", "", 0.0);
  }
  
  //function -281v_2^2 199v_2 -22v_0^2 252v_0 288v_2v_0 358;
  /**
   * This is the constructor built for BinaryQuadFunction. This is used to read the input files.
   * @param coeff
   * @param selfAgent
   * @param otherAgent
   * @param globalInterval
   */
  public MultivariateQuadFunction(double[] coeff, String selfAgent, String otherAgent, Interval globalInterval) {
    coefficients.put(selfAgent, selfAgent, coeff[0]);
    coefficients.put(selfAgent, "", coeff[1]);
    coefficients.put(otherAgent, otherAgent, coeff[2]);
    coefficients.put(otherAgent, "", coeff[3]);
    coefficients.put(selfAgent, otherAgent, coeff[4]);
    coefficients.put("", "", coeff[5]);
    
    owner = selfAgent;
    intervals.put(selfAgent, globalInterval);
    intervals.put(otherAgent, globalInterval);
  }
  
  /**
   * This is a unary quadratic function
   * @param a
   * @param b
   * @param c
   * @param owner
   * @param interval
   */
  public MultivariateQuadFunction(double a, double b, double c, final String owner, final Interval interval) {
    coefficients.put(owner, owner, a);
    coefficients.put(owner, "", b);
    coefficients.put("", "", c);
    this.owner = owner;
    intervals.put(owner, interval);
  }

  /**
   * Constructor with all parameters <br>
   * This function is already TESTED
   * 
   * @param quadratic
   *          in Table String, String, Double format
   * @param intervals
   *          in Map String, Interval
   */
  public MultivariateQuadFunction(final String owner, final Table<String, String, Double> coefficients,
      final Map<String, Interval> intervals) {
    this();
    this.owner = owner;
    this.coefficients.putAll(coefficients);
    this.intervals.putAll(intervals);
  }

  /**
   * Copy constructor <br>
   * This function is already TESTED
   * 
   * @param object
   *          is the MultivariateQuadFunction to be copied
   */
  public MultivariateQuadFunction(final MultivariateQuadFunction object) {
    this(object.getOwner(), object.getCoefficients(), object.getIntervals());
  }

  /**
   * ADD operator <br>
   * This function is being TESTED
   * Precondition: two functions share the same ranges of common variables
   * This testing is done by addNewInterval() function
   * @param tobeAddedFunction
   *          another MultivariateQuadFunction to add
   * @return a new function which is a sum of this function and
   *         tobeAddedFunction
   */
  public MultivariateQuadFunction add(final MultivariateQuadFunction tobeAddedFunction, boolean isCheckingSameInterval) {
    MultivariateQuadFunction result = new MultivariateQuadFunction(this);

    // Update quadratic coefficients
    for (Cell<String, String, Double> cellEntry : tobeAddedFunction.getCoefficients().cellSet()) {
      result.updateCoefficient(cellEntry.getRowKey(), cellEntry.getColumnKey(), cellEntry.getValue());
    }

    for (Entry<String, Interval> entry : tobeAddedFunction.getIntervals().entrySet()) {
      result.addNewInterval(entry.getKey(), entry.getValue(), isCheckingSameInterval);
    }

    return result;
  }

  public void updateCoefficient(final String key1, final String key2, final double value) {
    if (coefficients.contains(key1, key2)) {
      coefficients.put(key1, key2, coefficients.get(key1, key2) + value);
    } else if (coefficients.contains(key2, key1)) {
      coefficients.put(key2, key1, coefficients.get(key2, key1) + value);
    } else {
      coefficients.put(key1, key2, value);
    }
  }

  /**
   * Add a new interval to this function <br>
   * If already contained this variable, check for same interval <br>
   * If not containing this variable, add <variable, interval> to list <br>
   * This function is already TESTED
   * 
   * @param variable
   * @param interval
   * @param isCheckingSameInterval
   */
  public void addNewInterval(String variable, Interval interval, boolean isCheckingSameInterval) {
    if (isCheckingSameInterval) {
      if (intervals.containsKey(variable)) {
        if (!intervals.get(variable).equals(interval)) {
          throw new FunctionException("Different INTERVAL when adding QUAD MULTIVARIATE function");
        }
      } else {
        intervals.put(variable, new Interval(interval));
      }      
    } else {
      intervals.put(variable, new Interval(interval));
    }
  }
  
  public void replaceOrUpdateInterval(Map<String, Interval> tobeAddedInterval) {
    for (Entry<String, Interval> entry : tobeAddedInterval.entrySet()) {
      intervals.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * EVALUATE operator <br>
   * This function is already TESTED
   * @param variable
   * @param value
   * @return
   */
  public MultivariateQuadFunction evaluate(final String variable, final double value) {
    if (!getVariables().contains(variable)) {
      throw new FunctionException("The function doesn't contain the variable that needed to be evaluated " + variable + " " + intervals);
    }

    MultivariateQuadFunction evaluatedFunc = new MultivariateQuadFunction(this);

    // Adding a_ii * xi^2 and a_i * xi to d (x_i = value)
    evaluatedFunc.updateCoefficient("", "", evaluatedFunc.getCoefficients().get(variable, variable) * Math.pow(value, 2)
        + evaluatedFunc.getCoefficients().get(variable, "") * value);

    // Find aij * (xi, xj)
    // Update a_ij * xi * xj => (a_ij * value) xj
    for (Map.Entry<String, Double> rowEntry : evaluatedFunc.getCoefficients().row(variable).entrySet()) {
      String variable_j = rowEntry.getKey();
      if (!variable_j.equals("")) {
        double a_ij = rowEntry.getValue();
        evaluatedFunc.updateCoefficient(variable_j, "", a_ij * value);
      }
    }

    // Find aji * (xj, xi)
    // Update a_ji * xi * xj => (a_ji * value) xj
    for (Map.Entry<String, Double> columnEntry : evaluatedFunc.getCoefficients().column(variable).entrySet()) {
      String variable_j = columnEntry.getKey();
      if (!variable_j.equals("")) {
        double a_ji = columnEntry.getValue();
        evaluatedFunc.updateCoefficient(variable_j, "", a_ji * value);
      }
    }

    // Delete all columns and rows that contain the variable
    evaluatedFunc.getCoefficients().row(variable).clear();
    evaluatedFunc.getCoefficients().column(variable).clear();
    
    // Delete the entry from intervals
    evaluatedFunc.getIntervals().remove(variable);

    return evaluatedFunc;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("[Coefficients = ");
    sb.append(coefficients);
    sb.append(", Intervals = ");
    sb.append(intervals);
    sb.append(", Owner = ");
    sb.append(owner + " ]");
    return sb.toString();
  }

  /**
   * Create an ordered set, where each element is a list of ordered intervals. <br>
   * The ordering of interval in the list match the ordering of <variable, interval> from this.getIntervals()
   * This function is already TESTED
   * @param numberOfIntervals
   *                is the number of smaller intervals to be divided
   * @param numberOfAgents
   *                is the limit of number of agents to choose to project
   * @return
   */
  public Set<List<Interval>> cartesianProductIntervalExcludingOwner(int numberOfIntervals, int numberOfAgents, boolean isApprox) {
    // List of {intervalSetVar1,...,intervalSetVarN}
    List<Set<Interval>> intervalsSetList = new ArrayList<Set<Interval>>();
    int agentCount = 0;
    for (Map.Entry<String, Interval> entry : intervals.entrySet()) {
      String entryVariable = entry.getKey();
      if (entryVariable.equals(owner))
        continue;
      
      agentCount++;
      
      // stop choosing agents to divide the intervals
      if (isApprox && agentCount > numberOfAgents)
        break;

      Interval entryInterval = entry.getValue();

      // Use LinkedHashSet to preserve the ordering of inserted intervals
      Set<Interval> intervalsSet = new LinkedHashSet<Interval>(entryInterval.separateIntoAListOfIncreasingIntervals(numberOfIntervals));
      intervalsSetList.add(intervalsSet);
    }

    // The ordering of intervals is preserved in the ordering of variables from intervals
    Set<List<Interval>> productIntervals = Sets.cartesianProduct(intervalsSetList);
    return productIntervals;
  }
  
  /**
   * PROJECT operator
   * This function is UNIT-TESTED
   * 1. Divide each interval into to smaller k intervals accordingly <br>
   * 2. In <itv1, itv2,..., itvn> in k^(#variables) intervals, get the midpoints <br>
   * 3. Evaluate the original function recursively into a unary function where the selfvariable is the only variable <br>
   * 4. Get v_i = argmax of the unary function<br>
   * 5. Evaluate the original function with this argmax value (Now the arity is decreased by 1)
   * @param numberOfIntervals
   *          each agent divides its interval into this number of smaller
   *          intervals
   * @param selfvariable
   *          is the function's owner that needs to be projected out
   * @param numberOfAgents
   *          numberOfAgents to choose to divide the interval 
   * @return a piecewise function of MultivariateQuadratic functions
   */
  public PiecewiseMultivariateQuadFunction approxProject(int numberOfIntervals, String agentID, int numberOfAgents, boolean isApprox) {
    PiecewiseMultivariateQuadFunction mpwFunc = new PiecewiseMultivariateQuadFunction();
    Set<List<Interval>> productIntervals = cartesianProductIntervalExcludingOwner(numberOfIntervals, numberOfAgents, isApprox);
      
    // for each list of intervals, we have a function 
    // the result of this process is a piecewise multivariate function
    for (List<Interval> prodItvList : productIntervals) {
      // to match variable from original this.intervals with the interval from prodItvList
      int varIndex = -1;
      
      // for each variable, gets the values, evaluate the function
      MultivariateQuadFunction midPointedFunction = new MultivariateQuadFunction(this);
      Map<String, Interval> intervalsOfNewFunction = new HashMap<>();
      for (Map.Entry<String, Interval> entry : intervals.entrySet()) {
        String entryVariable = entry.getKey();
        if (entryVariable.equals(agentID)) {continue;}
        varIndex++;
        
        if (varIndex == prodItvList.size()) {break;}

        Interval interval = prodItvList.get(varIndex);
        intervalsOfNewFunction.put(entryVariable, interval);
      }
      
      // Now adding the rest
      Set<String> intervalVar = intervalsOfNewFunction.keySet();
      Set<String> functionVar = new HashSet<String>(intervals.keySet());
      functionVar.removeAll(intervalVar);
      functionVar.remove(agentID);
      for (String varToAdd : functionVar) {
        intervalsOfNewFunction.put(varToAdd, intervals.get(varToAdd));
      }
            
      MultivariateQuadFunction unaryEvalutedFunction = midPointedFunction.evaluateWithIntervalMap(intervalsOfNewFunction);
      
      System.out.println(unaryEvalutedFunction);
      
      double argmax = unaryEvalutedFunction.getMaxAndArgMax()[1];
      
      System.out.println("Arg max is " + argmax);
      
      unaryEvalutedFunction = midPointedFunction.evaluate(owner, argmax);
      unaryEvalutedFunction.setIntervals(intervalsOfNewFunction);

      mpwFunc.addToFunctionList(unaryEvalutedFunction);
    }

    return mpwFunc;
  }
  
  public PiecewiseMultivariateQuadFunction analyticalProject() {
    // After project, the function become PiecewiseQuadFunction
    // Also change the owner to the otherAgent
    PiecewiseMultivariateQuadFunction pwFunction = new PiecewiseMultivariateQuadFunction();
    TreeSet<Double> sortedPoint = new TreeSet<>();
    List<Interval> itvSortedList = new ArrayList<>();
    
    // All of them should have the same interval
    List<MultivariateQuadFunction> candidateFuncList = new ArrayList<>();
    MultivariateQuadFunction critF = getUnaryFunctionAtCriticalPoint();
    Interval critInterval = null;
    
    if (null != critF) {
      candidateFuncList.add(critF);
      critInterval = critF.getIntervals().get(critF.getOwner());
      sortedPoint.add(critInterval.getLowerBound());
      sortedPoint.add(critInterval.getUpperBound());
    }
    candidateFuncList.add(evaluateBinaryFunctionX1(intervals.get(owner).getLowerBound()));
    candidateFuncList.add(evaluateBinaryFunctionX1(intervals.get(owner).getUpperBound()));
    
    // To compare those quadratic unary functions, we need to solve f1 = x2, and then get the range
    
    for (int first = 0; first < candidateFuncList.size() - 1; first++) {
      for (int second = first + 1; second < candidateFuncList.size(); second++) {
        sortedPoint.addAll(Utilities.solveUnaryQuadForValues(candidateFuncList.get(first), candidateFuncList.get(second), true));
      }
    }
    
    System.out.println("Sorted Points " + sortedPoint);
    
    double LB = intervals.get(getOtherAgent()).getLowerBound();
    double UB = intervals.get(getOtherAgent()).getUpperBound();
    
    sortedPoint.add(LB);
    sortedPoint.add(UB);
    
    itvSortedList = Utilities.createSortedInterval(sortedPoint.subSet(LB, true, UB, true));
    
    candidateFuncList.remove(critF);
    
    for (Interval interval : itvSortedList) {
      double midPoint = 0.5 * (interval.getLowerBound() + interval.getUpperBound());
      List<Double> evalList = candidateFuncList.stream().map(x -> x.evaluateUnary(midPoint)).collect(Collectors.toList());
      
      double maxEval = Collections.max(evalList);
      int maxIndex = evalList.indexOf(maxEval);
      MultivariateQuadFunction functionToAdd = new MultivariateQuadFunction(candidateFuncList.get(maxIndex));
      
      if (null != critF && critInterval.isTotallyBigger(interval)) {
        // compare with critF
        double critVal = critF.evaluateUnary(midPoint);
        if (compare(critVal, maxEval) > 0) {
          functionToAdd = new MultivariateQuadFunction(critF);
        }
      }
      
      Map<String, Interval> domain = new HashMap<>();
      domain.put(getOtherAgent(), interval);
      functionToAdd.setIntervals(domain);
      
      pwFunction.addToFunctionList(functionToAdd);
    }
        
    return pwFunction;
  }

  /**
   * Apply for binary function only
   * This function is correct
   * @param x1
   * @return
   */
  public MultivariateQuadFunction evaluateBinaryFunctionX1(double x1) {
      
    String otherAgent = getOtherAgent();
    
    double a1 = coefficients.get(owner, owner);
    double b1 = coefficients.get(owner, "");
    double a2 = coefficients.get(otherAgent, otherAgent);
    double b2 = coefficients.get(otherAgent, "");
    double a3 = coefficients.contains(owner, otherAgent) ? coefficients.get(owner, otherAgent) : coefficients.get(otherAgent, owner);
    double b3 = coefficients.get("", "");
    
    return new MultivariateQuadFunction(a2,
        b2 + a3*x1,
        a1 * Math.pow(x1, 2) + b1 * x1 + b3,
        otherAgent,
        intervals.get(otherAgent));
  }

  /**
   * This function is correct
   * @return UnaryFunction from taking the derivative and set to 0
   */
  public MultivariateQuadFunction getUnaryFunctionAtCriticalPoint() {
    String otherAgent = getOtherAgent();
    
    double a1 = coefficients.get(owner, owner);
    double b1 = coefficients.get(owner, "");
    double a2 = coefficients.get(otherAgent, otherAgent);
    double b2 = coefficients.get(otherAgent, "");
    double a3 = coefficients.contains(owner, otherAgent) ? coefficients.get(owner, otherAgent) : coefficients.get(otherAgent, owner);
    double b3 = coefficients.get("", "");
    
    double newA1 = - Math.pow(a3, 2) / (4*a1) + a2;
    double newB1 = - b1*a3 / (2*a1) + b2;
    double newC1 = - Math.pow(b1, 2) / (4*a1) + b3;

    double LB = intervals.get(owner).getLowerBound();
    double UB = intervals.get(owner).getUpperBound();
    
    // Solve for LB <= df/dx = 0 <= UB
    // Then we get the interval for the other agent, such that root of df/fx = 0 belongs to [LB, UB] of owner
    // Intersect the two intervals, if not valid, return null
    // If valid, return the intersected interval
    
    double bound1 = (LB * 2 * a1 + b1) / (-a3);
    double bound2 = (UB * 2 * a1 + b1) / (-a3);
    
    Interval boundCritPointInInterval = compare(bound1, bound2) <= 0 ? new Interval(bound1, bound2) : new Interval(bound2, bound1);
    Interval resultInterval = intervals.get(otherAgent).intersectInterval(boundCritPointInInterval);
    
    if (null == resultInterval)
      return null;
     
    return new MultivariateQuadFunction(newA1, newB1, newC1, otherAgent, resultInterval);
  }

  /**
   * Sequentially evaluate the function with the midpoint from the interval
   * until the function becomes unary This function is already TESTED
   * 
   * @param variableValueMap
   *          is the map {@code <}variable, interval>
   * @return
   */
  public MultivariateQuadFunction evaluateWithIntervalMap(Map<String, Interval> variableValueMap) {
    // sequentially evaluate the function
    MultivariateQuadFunction func = new MultivariateQuadFunction(this);
    for (Map.Entry<String, Interval> entry : variableValueMap.entrySet()) {
      String variable = entry.getKey();
      double value = entry.getValue().midValue();
      func = func.evaluate(variable, value);
    }

    if (func.getNumberOfVariable() != 1) {
      throw new FunctionException("The number of variables shoule be ONE after being evaluated by a map: " + func.getIntervals());
    }

    return func;
  }
  
  public double evaluateToValueGivenValueMap(Map<String, Double> valueMap) {
    
    if (valueMap.size() != intervals.size()) {
      throw new FunctionException("Different in size between valueMap and intervalMap: " + valueMap + " " + intervals);
    }
    MultivariateQuadFunction func = new MultivariateQuadFunction(this);
    int count = 1;
    for (Map.Entry<String, Double> entry : valueMap.entrySet()) {
      if (count < valueMap.size() ) {
        func = func.evaluate(entry.getKey(), entry.getValue());
      } else {
        return func.evaluateUnaryFunction(entry.getKey(), entry.getValue());
      }
      count++;
    }
    return Double.MAX_VALUE;
  }

  /**
   * Evaluate this unary function. The number of variables is checked if 1 This
   * function is already TESTED
   * 
   * @param variable
   * @param value
   * @return
   */
  public double evaluateUnaryFunction(String variable, Double value) {
    if (getNumberOfVariable() != 1)
      throw new FunctionException(
          "The number of variable should be ONE in order to be evaluated as a unaryFunction: " + getNumberOfVariable());

    return coefficients.get(variable, variable) * Math.pow(value, 2) + coefficients.get(variable, "") * value
        + coefficients.get("", "");
  }
  
  /**
   * This function is already TESTED 
   * owner is the only variable left
   * @return max and argmax of the function given the interval
   */
  public double[] getMaxAndArgMax() {
    double[] result = new double[2];
    
    if (getNumberOfVariable() != 1) {
      throw new FunctionException(
          "The number of variable should be ONE in order to be evaluated as a unaryFunction: " + getIntervals());
    }

    if (!getVariables().contains(owner)) {
      throw new FunctionException("Owner, which is the only variable left, is not contained in variable list: " + getVariables()
          + " and the owner " + owner);
    }
    
    double LB = intervals.get(owner).getLowerBound();
    double UB = intervals.get(owner).getUpperBound();
    // -b / 2a
    double midPoint = -getB()/(2 * getA());
    
    double lowerEvaluated = evaluateUnaryFunction(owner, LB);
    double upperEvaluated = evaluateUnaryFunction(owner, UB);
    double midEvaluated = compare(LB, midPoint) <= 0 &&
        compare(midPoint, UB) <= 0 ? evaluateUnaryFunction(owner, midPoint) : -Double.MAX_VALUE;

    double max = Math.max(Math.max(lowerEvaluated, upperEvaluated), midEvaluated);
    result[0] = max;

    if (compare(max, lowerEvaluated) == 0)
      result[1] = LB;
    else if (compare(max, upperEvaluated) == 0)
      result[1] = UB;
    else if (compare(max, midEvaluated) == 0)
      result[1] = midPoint;
    
    return result;
  }
  
  public boolean isTotallyBigger(Map<String, Interval> varIntervalMap) {
    for (Entry<String, Interval> entry : varIntervalMap.entrySet()) {
      if (!intervals.get(entry.getKey()).isTotallyBigger(entry.getValue()))
          return false;
    }
    
    return true;
  }
  
  /**
   * Assume this is a binary function
   * @return
   */
  public String getOtherAgent() {
    List<String> tempVarList = new ArrayList<>(getVariables());
    tempVarList.remove(owner);
    return tempVarList.get(0); // assume binary function
  }
  
  public double getA() {
    return coefficients.get(owner, owner);
  }
  
  public double getB() {
    return coefficients.get(owner, "");
  }
  
  public double getC() {
    return coefficients.get("", "");
  }
  
  public Set<Double> solveForRootsInsideInterval() {
    System.out.println("Function to solve: " + toString());
    
    TreeSet<Double> rootsAndBounds = new TreeSet<>();
    
    double a = getA();
    double b = getB();
    double c = getC();
    
    double LB = intervals.get(owner).getLowerBound();
    double UB = intervals.get(owner).getUpperBound();
    
    rootsAndBounds.add(LB);
    rootsAndBounds.add(UB);
    
    // Linear
    if (compare(a, 0) == 0) {
      rootsAndBounds.add(-c / b);
      return rootsAndBounds.subSet(LB, false, UB, false);
    }

    // Quadratic
    double delta = pow(b, 2) - 4 * a * c;
    if (compare(delta, 0) == 0) {
      rootsAndBounds.add(-b / (2 * a));
    } else if (compare(delta, 0) > 0) {
      rootsAndBounds.add((-b - sqrt(delta)) / (2 * a));
      rootsAndBounds.add((-b + sqrt(delta)) / (2 * a));
    }

    return rootsAndBounds.subSet(LB, false, UB, false);
  }
  
  double evaluateUnary(double value) {
    double a = getA();
    double b = getB();
    double c = getC();
    
    return a * pow(value, 2) + b * value + c;
  }

  /**
   * Return the number of variables in this function This function is already
   * TESTED
   * 
   * @return the number of variables
   */
  public int getNumberOfVariable() {
    return getVariables().size();
  }

  /**
   * This function is already TESTED
   * 
   * @return the set of variables through intervals.keySet()
   */
  public Set<String> getVariables() {
    return intervals.keySet();
  }

  /**
   * @return the coefficients in Table{@code <}String, String, Double> form
   */
  public Table<String, String, Double> getCoefficients() {
    return coefficients;
  }

  /**
   * @return the interval maps in Map{@code <}String, Interval> form
   */
  public Map<String, Interval> getIntervals() {
    return intervals;
  }

  public String getOwner() {
    return owner;
  }

  public void setCoefficients(final Table<String, String, Double> coefficients) {
    this.coefficients = coefficients;
  }

  public void setIntervals(final Map<String, Interval> intervals) {
    this.intervals.clear();
    this.intervals.putAll(intervals);
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void checkSameSelfAgent(MultivariateQuadFunction func2) {
    // TODO Auto-generated method stub
    if (!owner.equals(func2.getOwner()))
      throw new FunctionException("DIFFERENT owner when checkSameSelfAgent " + owner + " " + func2.getOwner());
  }

  public void checkSameSelfInterval(MultivariateQuadFunction func2) {
    // TODO Auto-generated method stub
    if (!intervals.get(owner).equals(func2.getIntervals().get(func2.getOwner())))
    throw new FunctionException(
        "DIFFERENT interval when checkSameSelfInterval " + intervals.get(owner) + " " + func2.getIntervals().get(func2.getOwner()));
  }
  
}
