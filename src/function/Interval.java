package function;

import java.io.Serializable;
import java.util.*;

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
     * @param object the object to be copied
     */
    public Interval(Interval object) {
        this.lowerBound = object.getLowerBound();
        this.upperBound = object.getUpperBound();
    }
    
    public Interval(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }
    
    public double midValue() {
      return 0.5 * (lowerBound + upperBound);
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }  
    
    /**
     * @param numberOfIntervals the number of intervals, size of the returned list
     * @return a list of separated intervals in the ascending order
     */
    public List<Interval> separateIntoAListOfIntervals(int numberOfIntervals) {
        List<Interval> intervalList = new ArrayList<>();
        double increment = (upperBound - lowerBound) / numberOfIntervals;
        double currentLB = lowerBound;
        for (int i = 0; i < numberOfIntervals; i++) {          
            intervalList.add(new Interval(currentLB, currentLB + increment));
            currentLB += increment;
        }
        return intervalList;
    }
    
    @Override
    public String toString() {
        return "[" + lowerBound + ", " + upperBound + "]";
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Interval)) {
            return false;
        }
        Interval interval = (Interval) o;
        return Double.compare(this.lowerBound, interval.getLowerBound()) == 0 &&
                Double.compare(this.upperBound, interval.getUpperBound()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }
}
