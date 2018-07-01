package function;

import java.io.Serializable;
import java.util.Set;

import function.multivariate.MultivariateQuadFunction;
import zexception.DiffAgentException;
import zexception.DiffIntervalException;

public abstract class Function implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -8675857466946038096L;
  /**
   * 
   */
  protected String selfAgent;
  protected String otherAgent;
  protected Interval selfInterval;
  protected Interval otherInterval;
  
  public static String FUNCTION_TYPE;
  
  public Function(final String selfAgent, final String otherAgent, final Interval selfInterval, final Interval otherInterval) {
    this.selfAgent = selfAgent;
    this.otherAgent = otherAgent;
    this.selfInterval = selfInterval;
    this.otherInterval = otherInterval;
  }
  
  public abstract Object evaluate(double x);
  
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
  
  public void checkSameSelfAgent(Function f, String funcType) {
    if (!this.selfAgent.equals(f.getSelfAgent())) {
      throw new DiffAgentException("Different AGENT when adding " + funcType + " function");
    }
  }
  
  public void checkSameSelfInterval(Function f, String funcType) {
    if (!this.selfInterval.equals(f.getSelfInterval())) {
        throw new DiffIntervalException("Different INTERVAL when adding " + funcType + " function");
    }
  }
  
  public void checkSameAgents(Function f, String funcType) {
    Set<String> selfAgentSet = Set.of(this.getSelfAgent(), this.getOtherAgent());
    Set<String> otherAgentSet = Set.of(f.getSelfAgent(), f.getOtherAgent());
    
    if (!selfAgentSet.equals(otherAgentSet)) {
      throw new DiffAgentException("Different AGENT SET when adding " + funcType + " function");
    }
  }
  
  public void checkSameIntervals(Function f, String funcType) {
    Set<Interval> selfIntervalSet = Set.of(this.getSelfInterval(), this.getOtherInterval());
    Set<Interval> otherIntervalSet = Set.of(f.getSelfInterval(), f.getOtherInterval());
    
    if (!selfIntervalSet.equals(otherIntervalSet)) {
      throw new DiffAgentException("Different INTERVAL SET when adding " + funcType + " function");
    }
  }
}
