package ca.phon.query.report.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ca.phon.query.TableUtils;

/**
 * 
 */
public class DefaultTableDataSource implements TableDataSource {
	
	private String[] columnNames = new String[0];
	
	private List<Object[]> rowData = new ArrayList<Object[]>();
	
	public DefaultTableDataSource() {
		super();
	}
	
	public List<Object[]> getRowData() {
		return this.rowData;
	}
	
	public Object[] getRow(int row) {
		return rowData.get(row);
	}
	
	public void deleteRow(int row) {
		rowData.remove(row);
	}
	
	public void insertRow(int idx, Object[] row) {
		if(row.length < getColumnCount()) {
			row = Arrays.copyOf(row, getColumnCount());
		} else if(row.length > getColumnCount()) {
			setColumnCount(row.length);
		}
		
		rowData.add(idx, row);
	}
	
	public void addRow(Object[] row) {
		if(row.length < getColumnCount()) {
			row = Arrays.copyOf(row, getColumnCount());
		} else if(row.length > getColumnCount()) {
			setColumnCount(row.length);
		}
		
		rowData.add(row);
	}
	
	protected void setColumnCount(int columns) {
		if(columns == getColumnCount()) return;
		
		columnNames = Arrays.copyOf(columnNames, columns);
		
		List<Object[]> newRowData = new ArrayList<>();
		rowData.forEach( (row) -> newRowData.add(Arrays.copyOf(row, columns)) );
		rowData = newRowData;
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return rowData.size();
	}
	
	public int getColumnIndex(String columnName) {
		int colIdx = -1;
		for(int c = 0; c < getColumnCount(); c++) {
			if(getColumnTitle(c).equals(columnName)) {
				colIdx = c;
				break;
			}
		}
		return colIdx;
	}
	
	public Object getValueAt(int row, String columnName) {
		Object retVal = null;
		
		if(row < rowData.size()) {
			int colIdx = getColumnIndex(columnName);
			if(colIdx > 0 && colIdx < getColumnCount()) {
				retVal = getValueAt(row, colIdx);
			}
		}
		
		return retVal;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = null;
		
		if(row < rowData.size()) {
			final Object[] rowVal = rowData.get(row);
			if(col < rowVal.length) {
				retVal = rowVal[col];
			}
		}
		
		return retVal;
	}
	
	/**
	 * Return the value at cell for given rowKey, colName.
	 * 
	 * This method assumes that each row begins with a unique 
	 * key.  
	 * 
	 * @param rowKey
	 * @param colName
	 * @return cellValue at given rowKey, colName intersection.
	 */
	public Object getValueAt(String rowKey, String colName) {
		return getValueAt(0, rowKey, colName);
	}
	
	public Object getValueAt(String keyCol, String rowKey, String colName) {
		return getValueAt(getColumnIndex(keyCol), rowKey, colName);
	}
	
	public Object getValueAt(int keyCol, String rowKey, String colName) {
		Optional<Object[]> row = 
				rowData.parallelStream()
					.filter( r -> TableUtils.objToString(r[keyCol], true).equalsIgnoreCase(rowKey) )
					.findAny();
		if(row.isPresent()) {
			return row.get()[getColumnIndex(colName)];
		} else {
			return null;
		}
	}
	
	public void setValueAt(int row, int col, Object newVal) {
		final Object[] rowVals = getRow(row);
		rowVals[col] = newVal;
	}
	
	public String getDefaultColumnTitle(int col) {
		int let1 = col / 26;
		int let2 = col % 26;
		
		String retVal = "" + 
				(let1 > 0 ? (char)('A' + (let1-1)) : "") +
				(char)('A' + let2);
		return retVal;
	}

	@Override
	public String getColumnTitle(int col) {
		String retVal = null;
		
		if(col < columnNames.length) {
			retVal = 
				(columnNames[col] != null ? columnNames[col] : getDefaultColumnTitle(col));
		}
		
		return retVal;
	}
	
	public void setColumnTitle(int col, String title) {
		columnNames[col] = title;
	}
	
}
