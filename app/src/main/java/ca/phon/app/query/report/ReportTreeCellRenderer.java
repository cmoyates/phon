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
package ca.phon.app.query.report;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.lang3.StringUtils;

import ca.phon.query.report.io.AggregrateInventory;
import ca.phon.query.report.io.CommentSection;
import ca.phon.query.report.io.Group;
import ca.phon.query.report.io.InventorySection;
import ca.phon.query.report.io.ParamSection;
import ca.phon.query.report.io.ResultListing;
import ca.phon.query.report.io.Section;
import ca.phon.query.report.io.SummarySection;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class ReportTreeCellRenderer extends DefaultTreeCellRenderer {
	
	/** Icon hash */
	private HashMap<Class<? extends Section>, ImageIcon> icons = 
		new HashMap<Class<? extends Section>, ImageIcon>();
	
	private ImageIcon rptIcon;

	public ReportTreeCellRenderer() {
		// load icons
		loadIcons();
	}
	
	private void loadIcons() {
		// comment
		ImageIcon comIcon = IconManager.getInstance().getIcon("actions/comment", IconSize.SMALL);
		icons.put(CommentSection.class, comIcon);
//		ImageIcon listIcon = IconManager.getInstance().getIcon("actions/enumList", IconSize.SMALL);
//		icons.put(SectionFlavor.LISTING, listIcon);
		ImageIcon invIcon = IconManager.getInstance().getIcon("actions/unsortedList", IconSize.SMALL);
		icons.put(InventorySection.class, invIcon);
		rptIcon = IconManager.getInstance().getIcon("mimetypes/txt", IconSize.SMALL);
//		ImageIcon paramIcon = IconManager.getInstance().getIcon("actions/playlist", IconSize.SMALL);
		icons.put(ParamSection.class, invIcon);
		icons.put(SummarySection.class, invIcon);
		icons.put(ResultListing.class, invIcon);
		icons.put(AggregrateInventory.class, invIcon);
		ImageIcon grpIcon = IconManager.getInstance().getIcon("mimetypes/kmultiple", IconSize.SMALL);
		icons.put(Group.class, grpIcon);
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree arg0, Object arg1,
			boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
		JLabel lbl = (JLabel)super.getTreeCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5,
				arg6);
		

		Section section = (Section)arg1;
		
		if(arg1 == arg0.getModel().getRoot()) {
			lbl.setIcon(rptIcon);
		} else {
			lbl.setIcon(icons.get(section.getClass()));
		}
		
		String lblText = StringUtils.strip(section.getName());
		if(lblText == null || lblText.length() == 0) {
			lblText = "<html><i>Untitled</i></html>";
		}
		lbl.setText(lblText);
		
//		if(arg1 instanceof ReportDesign) {
//			ReportDesign report = (ReportDesign)arg1;
//			lbl.setText(report.getName());
//			lbl.setIcon(rptIcon);
//		} else {
//			Section section = (Section)arg1;
//			lbl.setIcon(icons.get(section.getClass()));
//			
//			
//			lbl.setText(section.getName());
////			if(section instanceof Group) {
////				lbl.setText("Group : " + section.getName());
////			} else {
////				lbl.setText("Section : " + section.getName());
////			}
//		}
		
		return lbl;
	}
	
}
