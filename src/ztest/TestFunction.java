package ztest;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import function.Interval;
//import function.binary.CubicUnaryFunction;
//import function.binary.QuadraticBinaryFunction;
//import function.binary.QuadraticUnaryFunction;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;

class TestFunction {
  
//  @Test
  void testAnalyticalProjectDPOP() {
    PiecewiseMultivariateQuadFunction f = new PiecewiseMultivariateQuadFunction();
    MultivariateQuadFunction f1 = new MultivariateQuadFunction();

    f1.getCoefficients().put("1", "1", -2.0);
    f1.getCoefficients().put("1", "", 4.0);    
    f1.getCoefficients().put("2", "2", 2.0);
    f1.getCoefficients().put("2", "", 1.0);
    f1.getCoefficients().put("2", "1", 7.0);
    f1.getCoefficients().put("", "", -10.0);    

    Interval int_1 = new Interval(-5, 5);
    Interval int_2 = new Interval(-10, 10);
    
    Map<String, Interval> intervalMap = new HashMap<>();

    intervalMap.put("1", int_1);
    intervalMap.put("2", int_2);
    
    f.addToFunctionMapWithInterval(f1, intervalMap);
    f.setOwner("1");
    
    out.println("BEFORE projecting:\n" + f);
    
    MultivariateQuadFunction critF = f1.getUnaryFunctionAtCriticalPoint(intervalMap);
    MultivariateQuadFunction LB_function = f1.evaluateBinaryFunctionX1(intervalMap.get(f1.getOwner()).getLowerBound(), intervalMap);
    MultivariateQuadFunction UB_function = f1.evaluateBinaryFunctionX1(intervalMap.get(f1.getOwner()).getUpperBound(), intervalMap);
    
    out.println("CritF :\n" + critF);
    out.println("CritF interval: " + critF.getCritFuncIntervalMap());
    out.println("LB_F :\n" + LB_function);
    out.println("LB_F interval: " + LB_function.getCritFuncIntervalMap());
    out.println("UB_F :\n" + UB_function);
    out.println("UB_F interval: " + UB_function.getCritFuncIntervalMap());
    
    PiecewiseMultivariateQuadFunction projected = f1.analyticalProject(intervalMap);
    out.println("AFTER projecting:\n" + projected);
    
//    double max = -Double.MAX_VALUE;
//    double argmax = -Double.MAX_VALUE;
//    
//    for (MultivariateQuadFunction func : projected.getFunctionMap()) {
//      double[] maxAndArgMax = func.getMaxAndArgMax();
//           
//      if (Double.compare(maxAndArgMax[0], max) > 0) {
//        max = maxAndArgMax[0];
//        argmax = maxAndArgMax[1];
//      }
//    }
//    
//    out.println("MAX VALUE IS " + max);
//    out.println("ARGMAX VALUE IS " + argmax);  
//    
//    max = -Double.MAX_VALUE;
//    argmax = -Double.MAX_VALUE;
//    for (MultivariateQuadFunction func : f.getFunctionMap()) {
//      for (double val1 = -5; Double.compare(val1, 5) <= 0; val1+=0.5) {
//        for (double val2 = -5; Double.compare(val2, 5) <= 0; val2+=0.5) {
//          Map<String, Double> valueMap = new HashMap<>();
//          valueMap.put("1", val1);
//          valueMap.put("2", val2);
//          
//          double evaluated = func.evaluateToValueGivenValueMap(valueMap);
//          if (Double.compare(evaluated, max) > 0) {
//            max = evaluated;
//          }
//        }
//      }
//    }
//    
//    out.println("MAX VALUE IS " + max);
  }
  

//  Internal node combined function: 
//  [Coefficients = {={=-151.1875}, 2={2=4.0, =-35.875, 1=5.0}, 1={1=1.0, =-7.0}}, Intervals = {1=[-10.0, 10.0], 2=[-10.0, 0.0]} ]
//  [Coefficients = {={=-151.1875}, 2={2=4.0, =26.625, 1=5.0}, 1={1=1.0, =-7.0}}, Intervals = {1=[-10.0, 10.0], 2=[0.0, 10.0]} ]
  @Test 
  void testApproxProjectDPOP() {
    PiecewiseMultivariateQuadFunction f = new PiecewiseMultivariateQuadFunction();
    MultivariateQuadFunction f1 = new MultivariateQuadFunction();

    f1.getCoefficients().put("1", "1", -2.0);
    f1.getCoefficients().put("2", "2", 2.0);
    f1.getCoefficients().put("3", "3", 1.0);
    
    f1.getCoefficients().put("2", "1", 7.0);
    f1.getCoefficients().put("2", "3", 8.0);
    f1.getCoefficients().put("3", "1", 9.0);
    
    f1.getCoefficients().put("1", "", 4.0);
    f1.getCoefficients().put("2", "", 1.0);
    f1.getCoefficients().put("3", "", 2.0);

    f1.getCoefficients().put("", "", -10.0); 
    

    Interval int_1 = new Interval(-5, 5);
    Interval int_2 = new Interval(-10, 10);
    Interval int_3 = new Interval(-8, 8);

    Map<String, Interval> intervalMap = new HashMap<>();
    
    intervalMap.put("1", int_1);
    intervalMap.put("2", int_2);
    intervalMap.put("3", int_3);
    
    f.addToFunctionMapWithInterval(f1, intervalMap);
    f.setOwner("1");
    
    System.out.println(f.approxProject(2, "1", 2, true));
  }
  
//  @Test
  void TestApproximateProject() {
//    MultivariateQuadFunction func_1234 = createMultivariateFunction(1, 4);
//    func_1234.setOwner("x1");
//    PiecewiseMultivariateQuadFunction pw1 = func_1234.approxProject(2, "x1", 99);
//    System.out.println(pw1);
  }
  
//  @Test
  void TestAddingPiecewiseMultivariateQuadFunction() {
//    MultivariateQuadFunction func_012 = createMultivariateFunction(1, 3);
//    func_012.setOwner("x1");
//    PiecewiseMultivariateQuadFunction pw1 = func_012.approxProject(2, "x1", 99);
//    
//    System.out.println("pw1 " + pw1);
//
//    MultivariateQuadFunction func_123 = createMultivariateFunction(2, 4);
//    func_123.setOwner("x2");
//    PiecewiseMultivariateQuadFunction pw2 = func_123.approxProject(2, "x2", 99);
//    
//    System.out.println("pw2 " + pw2);
//    
//    System.out.println(pw1.addPiecewiseFunction(pw2));
  }
    
//  @Test
  void testAddingMultivariateQuadFunction() {
//    MultivariateQuadFunction func_123 = createMultivariateFunction(1, 3);
//    MultivariateQuadFunction func_234 = createMultivariateFunction(2, 4);
//
//    System.out.println("func_123" + func_123);
//    System.out.println("func_234" + func_234);
//
//    MultivariateQuadFunction addedFunction = null;
//    addedFunction = func_123.add(func_234, true);
//    System.out.print("addedFunction " + addedFunction);
  }
  
//  @Test
  // Tested
//  void testCartesianProduct() {
//    PiecewiseMultivariateQuadFunction pwFunc1 = new PiecewiseMultivariateQuadFunction();
//    PiecewiseMultivariateQuadFunction pwFunc2 = new PiecewiseMultivariateQuadFunction();
//
//    MultivariateQuadFunction func11 = new MultivariateQuadFunction();
//    Map<String, Interval> interval1 = new HashMap<>();
//    for (int i = 0; i < 10; i++) {
//      interval1.put("x" + i, new Interval(10 * i, 10 * i + 9));
//    }
//    func11.setIntervals(interval1);
//    func11.setOwner("x1");
//    pwFunc1.addToFunctionMapWithInterval(func11);
//
//    MultivariateQuadFunction func21 = new MultivariateQuadFunction();
//    Map<String, Interval> interval2 = new HashMap<>();
//    for (int i = 3; i < 15; i++) {
//      interval2.put("x" + i, new Interval(10 * i + 3, 10 * i + 7));
//    }
//    func21.setIntervals(interval2);
//    func21.setOwner("x3");
//
//    pwFunc2.addToFunctionMapWithInterval(func21);
//  }
  
//  MultivariateQuadFunction createMultivariateFunction(int firstVar, int lastVar) {
//    MultivariateQuadFunction func = new MultivariateQuadFunction();
//    Table<String, String, Double> coefficients = HashBasedTable.create();
//    Map<String, Interval> ranges = new HashMap<>();
//    
//    Map<Integer, String> varMap = new HashMap<>();
//    for (int i = firstVar; i <= lastVar; i++) {
//      varMap.put(i, "x" + i);
//    }
//
//    // x_i^2
//    for (int i = firstVar; i <= lastVar; i++) {
//      coefficients.put(varMap.get(i), varMap.get(i), (double) i * i);
//    }
//    
//    // x_i * x_j
//    for (int i = firstVar; i <= lastVar - 1; i++) {
//      for (int j = i + 1; j <= lastVar; j++) {
//        coefficients.put(varMap.get(i), varMap.get(j), (double) i + j);
//      }
//    }
//    
//    // x_i^2
//    for (int i = firstVar; i <= lastVar; i++) {
//      coefficients.put(varMap.get(i), "", (double) i);
//    } 
//
//    double constantCoeff = 1;
//    for (int i = firstVar; i <= lastVar; i++) {
//      constantCoeff *= i;
//    }
//    coefficients.put("", "", constantCoeff);
//    
//    for (int i = firstVar; i <= lastVar; i++) {
//      ranges.put(varMap.get(i), new Interval(10 * i, 10 * i + 9));
//    }
//
//    func.setCoefficients(coefficients);
//    func.setIntervals(ranges);
//    func.setOwner(varMap.get(0));
//
//    return func;
//  }

}
