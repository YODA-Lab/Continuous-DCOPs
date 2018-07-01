package function.binary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.Math.*;

import function.Function;
import function.Interval;
import static utilities.Utilities.*;

/**
 *  <p> ax^3 + bx^2 + cx + d
 *  @author khoihd
 */
public class CubicUnaryFunction extends Function implements Serializable {  
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
  private double d;

  
  /**
   * @param selfAgent
   * @param selfInterval
   */
  public CubicUnaryFunction(final String selfAgent, final Interval selfInterval) {
    super(selfAgent, null, selfInterval, null);
    FUNCTION_TYPE = "UNARY CUBIC";
  }

  /**
   * @param f1
   */
  public CubicUnaryFunction(CubicUnaryFunction f1) {
    super(f1.getSelfAgent(), null, f1.getSelfInterval(), null);
    this.a = f1.getA();
    this.b = f1.getB();
    this.c = f1.getC();
    this.d = f1.getD();
  }
  
  /**
   * @param a
   * @param b
   * @param c
   * @param d
   * @param agent
   * @param interval
   */
  public CubicUnaryFunction(double a, double b, double c, double d, String agent, Interval interval) {
    super(agent, null, interval, null);
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  /**
   * @param a
   * @param b
   * @param c
   * @param d
   */
  public void addNewUnaryFunction(double a, double b, double c, double d) {
    this.a += a;
    this.b += b;
    this.c += c;
    this.d += d;
  }
  
  public CubicUnaryFunction addNewUnaryFunction(CubicUnaryFunction f) {
    checkSameSelfAgent(f, "CUBIC");
    checkSameSelfInterval(f, "CUBIC");
    return new CubicUnaryFunction(a + f.getA(), b + f.getB(), c + f.getC(), d + f.getD(), this.selfAgent, this.selfInterval);
  }

  // TODO
  /**
   * @return maximum value of the function
   */
  public double getMax() {
    List<Double> values = new ArrayList<>();
    List<Double> eval = new ArrayList<>();
    
    // differentiate the function
    QuadraticUnaryFunction quadFunc = new QuadraticUnaryFunction(3 * a, 2 * b, c, selfAgent, selfInterval);
    values.addAll(quadFunc.solveForRoots());    
    values.add(selfInterval.getLowerBound());
    values.add(selfInterval.getUpperBound());

    eval = values.stream().map(x -> evaluate(x)).collect(Collectors.toList());
    return Collections.max(eval);
  }
  
  // TODO rewrite
  public double getArgMax() {
    List<Double> values = new ArrayList<>();
    List<Double> eval = new ArrayList<>();
    
    // differentiate the function
    QuadraticUnaryFunction quadFunc = new QuadraticUnaryFunction(3 * a, 2 * b, c, selfAgent, selfInterval);
    values.addAll(quadFunc.solveForRoots());    
    values.add(selfInterval.getLowerBound());
    values.add(selfInterval.getUpperBound());

    eval = values.stream().map(x -> evaluate(x)).collect(Collectors.toList());
    return values.get(eval.indexOf(Collections.max(eval)));
  }
  
  @Override
  public Set<Double> solveForRoots() {
    TreeSet<Double> rootsAndBounds = new TreeSet<>();
    double LB = selfInterval.getLowerBound();
    double UB = selfInterval.getUpperBound();
    rootsAndBounds.add(LB);
    rootsAndBounds.add(UB);
    
    double normalizedB = b / a;
    double normalizedC = c / a;
    double normalizedD = d / a;

    double newA = normalizedB;
    double newB = normalizedC;
    double newC = normalizedD;
    
    double newP = newB - pow(newA, 2)/3;
    double newQ = 2*pow(newA, 3)/27 - newA*newB/3 + newC;
    
    double discriminant = pow(newQ, 2)/4 + pow(newP, 3)/27;
    discriminant = roundDouble(discriminant);
        
    if (Double.compare(discriminant, Double.valueOf(0.0)) > 0) {
      double x = cbrt(-newQ/2 + sqrt(discriminant)) + cbrt(-newQ/2 - sqrt(discriminant)) - a/3;
      rootsAndBounds.add(roundDouble(x));
    }
    else if (Double.compare(discriminant, Double.valueOf(0.0)) == 0) {
      double x1 = -2*cbrt(newQ/2) - newA/3;
      double x2 = cbrt(newQ/2) - newA/3;
      rootsAndBounds.add(roundDouble(x1));
      rootsAndBounds.add(roundDouble(x2));
    }
    else {
      double x1 = 2/sqrt(3) * sqrt(-newP) * sin(1.0/3 * asin(3*sqrt(3)/2 * newQ/pow(-newP, 3.0/2))) - newA/3;
      double x2 = -2/sqrt(3) * sqrt(-newP) * sin(1.0/3 * asin(3*sqrt(3)/2 * newQ/pow(-newP, 3.0/2)) + PI/3) - newA/3;
      double x3 = 2/sqrt(3) * sqrt(-newP) * cos(1.0/3 * asin(3*sqrt(3)/2 * newQ/pow(-newP, 3.0/2)) + PI/6) - newA/3;
      rootsAndBounds.add(roundDouble(x1));
      rootsAndBounds.add(roundDouble(x2));
      rootsAndBounds.add(roundDouble(x3));
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

  public double getD() {
    return d;
  }

  public void setD(double d) {
    this.d = d;
  }
  
  @Override
  public String toString() {
    return a + " X^2 " + b + " X " + c + "\t" + selfInterval + "\t" + selfAgent;
  }

  @Override
  public double evaluate(double x, double y) {
    return Double.MAX_VALUE;
  }
  
  @Override
  // ax^3 + bx^2 + cx + d
  public Double evaluate(double x) {
    return a * pow(x, 3) + b * pow(x, 2) + c * x + d;
  }
  
  @Override
  public boolean equals(Object cubicUnaryFuncToCompare) {
    // If the object is compared with itself then return true  
    if (cubicUnaryFuncToCompare == this) {
        return true;
    }
    
    if (!(cubicUnaryFuncToCompare instanceof CubicUnaryFunction)) {
      return false;
    }
       
    CubicUnaryFunction castedCubicUnaryFunc = (CubicUnaryFunction) cubicUnaryFuncToCompare;
      
    return Double.compare(castedCubicUnaryFunc.getA(), this.getA()) == 0 &&
        Double.compare(castedCubicUnaryFunc.getB(), this.getB()) == 0 &&
        Double.compare(castedCubicUnaryFunc.getC(), this.getC()) == 0 &&
        Double.compare(castedCubicUnaryFunc.getD(), this.getD()) == 0 &&
        castedCubicUnaryFunc.getSelfAgent().equals(this.getSelfAgent()) &&
        castedCubicUnaryFunc.getOtherAgent().equals(this.getOtherAgent()) &&
        castedCubicUnaryFunc.getSelfInterval().equals(this.getSelfInterval()) &&
        castedCubicUnaryFunc.getOtherInterval().equals(this.getOtherInterval());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(a, b, c, d, selfAgent, otherAgent, selfInterval, otherInterval);
  }
}
