package ca.phon.app.session.editor.tier;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import ca.phon.app.session.editor.tier.TierDataLayout.GroupMode;
import ca.phon.ui.SegmentedButtonBuilder;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Buttons for controlling layout options for a given
 * TierDataLayout instance.
 */
public class TierDataLayoutButtons extends JComponent {

	private static final long serialVersionUID = 4389264170362603709L;
	
	private static final String WRAP_ICON = "actions/format-text-align-left";
	
	private static final String WRAP_DESC = "Left-align and wrap groups";
	
	private static final String ALIGN_ICON = "actions/format-text-columns";
	
	private static final String ALIGN_DESC = "Keep groups aligned vertically";

	private final WeakReference<Container> containerRef;
	
	private final WeakReference<TierDataLayout> layoutRef;
	
	/*
	 * Buttons
	 */
	private ButtonGroup buttonGroup;
	private JButton alignButton;
	private JButton wrapButton;
	
	public TierDataLayoutButtons(Container container, TierDataLayout layout) {
		super();
		
		this.containerRef = new WeakReference<>(container);
		this.layoutRef = new WeakReference<>(layout);
		init();
	}
	
	public TierDataLayout getLayout() {
		return layoutRef.get();
	}
	
	public Container getContainer() {
		return containerRef.get();
	}

	
	private void init() {
		buttonGroup = new ButtonGroup();
		final List<JButton> buttons = SegmentedButtonBuilder.createSegmentedButtons(2, buttonGroup);
		
		final ImageIcon wrapIcon = IconManager.getInstance().getIcon(WRAP_ICON, IconSize.SMALL);
		final PhonUIAction wrapAct = new PhonUIAction(this, "wrapGroups");
		wrapAct.putValue(PhonUIAction.SMALL_ICON, wrapIcon);
		wrapAct.putValue(PhonUIAction.SHORT_DESCRIPTION, WRAP_DESC);
		wrapButton = buttons.get(0);
		wrapButton.setAction(wrapAct);
		
		final ImageIcon alignIcon = IconManager.getInstance().getIcon(ALIGN_ICON, IconSize.SMALL);
		final PhonUIAction alignAct = new PhonUIAction(this, "alignGroups");
		alignAct.putValue(PhonUIAction.SMALL_ICON, alignIcon);
		alignAct.putValue(PhonUIAction.SHORT_DESCRIPTION, ALIGN_DESC);
		alignButton = buttons.get(1);
		alignButton.setAction(alignAct);
		
		if(getLayout().getGroupMode() == GroupMode.ALIGNED)
			alignButton.setSelected(true);
		else
			wrapButton.setSelected(true);
		
		final JComponent comp = SegmentedButtonBuilder.createLayoutComponent(buttons);
		setLayout(new BorderLayout());
		add(comp, BorderLayout.CENTER);
	}
	
	/*
	 * Button actions
	 */
	public void alignGroups() {
		alignButton.setSelected(true);
		wrapButton.setSelected(false);
		getLayout().setGroupMode(GroupMode.ALIGNED);
		
		getContainer().invalidate();
		getLayout().layoutContainer(getContainer());
		if(getContainer().getParent() != null) {
			getContainer().getParent().validate();
		}
	}
	
	public void wrapGroups() {
		wrapButton.setSelected(true);
		alignButton.setSelected(false);
		getLayout().setGroupMode(GroupMode.WRAPPED);
		
		getContainer().invalidate();
		getLayout().layoutContainer(getContainer());
		if(getContainer().getParent() != null) {
			getContainer().getParent().validate();
		}
	}
	
	
}
