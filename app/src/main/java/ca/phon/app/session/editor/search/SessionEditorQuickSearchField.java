package ca.phon.app.session.editor.search;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.text.TableSearchField;

/**
 * Overrides the default table search field so that the row
 * filter includes options for dealing with excluded records.
 * 
 * 
 */
public class SessionEditorQuickSearchField extends TableSearchField {
	
	private static final long serialVersionUID = -6078848699188278431L;

	public final static String INCLUDE_EXCLUDED_PROP = "include_excluded";
	
	public final static String SEARCH_TYPE_PROP = "search_type";
	
	private boolean includeExcludedRecords = false;

	private SearchType searchType = SearchType.PLAIN;
	
	private final Session session;
	
	public SessionEditorQuickSearchField(Session session, JTable table) {
		super(table);
		this.session = session;
	}
	
	@Override
	protected void setupPopupMenu(JPopupMenu menu) {
		final PhonUIAction incExcludedAct = new PhonUIAction(this, "toggleIncludeExcluded");
		incExcludedAct.putValue(PhonUIAction.NAME, "Include excluded records");
		incExcludedAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Include excluded records in search.");
		incExcludedAct.putValue(PhonUIAction.SELECTED_KEY, this.includeExcludedRecords);
		final JCheckBoxMenuItem incExcludedItem = new JCheckBoxMenuItem(incExcludedAct);
		menu.add(incExcludedItem);
		menu.addSeparator();
		
		ButtonGroup btnGroup = new ButtonGroup();
		
		final PhonUIAction usePlainAct = new PhonUIAction(this, "setSearchType", SearchType.PLAIN);
		usePlainAct.putValue(PhonUIAction.NAME, "Plain text");
		usePlainAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Plain text search");
		usePlainAct.putValue(PhonUIAction.SELECTED_KEY, searchType == SearchType.PLAIN);
		final JRadioButtonMenuItem usePlainItem = new JRadioButtonMenuItem(usePlainAct);
		menu.add(usePlainItem);
		
		final PhonUIAction useRegexAct = new PhonUIAction(this, "setSearchType", SearchType.REGEX);
		useRegexAct.putValue(PhonUIAction.NAME, "Regex");
		useRegexAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Regex search");
		useRegexAct.putValue(PhonUIAction.SELECTED_KEY, searchType == SearchType.REGEX);
		final JRadioButtonMenuItem useRegexItem = new JRadioButtonMenuItem(useRegexAct);
		menu.add(useRegexItem);

		final PhonUIAction usePhonexAct = new PhonUIAction(this, "setSearchType", SearchType.PHONEX);
		usePhonexAct.putValue(PhonUIAction.NAME, "Phonex");
		usePhonexAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Phonex search");
		usePhonexAct.putValue(PhonUIAction.SELECTED_KEY, searchType == SearchType.PHONEX);
		final JRadioButtonMenuItem usePhonexItem = new JRadioButtonMenuItem(usePhonexAct);
		menu.add(usePhonexItem);
		
		btnGroup.add(usePlainItem);
		btnGroup.add(useRegexItem);
		btnGroup.add(usePhonexItem);
		
		menu.addSeparator();
		super.setupPopupMenu(menu);
	}
	
	public void setSearchType(SearchType searchType) {
		final SearchType oldType = this.searchType;
		this.searchType = searchType;
		super.firePropertyChange(SEARCH_TYPE_PROP, oldType, searchType);
	}

	@Override
	public RowFilter<TableModel, Integer> getRowFilter(String expr) {
		final RowFilter<TableModel, Integer> filter = new SessionRowFilter(expr, searchType);
				//super.getRowFilter(expr);
		return new RecordRowFilter(filter);
	}
	
	public void toggleIncludeExcluded() {
		final boolean oldVal = isIncludeExcludedRecords();
		this.includeExcludedRecords = !this.includeExcludedRecords;
		super.firePropertyChange(INCLUDE_EXCLUDED_PROP, oldVal, includeExcludedRecords);
	}

	public boolean isIncludeExcludedRecords() {
		return includeExcludedRecords;
	}

	public void setIncludeExcludedRecords(boolean includeExcludedRecords) {
		final boolean oldVal = isIncludeExcludedRecords();
		this.includeExcludedRecords = includeExcludedRecords;
		super.firePropertyChange(INCLUDE_EXCLUDED_PROP, oldVal, includeExcludedRecords);
	}
	
	@Override
	public void onClearText(PhonActionEvent pae) {
		setText("");
	}
	
	@Override
	public void toggleCaseSensitive() {
		caseSensitive = !caseSensitive;
	}

	private class RecordRowFilter extends RowFilter<TableModel, Integer> {

		private RowFilter<TableModel, Integer> filter;
		
		public RecordRowFilter(RowFilter<TableModel, Integer> filter) {
			super();
			this.filter = filter;
		}
		
		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> arg0) {
			final int idx = arg0.getIdentifier();
			
			if(idx >= 0 && idx < session.getRecordCount()) {
				final Record utt = session.getRecord(idx);
				
				boolean includeRecord = !utt.isExcludeFromSearches() || 
						(utt.isExcludeFromSearches() && includeExcludedRecords);
				if(includeRecord) {
					return filter.include(arg0);
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
	}
	
}
