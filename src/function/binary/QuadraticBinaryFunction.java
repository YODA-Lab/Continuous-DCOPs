//package function.binary;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import function.BinaryFunction;
//import function.Interval;
//import table.Row;
//import table.Table;
//import utilities.*;
//
///**
// * @author khoihd
// *    <p>a1x1^2 + b1x1
// *    <p>a2x2^2 + b2x2
// *    <p>a3x1x2 + b3
// */
//public class QuadraticBinaryFunction extends BinaryFunction implements Serializable {
//
//  
//  /**
//   * 
//   */
//  private static final long serialVersionUID = 917101260312191808L;
//  
//  private double a1;
//  private double b1;
//  private double a2;
//  private double b2;
//  private double a3;
//  private double b3;
//    
//  public QuadraticBinaryFunction(String selfAgent, String otherAgent, Interval selfInterval, Interval otherInterval) {
//    super(selfAgent, otherAgent, selfInterval, otherInterval);
//    FUNCTION_TYPE = "BINARY QUADRATIC";
//  }
//  
//  public QuadraticBinaryFunction(double a1, double b1, double a2, double b2, double a3, double b3, String selfAgent,
//      String otherAgent, Interval selfInterval, Interval otherInterval) {
//    super(selfAgent, otherAgent, selfInterval, otherInterval);
//    this.a1 = a1;
//    this.b1 = b1;
//    this.a2 = a2;
//    this.b2 = b2;
//    this.a3 = a3;
//    this.b3 = b3;
//  }
//  
//  public QuadraticBinaryFunction(QuadraticBinaryFunction f) {
//    this(f.getA1(), f.getB1(), f.getA2(), f.getB2(), f.getA3(), f.getB3(), f.getSelfAgent(), f.getOtherAgent(), f.getSelfInterval(), f.getOtherInterval());
//  }
//
//  public QuadraticBinaryFunction addUnaryFuncDiffInterval(QuadraticUnaryFunction unaryFunc) {
//    checkSameSelfAgent(unaryFunc, FUNCTION_TYPE);
//    return new QuadraticBinaryFunction(this.a1 + unaryFunc.getA(), this.b1 + unaryFunc.getB(), this.a2, this.b2,
//        this.a3, this.b3 + unaryFunc.getC(), selfAgent, otherAgent, selfInterval, otherInterval);
//  }
//  
//  public QuadraticBinaryFunction addUnaryFunction(QuadraticUnaryFunction unaryFunc) {
//    checkSameSelfAgent(unaryFunc, FUNCTION_TYPE);
//    checkSameSelfInterval(unaryFunc, FUNCTION_TYPE);
//    return new QuadraticBinaryFunction(this.a1 + unaryFunc.getA(), this.b1 + unaryFunc.getB(), this.a2, this.b2,
//        this.a3, this.b3 + unaryFunc.getC(), selfAgent, otherAgent, selfInterval, otherInterval);
//  }
//  
//  public QuadraticBinaryFunction addBinaryFunction(QuadraticBinaryFunction binaryFunc) {
//    checkSameAgents(binaryFunc, FUNCTION_TYPE);
//    checkSameIntervals(binaryFunc, FUNCTION_TYPE);
//    return new QuadraticBinaryFunction(this.a1 + binaryFunc.getA1(), this.b1 + binaryFunc.getB1(),
//        this.a2 + binaryFunc.getA2(), this.b2 + binaryFunc.getB2(), this.a3 + binaryFunc.getA3(),
//        this.b3 + binaryFunc.getB3(), selfAgent, otherAgent, selfInterval, otherInterval);
//  }
//  
//  /**
//   * This function is manually checked
//   * @param numberOfIntervals
//   * @return
//   */
//  public PiecewiseFunction approxProject(int numberOfIntervals) {
//    PiecewiseFunction pwFunction = new PiecewiseFunction(otherAgent, null); 
//    // divide otherIntervals into smallers one
//    // for each smaller divided interval, pick the midPoint
//    // evaluateX2 => get x1 = argmax
//    // return f(x1 = argmax,x2)
//    
//    List<Interval> intervalList = otherInterval.separateIntoAListOfIncreasingIntervals(numberOfIntervals);
//    
//    for (Interval interval : intervalList) {
//      double midPoint = 0.5 * (interval.getLowerBound() + interval.getUpperBound());
//      QuadraticUnaryFunction midpointedFunction = this.evaluateBinaryFunctionX2(midPoint);
//      double argmaxX1 = midpointedFunction.getArgMax();
//      QuadraticUnaryFunction unaryF = this.evaluateBinaryFunctionX1(argmaxX1);
//      unaryF.setSelfInterval(interval);
//      pwFunction.addNewFunction(unaryF);
//      
////      System.out.println("Interval " + interval);
////      System.out.println("Unary function " + midpointedFunction);
////      System.out.println("Armax " + argmax);
////      System.out.println("UnaryF " + unaryF);
//    }
////    System.out.println("Approximately projected functions: " + pwFunction);
//    
//    return pwFunction;
//  }
//    
//  /**
//   * This funcion is checked manually
//   * @return a PiecewiseFunction of largest function on its interval
//   */
//  public PiecewiseFunction analyticallyProject() {
//    // After project, the function become Piecewise of UnaryFunction
//    // Also change the owner to the otherAgent
//    PiecewiseFunction pwFunction = new PiecewiseFunction(otherAgent, null); //projected into a unary function
//    
//    // All of them should have the same interval
//    List<QuadraticUnaryFunction> candidateFuncList = new ArrayList<>();
//    
//    candidateFuncList.add(getUnaryFunctionAtCriticalPoint());
//    candidateFuncList.add(evaluateBinaryFunctionX1(selfInterval.getLowerBound()));
//    candidateFuncList.add(evaluateBinaryFunctionX1(selfInterval.getUpperBound()));
//    
//    // To compare those quadratic unary functions, we need to solve f1 = x2, and then get the range
//    List<Interval> itvSortedList = new ArrayList<>();
//    for (int index = 0; index < candidateFuncList.size() - 1; index++) {
//      itvSortedList.addAll(Utilities.solveUnaryQuadForIntervals(candidateFuncList.get(index), candidateFuncList.get(index + 1)));
//    }
//    
//    itvSortedList = Utilities.mergeIntervals(itvSortedList);
//    
//    for (Interval interval : itvSortedList) {
//      double midPoint = 0.5 * (interval.getLowerBound() + interval.getUpperBound());
//      List<Double> evalList = candidateFuncList.stream().map(x -> x.evaluate(midPoint)).collect(Collectors.toList());
//      int maxIndex = evalList.indexOf(Collections.max(evalList));
//      pwFunction.addNewFunction(new QuadraticUnaryFunction(candidateFuncList.get(maxIndex)));
//    }
//    
//    System.out.println("MAX FUNCTION:\n" + pwFunction);
//    
//    return pwFunction;
//  }
//  
//  /**
//   * This function is correct
//   * @return UnaryFunction from taking the derivative and set to 0
//   */
//  public QuadraticUnaryFunction getUnaryFunctionAtCriticalPoint() {
//    double newA1 = - Math.pow(a3, 2) / (4*a1) + a2;
//    double newB1 = - b1*a3 / (2*a1) + b2;
//    double newC1 = - Math.pow(b1, 2) / (4*a1) + b3;
//    return new QuadraticUnaryFunction(newA1, newB1, newC1, otherAgent, otherInterval);
//  }
//  
//  /**
//   * This function is correct
//   * @param x1 value of the selfAgent
//   * @return a new {@link QuadraticUnaryFunction} owned by otherAgent
//   */
//  public QuadraticUnaryFunction evaluateBinaryFunctionX1(double x1) {
//    return new QuadraticUnaryFunction(a2,
//        b2 + a3*x1,
//        a1*Math.pow(x1, 2) + b1*x1 + b3,
//        otherAgent,
//        otherInterval);
//  }
//  
//  /**
//   * This function is correct
//   * @param x2 value of the otherAgent
//   * @return a new {@link QuadraticUnaryFunction} owned by selfAgent
//   */
//  public QuadraticUnaryFunction evaluateBinaryFunctionX2(double x2) {
//    return new QuadraticUnaryFunction(a1, 
//        b1 + a3*x2,
//        a2*Math.pow(x2, 2) + b2*x2 + b3,
//        selfAgent,
//        selfInterval);
//  }
//  
//  public double getA1() {
//    return a1;
//  }
//
//  public void setA1(double a1) {
//    this.a1 = a1;
//  }
//
//  public double getB1() {
//    return b1;
//  }
//
//  public void setB1(double b1) {
//    this.b1 = b1;
//  }
//
//  public double getA2() {
//    return a2;
//  }
//
//  public void setA2(double a2) {
//    this.a2 = a2;
//  }
//
//  public double getB2() {
//    return b2;
//  }
//
//  public void setB2(double b2) {
//    this.b2 = b2;
//  }
//
//  public double getA3() {
//    return a3;
//  }
//
//  public void setA3(double a3) {
//    this.a3 = a3;
//  }
//
//  public double getB3() {
//    return b3;
//  }
//
//  public void setB3(double b3) {
//    this.b3 = b3;
//  }
//
//  public String getOtherAgent() {
//    return otherAgent;
//  }
//
//  public void setOtherAgent(String otherAgent) {
//    this.otherAgent = otherAgent;
//  }
//
//  public Interval getOtherInterval() {
//    return otherInterval;
//  }
//
//  public void setOtherInterval(Interval otherInterval) {
//    this.otherInterval = otherInterval;
//  }
//  
//  @Override
//  public String toString() {
//    return a1 + " X1^2 " + b1 + " X1 " + "\n"
//        + a2 + " X2^2 " + b2 + " X2 " + "\n"
//        + a3 + " X1X2 " + b3
//        + "\t" + selfInterval + "\t" + otherInterval
//        + "\t" + selfAgent + "\t" + otherAgent;
//  }
//
//  @Override
//  public Object evaluate(double x) {
//    // TODO Auto-generated method stub
//    return null;
//  }
//  
//  @Override
//  public double evaluate(double x1, double x2) {
//    return evaluateBinaryFunctionX1(x1).evaluate(x2);
//  }
//  
//  @Override
//  public boolean equals(Object quadBinaryFuncToCompare) {
//    // If the object is compared with itself then return true  
//    if (quadBinaryFuncToCompare == this) {
//        return true;
//    }
//    
//    if (!(quadBinaryFuncToCompare instanceof QuadraticBinaryFunction)) {
//      return false;
//    }
//       
//    QuadraticBinaryFunction castedQuadBinaryFunc = (QuadraticBinaryFunction) quadBinaryFuncToCompare;
//      
//    return Double.compare(castedQuadBinaryFunc.getA1(), this.getA1()) == 0 &&
//        Double.compare(castedQuadBinaryFunc.getB1(), this.getB1()) == 0 &&
//        Double.compare(castedQuadBinaryFunc.getA2(), this.getA2()) == 0 &&
//        Double.compare(castedQuadBinaryFunc.getB2(), this.getB2()) == 0 &&
//        Double.compare(castedQuadBinaryFunc.getA3(), this.getA3()) == 0 &&
//        Double.compare(castedQuadBinaryFunc.getB3(), this.getB3()) == 0 &&
//        castedQuadBinaryFunc.getSelfAgent().equals(this.getSelfAgent()) &&
//        castedQuadBinaryFunc.getOtherAgent().equals(this.getOtherAgent()) &&
//        castedQuadBinaryFunc.getSelfInterval().equals(this.getSelfInterval()) &&
//        castedQuadBinaryFunc.getOtherInterval().equals(this.getOtherInterval());
//  }
//  
//  @Override
//  public int hashCode() {
//    return Objects.hash(a1, b1, a2, b2, a3, b3, selfAgent, otherAgent, selfInterval, otherInterval);
//  }
//}
