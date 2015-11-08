package ca.phon.app.opgraph.nodes.query;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.opgraph.nodes.query.SortNodeSettings.FeatureFamily;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortColumn;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortOrder;
import ca.phon.app.opgraph.nodes.query.SortNodeSettings.SortType;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SortNodeSettingsPanel extends JPanel {

	private static final long serialVersionUID = 4289280424233502931L;
	
	private final SortNodeSettings settings;
	
	private JPanel sortByPanel;
	
	private JButton addSortButton;
	
	public SortNodeSettingsPanel(SortNodeSettings settings) {
		super();
		this.settings = settings;
		
		init();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		SortColumn groupByColumn = getSettings().getGroupBy();
		if(groupByColumn == null) {
			groupByColumn = new SortColumn();
			getSettings().setGroupBy(groupByColumn);
		}
		final SortColumnPanel groupByPanel = new SortColumnPanel(groupByColumn);
		groupByPanel.setBorder(BorderFactory.createTitledBorder("Group by (optional)"));
		add(groupByPanel);
		
		sortByPanel = new JPanel(new VerticalLayout());
		
		final ImageIcon icon = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final Action onAddAction = new PhonUIAction(this, "onAddColumn");
		onAddAction.putValue(Action.NAME, "Add");
		onAddAction.putValue(Action.SHORT_DESCRIPTION, "Add column to sort");
		onAddAction.putValue(Action.SMALL_ICON, icon);
		addSortButton = new JButton(onAddAction);
		addSortButton.setVisible(settings.getSorting().size() < 3);
		
		int scIdx = 0;
		for(SortColumn sc:settings.getSorting()) {
			final SortColumnPanel scPanel = new SortColumnPanel(sc);
			if(scIdx > 0 && scIdx < 2) {
				final JComponent sep = createSeparator(scPanel);
				sortByPanel.add(sep);
			}
			sortByPanel.add(scPanel);
			++scIdx;
		}
		
		final JPanel btmPanel = new JPanel(new VerticalLayout());
		btmPanel.setBorder(BorderFactory.createTitledBorder("Sort by"));
		btmPanel.add(sortByPanel);
		btmPanel.add(ButtonBarBuilder.buildOkBar(addSortButton));
		add(btmPanel);
	}
	
	public void onAddColumn() {
		if(settings.getSorting().size() == 3) return;
		final SortColumn sc = new SortColumn();
		settings.getSorting().add(sc);
		final SortColumnPanel scPanel = new SortColumnPanel(sc);
		final JComponent sep = createSeparator(scPanel);
		sortByPanel.add(sep);
		sortByPanel.add(scPanel);
		addSortButton.setVisible(settings.getSorting().size() < 3);
		revalidate();
	}
	
	public void onRemoveColumn(SortColumnPanel scPanel) {
		sortByPanel.remove(scPanel);
		if(scPanel.getSeparator() != null)
			sortByPanel.remove(scPanel.getSeparator());
		settings.getSorting().remove(scPanel.getSortColumn());
		addSortButton.setVisible(settings.getSorting().size() < 3);
		revalidate();
	}

	public SortNodeSettings getSettings() {
		return this.settings;
	}
	
	private JComponent createSeparator(SortColumnPanel scPanel) {
		final ImageIcon removeIcon =
				IconManager.getInstance().getDisabledIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemoveColumn", scPanel);
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove sort column");
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcon);
		final JButton removeButton = new JButton(removeAct);
		removeButton.setBorderPainted(false);
		
		final JPanel sep = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		sep.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
		
		gbc.weightx = 0.0;
		++gbc.gridx;
		sep.add(removeButton, gbc);
		
		scPanel.setSeparator(sep);
		
		return sep;
	}
	
	private PromptedTextField createColumnField() {
		final PromptedTextField retVal = new PromptedTextField();
		retVal.setPrompt("Enter column name or number");
		return retVal;
	}
	
	private JComboBox<SortType> createSortTypeBox() {
		final JComboBox<SortType> retVal = new JComboBox<>(SortType.values());
		retVal.setSelectedItem(null);
		return retVal;
	}
	
	private JComboBox<FeatureFamily> createFeatureBox() {
		final FeatureFamily[] boxVals = new FeatureFamily[FeatureFamily.values().length + 1];
		int idx = 0;
		boxVals[idx++] = null;
		for(FeatureFamily v:FeatureFamily.values()) boxVals[idx++] = v;
		
		final JComboBox<FeatureFamily> retVal = new JComboBox<>(boxVals);
		retVal.setSelectedItem(null);
		return retVal;
	}
	
	class SortColumnPanel extends JPanel {
		private PromptedTextField columnField = createColumnField();
		private JComboBox<SortType> typeBox = createSortTypeBox();
		
		// plain text options
		private JPanel plainTextOptions = new JPanel();
		private JRadioButton ascendingBox = new JRadioButton("Ascending");
		private JRadioButton descendingBox = new JRadioButton("Descending");
		
		// feature options
		private JPanel featureOptions = new JPanel();
		private JComboBox<FeatureFamily> feature1Box = createFeatureBox();
		private JComboBox<FeatureFamily> feature2Box = createFeatureBox();
		private JComboBox<FeatureFamily> feature3Box = createFeatureBox();
		
		private final SortColumn sortColumn;
		
		private JComponent separator;
		
		public SortColumnPanel(SortColumn sortColumn) {
			super();
			
			this.sortColumn = sortColumn;
			init();
		}
		
		private void init() {
			setLayout(new GridBagLayout());
			
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.insets = new Insets(2, 2, 5, 2);
			
			columnField.setText(sortColumn.getColumn());
			columnField.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateColumn();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateColumn();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					
				}
			});
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.weightx = 0.0;
			add(new JLabel("Column:"), gbc);
			
			gbc.gridx++;
			gbc.weightx = 1.0;
			add(columnField, gbc);
			
			gbc.weightx = 0.0;
			gbc.gridx = 0;
			gbc.gridy++;
			add(new JLabel("Sort type:"), gbc);
			
			typeBox.addItemListener( (e) -> {
				plainTextOptions.setVisible(typeBox.getSelectedItem() == SortType.PLAIN);
				featureOptions.setVisible(typeBox.getSelectedItem() == SortType.IPA);
				sortColumn.setType((SortType)typeBox.getSelectedItem());
			});
			typeBox.setSelectedItem(sortColumn.getType());
			gbc.gridx++;
			gbc.weightx = 1.0;
			add(typeBox, gbc);

			final ButtonGroup grp = new ButtonGroup();
			grp.add(ascendingBox);
			grp.add(descendingBox);
			ascendingBox.setSelected(sortColumn.getOrder() == SortOrder.ASCENDING);
			descendingBox.setSelected(sortColumn.getOrder() == SortOrder.DESCENDING);
			plainTextOptions.setLayout(new HorizontalLayout());
			
			final ChangeListener l = (e) -> {
				if(ascendingBox.isSelected())
					sortColumn.setOrder(SortOrder.ASCENDING);
				else
					sortColumn.setOrder(SortOrder.DESCENDING);
			};
			plainTextOptions.add(ascendingBox);
			plainTextOptions.add(descendingBox);
			ascendingBox.addChangeListener(l);
			descendingBox.addChangeListener(l);
			
			gbc.gridx = 1;
			gbc.gridy++;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(plainTextOptions, gbc);
			
			featureOptions.setLayout(new VerticalLayout());
			featureOptions.add(feature1Box);
			featureOptions.add(feature2Box);
			featureOptions.add(feature3Box);
			
			feature1Box.addItemListener( (e) -> {
				feature2Box.setEnabled(feature1Box.getSelectedItem() != null);
				feature3Box.setEnabled(feature1Box.getSelectedItem() != null 
						&& feature2Box.getSelectedItem() != null);
			});
			feature2Box.addItemListener( (e) -> {
				feature3Box.setEnabled(feature2Box.getSelectedItem() != null);
			});
			
			feature1Box.addItemListener(this::updateFeatureOrder);
			feature2Box.addItemListener(this::updateFeatureOrder);
			feature3Box.addItemListener(this::updateFeatureOrder);
			
			feature2Box.setEnabled(false);
			feature3Box.setEnabled(false);

			final JComboBox<?>[] featureBoxes = new JComboBox[] { feature1Box, feature2Box, feature3Box };
			final FeatureFamily[] featureOrder = sortColumn.getFeatureOrder();
			for(int i = 0; i < featureOrder.length; i++) {
				featureBoxes[i].setSelectedItem(featureOrder[i]);
			}
			gbc.gridy++;
			add(featureOptions, gbc);
			
			add(new JSeparator(SwingConstants.HORIZONTAL));
		}
		
		void setSeparator(JComponent sep) {
			this.separator = sep;
		}
		
		JComponent getSeparator() {
			return this.separator;
		}
		
		public SortColumn getSortColumn() {
			return sortColumn;
		}
		
		public void updateColumn() {
			sortColumn.setColumn(columnField.getText().trim());
		}
		
		public void updateFeatureOrder(ItemEvent e) {
			int featureOrderLength = 0;
			if(feature1Box.getSelectedItem() != null) {
				featureOrderLength++;
				
				if(feature2Box.getSelectedItem() != null) {
					featureOrderLength++;
					
					if(feature3Box.getSelectedItem() != null) {
						featureOrderLength++;
					}
				}
			}
			
			final FeatureFamily[] tempOrder = new FeatureFamily[3];
			tempOrder[0] = (FeatureFamily)feature1Box.getSelectedItem();
			tempOrder[1] = (FeatureFamily)feature2Box.getSelectedItem();
			tempOrder[2] = (FeatureFamily)feature3Box.getSelectedItem();
			final FeatureFamily[] finalOrder = Arrays.copyOf(tempOrder, featureOrderLength);
			sortColumn.setFeatureOrder(finalOrder);
		}
	}
}
