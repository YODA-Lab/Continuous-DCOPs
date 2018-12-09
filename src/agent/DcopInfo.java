package agent;

/**
 * @author khoihd
 *
 */
public interface DcopInfo {
  public static final boolean WAITING_FOR_MSG = true;

  public static final int DISCRETE_DPOP = 0;
  public static final int ANALYTICAL_DPOP = 1;
  public static final int APPROX_DPOP = 2;
  public static final int HYBRID_MAXSUM = 3;
  
  public static final int FUNC_TO_VAR_TO_SEND_OUT = 0;
  public static final int FUNC_TO_VAR_TO_STORE = 1;
  
  public static final boolean NOT_TO_OPTIMIZE_INTERVAL = false;
  public static final boolean TO_OPTIMIZE_INTERVAL = true;

  public static final String algTypes[] = { "DISCRETE_DPOP", "ANALYTICAL_DPOP", "APPROX_DPOP", "HYBRID_MAXSUM" };

  public static final int MAX_ITERATION = 5;

  public static final int PSEUDOTREE = 0;
  public static final int PSEUDO_INFO = 1;
  public static final int DPOP_UTIL = 2;
  public static final int DPOP_VALUE = 3;
  public static final int VAR_TO_FUNC = 4;
  public static final int FUNC_TO_VAR = 5;
  public static final int PROPAGATE_DPOP_VALUE = 4;
  public static final int LS_ITERATION_DONE = 5;
  public static final int INIT_LS_UTIL = 6;
  public static final String msgTypes[] = { "PSEUDOTREE", "PSEUDO_INFO", "DPOP_UTIL", "DPOP_VALUE", "VAR_TO_FUNC", "FUNC_TO_VAR",
      "PROPAGATE_DPOP_VALUE", "RAND_VALUE", "LS_ITERATION_DONE", "INIT_LS_UTIL" };
}
