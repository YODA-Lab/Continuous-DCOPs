package behavior;

import java.util.Map;

import agent.DcopAgent;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.behaviours.OneShotBehaviour;

public class CONTINUOUS_DSA extends OneShotBehaviour {
  /**
   * 
   */
  private static final long serialVersionUID = 5573433680877118333L;
  
  private DcopAgent agent;

  public CONTINUOUS_DSA(DcopAgent agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    // Return a map from neighbor -> piecewise multivariate quadratic function
    agent.getFunctionMap();
    
    // Add a PMQ function with another PWQ function
//    PiecewiseMultivariateQuadFunction combinedFunction = pwFuncList.get(0);
//    for (int i = 1; i < pwFuncList.size(); i++) {
//      combinedFunction = combinedFunction.addPiecewiseFunction(pwFuncList.get(i));
//    }
    
    // To find the argmax of a function f
    // First you need to create a map String -> Double containing the values of all other variables
    // except for the variable you're finding the argmax
    // Look at the below function from PiecewiseMultivariateQuadFunction 
    // public double getArgmaxGivenVariableAndValueMap(String variableID, Map<String, Double> valuesOfOtherVariables)
  }
}
