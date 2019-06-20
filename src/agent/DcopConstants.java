package agent;

/**
 * @author khoihd
 *
 */
public interface DcopConstants {
  public static final boolean WAITING_FOR_MSG = true;

  public static final int ANALYTICAL_DPOP = 0;
  
  public static final int DISCRETE_DPOP = 1;
  public static final int MOVING_DPOP = 2;
  public static final int CLUSTERING_DPOP = 3;
  
  public static final int DISCRETE_MAXSUM = 4;
  public static final int MOVING_MAXSUM = 5;
  public static final int CLUSTERING_MAXSUM = 6;
  
  public static final int APPROX_DPOP = 7;
  
  public static final int DISCRETE_DSA = 8;
  public static final int CONTINUOUS_DSA = 9;
  
  public static final String algTypes[] = { "ANALYTICAL_DPOP", "DISCRETE_DPOP", "MOVING_DPOP", "CLUSTERING_DPOP",
      "DISCRETE_MAXSUM", "MOVING_MAXSUM", "CLUSTERING_MAXSUM", "DISCRETE_DSA", "CONTINUOUS_DSA"};
  
  public static final int FUNC_TO_VAR_TO_SEND_OUT = 0;
  public static final int FUNC_TO_VAR_TO_STORE = 1;
  
  public static final boolean NOT_TO_OPTIMIZE_INTERVAL = false;
  public static final boolean TO_OPTIMIZE_INTERVAL = true;
  
  public static final boolean IS_CLUSTERING = true;
  public static final boolean NOT_CLUSTERING = false;
  
  public static final boolean ADD_MORE_POINTS = true;
  public static final boolean NOT_ADD_POINTS = false;
  
  public static final int MAX_ITERATION = 20;

  public static final int PSEUDOTREE = 0;
  public static final int PSEUDO_INFO = 1;
  public static final int DPOP_UTIL = 2;
  public static final int DPOP_VALUE = 3;
  public static final int VAR_TO_FUNC = 4;
  public static final int FUNC_TO_VAR = 5;
  public static final int PROPAGATE_VALUE = 6;
  public static final int UTILITY_TO_THE_ROOT = 7;
  public static final int MSG_COUNT_TO_THE_ROOT = 8;
  public static final int DSA_VALUE = 9;
  public static final String msgTypes[] = { "PSEUDOTREE", "PSEUDO_INFO", "DPOP_UTIL", "DPOP_VALUE", "VAR_TO_FUNC", "FUNC_TO_VAR",
      "PROPAGATE_VALUE", "UTILITY_TO_THE_ROOT", "MSG_COUNT_TO_THE_ROOT", "DSA_VALUE"};
  
  public static final int DONE_AT_LEAF = 1;
  public static final int DONE_AT_INTERNAL_NODE = 2;
}
