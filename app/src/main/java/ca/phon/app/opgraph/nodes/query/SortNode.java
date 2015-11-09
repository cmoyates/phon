package ca.phon.app.opgraph.nodes.query;

import groovyjarjarcommonscli.ParseException;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.components.NodeSettingsPanel;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.FeatureFamily;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortColumn;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortType;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.features.CompoundFeatureComparator;
import ca.phon.ipa.features.FeatureComparator;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.report.datasource.TableDataSource;

@OpNodeInfo(
		name="Sort",
		description="Sort table",
		category="Report"
)
public class SortNode extends TableOpNode implements NodeSettings {
	
	private SortNodeSettingsPanel nodeSettingsPanel;
	
	public SortNode() {
		super();
		
		putExtension(SortNodeSettings.class, new SortNodeSettings());
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final DefaultTableDataSource table = (DefaultTableDataSource)context.get(tableInput);
	
		List<Object[]> rowData = table.getRowData();
		Collections.sort(rowData, new RowComparator(table));
		
		context.put(tableOutput, table);
	}
	
	public SortNodeSettings getSortSettings() {
		return getExtension(SortNodeSettings.class);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(nodeSettingsPanel == null) {
			nodeSettingsPanel = new SortNodeSettingsPanel(getSortSettings());
		}
		return nodeSettingsPanel;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}

	private class RowComparator implements Comparator<Object[]> {
		
		private TableDataSource table;
		
		public RowComparator(TableDataSource table) {
			this.table = table;
		}

		@Override
		public int compare(Object[] row1, Object[] row2) {
			int retVal = 0;
			
			for(SortColumn sc:getSortSettings().getSorting()) {
				final int[] columnIndices = getColumnIndices(table, sc.getColumn());
				if(columnIndices.length == 0 || columnIndices[0] < 0) continue;
				
				final int colIdx = columnIndices[0];
				final Object v1 = row1[colIdx];
				final Object v2 = row2[colIdx];
				
				final String v1Txt = (v1 != null ? v1.toString() : "");
				final String v2Txt = (v2 != null ? v2.toString() : "");
				if(sc.getType() == SortType.PLAIN) {
					retVal = v1Txt.compareTo(v2Txt);
				} else if(sc.getType() == SortType.IPA) {
					List<Comparator<IPAElement>> comparatorOrder =
							new ArrayList<Comparator<IPAElement>>();
					for(FeatureFamily family:sc.getFeatureOrder()) {
						switch(family) {
						case MANNER:
							comparatorOrder.add(FeatureComparator.createMannerComparator());
							break;
							
						case PLACE:
							comparatorOrder.add(FeatureComparator.createPlaceComparator());
							break;
							
						case VOICING:
							comparatorOrder.add(FeatureComparator.createVoicingComparator());
							break;
						
						default:
							break;
						}
					}
					
					try {
						IPATranscript v1ipa = 
								(v1 != null && v1 instanceof IPATranscript ? (IPATranscript)v1 : IPATranscript.parseIPATranscript(v1Txt));
						IPATranscript v2ipa = 
								(v2 != null && v2 instanceof IPATranscript ? (IPATranscript)v2 : IPATranscript.parseIPATranscript(v2Txt));
						
						@SuppressWarnings("unchecked")
						final Comparator<IPAElement> comparator = 
								new CompoundFeatureComparator(comparatorOrder.toArray(new Comparator[0]));
						retVal = v1ipa.compareTo(v2ipa, comparator);
					} catch (java.text.ParseException pe) {
						throw new ProcessingException(null, pe);
					}
					
				}
				// only continue if necessary
				if(retVal != 0) break;
			}
			
			return retVal;
		}
		
	}
	
}
