package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.nodes.query.InventorySettings.ColumnInfo;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;
import ca.phon.ipa.IPATranscript;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Inventory",
		description="Aggregated inventory of query results",
		category="Report"
)		
public class InventoryNode extends TableOpNode implements NodeSettings {

	private InventorySettingsPanel settingsPanel = null;
	
	public InventoryNode() {
		super();
		
		final InventorySettings settings = new InventorySettings();
		putExtension(InventorySettings.class, settings);
		putExtension(NodeSettings.class, this);
	}

	public InventorySettings getInventorySettings() {
		return getExtension(InventorySettings.class);
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new InventorySettingsPanel(getInventorySettings());
		}
		return settingsPanel;
	}
	
	@Override
	public Properties getSettings() {
		final Properties props = new Properties();
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
	}
	
	private String getGroupBy() {
		return getInventorySettings().getGroupBy().getName();
	}
	
	private List<String> getColumns() {
		return getInventorySettings().getColumns().stream()
			.map(info -> info.getName()).collect(Collectors.toList());
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final TableDataSource inputTable = (TableDataSource)context.get(tableInput);
		final DefaultTableDataSource outputTable = new DefaultTableDataSource();

		Set<GroupKey> groupKeys = collectGroupKeys(inputTable);
		
		Map<InventoryRowData, Map<GroupKey, Long>> inventory = 
				generateInventory(groupKeys, inputTable);
		
		
		int[] inventoryCols = getColumnIndices(inputTable, getColumns());
		
		List<String> colNames = new ArrayList<>();
		Arrays.stream(inventoryCols).forEach( col -> colNames.add(inputTable.getColumnTitle(col)) );
		groupKeys.forEach( key -> colNames.add(key.toString()) );
		
		for(InventoryRowData key:inventory.keySet()) {
			Object[] rowData = new Object[colNames.size()];
			int rowDataIdx = 0;
			for(int i = 0; i < key.rowVals.length; i++) {
				rowData[rowDataIdx++] = key.rowVals[i];
			}
			final Map<GroupKey, Long> count = inventory.get(key);
			for(GroupKey groupKey:groupKeys) {
				rowData[rowDataIdx++] = count.get(groupKey);
			}
			
			outputTable.addRow(rowData);
		}
		
		for(int i = 0; i < colNames.size(); i++) {
			outputTable.setColumntTitle(i, colNames.get(i));
		}
		
		context.put(tableOutput, outputTable);
	}
	
	private Set<GroupKey> collectGroupKeys(TableDataSource table) {
		Set<GroupKey> retVal = new LinkedHashSet<>();
		
		int grouping = getColumnIndex(table, getGroupBy());
		if(grouping >= 0 && grouping < table.getColumnCount()) {
			for(int rowIdx = 0; rowIdx < table.getRowCount(); rowIdx++) {
				retVal.add(new GroupKey(table.getValueAt(rowIdx, grouping)));
			}
		} else {
			retVal.add(new GroupKey("Total"));
		}
		
		return retVal;
	}
	
	private Map<InventoryRowData, Map<GroupKey, Long>> generateInventory(Set<GroupKey> groupKeys, TableDataSource table) {
		Map<InventoryRowData, Map<GroupKey, Long>> retVal = new LinkedHashMap<>();
		
		int groupingCol = getColumnIndex(table, getGroupBy());
		int[] inventoryCols = getColumnIndices(table, getColumns());
		
		for(int row = 0; row < table.getRowCount(); row++) {
			Object grouping = (groupingCol >= 0 
					? table.getValueAt(row, groupingCol) : "Total");
			
			Object[] rowData = new Object[inventoryCols.length];
			for(int ic = 0; ic < inventoryCols.length; ic++) {
				int col = inventoryCols[ic];
				rowData[ic] = (col >= 0 ? 
						table.getValueAt(row, inventoryCols[ic]) : "");
			}
			
			final GroupKey groupKey = new GroupKey(grouping);
			final InventoryRowData key = new InventoryRowData(rowData);
			Map<GroupKey, Long> counts = retVal.get(key);
			if(counts == null) {
				counts = new LinkedHashMap<>();
				for(GroupKey gk:groupKeys) counts.put(gk, 0L);
				retVal.put(key, counts);
			}
			long count = counts.get(groupKey);
			counts.put(groupKey, ++count);
		}
		
		return retVal;
	}
	
	private class GroupKey implements Comparable<GroupKey> {
		Object key;
		
		public GroupKey(Object key) {
			this.key = key;
		}
		
		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof GroupKey)) return false;
			return TableUtils.checkEquals(key, ((GroupKey)o2).key, 
					getInventorySettings().getGroupBy().caseSensitive,
					getInventorySettings().getGroupBy().ignoreDiacritics);
		}
		
		@Override
		public String toString() {
			return TableUtils.objToString(key, getInventorySettings().getGroupBy().ignoreDiacritics);
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
		
		@Override
		public int compareTo(GroupKey k2) {
			return toString().compareTo(k2.toString());
		}
		
	}
	
	private class InventoryRowData {
		Object[] rowVals;
		
		public InventoryRowData(Object[] vals) {
			this.rowVals = vals;
		}
		
		@Override
		public boolean equals(Object o2) {
			if(!(o2 instanceof InventoryRowData)) return false;
			
			final InventoryRowData otherRow = (InventoryRowData)o2;
			if(otherRow == this) return true;
			
			if(otherRow.rowVals.length != rowVals.length) return false;
			
			boolean equals = true;
			for(int i = 0; i < rowVals.length; i++) {
				Object rowVal1 = rowVals[i];
				Object rowVal2 = otherRow.rowVals[i];
				final ColumnInfo info = getInventorySettings().getColumns().get(i);
				equals &= TableUtils.checkEquals(rowVal1, rowVal2, info.caseSensitive, info.ignoreDiacritics);
			}
			return equals;
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			for(Object rowVal:rowVals) {
				sb.append((sb.length() > 0 ? "," : ""));
				final  ColumnInfo info = getInventorySettings().getColumns().get(i++);
				sb.append(TableUtils.objToString(rowVal, info.ignoreDiacritics));
			}
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}

}
