package main;

import agent.DcopInfo;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

  public static void main(String[] args) {    
    String inputFileName = "rep_0_d10.dzn"; // (String) args[0];
    String a[] = inputFileName.replaceAll("rep_","").replaceAll(".dzn","").split("_d");
    int noAgent = Integer.parseInt(a[1]); 
    
    String arg[] = new String[4];
    arg[0] = inputFileName;
    arg[1] = String.valueOf(DcopInfo.DISCRETE_DSA);
    arg[2] = String.valueOf(3); // number of points
    arg[3] = String.valueOf(10); // number of iterations
    
    Runtime rt = Runtime.instance();
    rt.setCloseVM(true);
    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost");
    p.setParameter(Profile.GUI, "true");
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
}
