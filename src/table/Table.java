package table;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
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
	ArrayList<Row> table;
	ArrayList<Double> decVarLabel;
	ArrayList<Double> randVarLabel;
//	HashMap<Double, ArrayList<Row>> loadMap;
	HashMap<Double, ArrayList<Row>> keyMap;

	public Table(Table anotherTable) {
		this.rowCount = anotherTable.rowCount;
		this.variableCount = anotherTable.variableCount;
		this.table = new ArrayList<Row>();
//		this.decVarLabel = (ArrayList<String>) anotherTable.decVarLabel.clone();
		this.decVarLabel = new ArrayList<>();
		ArrayList<Double> anotherTableDecVarLabel = anotherTable.getDecVarLabel();
		for (Double label:anotherTableDecVarLabel) {
			this.decVarLabel.add(label);
		}

		//add row to new table
		for (Row row:anotherTable.getTable()) {
			this.table.add(new Row(row));
		}
	}
	
	boolean equalArray(ArrayList<?> list1, ArrayList<?> list2) {
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
	
	public double getUtilityGivenDecValueList(ArrayList<Double> decValueList) {
		for (int index=0; index<table.size(); index++) {
			if (equalArray(table.get(index).getValueList(), decValueList))
				return table.get(index).getUtility();
		}
		return Integer.MIN_VALUE;
	}
	
	public double getUtilityGivenDecAndRandValueList(ArrayList<Double> decValueList, ArrayList<Double> randValueList) {
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
	
	public Table(ArrayList<Double> newLabel) {
		table = new ArrayList<Row>();
		decVarLabel = new ArrayList<>();
		for (Double variable:newLabel)
			decVarLabel.add(variable);
		variableCount = decVarLabel.size();
		rowCount = 0;
		keyMap = new HashMap<Double, ArrayList<Row>>();
	}
	
//	public Table(ArrayList<String> newLabel, int randType) {
//		this(newLabel);
//		this.randType = randType;
//	}
	
	public Table(ArrayList<Double> decVarList, ArrayList<Double> randVarList) {
		table = new ArrayList<Row>();
		decVarLabel = decVarList;
		randVarLabel = randVarList;
		variableCount = decVarList.size();
		randomCount = randVarList.size();
	}
	
//	public Table(ArrayList<String> decVarList, ArrayList<String> randVarList, int randType) {
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
			ArrayList<Row> rowList = keyMap.get(key);
			rowList.add(newRow);
			keyMap.put(key, rowList);
		}
		else {
			ArrayList<Row> rowList = new ArrayList<Row>();
			rowList.add(newRow);
			keyMap.put(key, rowList);
		}
	}
	
	public ArrayList<Row> getRowListFromKey(double key) {
		return keyMap.get(key);
	}
	
	//tao 1 array chua values cua 1 bien
	//chay tung dong cua vector
	//kiem tra gia tri cua bien, co trong listValues chua
	//neu chua thi them vao
	//tra ve danh sach tat ca gia tri cua 1 bien, khong bi duplicate
	ArrayList<Double> listValuesOfVariable(int index) {
		ArrayList<Double> listValues = new ArrayList<Double>();
		for (Row row: table) {			
			if (listValues.contains(row.getValueAtPosition(index)) == false)
				listValues.add(row.getValueAtPosition(index));
		}
		return listValues;
	}
	
//	public boolean containVariable(ArrayList<String> list, String input) {
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

	public int getRowCount() {
		return rowCount;
	}
	
	public int getVariableCount() {
		return variableCount;
	}

	public ArrayList<Row> getTable() {
		return table;
	}

	public ArrayList<Double> getDecVarLabel() {
		return decVarLabel;
	}

	public ArrayList<Double> getRandVarLabel() {
		return randVarLabel;
	}
	
	public HashMap<Double, ArrayList<Row>> getKeyMap() {
		return keyMap;
	}

	public void setLoadMap(HashMap<Double, ArrayList<Row>> keyMap) {
		this.keyMap = keyMap;
	}
}
