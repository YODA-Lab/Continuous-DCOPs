package function.multivariate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import function.Interval;
import zexception.DiffIntervalException;
import zexception.NotAUnaryFunction;

import com.google.common.collect.Sets;

public class MultivariateQuadFunction implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 2751671064195985634L;

  Table<String, String, Double> quadratic;
  Map<String, Double> linear;
  double intercept;
  Map<String, Interval> intervals;
  
  /** Constructor with no parameter
   * 
   */
  public MultivariateQuadFunction() {
    this.quadratic = HashBasedTable.create();
    this.linear = new HashMap<>();
    this.intercept = Double.NEGATIVE_INFINITY;
    this.intervals = new HashMap<>();
  }
  
  /**
   * Constructor with all parameters
   * @param quadratic in Table String, String, Double format
   * @param linear in Map String, Double format
   * @param intercept in double
   * @param intervals in Map String, Interval
   */
  public MultivariateQuadFunction(final Table<String, String, Double> quadratic, final Map<String, Double> linear,
      final double intercept, final Map<String, Interval> intervals) {
    
    this.quadratic = HashBasedTable.create(quadratic);
    this.linear = new HashMap<>(linear);
    this.intercept = intercept;
    this.intervals = new HashMap<>(intervals);
  }
  
  /** Copy constructor
   * @param object is the MultivariateQuadFunction to be copied 
   */
  public MultivariateQuadFunction(final MultivariateQuadFunction object) {
    this(object.getQuadratic(), object.getLinear(), object.getIntercept(), object.getIntervals());
  }
  
  public MultivariateQuadFunction add(final MultivariateQuadFunction tobeAddedFunction) {
    this.checkSameIntervalForCommonVariables(tobeAddedFunction);
    MultivariateQuadFunction result = new MultivariateQuadFunction(this);
    // Update quadratic
    for (String agent1 : tobeAddedFunction.getAgents()) {
      for (String agent2 : tobeAddedFunction.getAgents()) {
        if (result.getQuadratic().contains(agent1, agent2)) {
          result.addNewOrUpdateQuadratic(agent1, agent2,
              result.getQuadratic().get(agent1, agent2) + tobeAddedFunction.getQuadratic().get(agent1, agent2));
        }
        else {
          result.addNewOrUpdateQuadratic(agent1, agent2, 
              tobeAddedFunction.getQuadratic().get(agent1, agent2));
        }
      }
    }
    
    // Update linear
    for (String tobeAddedAgent : tobeAddedFunction.getAgents()) {
      result.addNewOrUpdateLinear(tobeAddedAgent,
          tobeAddedFunction.getLinear().get(tobeAddedAgent) + result.getLinear().getOrDefault(tobeAddedAgent, 0.0));
    }
    
    // Update intercept
    result.addToIntercept(tobeAddedFunction.getIntercept());
    
    // Update intervals
    // Common variables have the same intervals
    // Need to add intervals from toBeAddedFunction to the current function
    Set<String> variablesFromAddedFunction = Sets.difference(tobeAddedFunction.getIntervals().keySet(), this.getIntervals().keySet());
    variablesFromAddedFunction.forEach(var -> this.addNewInterval(var, tobeAddedFunction.getIntervals().get(var)));
        
    return result;
  }
  
  
  
  public void addNewOrUpdateQuadratic(final String key1, final String key2, final double value) {
    quadratic.put(key1, key2, value);
  }
  
  public void addNewOrUpdateLinear(final String key, final double value) {
    linear.put(key, value);
  }
  
  public void addToIntercept(final double value) {
    intercept += value;
  }
  
  public void addNewInterval(String variable, Interval interval) {
    intervals.put(variable, interval);
  }
  
  public void checkSameIntervalForCommonVariables(final MultivariateQuadFunction tobeAddedFunction) {
    for (String agent : this.getAgents()) {
      if (tobeAddedFunction.getAgents().contains(agent)) {
        if (!this.getIntervals().get(agent).equals(tobeAddedFunction.getIntervals().get(agent))) {
          throw new DiffIntervalException("Different INTERVAL when adding QUAD MULTIVARIATE function");
        }
      }
    }
  }
  
  public MultivariateQuadFunction evaluateMultivariate(final String agent, final double value) {
    assert (this.getAgents().contains(agent)) : "The agent not in agent list when evaluating";
    MultivariateQuadFunction evaluatedFunc = new MultivariateQuadFunction(this);
    
    // Adding xi^2 and xi to d
    evaluatedFunc.addToIntercept(this.getQuadratic().get(agent, agent) * Math.pow(value, 2));
    evaluatedFunc.addToIntercept(this.getLinear().get(agent) * value);    
    
    // Update xi, xj
    for (Map.Entry<String, Double> rowEntry : this.getQuadratic().row(agent).entrySet()) {
      String agent_j = rowEntry.getKey();
      Double c_ij = rowEntry.getValue();
      
      evaluatedFunc.addNewOrUpdateLinear(agent_j, c_ij * value + evaluatedFunc.getLinear().get(agent_j));
    }
    
    evaluatedFunc.removeEntryFromLinear(agent);
    evaluatedFunc.removeEntryFromQuadratic(agent);
        
    return evaluatedFunc;
  }
  
  public void removeEntryFromLinear(final String agent) {
    assert this.linear.keySet().contains(agent) : "Linear not containing agent when removing";
    this.linear.remove(agent);
  }
  
  public void removeEntryFromQuadratic(final String agent) {
    assert this.quadratic.rowKeySet().contains(agent) : "Quadratic row not containing agent when removing";
    assert this.quadratic.columnKeySet().contains(agent) : "Quadratic column not containing agent when removing";
    this.quadratic.rowMap().remove(agent);
    this.quadratic.columnMap().remove(agent);
  }
  
  @Override
  public String toString() {
    return "Agents = " + getAgents() + "\n" +
        "b[] = " + linear + "\n" + 
        "c[][] = " + quadratic + "\n" +
        "d = " + intercept + "\n";
  }

  public void checkEqualsize() {
    assert linear.entrySet().size() == quadratic.rowMap().size() : "linear and quadratic has different size";
    assert quadratic.rowMap().size() == quadratic.columnMap().size() : "quadratic has different size of row and column";
  }
  
  // TODO review this function
  public PiecewiseMultivariateFunction approxProject(int numberOfIntervals, String selfAgent) {
    PiecewiseMultivariateFunction mpwFunc = new PiecewiseMultivariateFunction();
    // Return: projected into a unary function
    // 1. Divide each interval into to smaller k intervals accordingly
    //  Then we have k^(#agents) intervals
    // 2. In <itv1, itv2,..., itvn> in k^(#agents) intervals, get the midpoints 
    // 
    // 3. Evaluate into a unary function where the selfAgent is the only variable
    // 4. Get argmax
    // 5. Evaluate the original function with this argmax value (Now the arity is decreased by 1)
    //  The function becomes Piecewise MultivariateFunction
    
    // List of {intervalListAgent1,...,intervalListAgentN}
    List<Set<Interval>> intervalsSetList = new ArrayList<Set<Interval>>();
    for (Map.Entry<String, Interval> entry : this.intervals.entrySet()) {
      String entryAgent = entry.getKey();
      if (entryAgent.equals(selfAgent)) continue;

      Interval entryInterval = entry.getValue();
      
      Set<Interval> intervalsSet = new HashSet<Interval>(entryInterval.separateIntoAListOfIntervals(numberOfIntervals)); 
      intervalsSetList.add(intervalsSet);
    }
    System.out.println(intervalsSetList);
    // The ordering of intervals is preserved in the ordering of agents from intervals
    Set<List<Interval>> productIntervals = Sets.cartesianProduct(intervalsSetList);

    // for each list of intervals, we have a function
    // the result of this process is a piecewise multivariate function
    for (List<Interval> prodItvList : productIntervals) {
      int index = -1;
      // for each agent, gets the values, evaluate the function
      MultivariateQuadFunction midPointedFunction = new MultivariateQuadFunction(this);
      Map<String, Interval> intervalsOfNewFunction = new HashMap<>();
      for (Map.Entry<String, Interval> entry : this.intervals.entrySet()) {
        String entryAgent = entry.getKey();
        if (entryAgent.equals(selfAgent)) continue;
        index++;

        Interval interval = prodItvList.get(index);
        intervalsOfNewFunction.put(entryAgent, interval);
      }
      midPointedFunction = this.evaluateWithMap(intervalsOfNewFunction);
      double argmax = midPointedFunction.getArgmax();
      midPointedFunction = this.evaluateMultivariate(selfAgent, argmax);
      midPointedFunction.setIntervals(intervalsOfNewFunction);
      mpwFunc.addNewFunction(midPointedFunction);
    }
    
    return mpwFunc;
  } 
  
  public MultivariateQuadFunction evaluateWithMap(Map<String, Interval> agentValueMap) {
    // recursively evaluate the function
    // until the function become unary function
    // at this time, find the argmax
    MultivariateQuadFunction func = new MultivariateQuadFunction(this);
    for (Map.Entry<String, Interval> entry : agentValueMap.entrySet()) {
      String agent = entry.getKey();
      double value = entry.getValue().midValue();
      func = func.evaluateMultivariate(agent, value);
    }
    
    if (func.getNumberOfVars() != 1) {
      throw new NotAUnaryFunction("The number of variables is " + func.getNumberOfVars());
    }
    
    return func;
  }
  
  public double evaluateUnaryFunction(String agent, Double value) {
    assert this.getNumberOfVars() == 1 : "Number of vars should be evaluated in unary function " + this.getNumberOfVars();
    
    return quadratic.get(agent, agent) * Math.pow(value, 2) + linear.get(agent) * value + intercept;
  }
  
  public double getArgmax() {
    if (this.getNumberOfVars() == 1) {
      throw new NotAUnaryFunction("The number of variables is " + this.getNumberOfVars()); 
    }
    String agent = new ArrayList<String>(getAgents()).get(0);
    double LB = intervals.get(agent).getLowerBound();
    double UB = intervals.get(agent).getUpperBound();
    double midPoint = - linear.get(agent) / (2 * quadratic.get(agent, agent));
    
    double lowerEvaluated = evaluateUnaryFunction(agent, LB);
    double upperEvaluated = evaluateUnaryFunction(agent, UB);
    double midEvaluated = evaluateUnaryFunction(agent, midPoint);
    
    double max = Math.max(Math.max(lowerEvaluated, upperEvaluated), midEvaluated);
    
    if (Double.compare(max, lowerEvaluated) == 0) return LB;
    else if (Double.compare(max, upperEvaluated) == 0) return UB;
    else if (Double.compare(max, midEvaluated) == 0) return midEvaluated;
    
    return -Double.MAX_VALUE;
  }
  
  public int getNumberOfVars() {
    checkEqualsize();
    return linear.entrySet().size();
  }

  public Set<String> getAgents() {
    checkEqualsize();
    return linear.keySet();
  }

  public Table<String, String, Double> getQuadratic() {
    return quadratic;
  }

  public void setQuadratic(Table<String, String, Double> quadratic) {
    this.quadratic = quadratic;
  }

  public Map<String, Double> getLinear() {
    return linear;
  }

  public void setLinear(Map<String, Double> linear) {
    this.linear = linear;
  }

  public double getIntercept() {
    return intercept;
  }

  public void setIntercept(double intercept) {
    this.intercept = intercept;
  }

  public Map<String, Interval> getIntervals() {
    return intervals;
  }

  public void setIntervals(Map<String, Interval> intervals) {
    this.intervals = intervals;
  }   
}
