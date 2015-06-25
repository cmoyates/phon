package ca.phon.app.query.analysis.actions;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.app.commands.graph.AutoLayoutCommand;
import ca.phon.app.hooks.HookableAction;
import ca.phon.app.query.analysis.AssessmentEditor;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

public class AssessmentEditorAction extends HookableAction {

	private static final long serialVersionUID = -6806789019274420560L;

	private CommonModuleFrame owner;
	
	public AssessmentEditorAction(CommonModuleFrame owner) {
		this.owner = owner;
		
		putValue(NAME, "Assessment Editor...");
		putValue(SHORT_DESCRIPTION, "Display analysis editor");
	}
	
	@Override
	public void hookableActionPerformed(final ActionEvent ae) {
		final AssessmentEditor editor = new AssessmentEditor(owner.getExtension(Project.class));
		editor.setParentFrame(owner);
		editor.setSize(1024, 760);
		editor.centerWindow();
		editor.setVisible(true);
		
		SwingUtilities.invokeLater( () -> {
			final AutoLayoutCommand autoLayout = new AutoLayoutCommand();
			autoLayout.actionPerformed(ae);
		});
	}

}
