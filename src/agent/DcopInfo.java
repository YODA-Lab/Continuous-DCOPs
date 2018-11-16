package agent;

/**
 * @author khoihd
 *
 */
public interface DcopInfo {
  public static final boolean WAITING_FOR_MSG = true;
  public static final int BASE_DPOP = 0;
  public static final int ANALYTICAL_DPOP = 1;
  public static final int APPROX_DPOP = 2;

  public static final String algTypes[] = { "BASE_DPOP", "ANALYTICAL_DPOP", "APPROX_DPOP" };

  // for creating switching cost table
  public static final boolean FORWARD_BOOL = true;
  public static final boolean BACKWARD_BOOL = false;

  public static final int MAX_ITERATION = 20;

  public static final String switchYes = "yes";
  public static final String switchNo = "no";

  public static final int CONSTANT = 0;
  public static final int LINEAR = 1;
  public static final int QUAD = 2;
  public static final int EXP_2 = 3;
  public static final int EXP_3 = 4;

  public static final int FIRST_PHASE = 1;
  public static final int SECOND_PHASE = 2;

  public static final double DISCRETE_TICK = 1.0; // for not processed loads
  public static final double DISCRETE_P_TICK = 1.0; // for processed loads

  public static final double PROBABILITY = 0.5;
  public static final double PROBABILITY_LOAD = 0.6;

  public static final boolean PROCESSED = true;
  public static final boolean NON_PROCESSED = false;

  public static final boolean IS_NEXT = true;
  public static final boolean IS_FURTHER = false;

  public static final double DISCRETIZED_VALUE = 0.5;
  public static final int NUMBER_OF_INTERVALS = 25;

  public static enum SolvingType {
    APPROXIMATE, ANALYTICALLY
  };
}
