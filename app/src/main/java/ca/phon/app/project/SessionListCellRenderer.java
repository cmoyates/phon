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
package ca.phon.app.project;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SessionListCellRenderer extends DefaultListCellRenderer {
	
	private static final long serialVersionUID = 576253657524546120L;

	@Override
	public Component getListCellRendererComponent(
			JList list, Object value, int index, 
			boolean isSelected, boolean cellHasFocus) {
		JLabel comp = (JLabel)
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if(isSelected) {
//			ImageIcon icon = 
//				IconManager.getInstance().getIcon("animations/process-working", IconSize.SMALL);
//				icon.setImageObserver(list);
			comp.setIcon(
					IconManager.getInstance().getIcon("mimetypes/text-xml", IconSize.SMALL));
//					icon );
		} else {
			comp.setIcon(
					IconManager.getInstance().getIcon("blank", IconSize.SMALL));
		}
		
		// see if the transcript it locked...
		SessionListModel model = (SessionListModel)list.getModel();
		if(model.getProject().isSessionLocked(model.getCorpus(), value.toString())) {
			comp.setIcon(
					IconManager.getInstance().getIcon("emblems/emblem-readonly", IconSize.SMALL));
		}
	
		return comp;
	}
}
