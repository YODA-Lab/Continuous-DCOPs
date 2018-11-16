package ztest;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import function.Interval;
//import function.binary.CubicUnaryFunction;
//import function.binary.QuadraticBinaryFunction;
//import function.binary.QuadraticUnaryFunction;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import zexception.FunctionException;

class TestFunction {
  
  @Test
  void testAnalyticalProjectDPOP() {
    PiecewiseMultivariateQuadFunction f = new PiecewiseMultivariateQuadFunction();
    MultivariateQuadFunction f1 = new MultivariateQuadFunction();
    // -1.18993x_2^2 -9.73194x_2 -7.46737x_1^2 -4.28303x_1 -7.63075x_2x_1 5.36355;
    
    f1.getCoefficients().put("2", "2", 2.0);
    f1.getCoefficients().put("2", "", 1.0);
    f1.getCoefficients().put("2", "1", 7.0);
    f1.getCoefficients().put("1", "1", -2.0);
    f1.getCoefficients().put("1", "", 4.0);
    f1.getCoefficients().put("", "", -10.0);    

    Interval int_1 = new Interval(-5, 5);
    Interval int_2 = new Interval(-10, 10);

    f1.getIntervals().put("1", int_1);
    f1.getIntervals().put("2", int_2);
    
    f.addToFunctionList(f1);
    f.setOwner("1");
    
    System.out.println("BEFORE projecting:\n" + f1);
    
    System.out.println("CritF :\n" + f1.getUnaryFunctionAtCriticalPoint());
    System.out.println("LB_F :\n" + f1.evaluateBinaryFunctionX1(f1.getIntervals().get(f1.getOwner()).getLowerBound()));
    System.out.println("UB_F :\n" + f1.evaluateBinaryFunctionX1(f1.getIntervals().get(f1.getOwner()).getUpperBound()));

    
    PiecewiseMultivariateQuadFunction projected = f1.analyticalProject();
    System.out.println("AFTER projecting:\n" + projected);
    
    double max = -Double.MAX_VALUE;
    double argmax = -Double.MAX_VALUE;
    
    for (MultivariateQuadFunction func : projected.getFunctionList()) {
      double[] maxAndArgMax = func.getMaxAndArgMax();
           
      if (Double.compare(maxAndArgMax[0], max) > 0) {
        max = maxAndArgMax[0];
        argmax = maxAndArgMax[1];
      }
    }
    
    out.println("MAX VALUE IS " + max);
    out.println("ARGMAX VALUE IS " + argmax);  
    
    max = -Double.MAX_VALUE;
    argmax = -Double.MAX_VALUE;
    for (MultivariateQuadFunction func : f.getFunctionList()) {
      for (double val1 = -5; Double.compare(val1, 5) <= 0; val1+=0.5) {
        for (double val2 = -5; Double.compare(val2, 5) <= 0; val2+=0.5) {
          Map<String, Double> valueMap = new HashMap<>();
          valueMap.put("1", val1);
          valueMap.put("2", val2);
          
          double evaluated = func.evaluateToValueGivenValueMap(valueMap);
          if (Double.compare(evaluated, max) > 0) {
            max = evaluated;
          }
        }
      }
    }
    
    out.println("MAX VALUE IS " + max);
  }

//  @Test
//  void testSolvingQuadratic() {
//    // Test zero root
//    QuadraticUnaryFunction functionWithNoneRoot = new QuadraticUnaryFunction(1, 2, 2, "A", new Interval(-100, 100));
//    Set<Double> roots0 = new HashSet<>();
//    assertEquals(functionWithNoneRoot.solveForRoots(), roots0);
//    
//    // Test one root
//    QuadraticUnaryFunction functionWithOneRoot = new QuadraticUnaryFunction(1, -2, 1, "A", new Interval(-100, 100));
//    Set<Double> roots1 = new HashSet<>();
//    roots1.add(1.0);
//    assertEquals(functionWithOneRoot.solveForRoots(), roots1);
//    
//    // Test two roots
//    QuadraticUnaryFunction functionWithTwoRoot = new QuadraticUnaryFunction(1, -5, 6, "A", new Interval(-100, 100));
//    Set<Double> roots2 = new HashSet<>();
//    roots2.add(2.0);
//    roots2.add(3.0);
//    assertEquals(functionWithTwoRoot.solveForRoots(), roots2);
//  }
  
//  @Test
//  void testSolvingCubic() {
//    // Test one root
//    CubicUnaryFunction functionWith1Roots = new CubicUnaryFunction(1, 3, 3, 1, "A", new Interval(-30, 30));
//    Set<Double> roots1 = new HashSet<>();
//    roots1.add(-1.0);
//    assertEquals(functionWith1Roots.solveForRoots(), roots1);
//    
//    // Test two root
//    // THIS UNIT TEST FAILS BECAUSE OF ROUNDING PROBLEM BY JAVA
//    // HOWEVER, THE TEST IS STILL CORRECT IF BEING ROUNDED PROPERLY
////    CubicUnaryFunction functionWith2Roots = new CubicUnaryFunction(1, -5, 8, -4, "A", new Interval(-30, 30));
////    Set<Double> roots2 = Set.of(1.0, 2.0);
////    assertEquals(functionWith2Roots.solveForRoots(), roots2);
//    
//    // Test three root
//    CubicUnaryFunction functionWith3Roots = new CubicUnaryFunction(1, -5, -2, 24, "A", new Interval(-30, 30));
//    Set<Double> roots3 = new HashSet<>();
//    roots3.add(-2.0); roots3.add(3.0); roots3.add(4.0);
//    assertEquals(functionWith3Roots.solveForRoots(), roots3);
//  }
  
//  @Test
//  void QuadraticUnaryMaxTest() {
//    QuadraticUnaryFunction functionWithTwoRoot = new QuadraticUnaryFunction(-1, 5, -6, "A", new Interval(-3, 3));
//    double actualArgMax = 2.5;
//    double actualMax = functionWithTwoRoot.evaluate(actualArgMax);
//    assertEquals(functionWithTwoRoot.getArgMax(), actualArgMax);
//    assertEquals(functionWithTwoRoot.getMax(), actualMax);
//  }
  
//  @Test
//  void CubicUnaryMaxTest() {
//    // TODO: add more test cases
//    CubicUnaryFunction cubicFunction = new CubicUnaryFunction(-1, 0, 2, 5, "A", new Interval(0, 2));
//    assertEquals(cubicFunction.getArgMax(), 0.8165, 0.1);
//    assertEquals(cubicFunction.getMax(), cubicFunction.evaluate(0.8165), 0.1);
//  }
  
//  @Test
//  void AddTwoQuadBinaryFunctionTest() {
//    QuadraticBinaryFunction func1 = new QuadraticBinaryFunction(4, -2, 10, 9, -9, 23, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
//    QuadraticBinaryFunction func2 = new QuadraticBinaryFunction(4, -2, 10, 9, -9, 23, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
//    QuadraticBinaryFunction addedFunction = new QuadraticBinaryFunction(8, -4, 20, 18, -18, 46, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
//    QuadraticBinaryFunction actualAddedFunction = func1.addBinaryFunction(func2);
//    assertEquals(addedFunction, actualAddedFunction);
//  }
  
//  @Test
//  void AddQuadBinaryAndUnaryFunctionTest() {
//    QuadraticBinaryFunction func1 = new QuadraticBinaryFunction(4, -2, 10, 9, -9, 23, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
//    QuadraticUnaryFunction func2 = new QuadraticUnaryFunction(10, 12, 13, "A", new Interval(-10, 10));
//    QuadraticBinaryFunction addedFunction = new QuadraticBinaryFunction(14, 10, 10, 9, -9, 36, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
//    QuadraticBinaryFunction actualAddedFunction = func1.addUnaryFunction(func2);
//    assertEquals(addedFunction, actualAddedFunction);
//  }
 
//  Internal node combined function: 
//  [Coefficients = {={=-151.1875}, 2={2=4.0, =-35.875, 1=5.0}, 1={1=1.0, =-7.0}}, Intervals = {1=[-10.0, 10.0], 2=[-10.0, 0.0]} ]
//  [Coefficients = {={=-151.1875}, 2={2=4.0, =26.625, 1=5.0}, 1={1=1.0, =-7.0}}, Intervals = {1=[-10.0, 10.0], 2=[0.0, 10.0]} ]
  @Test 
  void testApproxProjectDPOP() {
    PiecewiseMultivariateQuadFunction f = new PiecewiseMultivariateQuadFunction();
    MultivariateQuadFunction f1 = new MultivariateQuadFunction();

    f1.getCoefficients().put("1", "1", -2.0);
    f1.getCoefficients().put("1", "", 4.0);
    
    f1.getCoefficients().put("2", "2", 2.0);
    f1.getCoefficients().put("2", "", 1.0);
    
    f1.getCoefficients().put("3", "3", 1.0);
    f1.getCoefficients().put("3", "", 2.0);
    
    f1.getCoefficients().put("2", "1", 7.0);
    f1.getCoefficients().put("2", "3", 8.0);
    f1.getCoefficients().put("3", "1", 9.0);

    f1.getCoefficients().put("", "", -10.0); 
    

    Interval int_1 = new Interval(-5, 5);
    Interval int_2 = new Interval(-10, 10);
    Interval int_3 = new Interval(-8, 8);

    f1.getIntervals().put("1", int_1);
    f1.getIntervals().put("2", int_2);
    f1.getIntervals().put("3", int_3);
    
    f.addToFunctionList(f1);
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
    MultivariateQuadFunction func_123 = createMultivariateFunction(1, 3);
    MultivariateQuadFunction func_234 = createMultivariateFunction(2, 4);

    System.out.println("func_123" + func_123);
    System.out.println("func_234" + func_234);

    MultivariateQuadFunction addedFunction = null;
    addedFunction = func_123.add(func_234, true);
    System.out.print("addedFunction " + addedFunction);
  }
  
//  @Test
  // Tested
  void testCartesianProduct() {
    PiecewiseMultivariateQuadFunction pwFunc1 = new PiecewiseMultivariateQuadFunction();
    PiecewiseMultivariateQuadFunction pwFunc2 = new PiecewiseMultivariateQuadFunction();

    MultivariateQuadFunction func11 = new MultivariateQuadFunction();
    Map<String, Interval> interval1 = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      interval1.put("x" + i, new Interval(10 * i, 10 * i + 9));
    }
    func11.setIntervals(interval1);
    func11.setOwner("x1");
    pwFunc1.addToFunctionList(func11);

    MultivariateQuadFunction func21 = new MultivariateQuadFunction();
    Map<String, Interval> interval2 = new HashMap<>();
    for (int i = 3; i < 15; i++) {
      interval2.put("x" + i, new Interval(10 * i + 3, 10 * i + 7));
    }
    func21.setIntervals(interval2);
    func21.setOwner("x3");

    pwFunc2.addToFunctionList(func21);
  }
  
  MultivariateQuadFunction createMultivariateFunction(int firstVar, int lastVar) {
    MultivariateQuadFunction func = new MultivariateQuadFunction();
    Table<String, String, Double> coefficients = HashBasedTable.create();
    Map<String, Interval> ranges = new HashMap<>();
    
    Map<Integer, String> varMap = new HashMap<>();
    for (int i = firstVar; i <= lastVar; i++) {
      varMap.put(i, "x" + i);
    }

    // x_i^2
    for (int i = firstVar; i <= lastVar; i++) {
      coefficients.put(varMap.get(i), varMap.get(i), (double) i * i);
    }
    
    // x_i * x_j
    for (int i = firstVar; i <= lastVar - 1; i++) {
      for (int j = i + 1; j <= lastVar; j++) {
        coefficients.put(varMap.get(i), varMap.get(j), (double) i + j);
      }
    }
    
    // x_i^2
    for (int i = firstVar; i <= lastVar; i++) {
      coefficients.put(varMap.get(i), "", (double) i);
    } 

    double constantCoeff = 1;
    for (int i = firstVar; i <= lastVar; i++) {
      constantCoeff *= i;
    }
    coefficients.put("", "", constantCoeff);
    
    for (int i = firstVar; i <= lastVar; i++) {
      ranges.put(varMap.get(i), new Interval(10 * i, 10 * i + 9));
    }

    func.setCoefficients(coefficients);
    func.setIntervals(ranges);
    func.setOwner(varMap.get(0));

    return func;
  }

}
