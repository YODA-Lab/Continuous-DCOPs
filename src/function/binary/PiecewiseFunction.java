package function.binary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import agent.DcopInfo;
import agent.DcopInfo.SolvingType;
import function.Function;
import function.Interval;

/**
 * @author khoihd
 *
 */
public class PiecewiseFunction extends Function implements Serializable {
  // function -> intervals
  
  /**
   * 
   */
  private static final long serialVersionUID = 2147073840063782655L;
  private List<Function> functionList;
  
  public PiecewiseFunction(PiecewiseFunction pwf) {
    super(pwf.getSelfAgent(), pwf.getOtherAgent(), null, null);
    for (Function f : pwf.getFunctionList()) {
      functionList.add(f);
    }
  }
  
  public PiecewiseFunction(String selfAgent, String otherAgent) {
    super(selfAgent, otherAgent, null, null);
    functionList = new ArrayList<>();
  }
  
  // Unary function
  public TreeSet<Double> getSortedSegmentedRange() {
    TreeSet<Double> range = new TreeSet<>();
    for (Function func : functionList) {
      range.add(func.getSelfInterval().getLowerBound());
      range.add(func.getSelfInterval().getUpperBound());
    }
    
    return range;
  }
  
  public void addNewFunction(Function function) {
    this.selfAgent = function.getSelfAgent();
    this.otherAgent = function.getOtherAgent();
    functionList.add(function);
  }
  
  /**
   * This function is manually checked
   * @param solType
   * @return
   */
  public PiecewiseFunction project(SolvingType solType) {
    return solType == SolvingType.APPROXIMATE ? this.approxProject(DcopInfo.NUMBER_OF_INTERVALS)
        : this.analyticallyProject();
  }
  
  /**
   * @return a Piecewise UnaryFunction after being projected
   */
  public PiecewiseFunction analyticallyProject() {
    PiecewiseFunction pwFunc = new PiecewiseFunction(selfAgent, otherAgent);
    for (Function f : functionList) {
      PiecewiseFunction newPiecewise = ((QuadraticBinaryFunction) f).analyticallyProject();
      pwFunc.addPiecewiseFunction(newPiecewise);
    }

    return pwFunc;
  }
  
  // Divide into smaller intervals
  //  + Divide each intervals into k equal intervals
  //  + In each k intervals, pick the middle values
  // Get the final function
  // Find the argmax of that function
  // The max is the orgin
  public PiecewiseFunction approxProject(int numberOfIntervals) {
    PiecewiseFunction pwFunc = new PiecewiseFunction(selfAgent, otherAgent);
    for (Function f:functionList) {
      PiecewiseFunction newPiecewise = (((QuadraticBinaryFunction) f).approxProject(numberOfIntervals));
      pwFunc.addPiecewiseFunction(newPiecewise);
    }
//    System.out.println("APPROXIMATELY PROJECTED FUNCTION: " + pwFunc);
    return pwFunc;
  }
  
  public void addPiecewiseFunction(PiecewiseFunction f) {
    functionList.addAll(f.getFunctionList());
  }

  public List<Function> getFunctionList() {
    return functionList;
  }

  public void setFunctionList(List<Function> functionList) {
    this.functionList = functionList;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Function f : functionList) {
      builder.append(f);
      builder.append("\n");
    }
    return builder.toString();
  }

  @Override
  public Object evaluate(double x) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double evaluate(double x, double y) {
    // TODO Auto-generated method stub
    return -Double.MAX_VALUE;
  }
}
