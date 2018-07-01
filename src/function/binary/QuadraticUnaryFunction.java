package function.binary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.Math.*;

import function.Function;
import function.Interval;

/**
 * @author khoihd
 *    ax^2 + bx + c
 */
public class QuadraticUnaryFunction extends Function implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = -902091853584620558L;
  /**
   * 
   */
  private double a;
  private double b;
  private double c;
  
  public QuadraticUnaryFunction(final String selfAgent, final String otherAgent, final Interval selfInterval, final Interval otherInterval) {
    super(selfAgent, otherAgent, selfInterval, otherInterval);
    FUNCTION_TYPE = "UNARY QUADRATIC";
  }
  
  public QuadraticUnaryFunction(QuadraticUnaryFunction f) {
    this(f.getSelfAgent(), f.getOtherAgent(), f.getSelfInterval(), f.getOtherInterval());
    this.a = f.getA();
    this.b = f.getB();
    this.c = f.getC();
  }
  
  public QuadraticUnaryFunction(double a, double b, double c, String selfAgent, Interval interval) {
    super(selfAgent, null, interval, null);
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public void addNewUnaryFunction(double a, double b, double c) {
    this.a += a;
    this.b += b;
    this.c += c;
  }
  
  public QuadraticUnaryFunction addNewUnaryFunction(QuadraticUnaryFunction f) {
    checkSameSelfAgent(f, "QUADRATIC");
    checkSameSelfInterval(f, "QUADRATIC");
    return new QuadraticUnaryFunction(a + f.getA(), b + f.getB(), c + f.getC(), selfAgent, selfInterval);
  }
  
  /**
   * This function is checked
   * @return max function
   */
  public double getMax() {
    List<Double> values = new ArrayList<>();
    List<Double> eval = new ArrayList<>();
    double LB = selfInterval.getLowerBound();
    double UB = selfInterval.getUpperBound();
    double critPoint = -b / (2*a);
    
    values.add(LB);    
    values.add(UB);
    if (Double.compare(LB, critPoint) <= 0 && Double.compare(critPoint, UB) <= 0) {
      values.add(critPoint);
    }

    eval = values.stream().map(x -> evaluate(x)).collect(Collectors.toList());
    return Collections.max(eval);
  }
  
  /**
   * This function is checked
   * @return argmax function
   */
  public double getArgMax() {
    List<Double> values = new ArrayList<>();
    List<Double> eval = new ArrayList<>();
    double LB = selfInterval.getLowerBound();
    double UB = selfInterval.getUpperBound();
    double critPoint = -b / (2*a);
    
    values.add(LB);    
    values.add(UB);
    if (Double.compare(LB, critPoint) <= 0 && Double.compare(critPoint, UB) <= 0) {
      values.add(critPoint);
    }

    eval = values.stream().map(x -> evaluate(x)).collect(Collectors.toList());
    return values.get(eval.indexOf(Collections.max(eval)));
  }
  
  /** 
   * Tested in unit test
   * @return Set of roots
   */
  @Override
  public Set<Double> solveForRoots() {
    TreeSet<Double> rootsAndBounds = new TreeSet<>();
    double LB = selfInterval.getLowerBound();
    double UB = selfInterval.getUpperBound();
    rootsAndBounds.add(LB);
    rootsAndBounds.add(UB);
    
    double delta = pow(b, 2) - 4*a*c;
    if (Double.compare(delta, 0) == 0) {
      rootsAndBounds.add(-b / (2*a));
    }
    else if (Double.compare(delta, 0) > 0){
      rootsAndBounds.add((-b-sqrt(delta)) / (2*a));
      rootsAndBounds.add((-b+sqrt(delta)) / (2*a));
    }
    
    return rootsAndBounds.subSet(LB, false, UB, false);
  }
  
  public double getA() {
    return a;
  }

  public void setA(double a) {
    this.a = a;
  }

  public double getB() {
    return b;
  }

  public void setB(double b) {
    this.b = b;
  }

  public double getC() {
    return c;
  }

  public void setC(double c) {
    this.c = c;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(a).append(" X2").append(b).append(" X").append(c).append("\t").append(selfInterval).append("\t").append(selfAgent);
    return sb.toString();
  }

  @Override
  public double evaluate(double x, double y) {
    return Double.MAX_VALUE;
  }
  
  @Override
  public Double evaluate(double x) {
    return a * pow(x, 2) + b * x + c;
  }
  
  @Override
  public boolean equals(Object quadUnaryFuncToCompare) {
    // If the object is compared with itself then return true  
    if (quadUnaryFuncToCompare == this) {
        return true;
    }
    
    if (!(quadUnaryFuncToCompare instanceof QuadraticUnaryFunction)) {
      return false;
    }
       
    QuadraticUnaryFunction castedQuadUnaryFunc = (QuadraticUnaryFunction) quadUnaryFuncToCompare;
      
    return Double.compare(castedQuadUnaryFunc.getA(), this.getA()) == 0 &&
        Double.compare(castedQuadUnaryFunc.getB(), this.getB()) == 0 &&
        Double.compare(castedQuadUnaryFunc.getC(), this.getC()) == 0 &&
        castedQuadUnaryFunc.getSelfAgent().equals(this.getSelfAgent()) &&
        castedQuadUnaryFunc.getOtherAgent().equals(this.getOtherAgent()) &&
        castedQuadUnaryFunc.getSelfInterval().equals(this.getSelfInterval()) &&
        castedQuadUnaryFunc.getOtherInterval().equals(this.getOtherInterval());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(a, b, c, selfAgent, otherAgent, selfInterval, otherInterval);
  }
}
