package table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Row implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7773374424812673056L;
	List<Double> valueList = new ArrayList<>();
	double utility;
	
	public Row() {}
	
	public Row(Row newRow) {
		this.valueList.addAll(newRow.getValueList());
    this.utility = newRow.utility;
	}
	
	//input: X1, X2, X3,...,Xn
	//input utility
	public Row(List<Double> input, double utility) {
		valueList.addAll(input);
		this.utility = utility;
	}
	
	public Row(double[] inputValueList, double utility) {
	  for (Double value : inputValueList) {
	    this.valueList.add(value);
	  }
	  this.utility = utility;
	}
	
	public Double getValueAtPosition(int index) {
		if (index >= getNumberOfVariables()) {
			System.err.println("Index out of bounds: " + index);
			System.err.println("Size:" +  getNumberOfVariables());
		}
		return valueList.get(index);
	}
	
	public void addValueToTheEnd(double value) {
	  valueList.add(value);
	}

	@Override
	public String toString() {
	  StringBuilder sb = new StringBuilder();
	   for (Double value:valueList) {
	     sb.append(value + " ");
	   }
	   sb.append("utility " + utility);

	   return sb.toString();
	}
	
//	public boolean equalDecisionVar(Object rowToCompare) {
//		// If the object is compared with itself then return true  
//        if (rowToCompare == this) {
//            return true;
//        }
// 
//        if (!(rowToCompare instanceof Row)) {
//            return false;
//        }
//        
//        // typecast o to Complex so that we can compare data members 
//        Row castedTypeRow = (Row) rowToCompare;
//        
//        if (castedTypeRow.getVariableCount() != this.variableCount)
//        	System.err.println("Different number of variable count: " + this.variableCount + " " + 
//        							castedTypeRow.getRandomCount());
//         
//        // Compare the data members and return accordingly 
//        return 	castedTypeRow.randomCount == this.randomCount
//        	&&	castedTypeRow.randomList.equals(this.randomList);
//	}
	
	@Override
  public boolean equals(Object rowToCompare) {
    // If the object is compared with itself then return true
    if (rowToCompare == this) {
      return true;
    }

    if (!(rowToCompare instanceof Row)) {
      return false;
    }

    Row castedTypeRow = (Row) rowToCompare;

    // Compare the data members and return accordingly
    return castedTypeRow.valueList.equals(this.valueList) 
        && castedTypeRow.utility == this.utility;
  }
	
	@Override
	public int hashCode() {
	  return Objects.hash(valueList, utility);
	}
	
	public int getNumberOfVariables() {
		return valueList.size();
	}

	public List<Double> getValueList() {
		return valueList;
	}

	public double getUtility() {
		return utility;
	}
	
	public void setUtility(double utility) {
		this.utility = utility;
	}
	
}
