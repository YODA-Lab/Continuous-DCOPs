package utilities;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import function.Interval;
import function.binary.CubicUnaryFunction;
import function.binary.QuadraticBinaryFunction;
import function.binary.QuadraticUnaryFunction;

public class Utilities {	
	public static String headerLine = "InstanceID" + "\t" + "Alg" + "\t" + "Decision"
									+ "\t" + "Time" + "\t" + "Utility";
	
	/**
	 * Code checked
	 * @param intervalList a list of input intervals
	 * @return list of sorted merged intervals
	 */
	public static List<Interval> mergeIntervals(List<Interval> intervalList) {
    List<Interval> shortedIntervalList = new ArrayList<>();
    TreeSet<Double> sortedBoundSet = new TreeSet<>();
  
    for (Interval interval : intervalList) {
      sortedBoundSet.add(interval.getLowerBound());
      sortedBoundSet.add(interval.getUpperBound());
    }
    
    double smallerValue = sortedBoundSet.pollFirst();
    for (Double biggerValue : sortedBoundSet) { 
      shortedIntervalList.add(new Interval(smallerValue, biggerValue));
      smallerValue = biggerValue;
    }
    
    return shortedIntervalList;
  }
	
  /**
   * @param func1 QuadraticUnaryFunction
   * @param func2 QuadraticUnaryFunction
   * @return a list of sorted intervals
   */
  public static List<Interval> solveUnaryQuadForIntervals(QuadraticUnaryFunction func1, QuadraticUnaryFunction func2) {
    List<Interval> intervalList = new ArrayList<>();
    TreeSet<Double> valueIntervalSet = new TreeSet<>();
    func1.checkSameSelfAgent(func2, QuadraticUnaryFunction.FUNCTION_TYPE);
    func1.checkSameSelfInterval(func2, QuadraticUnaryFunction.FUNCTION_TYPE);
    
    QuadraticUnaryFunction diffFunc1Func2 = new QuadraticUnaryFunction(func1.getA() - func2.getA(),
        func1.getB() - func2.getB(), func1.getC() - func2.getC(), func1.getSelfAgent(), func1.getSelfInterval());
    
    double LB = func1.getSelfInterval().getLowerBound();
    double UB = func1.getSelfInterval().getUpperBound();
    
    valueIntervalSet.addAll(diffFunc1Func2.solveForRoots());
    valueIntervalSet.add(LB);
    valueIntervalSet.add(UB);
    
    List<Double> valList = new ArrayList<>(valueIntervalSet.subSet(LB, true, UB, true));    
      
    for (int index = 0; index < valList.size() - 1; index++) {
      intervalList.add(new Interval(valList.get(index), valList.get(index + 1)));
    }
    
    return intervalList;
  }
  
  /**
   * @param func1 CubicUnaryFunction
   * @param func2 CubicUnaryFunction
   * @return a list of sorted intervals
   */
  public static List<Interval> solveCubicForIntervals(CubicUnaryFunction func1, CubicUnaryFunction func2) {
    List<Interval> intervalList = new ArrayList<>();
    TreeSet<Double> valueIntervalSet = new TreeSet<>();
    func1.checkSameSelfAgent(func2, "CUBIC");
    func1.checkSameSelfInterval(func2, "CUBIC");
    
    CubicUnaryFunction diffFunc1Func2 = new CubicUnaryFunction(func1.getA() - func2.getA(), func1.getB() - func2.getB(),
        func1.getC() - func2.getC(), func1.getD() - func2.getD(), func1.getSelfAgent(), func1.getSelfInterval());
    
    double LB = func1.getSelfInterval().getLowerBound();
    double UB = func1.getSelfInterval().getUpperBound();
    
    valueIntervalSet.addAll(diffFunc1Func2.solveForRoots());
    valueIntervalSet.add(LB);
    valueIntervalSet.add(UB);
    
    List<Double> valList = new ArrayList<>(valueIntervalSet.subSet(LB, true, UB, true));    
      
    for (int index = 0; index < valList.size() - 1; index++) {
      intervalList.add(new Interval(valList.get(index), valList.get(index + 1)));
    }
    
    return intervalList;
  }
  
  public static double roundDouble(double value) {
    return new BigDecimal(value).setScale(10, RoundingMode.DOWN).stripTrailingZeros().doubleValue();
  }
	
  public static void writeHeaderLineToFile(String outputFile) {
    byte data[] = headerLine.getBytes();
    Path p = Paths.get(outputFile);
  
    try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p, CREATE, APPEND))) {
      out.write(data, 0, data.length);
      out.flush();
      out.close();
    } catch (IOException x) {
      System.err.println(x);
    } 
	}
	
	public static void writeToFile(String line, String fileName) { 
	  byte data[] = line.getBytes();
	  Path p = Paths.get(fileName);

	  try (OutputStream out = new BufferedOutputStream(
	  Files.newOutputStream(p, CREATE, APPEND))) {
	  out.write(data, 0, data.length);
	  out.flush();
	  out.close();
	  } catch (IOException x) {
	  System.err.println(x);
	  }
	}

//  public static int indexMaxValueList(List<Double> evalList) {
//    double max = evalList.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
//    return evalList.indexOf(max);
//  }
}
