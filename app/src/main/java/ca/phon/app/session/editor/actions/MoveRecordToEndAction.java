/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.RecordMoveEdit;
import ca.phon.session.Record;
import ca.phon.session.Session;

public class MoveRecordToEndAction extends SessionEditorAction {

	private static final long serialVersionUID = 807539158703693401L;
	
	private final String TXT = "Move record to end";
	
	private final String DESC = "Move current record to end of session";

	public MoveRecordToEndAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final SessionEditor editor = getEditor();
		final Session session = editor.getSession();
		final Record record = editor.currentRecord();
		
		final int position = session.getRecordCount()-1;
		
		if(position >= 0) {
			final RecordMoveEdit edit = new RecordMoveEdit(getEditor(), record, position);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
