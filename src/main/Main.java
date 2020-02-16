package main;

import agent.DcopConstants;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

  public static void main(String[] args) {    
    String inputFileName = args[0]; // "rep_0_d10.dzn"; 
    String a[] = inputFileName.replaceAll("rep_","").replaceAll(".dzn","").split("_d");
    int noAgent = Integer.parseInt(a[1]); 
    
    String arg[] = new String[4];
    arg[0] = inputFileName; // The filename decides number of agents
    arg[1] = String.valueOf(parseAlgorithm(args[1])); 
    arg[2] = args[2]; // number of iterations
    arg[3] = args[3]; // number of points
    
    Runtime rt = Runtime.instance();
    rt.setCloseVM(true);
    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost");
    p.setParameter(Profile.GUI, "false");
    ContainerController cc = rt.createMainContainer(p);
    for (int i = 1; i <= noAgent; i++) {
      AgentController ac;
      try {
        ac = cc.createNewAgent(String.valueOf(i), "agent.DcopAgent", arg);
        ac.start();
      } catch (StaleProxyException e) {
        e.printStackTrace();
      }
    }
  }
  
  // "EF_DPOP", "DPOP", "AF_DPOP", "CAF_DPOP", "MAXSUM", "HYBRID_MAXSUM", "CAF_MAXSUM", 
  // "DISCRETE_DPOP", "DISCRETE_DSA", "CONTINUOUS_DSA"};  
  public static int parseAlgorithm(String algorithm) {
    switch (algorithm) {
      case "ef_dpop":         return DcopConstants.EF_DPOP;
      case "dpop":            return DcopConstants.DPOP;
      case "af_dpop":         return DcopConstants.AF_DPOP;
      case "caf_dpop":        return DcopConstants.CAF_DPOP;
      case "maxsum":          return DcopConstants.MAXSUM;
      case "hybrid_maxsum":   return DcopConstants.HYBRID_MAXSUM;
      case "caf_maxsum":      return DcopConstants.CAF_MAXSUM;
      case "discrete_dpop":   return DcopConstants.DISCRETE_DPOP;
      case "discrete_dsa":    return DcopConstants.DISCRETE_DSA;
      case "continuous_dsa":  return DcopConstants.CONTINUOUS_DSA;
      default:                return -1;
    }
  }
}
