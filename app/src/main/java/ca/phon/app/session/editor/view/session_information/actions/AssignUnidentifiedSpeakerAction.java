package ca.phon.app.session.editor.view.session_information.actions;

import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.view.session_information.SessionInfoEditorView;
import ca.phon.session.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.icons.*;

public class AssignUnidentifiedSpeakerAction extends SessionInfoAction {

	private final static String TXT = "Assign unidentified records to ";
	
	private final static String DESC = "Assign all unidentified records to speaker.";
	
	private final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/edit_user", IconSize.SMALL);
	
	private Participant participant;
	
	public AssignUnidentifiedSpeakerAction(SessionEditor editor, SessionInfoEditorView view, Participant participant) {
		super(editor, view);
		this.participant = participant;
		
		putValue(NAME, TXT + participant);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Session session = getEditor().getSession();
		
		int numChanged = 0;
		final CompoundEdit cmpEdit = new CompoundEdit();
		for(Record r:session.getRecords()) {
			if(r.getSpeaker() == Participant.UNKNOWN) {
				final ChangeSpeakerEdit edit = new ChangeSpeakerEdit(getEditor(), r, participant);
				edit.doIt();
				
				cmpEdit.addEdit(edit);
			
				++numChanged;
			}
		}
		
		cmpEdit.end();
		
		if(numChanged > 0) {
			getEditor().getUndoSupport().postEdit(cmpEdit);
			
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setRunAsync(true);
			props.setTitle(String.format("%d Records Modified", numChanged));
			props.setHeader(props.getTitle());
			props.setMessage("Speaker for records assigned to participant " + participant);
			props.setOptions(new String[]{"Ok", "Undo"});
			props.setParentWindow(getEditor());
			props.setListener(new NativeDialogListener() {
				
				@Override
				public void nativeDialogEvent(NativeDialogEvent event) {
					if(event.getDialogResult() == 1) {
						// undo operation
						SwingUtilities.invokeLater( () -> getEditor().getUndoManager().undo() );
					}
				}
			});
			NativeDialogs.showMessageDialog(props);
		}
	}

}
