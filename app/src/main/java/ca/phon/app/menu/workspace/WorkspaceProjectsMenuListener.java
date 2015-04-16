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
package ca.phon.app.menu.workspace;

import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.project.OpenProjectEP;
import ca.phon.plugin.PluginAction;
import ca.phon.project.Project;
import ca.phon.workspace.Workspace;

public class WorkspaceProjectsMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		final JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		final Workspace workspace = Workspace.userWorkspace();
		for(Project project:workspace.getProjects()) {
			final String projectPath = project.getLocation();
			final HashMap<String, Object> initInfo = new HashMap<String, Object>();
			initInfo.put(OpenProjectEP.PROJECTPATH_PROPERTY, projectPath);
			
			final PluginAction act = new PluginAction(OpenProjectEP.EP_NAME, true);
			act.putValue(PluginAction.NAME, project.getName());
			act.putValue(PluginAction.SHORT_DESCRIPTION, project.getLocation());
			act.putArgs(initInfo);
			menu.add(act);
		}
	}

	@Override
	public void menuDeselected(MenuEvent e) {

	}

	@Override
	public void menuCanceled(MenuEvent e) {
	
	}

}
