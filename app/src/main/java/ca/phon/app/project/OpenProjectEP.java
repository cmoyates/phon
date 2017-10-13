/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.*;

import javax.swing.SwingUtilities;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.*;

@PhonPlugin(name="default")
public class OpenProjectEP implements IPluginEntryPoint {
	
	private final static Logger LOGGER = Logger.getLogger(OpenProjectEP.class.getName());
	
	public static final String EP_NAME = "OpenProject";
	
	private String projectpath;
	
	@Override
	public String getName() {
		return EP_NAME;
	}
	
	/** Constructor */
	public OpenProjectEP() {
		super();
	}
	
	public void setProjectPath(String projectPath) {
		this.projectpath = projectPath;
	}
	
	public String getProjectPath() {
		return projectpath;
	}
	
	public void loadProject() {
		if(projectpath == null || projectpath.length() == 0)
			return;
		loadLocalProject();
	}
	
	public void newProject() {
		PluginEntryPointRunner.executePluginInBackground("NewProject");
	}
	
	//	 load the project
    private boolean loadLocalProject() {
    	final MessageDialogProperties props = new MessageDialogProperties();
    	props.setTitle("Open Project");
    	props.setHeader("Could not open project");
    	props.setRunAsync(false);
    	props.setOptions(MessageDialogProperties.okOptions);
    	props.setParentWindow(CommonModuleFrame.getCurrentFrame());
    	
    	try{
			File myFile = new File(projectpath);
			
			// file does not exist, return
			if(!myFile.exists()) {
				props.setMessage("Could not find a project at '" + projectpath + "'");
				NativeDialogs.showMessageDialog(props);
				return false;
			}
			
			// check to see if the project is already open...
			for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
				if(!(cmf instanceof ProjectWindow)) continue;
				final Project pfe = cmf.getExtension(Project.class);
				if(pfe != null && pfe.getLocation().equals(projectpath)) {
					cmf.toFront();
					cmf.requestFocus();
					return true;
				}
			}

			// 2010-11-24: New project format - plain directories.  We
			//             need to force people into decompressing the project.
			// is the file a .phon (or .zip) file or a directory?
			// if it's a .phon file we need to de-compress the file
			if(myFile.isFile() && (myFile.getName().endsWith(".phon")
									|| myFile.getName().endsWith(".zip")) ) {
				int retVal =
						NativeDialogs.showYesNoDialogBlocking(
							CommonModuleFrame.getCurrentFrame(), "",
							"Expand Project",
							"Phon 1.5+ no longer uses a compressed file format for projects. "
							+ " Press yes to expand the contents of your project before opening.");
				if(retVal == NativeDialogEvent.YES_OPTION) {
					File parentDir = myFile.getParentFile();
					String newProjName = myFile.getName().substring(0, myFile.getName().length()-5);
					File extractDir = new File(parentDir, newProjName);
					extractZip(myFile, extractDir);

					myFile = extractDir;
					
				} else {
					props.setMessage("Could not open project, see log for details.");
					NativeDialogs.showMessageDialog(props);
					return false;
				}
			}
			
			// file is read-only
			if(!myFile.canWrite()) {
				int result = NativeDialogs.showOkCancelDialogBlocking(
						null,
						"",
						"File Read-only",
						"You are about to open a project that is read-only. Changes will not be saved.");
				
				if(result == NativeDialogEvent.CANCEL_OPTION)
					return true; // file was actually found, so return true
			}
			
			final ProjectFactory factory = new DesktopProjectFactory();
			final Project proj = factory.openProject(myFile);
			
			// check project version and see if an update is needed
			if(proj.getVersion().equals("unk")) {
				String msgTitle = "Convert Project?";
				String msg = "This project needs to be upgraded for use in this version of Phon."
					+ " Click 'Yes' to convert this project.";
				int retVal = NativeDialogs.showYesNoDialogBlocking(CommonModuleFrame.getCurrentFrame(),
						"", msgTitle, msg);
				
				if(retVal == NativeDialogEvent.YES_OPTION) {
					HashMap<String, Object> initInfo = new HashMap<String, Object>();
					initInfo.put("oldProjectPath", myFile.getAbsolutePath());
					
					PluginEntryPointRunner.executePluginOnNewThread("ConvertProject", initInfo);
				}
				return true;
			}
			
			final ProjectWindow pwindow = new ProjectWindow(proj, proj.getLocation());
    		pwindow.pack();
    		pwindow.setSize(800, 600);
    		pwindow.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
    		pwindow.setVisible(true);
    		
    		return true;
		} catch (Exception e) {
			// catch anything and report
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
			props.setMessage(e.getLocalizedMessage());
			NativeDialogs.showMessageDialog(props);
		}
		
		return false;
    }

	private final static int ZIP_BUFFER = 2048;
	/**
	 * Extract contents of a zip file to the destination directory.
	 */
	private void extractZip(File zipFile, File destDir)
		throws IOException {

		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		
		
		// create output directory if it does not exist
		if(destDir.exists() && !destDir.isDirectory()) {
			throw new IOException("'" + destDir.getAbsolutePath() + "' is not a directory.");
		}
		
		ZipFile zip = new ZipFile(zipFile);

		if(!destDir.exists()) {
			destDir.mkdirs();
		}

		ZipEntry entry = null;
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while(entries.hasMoreElements()) {
			entry = entries.nextElement();

			if(entry.isDirectory()) {
				File outDir = new File(destDir, entry.getName());
				if(!outDir.exists())
					outDir.mkdirs();
			} else {
				LOGGER.info("Extracting: " + entry);

				in = new BufferedInputStream(zip.getInputStream(entry));
				int count = -1;
				byte data[] = new byte[ZIP_BUFFER];
				File outputFile = new File(destDir, entry.getName());

				if(outputFile.exists()) {
					LOGGER.warning("Overwriting file '" + outputFile.getAbsolutePath() + "'");
				}

				File parentFile = outputFile.getParentFile();

				if(!parentFile.exists())
					parentFile.mkdirs();

				FileOutputStream fos = new FileOutputStream(outputFile);
				out = new BufferedOutputStream(fos, ZIP_BUFFER);
				while((count = in.read(data, 0, ZIP_BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				out.flush();
				out.close();
				in.close();
			}
		}
		zip.close();
	}

	@Override
	public void pluginStart(Map<String, Object> args)  {
		if(GraphicsEnvironment.isHeadless()) return;
		
		final EntryPointArgs epArgs = new EntryPointArgs(args);
		this.projectpath = (String)epArgs.get(EntryPointArgs.PROJECT_LOCATION);
		
		if(SwingUtilities.isEventDispatchThread()) {
			openProject.run();
		} else {
			SwingUtilities.invokeLater(openProject);
		}
	}
	
	/**
	 * Runnable action
	 */
	private final Runnable openProject = new Runnable() {
		
		@Override
		public void run() {
			loadProject();
		}
		
	};
}
