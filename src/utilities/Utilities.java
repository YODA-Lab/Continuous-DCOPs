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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import agent.DCOP;
import agent.DcopInfo;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;

public class Utilities {	
	public static String headerLine = "ID" + "\t" + "Alg" + "\t" + "noAgent" + "\t" + "Time (ms)" + "\t" + "Utility" + "\t" + "RootArgMax";
	
	 //before local search iteration
  public static void writeUtil_Time(DCOP agent) {
    String alg = DcopInfo.algTypes[agent.algorithm];
    
    String newFileName;
    
    if (agent.algorithm == DcopInfo.APPROX_DPOP ) {
      newFileName = "alg=" + alg + "_d=" + agent.noAgent + "_domainSize=" + agent.domainSize + "_numIntervals=" + agent.getNumberOfIntervals() + "_numApproxAgents="
          + agent.getNumberOfApproxAgents() + ".txt";
    } else {
      newFileName = "alg=" + alg + "_d=" + agent.noAgent + "_domainSize=" + agent.domainSize + ".txt";  
    }
    
    
    if (agent.instanceID == 0) {
      writeHeaderLineToFile(newFileName);
    }
    
    DecimalFormat df = new DecimalFormat("##.##");
    df.setRoundingMode(RoundingMode.DOWN);
    String line = null;
    
    line = "\n" + agent.instanceID + "\t" + alg + "\t" + agent.noAgent + "\t" + 
        agent.getSimulatedTime()/1000000.0 + "\t" + df.format(agent.getTotalGlobalUtility()) + "\t" + agent.getRootArgMax();

    writeToFile(line, newFileName);
  }
	
	
	/**
	 * Code checked
	 * @param sortedValues
	 * @return list of sorted merged intervals
	 */
	public static List<Interval> createSortedInterval(Set<Double> sortedValues) {
    List<Double> sortedValueList = new ArrayList<>(sortedValues);
    List<Interval> sortedInterval = new ArrayList<>();
  
    for (int i = 0; i < sortedValueList.size() - 1; i++) {
      sortedInterval.add(new Interval(sortedValueList.get(i), sortedValueList.get(i + 1)));
    }
    
    return sortedInterval;
  }
	
  /**
   * @param func1 QuadraticUnaryFunction
   * @param func2 QuadraticUnaryFunction
   * @return a list of sorted intervals
   */
  public static Set<Double> solveUnaryQuadForValues(MultivariateQuadFunction func1, MultivariateQuadFunction func2, boolean isGettingSmallerInterval) {
    TreeSet<Double> valueIntervalSet = new TreeSet<>();

    func1.checkSameSelfAgent(func2);
    Interval intervalOfTheResult = null;
    
    if (!isGettingSmallerInterval) {
//      func1.checkSameSelfInterval(func2);
      intervalOfTheResult = func1.getCritFuncIntervalMap().get(func1.getOwner());
    }
    else {
      intervalOfTheResult = func1.getCritFuncIntervalMap().get(func1.getOwner()).intersectInterval(func2.getCritFuncIntervalMap().get(func2.getOwner()));
      if (null == intervalOfTheResult)
        return valueIntervalSet;
    }
    
    MultivariateQuadFunction diffFunc1Func2 = new MultivariateQuadFunction(func1.getA() - func2.getA(),
        func1.getB() - func2.getB(), func1.getC() - func2.getC(), func1.getOwner(), intervalOfTheResult);
    
    valueIntervalSet.addAll(diffFunc1Func2.solveForRootsInsideInterval());
    
    return valueIntervalSet;
  }
  
 

  /**
   * @param func1 CubicUnaryFunction
   * @param func2 CubicUnaryFunction
   * @return a list of sorted intervals
   */
//  public static List<Interval> solveCubicForIntervals(CubicUnaryFunction func1, CubicUnaryFunction func2) {
//    List<Interval> intervalList = new ArrayList<>();
//    TreeSet<Double> valueIntervalSet = new TreeSet<>();
//    func1.checkSameSelfAgent(func2, "CUBIC");
//    func1.checkSameSelfInterval(func2, "CUBIC");
//    
//    CubicUnaryFunction diffFunc1Func2 = new CubicUnaryFunction(func1.getA() - func2.getA(), func1.getB() - func2.getB(),
//        func1.getC() - func2.getC(), func1.getD() - func2.getD(), func1.getSelfAgent(), func1.getSelfInterval());
//    
//    double LB = func1.getSelfInterval().getLowerBound();
//    double UB = func1.getSelfInterval().getUpperBound();
//    
//    valueIntervalSet.addAll(diffFunc1Func2.solveForRoots());
//    valueIntervalSet.add(LB);
//    valueIntervalSet.add(UB);
//    
//    List<Double> valList = new ArrayList<>(valueIntervalSet.subSet(LB, true, UB, true));    
//      
//    for (int index = 0; index < valList.size() - 1; index++) {
//      intervalList.add(new Interval(valList.get(index), valList.get(index + 1)));
//    }
//    
//    return intervalList;
//  }
  
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
}
