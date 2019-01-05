package table;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import com.google.common.collect.Sets;

import agent.DCOP;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import zexception.FunctionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Double.*;
import static org.junit.Assert.assertNotNull;

public class Table implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2675509097502238364L;
	static final int CONTAIN_RAND = 0;
	static final int NOT_CONTAIN_RAND = 1;
	List<Row> rowList = new ArrayList<>();
	List<String> label = new ArrayList<>();
	HashMap<Double, List<Row>> keyMap = new HashMap<>();
	
	public Table() {}

	public Table(Table anotherTable) {
		label.addAll(anotherTable.getLabel());
		rowList.addAll(anotherTable.getRowList());
	}
	
	public double getUtilityGivenDecValueList(List<Double> decValueList) {
		for (Row row : rowList) {
			if (row.getValueList().equals(decValueList))
				return row.getUtility();
		}
		return -Double.MAX_VALUE;
	}
	
	public Table(List<String> newLabel) {
	  label.addAll(newLabel);
	}
	
	public void addRow(Row newRow) {
		if (!containRow(newRow)) {
		  rowList.add(newRow);
		}
	}
	
	public void addRowSet(Set<Row> rows) {
	  for (Row newRow : rows) {
	    if (!containRow(newRow)) {
	      rowList.add(newRow);
	    }
	  }
	}
	
	public void addRow(Row newRow, double key) {
		this.rowList.add(newRow);
		
		Set<Double> currentLoadSet = keyMap.keySet();
		if (currentLoadSet.contains(key)) {
			List<Row> rowList = keyMap.get(key);
			rowList.add(newRow);
			keyMap.put(key, rowList);
		}
		else {
			List<Row> rowList = new ArrayList<Row>();
			rowList.add(newRow);
			keyMap.put(key, rowList);
		}
	}
	
	public List<Row> getRowListFromKey(double key) {
		return keyMap.get(key);
	}
	
	/**
	 * Return empty HashSet if the table doesn't contain the agent
	 * @param agent
	 * @param isThrowException
	 * @return
	 */
	public Set<Double> getValueSetOfGivenAgent(String agent, boolean isThrowException) {
		if (!label.contains(agent)) {
		  if (isThrowException)
		    throw new FunctionException("The table label " + label + " doesn't contain the agent: " + agent);
		  else
		    return new HashSet<Double>();
		}
	  int index = label.indexOf(agent);
	  
	  Set<Double> valueSet = new HashSet<>();
		for (Row row: rowList) {			
			  valueSet.add(row.getValueAtPosition(index));
		}
		return valueSet;
	}
	
	public int positionOfVariableInTheLabel(String agent) {
	  return label.indexOf(agent);
	}
	
	@Override
	public String toString() {
	  StringBuilder sb = new StringBuilder();
	  sb.append("Dec lable list: ");
    for (String variable : label) {
      sb.append(variable);
      sb.append(" ");
    }
    sb.append("\n");
    for (Row row : rowList) {
      sb.append(row);
      sb.append("\n");
    }
    sb.append("Row count: " + rowList.size());
    return sb.toString();
	}
	
  /**
   * Create the map that contains the <String, Integer> of the points
   * @param valueMap
   * @return
   */
  public Set<Row> interpolateGivenValueSingleMap(Map<String, Double> valueMap) {
    Set<Row> interpolatedRows = new HashSet<>();
    
    for (Row row : rowList) {
      List<Double> point = new ArrayList<>(row.getValueList());

      // replace the point at the position of the agent in the label with the
      // value at the given position
      for (Entry<String, Double> entry : valueMap.entrySet()) {
        point.set(positionOfVariableInTheLabel(entry.getKey()), entry.getValue());
      }
      Row interpolatedRow = inverseWeightedInterpolation(point);
      if (null != interpolatedRow) {
        interpolatedRows.add(interpolatedRow);
      }
    }
    
    return interpolatedRows;
  }	
	/**
	 * Given a Map<String, Set<Double>>, find the corresponding points and do the interpolation
	 * Return a set of Row. Used to add to the table later in DPOP UTIL
	 * @param valueMap
	 * @return
	 */
	public Set<Row> interpolateGivenValueSetMap(Map<String, Set<Double>> valueMap) {
	  Set<Row> interpolatedRows = new HashSet<>();
	  
	  // create all points to be interpolated
    List<Set<Double>> valueSetList = new ArrayList<Set<Double>>();
    Map<String, Integer> agentPositionInTheValeSet = new HashMap<>();
    int position = 0;
    // The traversal order is the same as the variable ordering in the Cartesian product later
    for (Entry<String, Set<Double>> entry : valueMap.entrySet()) {
      valueSetList.add(entry.getValue());
      agentPositionInTheValeSet.put(entry.getKey(), position);
      position++;
    }
    Set<List<Double>> productInterpolatedValues = Sets.cartesianProduct(valueSetList);
        
    for (List<Double> partialPoint : productInterpolatedValues) {
      for (Row row : rowList) {
        List<Double> point = new ArrayList<>(row.getValueList());

        // replace the point at the position of the agent in the label with the value at the given position
        for (Entry<String, Integer> entry : agentPositionInTheValeSet.entrySet()) {
          point.set(positionOfVariableInTheLabel(entry.getKey()), partialPoint.get(entry.getValue()));
        }
        
        interpolatedRows.add(inverseWeightedInterpolation(point));
      }
    }
    
    return interpolatedRows;
	}
	
  /**
   * Find the argmax of this agent given the agentViewTable and the valueMapOfOtherVariables
   * 1. Create the partial row from the valueMapOfOtherVariables
   * 2. For each values (from the table and from agent.getGlobalInterval().discretize()
   * 3. Create the complete row => interpolate => save the current (max, argmax)
   * 4. Return the argmax
   * @param agentViewTable
   * @param valueMapOfOtherVariables
   * @return
   */
  public double[] maxArgmaxHybrid(DCOP agent, Map<String, Double> valueMapOfOtherVariables) {
    Set<Double> agentValues = getValueSetOfGivenAgent(agent.getID(), false);
    agentValues.addAll(agent.getGlobalInterval().getMidPointInIntegerRanges());
    
    double[] maxArgmax = new double[2];
    
    maxArgmax[0] = -Double.MAX_VALUE;
    maxArgmax[1] = -Double.MAX_VALUE;
    
    for (double value : agentValues) {      
      Map<String, Double> tempMap = new HashMap<>(valueMapOfOtherVariables);
      tempMap.put(agent.getID(), value);
      
      Row interpolatedRow = interpolateGivenPointMap(tempMap);
      
      if (compare(interpolatedRow.getUtility(), maxArgmax[0]) > 0) {
        maxArgmax[0] = interpolatedRow.getUtility();
        maxArgmax[1] = interpolatedRow.getValueList().get(indexOf(agent.getID()));
      }
    }
    
    return maxArgmax;
  }
  
  public boolean containRow(Row rowToCheck) {
    for (Row row : rowList) {
      if (row.getValueList().equals(rowToCheck.getValueList()))
        return true;
    }
    return false;
  }
	
  /**
   * THIS FUNCTION IS UNIT-TESTED
   * Do the interpolation with the inverse weights.
   * @param interpolatedPoint
   * @return the row if the point needed to be interpolated is already in the table
   */
  public Row inverseWeightedInterpolation(List<Double> interpolatedPoint) {
    List<Double> inverseWeights = new ArrayList<>();
    double interpolatedUtility = 0;
    for (Row row : this.rowList) {
      double eucliDistance = euclidDistance(row.getValueList(), interpolatedPoint);
      
      // Return null if the interpolatedPoint is already in the table
      if (compare(eucliDistance, 0) == 0) {
        return row;
      }
      
      double weight = 1.0 / eucliDistance;
      interpolatedUtility += weight * row.getUtility();
      inverseWeights.add(weight);
    }
            
    double sumOfWeights = inverseWeights.stream().mapToDouble(x -> x).sum();
    return new Row(interpolatedPoint, interpolatedUtility / sumOfWeights);
  }
  
  private double euclidDistance(List<Double> pointA, List<Double> pointB) {
    double distance = 0;
    for (int index = 0; index < pointA.size(); index++) {
      distance += Math.pow(pointA.get(index) - pointB.get(index), 2);
    }
    
    return Math.sqrt(distance);
  }
  

  /*
   * For each row of the table, create the Map<String, Double> value and update the utility from the function
   */
  public Table addPiecewiseFunction(PiecewiseMultivariateQuadFunction pwFunction) {
    Table addedTable = new Table(this);
    if (pwFunction.getFunctionMap().size() > 1) {
      throw new FunctionException("The piecewise function that needed to add to the table need only one function: " + pwFunction.getFunctionMap().size());
    }
    
    Map<String, Double> valueMap = new HashMap<>();
    for (Row row : addedTable.getRowList()) {
      for (int varIndex = 0; varIndex < row.getNumberOfVariables(); varIndex++) {
        valueMap.put(label.get(varIndex), row.getValueAtPosition(varIndex));
        row.setUtility(row.getUtility() + pwFunction.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap));
      }
    }
    
    return addedTable;
  }
  
  /*
   * THIS FUNCTION IS REVIEWED
   * Traverse each row of the table
   *  Check if the row contain the valueMap
   *  If yes, maintain the max utility and argmax
   * End
   * 
   * If the tables doesn't contain the valueMapOfOtherVariables, it return -Double.MAX_VALUE
   */
  public double getArgmaxGivenVariableAndValueMap(String variableToGetArgmax, Map<String, Double> valueMapOfOtherVariables) {
    Map<Integer, Double> varIndexValueMap = new HashMap<>();
    for (Entry<String, Double> entry : valueMapOfOtherVariables.entrySet()) {
      int position = positionOfVariableInTheLabel(entry.getKey());
      
      if (position != -1) {
        varIndexValueMap.put(position, entry.getValue());
      }
    }
    
    double max = -Double.MAX_VALUE;
    double argmax = -Double.MAX_VALUE;
    for (Row row : rowList) {
      if (!checkIfListContainValueGivenPosition(row.getValueList(), varIndexValueMap)) {
        continue;
      }
      
      if (compare(row.getUtility(), max) > 0) {
        max = row.getUtility();
        argmax = row.getValueList().get(positionOfVariableInTheLabel(variableToGetArgmax));
      }
    }
    
    return argmax;
  }
  
  public void printMinMaxRow() {
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    Row maxRow = null;
    Row minRow = null;
    for (Row row : rowList) {
      if (compare(row.getUtility(), max) > 0) {
        max = row.getUtility();
        maxRow = row;
      }
      if (compare(row.getUtility(), min) < 0) {
        min = row.getUtility();
        minRow = row;
      }
    }
    System.out.println("Min row: " + minRow);
    System.out.println("Max row: " + maxRow);
  }
  
  /**
   * Return false if the list doesn't contains all values with the corresponding position from the map
   * @param list
   * @param map
   * @return
   */
  private boolean checkIfListContainValueGivenPosition(List<Double> list, Map<Integer, Double> map) { 
    for (Entry<Integer, Double> entry : map.entrySet()) {
      if (compare(list.get(entry.getKey()), entry.getValue()) != 0)
        return false;
    }
    return true;
  }
  
  public void extendToTheEndOfLabel(String agent) {
    label.add(agent);
  }
  
  
  public boolean containsAgent(String agent) {
    return label.contains(agent);
  }
  
  public int indexOf(String agent) {
    return label.indexOf(agent);
  }
	
	@Override
	public boolean equals(Object tableToCompare) {
    // If the object is compared with itself then return true  
    if (tableToCompare == this) {
        return true;
    }
    
    if (!(tableToCompare instanceof Table)) {
      return false;
    }
       
    Table castedTypeTable = (Table) tableToCompare;
      
    return label.equals(castedTypeTable.getLabel()) 
        && rowList.equals(castedTypeTable.getRowList());
  }
	
  @Override
  public int hashCode() {
    return Objects.hash(label, rowList);
  }

	public int getRowCount() {
		return rowList.size();
	}
	
	public int getVariableCount() {
		return label.size();
	}

	public List<Row> getRowList() {
		return rowList;
	}

	public List<String> getLabel() {
		return label;
	}
	
	public HashMap<Double, List<Row>> getKeyMap() {
		return keyMap;
	}

	public void setLoadMap(HashMap<Double, List<Row>> keyMap) {
		this.keyMap = keyMap;
	}

  public String getVariableAt(int i) {
    return label.get(i);
  }

  /**
   * Return a interpolated row given a point in Map form
   * @param pointMap
   * @return
   */
  public Row interpolateGivenPointMap(Map<String, Double> pointMap) {
    List<Double> point = new ArrayList<>();
    for (String agent : label) {
      point.add(pointMap.get(agent));
    }
       
    return inverseWeightedInterpolation(point);
  }
}
