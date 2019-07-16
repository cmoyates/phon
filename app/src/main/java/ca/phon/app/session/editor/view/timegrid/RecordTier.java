package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.teamdev.jxbrowser.chromium.internal.ipc.message.SetupProtocolHandlerMessage;

import ca.phon.app.media.TimeUIModel.Interval;
import ca.phon.app.media.Timebar;
import ca.phon.app.session.editor.DelegateEditorAction;
import ca.phon.app.session.editor.EditorAction;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.EditorEventType;
import ca.phon.app.session.editor.RunOnEDT;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.TierViewItem;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;

public class RecordTier extends TimeGridTier {
	
	private RecordGrid recordGrid;
	
	private Map<Participant, Boolean> speakerVisibility = new HashMap<>();
	
	private Map<String, Boolean> tierVisibility = new HashMap<>();
	
	public RecordTier(TimeGridView parent) {
		super(parent);
	
		init();
		setupEditorEvents();
	}

	private void init() {
		Session session = getParentView().getEditor().getSession();
		recordGrid = new RecordGrid(getTimeModel(), session);
		
		recordGrid.setFont(FontPreferences.getTierFont());
		setupSpeakers();
		
		// add ortho by default
		tierVisibility.put(SystemTierType.Orthography.getName(), Boolean.TRUE);
		setupTiers();
		
		recordGrid.addRecordGridMouseListener(mouseListener);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(recordGrid, BorderLayout.CENTER);
	}
	
	private final DelegateEditorAction onRecordChange = 
			new DelegateEditorAction(this, "onRecordChange");
		
	private final DelegateEditorAction onTierChangedAct = 
			new DelegateEditorAction(this, "onTierChanged");
	
	private final DelegateEditorAction onParticipantRemoveAct =
			new DelegateEditorAction(this, "onParticipantRemoved");
	
	private final DelegateEditorAction onParticipantAddedAct =
			new DelegateEditorAction(this, "onParticipantAdded");
	
	private void setupEditorEvents() {
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantRemoveAct);
		getParentView().getEditor().getEventManager()
			.registerActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
	}
	
	private void deregisterEditorEvents() {
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.RECORD_CHANGED_EVT, onRecordChange);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.TIER_CHANGED_EVT, onTierChangedAct);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.PARTICIPANT_REMOVED, onParticipantRemoveAct);
		getParentView().getEditor().getEventManager()
			.removeActionForEvent(EditorEventType.PARTICIPANT_ADDED, onParticipantAddedAct);
	}
		
	/* Editor events */
	@RunOnEDT
	public void onRecordChange(EditorEvent evt) {
		Record r = (Record)evt.getEventData();
		
		getTimeModel().clearIntervals();
		MediaSegment segment = r.getSegment().getGroup(0);
		var segStartTime = segment.getStartValue() / 1000.0f;
		var segEndTime = segment.getEndValue() / 1000.0f;
		var interval = getTimeModel().addInterval(segStartTime, segEndTime);
		interval.addPropertyChangeListener(new RecordIntervalListener(interval));
		
		recordGrid.setCurrentRecord(r);
	}
	
	
	@RunOnEDT
	public void onParticipantRemoved(EditorEvent ee) {
		speakerVisibility.remove((Participant)ee.getEventData());
		setupSpeakers();
	}
	
	@RunOnEDT
	public void onParticipantAdded(EditorEvent ee) {
		setupSpeakers();
	}
	
	@RunOnEDT
	public void onTierChanged(EditorEvent ee) {
		if(SystemTierType.Orthography.getName().equals(ee.getEventData().toString())
				|| SystemTierType.Segment.getName().equals(ee.getEventData().toString())) {
			recordGrid.repaint();
		}
	}
	
	/**
	 * Is the speaker visible? 
	 * 
	 * @param speaker
	 * @return
	 */
	public boolean isSpeakerVisible(Participant speaker) {
		boolean retVal = true;
		
		if(speakerVisibility.containsKey(speaker))
			retVal = speakerVisibility.get(speaker);
		
		return retVal;
	}
	
	public void setSpeakerVisible(Participant speaker, boolean visible) {
		speakerVisibility.put(speaker, visible);
		setupSpeakers();
	}
	
	public void toggleSpeaker(PhonActionEvent pae) {
		Participant speaker = (Participant)pae.getData();
		setSpeakerVisible(speaker, !isSpeakerVisible(speaker));
	}
	
	private void setupSpeakers() {
		Session session = getParentView().getEditor().getSession();
		
		var speakerList = new ArrayList<Participant>();
		for(var speaker:session.getParticipants()) {
			if(isSpeakerVisible(speaker))
				speakerList.add(speaker);
		}
		if(isSpeakerVisible(Participant.UNKNOWN))
			speakerList.add(Participant.UNKNOWN);
		recordGrid.setSpeakers(speakerList);
	}
	
	public boolean isTierVisible(String tierName) {
		boolean retVal = false;
		
		if(tierVisibility.containsKey(tierName)) 
			retVal = tierVisibility.get(tierName);
		
		return retVal;
	}
	
	public void setTierVisible(String tierName, boolean visible) {
		tierVisibility.put(tierName, visible);
		setupTiers();
	}
	
	public void toggleTier(String tierName) {
		setTierVisible(tierName, !isTierVisible(tierName));
	}
	
	private void setupTiers() {
		Session session = getParentView().getEditor().getSession();
		recordGrid.setTiers(
				session.getTierView().stream()
					.map( TierViewItem::getTierName )
					.filter( this::isTierVisible )
					.collect( Collectors.toList() )
		);
	}
	
	public void setupContextMenu(MenuBuilder builder) {
		setupSpeakerMenu(builder);
	}
	
	public void setupSpeakerMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for(var speaker:session.getParticipants()) {
			final PhonUIAction toggleSpeakerAct = new PhonUIAction(this, "toggleSpeaker", speaker);
			toggleSpeakerAct.putValue(PhonUIAction.NAME, speaker.toString());
			toggleSpeakerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle speaker " + speaker);
			toggleSpeakerAct.putValue(PhonUIAction.SELECTED_KEY, isSpeakerVisible(speaker));
			final JCheckBoxMenuItem toggleSpeakerItem = new JCheckBoxMenuItem(toggleSpeakerAct);
			builder.addItem(".", toggleSpeakerItem);
		}
		
		final PhonUIAction toggleUnknownAct = new PhonUIAction(this, "toggleSpeaker", Participant.UNKNOWN);
		toggleUnknownAct.putValue(PhonUIAction.NAME, Participant.UNKNOWN.toString());
		toggleUnknownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle speaker " + Participant.UNKNOWN);
		toggleUnknownAct.putValue(PhonUIAction.SELECTED_KEY, isSpeakerVisible(Participant.UNKNOWN));
		final JCheckBoxMenuItem toggleUnknownItem = new JCheckBoxMenuItem(toggleUnknownAct);
		builder.addItem(".", toggleUnknownItem);
	}
	
	public void setupTierMenu(MenuBuilder builder) {
		Session session = getParentView().getEditor().getSession();
		for(var tierViewItem:session.getTierView()) {
			final PhonUIAction toggleTierAct = new PhonUIAction(this, "toggleTier", tierViewItem.getTierName());
			toggleTierAct.putValue(PhonUIAction.NAME, tierViewItem.getTierName());
			toggleTierAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Toggle tier " + tierViewItem.getTierName());
			toggleTierAct.putValue(PhonUIAction.SELECTED_KEY, isTierVisible(tierViewItem.getTierName()));
			final JCheckBoxMenuItem toggleTierItem = new JCheckBoxMenuItem(toggleTierAct);
			builder.addItem(".", toggleTierItem);
		}
	}

	
	public boolean isResizeable() {
		return false;
	}

	@Override
	public void onClose() {
		deregisterEditorEvents();
	}
	
	private class RecordIntervalListener implements PropertyChangeListener {
		
		private Interval interval;
		
		public RecordIntervalListener(Interval interval) {
			this.interval = interval;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Record r = recordGrid.getCurrentRecord();
			MediaSegment segment = r.getSegment().getGroup(0);
			
			if(evt.getSource() == this.interval.getStartMarker()) {
				segment.setStartValue((float)evt.getNewValue() * 1000.0f);
			} else if(evt.getSource() == this.interval.getEndMarker()) {
				segment.setEndValue((float)evt.getNewValue() * 1000.0f);
			}
			// TODO calculate clip rect
			recordGrid.repaint();
		}
		
	}
	
	private RecordGridMouseListener mouseListener = new RecordGridMouseListener() {

		@Override
		public void recordClicked(int recordIndex, MouseEvent me) {
			getParentView().getEditor().setCurrentRecordIndex(recordIndex);
		}

		@Override
		public void recordPressed(int recordIndex, MouseEvent me) {
		}

		@Override
		public void recordReleased(int recordIndex, MouseEvent me) {
		}

		@Override
		public void recordEntered(int recordIndex, MouseEvent me) {
		}

		@Override
		public void recordExited(int recordIndex, MouseEvent me) {
		}
		
	};
}
