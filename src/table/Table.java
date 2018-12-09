package table;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Table implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2675509097502238364L;
	static final int CONTAIN_RAND = 0;
	static final int NOT_CONTAIN_RAND = 1;
	int rowCount;
	int variableCount;
	int randomCount;
	List<Row> table;
	List<Double> decVarLabel;
	List<Double> randVarLabel;
//	HashMap<Double, List<Row>> loadMap;
	HashMap<Double, List<Row>> keyMap;

	public Table(Table anotherTable) {
		this.rowCount = anotherTable.rowCount;
		this.variableCount = anotherTable.variableCount;
		this.table = new ArrayList<Row>();
//		this.decVarLabel = (List<String>) anotherTable.decVarLabel.clone();
		this.decVarLabel = new ArrayList<>();
		List<Double> anotherTableDecVarLabel = anotherTable.getDecVarLabel();
		for (Double label:anotherTableDecVarLabel) {
			this.decVarLabel.add(label);
		}

		//add row to new table
		for (Row row:anotherTable.getRowList()) {
			this.table.add(new Row(row));
		}
	}
	
	boolean equalArray(List<?> list1, List<?> list2) {
		if (list1 == null || list2 == null)
			return false;
		if (list1.size() != list2.size())
			return false;
		for (int index=0; index<list1.size(); index++) {
			if (list1.get(index).equals(list2.get(index)) == false)
				return false;				
		}
		return true;
	}
	
	public double getUtilityGivenDecValueList(List<Double> decValueList) {
		for (int index=0; index<table.size(); index++) {
			if (equalArray(table.get(index).getValueList(), decValueList))
				return table.get(index).getUtility();
		}
		return Integer.MIN_VALUE;
	}
	
	public double getUtilityGivenDecAndRandValueList(List<Double> decValueList, List<Double> randValueList) {
		for (int index=0; index<table.size(); index++) {
			if (equalArray(table.get(index).getValueList(), decValueList)
			&& equalArray(table.get(index).getRandomList(), randValueList))
				return table.get(index).getUtility();
		}
		return Integer.MIN_VALUE;
	}
	
//	@Override
//    public boolean equals(Object tableToCompare) {
//        if (!(tableToCompare instanceof Table)) {
//            return false;
//        }
//        
//        // If the object is compared with itself then return true ?
//		if (tableToCompare == this) {
//            return true;
//        }
//         
//        // typecast o to Complex so that we can compare data members 
//        Table castedTypeTable = (Table) tableToCompare;
//         
//        // Compare the data members and return accordingly 
//        return  castedTypeTable.rowCount == this.rowCount	
//        	&&	castedTypeTable.variableCount == this.variableCount
//        	&&	castedTypeTable.table.equals(this.table)
//        	&&	castedTypeTable.decVarLabel == this.decVarLabel;
//	}
	
	public Table(List<Double> newLabel) {
		table = new ArrayList<Row>();
		decVarLabel = new ArrayList<>();
		for (Double variable:newLabel)
			decVarLabel.add(variable);
		variableCount = decVarLabel.size();
		rowCount = 0;
		keyMap = new HashMap<Double, List<Row>>();
	}
	
//	public Table(List<String> newLabel, int randType) {
//		this(newLabel);
//		this.randType = randType;
//	}
	
	public Table(List<Double> decVarList, List<Double> randVarList) {
		table = new ArrayList<Row>();
		decVarLabel = decVarList;
		randVarLabel = randVarList;
		variableCount = decVarList.size();
		randomCount = randVarList.size();
	}
	
//	public Table(List<String> decVarList, List<String> randVarList, int randType) {
//		this(decVarList, randVarList);
//	}
	
	public void addRow(Row newRow) {
		this.table.add(newRow);
		this.rowCount++;
	}
	
	public void addRow(Row newRow, double key) {
		this.table.add(newRow);
		this.rowCount++;
		
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
	
	//tao 1 array chua values cua 1 bien
	//chay tung dong cua vector
	//kiem tra gia tri cua bien, co trong listValues chua
	//neu chua thi them vao
	//tra ve danh sach tat ca gia tri cua 1 bien, khong bi duplicate
	List<Double> listValuesOfVariable(int index) {
		List<Double> listValues = new ArrayList<Double>();
		for (Row row: table) {			
			if (listValues.contains(row.getValueAtPosition(index)) == false)
				listValues.add(row.getValueAtPosition(index));
		}
		return listValues;
	}
	
//	public boolean containVariable(List<String> list, String input) {
//		if (list.size() == 0)
//			return false;
//		for (String temp: list) {
//			if (temp.equals(input))
//				return true;
//		}
//		return false;
//	}
	
	public void printDecVar() {
		System.out.println("Dec lable list: ");
		for (Double variable:decVarLabel)
			System.out.print(variable + " ");
		System.out.println();
		
		for (Row row:table) {
			row.printDecVar();
		}
	}
	
	public void printRandVar() {
		System.out.println("Rand lable list: ");
		for (Double variable:randVarLabel)
			System.out.print(variable + " ");
		System.out.println();
		
		for (Row row:table) {
			row.printRandVar();
		}
	}
	
	public void writeDecVarToFile(String filename) {
		try {
		    PrintWriter writer = new PrintWriter(filename, "UTF-8");
		    for (Row row:table)
		    	writer.println(row.getDecVar());

		    writer.close();
		} catch (IOException e) {
		   // do something
		}
	}
	
	public void printDecAndRandVar() {
		System.out.println("Dec and Rand lable list: ");
		for (Double variable:decVarLabel)
			System.out.print(variable + " ");
		System.out.print("y ");
		for (Double variable:randVarLabel)
			System.out.print(variable + " ");
		
		System.out.println();
		for (Row row:table) {
			row.printBoth();
		}
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
      
    // Compare the data members and return accordingly 
    for (Row row : castedTypeTable.getRowList()) {
      if (!this.getRowList().contains(row))
        return false;
    }
    return castedTypeTable.rowCount == this.rowCount && castedTypeTable.variableCount == this.variableCount;
  }

	public int getRowCount() {
		return rowCount;
	}
	
	public int getVariableCount() {
		return variableCount;
	}

	public List<Row> getRowList() {
		return table;
	}

	public List<Double> getDecVarLabel() {
		return decVarLabel;
	}

	public List<Double> getRandVarLabel() {
		return randVarLabel;
	}
	
	public HashMap<Double, List<Row>> getKeyMap() {
		return keyMap;
	}

	public void setLoadMap(HashMap<Double, List<Row>> keyMap) {
		this.keyMap = keyMap;
	}
}
