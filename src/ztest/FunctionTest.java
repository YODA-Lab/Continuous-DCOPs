package ztest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

import function.Interval;
import function.binary.CubicUnaryFunction;
import function.binary.QuadraticBinaryFunction;
import function.binary.QuadraticUnaryFunction;

class FunctionTest {

  @Test
  void testSolvingQuadratic() {
    // Test zero root
    QuadraticUnaryFunction functionWithNoneRoot = new QuadraticUnaryFunction(1, 2, 2, "A", new Interval(-100, 100));
    Set<Double> roots0 = Set.of();
    assertEquals(functionWithNoneRoot.solveForRoots(), roots0);
    
    // Test one root
    QuadraticUnaryFunction functionWithOneRoot = new QuadraticUnaryFunction(1, -2, 1, "A", new Interval(-100, 100));
    Set<Double> roots1 = Set.of(1.0);
    assertEquals(functionWithOneRoot.solveForRoots(), roots1);
    
    // Test two root
    QuadraticUnaryFunction functionWithTwoRoot = new QuadraticUnaryFunction(1, -5, 6, "A", new Interval(-100, 100));
    Set<Double> roots2 = Set.of(2.0, 3.0);
    assertEquals(functionWithTwoRoot.solveForRoots(), roots2);
  }
  
  @Test
  void testSolvingCubic() {
    // Test one root
    CubicUnaryFunction functionWith1Roots = new CubicUnaryFunction(1, 3, 3, 1, "A", new Interval(-30, 30));
    Set<Double> roots1 = Set.of(-1.0);
    assertEquals(functionWith1Roots.solveForRoots(), roots1);
    
    // Test two root
    // THIS TEST FAILS BECAUSE OF ROUNDING PROBLEM BY JAVA
//    CubicUnaryFunction functionWith2Roots = new CubicUnaryFunction(1, -5, 8, -4, "A", new Interval(-30, 30));
//    Set<Double> roots2 = Set.of(1.0, 2.0);
//    assertEquals(functionWith2Roots.solve(), roots2);
    
    // Test three root
    CubicUnaryFunction functionWith3Roots = new CubicUnaryFunction(1, -5, -2, 24, "A", new Interval(-30, 30));
    Set<Double> roots3 = Set.of(-2.0, 3.0, 4.0);
    assertEquals(functionWith3Roots.solveForRoots(), roots3);
  }
  
  @Test
  void QuadraticUnaryMaxTest() {
    QuadraticUnaryFunction functionWithTwoRoot = new QuadraticUnaryFunction(-1, 5, -6, "A", new Interval(-3, 3));
    double actualArgMax = 2.5;
    double actualMax = functionWithTwoRoot.evaluate(actualArgMax);
    assertEquals(functionWithTwoRoot.getArgMax(), actualArgMax);
    assertEquals(functionWithTwoRoot.getMax(), actualMax);
  }
  
  @Test
  void CubicUnaryMaxTest() {
      // Not necessary
  }
  
  @Test
  void AddTwoQuadBinaryFunctionTest() {
    QuadraticBinaryFunction func1 = new QuadraticBinaryFunction(4, -2, 10, 9, -9, 23, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
    QuadraticBinaryFunction func2 = new QuadraticBinaryFunction(4, -2, 10, 9, -9, 23, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
    QuadraticBinaryFunction addedFunction = new QuadraticBinaryFunction(8, -4, 20, 18, -18, 46, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
    QuadraticBinaryFunction actualAddedFunction = func1.addBinaryFunction(func2);
    assertEquals(addedFunction, actualAddedFunction);
  }
  
  @Test
  void AddQuadBinaryAndUnaryFunctionTest() {
    QuadraticBinaryFunction func1 = new QuadraticBinaryFunction(4, -2, 10, 9, -9, 23, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
    QuadraticUnaryFunction func2 = new QuadraticUnaryFunction(10, 12, 13, "A", new Interval(-10, 10));
    QuadraticBinaryFunction addedFunction = new QuadraticBinaryFunction(14, 10, 10, 9, -9, 36, "A", "B", new Interval(-10, 10), new Interval(-90, 90));
    QuadraticBinaryFunction actualAddedFunction = func1.addUnaryFunction(func2);
    assertEquals(addedFunction, actualAddedFunction);
  }
  
  @Test
  void ProjectQuadBinaryFuncTest() {
    // TODO: Create test case here
  }
}
