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
package ca.phon.app.project;

import java.io.File;
import java.util.Map;

import org.joda.time.DateTime;

import ca.phon.app.workspace.ProjectArchiveTask;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.project.Project;
import ca.phon.session.DateFormatter;
import ca.phon.workspace.Workspace;

@PhonPlugin(name="default")
public class ProjectArchiveEP implements IPluginEntryPoint {
	
	/**
	 * The project we are archiving
	 */
	private Project project;

	private final static String EP_NAME = "ProjectArchive";
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	@Override
	public void pluginStart(Map<String, Object> initInfo) {
		//make sure we have a project
		if(initInfo.get("project") == null) {
			throw new IllegalArgumentException("project cannot be null");
		}
		
		Object v = initInfo.get("project");
		if(!(v instanceof Project)) {
			throw new IllegalArgumentException("project object does not implement IPhonProject interface");
		}
		project = (Project)v;
		
		// display options UI
		
		// default output file
//		PhonDateFormat pdf = new PhonDateFormat(PhonDateFormat.YEAR_LONG);
//		String today = pdf.format(Calendar.getInstance());
		final String today = DateFormatter.dateTimeToString(DateTime.now());
		
		String zipFileName = 
			project.getName() + "-" + today + ".zip";
		File archiveDir = 
			new File(Workspace.userWorkspace().getWorkspaceFolder(), "archives");
		if(!archiveDir.exists()) {
			archiveDir.mkdirs();
		}
		
		File zipFile =
			new File(archiveDir, zipFileName);
		
		// create and execute task
		final ProjectArchiveTask task = new ProjectArchiveTask(project, zipFile);
		task.performTask();
		
	}
}
