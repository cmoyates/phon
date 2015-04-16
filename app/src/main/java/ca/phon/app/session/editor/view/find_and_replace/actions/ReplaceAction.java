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
package ca.phon.app.session.editor.view.find_and_replace.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.view.find_and_replace.FindAndReplaceEditorView;

public class ReplaceAction extends FindAndReplaceAction {

	private static final long serialVersionUID = 2574281425626924879L;
	
	private final static String CMD_NAME = "Replace";
	
	private final static String SHORT_DESC = "Replace";
	
	private boolean andFind = false;
	
	public ReplaceAction(SessionEditor editor, FindAndReplaceEditorView view, boolean andFind) {
		super(editor, view);
		
		this.andFind = andFind;
		
		putValue(NAME, CMD_NAME + (andFind ? " and find" : ""));
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		getView().replace();
		if(andFind) {
			getView().findNext();
		}
	}

}
