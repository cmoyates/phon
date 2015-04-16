/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.ipa_lookup;

import javax.swing.JMenu;

import ca.phon.app.session.editor.view.ipa_lookup.actions.AutoTranscribeCommand;
import ca.phon.app.session.editor.view.ipa_lookup.actions.ExportIPACommand;
import ca.phon.app.session.editor.view.ipa_lookup.actions.ImportIPACommand;

public class IPALookupViewMenu extends JMenu {
	
	private static final long serialVersionUID = 3248124841856311448L;

	private final IPALookupView lookupView;
	
	public IPALookupViewMenu(IPALookupView lookupView) {
		super();
		this.lookupView = lookupView;
		
		init();
	}
	
	private void init() {
		final ExportIPACommand exportAct = new ExportIPACommand(lookupView);
		add(exportAct);
		
		final ImportIPACommand importAct = new ImportIPACommand(lookupView);
		add(importAct);
		
		addSeparator();
		final AutoTranscribeCommand autoTranscribeAct = new AutoTranscribeCommand(lookupView);
		add(autoTranscribeAct);
	}

}
