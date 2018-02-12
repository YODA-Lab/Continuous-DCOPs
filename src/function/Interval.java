package function;

import java.io.Serializable;

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
    
    @Override
    public String toString() {
        return "[" + lowerBound + ", " + upperBound + "]";
    }
}
