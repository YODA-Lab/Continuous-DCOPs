package function;

import java.io.Serializable;
import java.util.*;

/**
 * This class is IMMUTABLE with all private fields and no setter function
 * @author khoihd
 */
public class Interval implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 803822839716215962L;
  private double lowerBound;
  private double upperBound;

  public Interval() {
  }

  /**
   * Copied constructor
   * 
   * @param object
   *          the object to be copied
   */
  public Interval(Interval object) {
    this();
    this.lowerBound = object.getLowerBound();
    this.upperBound = object.getUpperBound();
  }

  public Interval(double lowerBound, double upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public double midValue() {
    return 0.5 * (lowerBound + upperBound);
  }
  
  public double getLowerBound() {
    return lowerBound;
  }

  public double getUpperBound() {
    return upperBound;
  }

  /**
   * This function is TESTED
   * @param numberOfIntervals
   *          the number of intervals, size of the returned list
   * @return a list of separated intervals in the ascending order
   */
  public List<Interval> separateIntoAListOfIncreasingIntervals(int numberOfIntervals) {
    List<Interval> intervalList = new ArrayList<>();
    double increment = (upperBound - lowerBound) / numberOfIntervals;
    double currentLB = lowerBound;
    for (int i = 0; i < numberOfIntervals; i++) {
      intervalList.add(new Interval(currentLB, currentLB + increment));
      currentLB += increment;
    }
    return intervalList;
  }
  
  public boolean isTotallyBigger(Interval another) {
    return Double.compare(lowerBound, another.getLowerBound()) <= 0 &&
        Double.compare(upperBound, another.getUpperBound()) >= 0;
  }

  @Override
  public String toString() {
    return "[" + lowerBound + ", " + upperBound + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (!(o instanceof Interval)) {
      return false;
    }
    Interval interval = (Interval) o;
    return Double.compare(this.lowerBound, interval.getLowerBound()) == 0
        && Double.compare(this.upperBound, interval.getUpperBound()) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lowerBound, upperBound);
  }
}
