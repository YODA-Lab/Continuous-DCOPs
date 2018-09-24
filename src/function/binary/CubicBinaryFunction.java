//package function.binary;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//
//import function.BinaryFunction;
//import function.Interval;
//import utilities.Utilities;
//
///**
// * @author khoihd
// *    <p>a1x1^3 + b1x2x1^2
// *    <p>a2x2^3 + b2x1x2^2
// *    <p>c
// */
//public class CubicBinaryFunction extends BinaryFunction implements Serializable {
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
//  private double c;
//  
//  public CubicBinaryFunction(String selfAgent, String otherAgent, Interval selfInterval, Interval otherInterval) {
//    super(selfAgent, otherAgent, selfInterval, otherInterval);
//    FUNCTION_TYPE = "BINARY CUBIC";
//  }
//  
//  public CubicBinaryFunction(double a1, double b1, double a2, double b2, double c, String selfAgent,
//      String otherAgent, Interval selfInterval, Interval otherInterval) {
//    this(selfAgent, otherAgent, selfInterval, otherInterval);
//    this.a1 = a1;
//    this.b1 = b1;
//    this.a2 = a2;
//    this.b2 = b2;
//    this.c = c;
//  }
//    
//  public CubicBinaryFunction(CubicBinaryFunction f) {
//    this(f.getA1(), f.getB1(), f.getA2(), f.getB2(), f.getC(), f.getSelfAgent(), f.getOtherAgent(), f.getSelfInterval(), f.getOtherInterval());
//  }
//
////  public void addNewCubicBinaryFunction(double a1, double b1, double a2, double b2, double c) {
////    this.a1 += a1;
////    this.b1 += b1;
////    this.a2 += a2;
////    this.b2 += b2;
////    this.c += c;
////  }
//  
//
////  public CubicBinaryFunction addNewFunction(CubicBinaryFunction unaryFunc, Interval selfInterval,
////      Interval otherInterval, String selfAgent, String otherAgent) {
////    return new QuadraticBinaryFunction(this.a1 + unaryFunc.getA(), this.b1 + unaryFunc.getB(), this.a2, this.b2,
////        this.a3, this.b3 + unaryFunc.getC(), selfInterval, otherInterval, selfAgent, otherAgent);
////  }
//  
////  public QuadraticBinaryFunction addNewBinaryFunction(QuadraticBinaryFunction binaryFunc, Interval selfInterval,
////      Interval otherInterval, String selfAgent, String otherAgent) {
////    return new QuadraticBinaryFunction(this.a1 + binaryFunc.getA1(), this.b1 + binaryFunc.getB1(),
////        this.a2 + binaryFunc.getA2(), this.b2 + binaryFunc.getB2(), this.a3 + binaryFunc.getA3(),
////        this.b3 + binaryFunc.getB3(), selfInterval, otherInterval, selfAgent, otherAgent);
////  }
//  
//  
//  // TODO
////  public PiecewiseFunction approxProject(int numberOfIntervals) {}
//  
//  // TODO
//  public PiecewiseFunction analyticallyProject() {
//    // After project, the function become Piecewise of UnaryFunction
//    // Also change the owner to the otherAgent
//    
//    PiecewiseFunction pwFunction = new PiecewiseFunction(otherAgent, null); //projected into a unary function
//    
//    // All of them should have the same interval
//    List<CubicUnaryFunction> candidateFuncList = getUnaryFunctionAtCriticalPoint();
//    candidateFuncList.add(evaluateBinaryFunctionX1(selfInterval.getLowerBound()));
//    candidateFuncList.add(evaluateBinaryFunctionX1(selfInterval.getUpperBound()));
//    
//    // To compare those cubic unary functions, we need to solve f1 = x2, and then get the range
//    // To compare those quadratic unary functions, we need to solve f1 = x2, and then get the range
//    List<Interval> itvSortedList = new ArrayList<>();
//    
//    for (int index = 0; index < candidateFuncList.size() - 1; index++) {
//        itvSortedList.addAll(Utilities.solveCubicForIntervals(candidateFuncList.get(index), candidateFuncList.get(index + 1)));
//    }
//    
//    itvSortedList = Utilities.mergeIntervals(itvSortedList);
//    
//    for (Interval interval : itvSortedList) {
//      double midPoint = 0.5 * (interval.getLowerBound() + interval.getUpperBound());
//      
//      List<Double> evalList = new ArrayList<>();
//      for (CubicUnaryFunction func : candidateFuncList) {
//          evalList.add(func.evaluate(midPoint));
//      }
//
//      int maxIndex = evalList.indexOf(Collections.max(evalList));
//      
//      CubicUnaryFunction maxFunctionInTheInterval = new CubicUnaryFunction(candidateFuncList.get(maxIndex));
//      pwFunction.addNewFunction(maxFunctionInTheInterval);
//    }
//    
//    System.out.println("MAX FUNCTION:\n" + pwFunction);
//    
//    return pwFunction;
//  }
//  
//  public int maxFunctionAmong(double eval1, double eval2, double eval3) {
//    double maximum = Math.max(eval1, Math.max(eval2, eval3));
//    if (Double.compare(eval1, maximum) == 0) return 1;
//    else if (Double.compare(eval2, maximum) == 0) return 2;
//    else return 3;
//  }
//  
//  
//  // TODO In progress
//  public List<CubicUnaryFunction> getUnaryFunctionAtCriticalPoint() {
//    List<CubicUnaryFunction> projectedFuncList = new ArrayList<>();
//    
//    double quadraticDeltaX2 = Math.pow(b1, 2) - 3 * a1 * b2;
//    if (Double.compare(quadraticDeltaX2, 0) < 0) {
//      return projectedFuncList;
//    } 
//    else if (Double.compare(quadraticDeltaX2, 0) == 0) {
//      double t = -b1 / (3 * a1);
//      projectedFuncList.add(evaluateValueX2(t));
//      return projectedFuncList;
//    } 
//    else { // Delta > 0
//      double t1 = (-b1 - Math.sqrt(Math.pow(b1, 2) - 3 * a1 * b2)) / (3 * a1);
//      double t2 = (-b1 + Math.sqrt(Math.pow(b1, 2) - 3 * a1 * b2)) / (3 * a1);
//      projectedFuncList.add(evaluateValueX2(t1));
//      projectedFuncList.add(evaluateValueX2(t2));
//      return projectedFuncList;
//    }
//  }
//  
//  /**
//   * @param t The input is x1 = t * x2
//   * @return f(x2) = f(x1 = t * x2, x2)
//   */
//  public CubicUnaryFunction evaluateValueX2(double t) {
//    double newA = a1 * Math.pow(t, 3) + b1 * Math.pow(t, 2) + b2 * t + a2;
//    double newD = this.c;
//    
//    return new CubicUnaryFunction(newA, 0, 0, newD, otherAgent, otherInterval);
//  }
//  
//  public CubicUnaryFunction evaluateBinaryFunctionX1(double x1) {
//    return new CubicUnaryFunction(a2, b2 * x1, b1 * Math.pow(x1, 2), a1 * Math.pow(x1, 3) + c, otherAgent,
//        otherInterval);
//  }
//  
//  public CubicUnaryFunction evaluateBinaryFunctionX2(double x2) {
//    return new CubicUnaryFunction(a1, b1 * x2, b2 * Math.pow(x2, 2), a2 * Math.pow(x2, 3) + c, selfAgent,
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
//  public double getC() {
//    return c;
//  }
//
//  public void setC(double c) {
//    this.c = c;
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
//    
//    return a1 + " X1^3 " + b1 + "X2X1^2 " + "\n"
//        + a2 + " X2^3 " + b2 + " X1X2^2 " + "\n"
//        + c + "\n"
//        + "\t" + selfInterval + "\t" + otherInterval
//        + "\t" + selfAgent + "\t" + otherAgent;
//  }
//
//  @Override
//  public Object evaluate(double x) {
//    return null;
//  }
//  
//  @Override
//  public double evaluate(double x1, double x2) {
//    return evaluateBinaryFunctionX1(x1).evaluate(x2);
//  }
//
//  @Override
//  public boolean equals(Object cubicBinaryFuncToCompare) {
//    // If the object is compared with itself then return true  
//    if (cubicBinaryFuncToCompare == this) {
//        return true;
//    }
//    
//    if (!(cubicBinaryFuncToCompare instanceof CubicBinaryFunction)) {
//      return false;
//    }
//       
//    CubicBinaryFunction castedCubicBinaryFunc = (CubicBinaryFunction) cubicBinaryFuncToCompare;
//      
//    return Double.compare(castedCubicBinaryFunc.getA1(), this.getA1()) == 0 &&
//        Double.compare(castedCubicBinaryFunc.getB1(), this.getB1()) == 0 &&
//        Double.compare(castedCubicBinaryFunc.getA2(), this.getA2()) == 0 &&
//        Double.compare(castedCubicBinaryFunc.getB2(), this.getB2()) == 0 &&
//        Double.compare(castedCubicBinaryFunc.getC(), this.getC()) == 0 &&
//        castedCubicBinaryFunc.getSelfAgent().equals(this.getSelfAgent()) &&
//        castedCubicBinaryFunc.getOtherAgent().equals(this.getOtherAgent()) &&
//        castedCubicBinaryFunc.getSelfInterval().equals(this.getSelfInterval()) &&
//        castedCubicBinaryFunc.getOtherInterval().equals(this.getOtherInterval());
//  }
//  
//  @Override
//  public int hashCode() {
//    return Objects.hash(a1, b1, a2, b2, c, selfAgent, otherAgent, selfInterval, otherInterval);
//  }
//}
