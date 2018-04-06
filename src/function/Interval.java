package function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Interval implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 803822839716215962L;
    private double lowerBound;
    private double upperBound;
    
    public Interval() {
        lowerBound = 0;
        upperBound = 0;
    }
    
    public Interval(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
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
    
    public List<Interval> divideIntoSmaller(int numberOfIntervals) {
        List<Interval> intervalList = new ArrayList<Interval>();
        double increment = (upperBound - lowerBound) / numberOfIntervals;
        double currentLB = lowerBound;
        for (int i = 0; i < numberOfIntervals; i++) {
            Interval itv;
            itv = (i != numberOfIntervals - 1) ? new Interval(currentLB, currentLB + increment)
                    : new Interval(currentLB, upperBound);
            
            intervalList.add(itv);
            currentLB += increment;
        }
        return intervalList;
    }
    
    @Override
    public String toString() {
        return "[" + lowerBound + ", " + upperBound + "]";
    }
}
