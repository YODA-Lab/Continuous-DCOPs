package function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import function.multivariate.MultivariateQuadFunction;
import zexception.FunctionException;
import zexception.DiffIntervalException;

public class BinaryFunction extends Function implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -8675857466946038096L;
  /**
   * 
   */
 
  
  public BinaryFunction(FunctionType functionType) {
    this.functionType = functionType;
  }
    
  public abstract double evaluate(double x, double y);
  
  public Set<Double> solveForRoots() {
    return null;
  }
  
  public boolean isInRange(double a, double b) {
    return Double.compare(selfInterval.getLowerBound(), a) <= 0 
        && Double.compare(b, selfInterval.getUpperBound()) <= 0;
  }  
  
  public String getSelfAgent() {
    return selfAgent;
  }

  public void setSelfAgent(String selfAgent) {
    this.selfAgent = selfAgent;
  }

  public String getOtherAgent() {
    return otherAgent;
  }

  public void setOtherAgent(String otherAgent) {
    this.otherAgent = otherAgent;
  }

  public Interval getSelfInterval() {
    return selfInterval;
  }

  public void setSelfInterval(Interval selfInterval) {
    this.selfInterval = selfInterval;
  }

  public Interval getOtherInterval() {
    return otherInterval;
  }

  public void setOtherInterval(Interval otherInterval) {
    this.otherInterval = otherInterval;
  }  
  
  public void checkSameSelfAgent(BinaryFunction f, String funcType) {
    if (!this.selfAgent.equals(f.getSelfAgent())) {
      throw new FunctionException("Different AGENT when adding " + funcType + " function");
    }
  }
  
  public void checkSameSelfInterval(BinaryFunction f, String funcType) {
    if (!this.selfInterval.equals(f.getSelfInterval())) {
        throw new DiffIntervalException("Different INTERVAL when adding " + funcType + " function");
    }
  }
  
  public void checkSameAgents(BinaryFunction f, String funcType) {
    Set<String> selfAgentSet = new HashSet<>();
    selfAgentSet.add(this.getSelfAgent());
    selfAgentSet.add(this.getOtherAgent());

    Set<String> otherAgentSet = new HashSet<>();
    otherAgentSet.add(f.getSelfAgent());
    otherAgentSet.add(f.getOtherAgent());
    
    if (!selfAgentSet.equals(otherAgentSet)) {
      throw new FunctionException("Different AGENT SET when adding " + funcType + " function");
    }
  }
  
  public void checkSameIntervals(BinaryFunction f, String funcType) {
    Set<Interval> selfIntervalSet = new HashSet<>();
    selfIntervalSet.add(this.getSelfInterval());
    selfIntervalSet.add(this.getOtherInterval());
    
    Set<Interval> otherIntervalSet = new HashSet<>();
    selfIntervalSet.add(f.getSelfInterval());
    selfIntervalSet.add(f.getOtherInterval());
    
    if (!selfIntervalSet.equals(otherIntervalSet)) {
      throw new FunctionException("Different INTERVAL SET when adding " + funcType + " function");
    }
  }

  @Override
  public Function evaluate(Map<String, Double> variableValueMap) {
    // TODO Auto-generated method stub
    return null;
  }
}
