package function.multivariate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import function.Interval;
import zexception.FunctionException;

import com.google.common.collect.Sets;

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
   * This function is already TESTED
   * Precondition: two functions share the same ranges of common variables
   * This testing is done by addNewInterval() function
   * @param tobeAddedFunction
   *          another MultivariateQuadFunction to add
   * @return a new function which is a sum of this function and
   *         tobeAddedFunction
   */
  public MultivariateQuadFunction add(final MultivariateQuadFunction tobeAddedFunction) {
    MultivariateQuadFunction result = new MultivariateQuadFunction(this);

    // Update quadratic coefficients
    for (Cell<String, String, Double> cellEntry : tobeAddedFunction.getCoefficients().cellSet()) {
      updateCoefficient(cellEntry.getRowKey(), cellEntry.getColumnKey(), cellEntry.getValue());
    }

    for (Entry<String, Interval> entry : tobeAddedFunction.getIntervals().entrySet()) {
      result.addNewInterval(entry.getKey(), entry.getValue());
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
   */
  public void addNewInterval(String variable, Interval interval) {
    if (intervals.containsKey(variable)) {
      if (!intervals.get(variable).equals(interval)) {
        throw new FunctionException("Different INTERVAL when adding QUAD MULTIVARIATE function");
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
    if (getVariables().contains(variable)) {
      throw new FunctionException("The function doesn't contain the variable that needed to be evaluated");
    }

    MultivariateQuadFunction evaluatedFunc = new MultivariateQuadFunction(this);

    // Adding a_ii * xi^2 and a_i * xi to d (x_i = value)
    evaluatedFunc.updateCoefficient("", "", evaluatedFunc.getCoefficients().get(variable, variable) * Math.pow(value, 2)
        + evaluatedFunc.getCoefficients().get(variable, "") * value);

    // Find aij * (xi, xj)
    // Update a_ij * xi * xj => (a_ij * value) xj
    for (Map.Entry<String, Double> rowEntry : evaluatedFunc.getCoefficients().row(variable).entrySet()) {
      String variable_j = rowEntry.getKey();
      if (null != variable_j) {
        double a_ij = rowEntry.getValue();
        evaluatedFunc.updateCoefficient(variable_j, "", a_ij * value);
      }
    }

    // Find aji * (xj, xi)
    // Update a_ji * xi * xj => (a_ji * value) xj
    for (Map.Entry<String, Double> columnEntry : evaluatedFunc.getCoefficients().column(variable).entrySet()) {
      String variable_j = columnEntry.getKey();
      if (null != variable_j) {
        double a_ji = columnEntry.getValue();
        evaluatedFunc.updateCoefficient(variable_j, "", a_ji * value);
      }
    }

    // Delete all columns and rows that contain the variable
    evaluatedFunc.getCoefficients().row(variable).clear();
    evaluatedFunc.getCoefficients().column(variable).clear();

    return evaluatedFunc;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("[Coefficients = ");
    sb.append(coefficients);
    sb.append(", Intervals = ");
    sb.append(intervals + " ]");
    return sb.toString();
  }

  /**
   * Create an ordered set, where each element is a list of ordered intervals. <br>
   * The ordering of interval in the list match the ordering of <variable, interval> from this.getIntervals()
   * This function is already TESTED
   * @param numberOfIntervals
   * @return
   */
  public Set<List<Interval>> cartesianProductInterval(int numberOfIntervals) {
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
  
  /**
   * PROJECT operator
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
   * @return a piecewise function of MultivariateQuadratic functions
   */
  public PiecewiseMultivariateQuadFunction approxProject(int numberOfIntervals) {
    // TODO: needed to be BLACK-BOX TESTING

    PiecewiseMultivariateQuadFunction mpwFunc = new PiecewiseMultivariateQuadFunction();
    Set<List<Interval>> productIntervals = cartesianProductInterval(numberOfIntervals);
    
    System.out.println("productIntervals" + productIntervals);
    System.out.println("intervals " + intervals);
    
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
        if (entryVariable.equals(owner))
          continue;
        varIndex++;

        Interval interval = prodItvList.get(varIndex);
        intervalsOfNewFunction.put(entryVariable, interval);
      }
      midPointedFunction = midPointedFunction.evaluateWithIntervalMap(intervalsOfNewFunction);
      double argmax = midPointedFunction.getArgmax();
      midPointedFunction = midPointedFunction.evaluate(owner, argmax);
      midPointedFunction.setIntervals(intervalsOfNewFunction);

      mpwFunc.addNewFunction(midPointedFunction);
    }

    return mpwFunc;
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
      throw new FunctionException("The number of variables is " + func.getNumberOfVariable());
    }

    return func;
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
   * This function is BEING TESTED 
   * owner is the only variable left
   * @return v_i such that v_i = argmax{x_i} f where f is the unary function
   */
  public double getArgmax() {
    if (getNumberOfVariable() != 1)
      throw new FunctionException(
          "The number of variable should be ONE in order to be evaluated as a unaryFunction: " + getNumberOfVariable());

    if (!getVariables().contains(owner))
      throw new FunctionException("Owner, which is the only variable left, is not contained in variable list: " + getVariables()
          + " and the owner " + owner);

    double LB = intervals.get(owner).getLowerBound();
    double UB = intervals.get(owner).getUpperBound();
    // -b / 2a
    double midPoint = -coefficients.get(owner, "") / (2 * coefficients.get(owner, owner));

    double lowerEvaluated = evaluateUnaryFunction(owner, LB);
    double upperEvaluated = evaluateUnaryFunction(owner, UB);
    double midEvaluated = evaluateUnaryFunction(owner, midPoint);

    double max = Math.max(Math.max(lowerEvaluated, upperEvaluated), midEvaluated);

    if (Double.compare(max, lowerEvaluated) == 0)
      return LB;
    else if (Double.compare(max, upperEvaluated) == 0)
      return UB;
    else if (Double.compare(max, midEvaluated) == 0)
      return midEvaluated;

    return -Double.MAX_VALUE;
  }
  
  public boolean isTotallyBigger(Map<String, Interval> varIntervalMap) {
    for (Entry<String, Interval> entry : varIntervalMap.entrySet()) {
      if (!intervals.get(entry.getKey()).isTotallyBigger(entry.getValue()))
          return false;
    }
    
    return true;
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
    this.intervals = intervals;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

}
