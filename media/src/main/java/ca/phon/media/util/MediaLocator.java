/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.media.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.EmptyQueueException;
import ca.phon.util.PrefHelper;
import ca.phon.util.Queue;

/**
 * Helper methods for locating and handling paths for media files.
 * 
 * Also includes methods for setting up the media include paths.
 * 
 */
public class MediaLocator {

	/** 
	 * Media include path property.
	 * This value of this property should be a semi-colon separated list of
	 * paths in order of search priority.
	 */
	public static final String MEDIA_INCLUDE_PATH_PROP = "ca.phon.media.util.MediaLocator.includepath";
	
	/**
	 * Set media include path as a list of
	 * paths.
	 * 
	 * @param paths
	 * @param props
	 */
	public static void setMediaIncludePaths(List<String> paths) {
		String includePath = "";
		
		for(String path:paths) {
			includePath += path + ";";
		}
		
		PrefHelper.getUserPreferences().put(MEDIA_INCLUDE_PATH_PROP, includePath);
	}
	
	public static List<String> getMediaIncludePaths() {
		return parseMediaIncludePaths();
	}
	
	/**
	 * Get media include path as a list of
	 * paths.
	 * 
	 * @param props
	 * @return media include paths
	 */
	private static List<String> parseMediaIncludePaths() {
		List<String> retVal = new ArrayList<String>();
		
		final String includePath = PrefHelper.get(MEDIA_INCLUDE_PATH_PROP, null);
		if(includePath != null) {
			String[] paths = includePath.split(";");
			
			for(String path:paths) {
				if(StringUtils.strip(path).length() > 0) {
					retVal.add(path);
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Find media given a project and session.
	 */
	public static File findMediaFile(Project project, Session session) {
		// build a list of possible media locations
		Queue<String> mediaLocations = new Queue<String>();
//		final PathExpander pe = new PathExpander();
		String mediaRef = session.getMediaLocation();
		if(mediaRef != null && mediaRef.length() > 0) {
//			mediaRef = pe.expandPath(mediaRef);
			mediaLocations.queue(mediaRef);
			// check for extension on media ref
			if(mediaRef.indexOf('.') < 0) {
				for(String ext:FileFilter.mediaFilter.getAllExtensions()) {
					mediaLocations.add(mediaRef + ext);
				} 
			}
		} else {
			String baseName = session.getName();
			for(String ext:FileFilter.mediaFilter.getAllExtensions()) {
				mediaLocations.add(baseName + ext);
			}
		}
		
		File retVal = null;
		
		try {
			while((retVal == null) && (mediaLocations.peek() != null)) {
		
				String mediaLocation = mediaLocations.dequeue();
				retVal = findMediaFile(mediaLocation, project, session.getCorpus());
			}
		} catch (EmptyQueueException e) {
		}
		return retVal;
	}
	
	/**
	 * Search for a file in the media include paths.
	 * 
	 * @param filename
	 * @return the file object for the file or null if not found
	 */
	public static File findMediaFile(String filename) {
		return findMediaFile(filename, null, null);
	}
	
	/**
	 * Search for a file in the media include path.
	 * 
	 * Will look in the project resource directory first if
	 * project is not null.
	 * 
	 * @param filename
	 * @param project (may be <code>null</code>)
	 * @param corpus (may be <code>null</code>)
	 * @return the file object for the file or null if not found
	 */
	 public static File findMediaFile(String filename, Project project, String corpus) {
		 File retVal = null;
		 
		 // do we already have an absolute path
		 final File mediaFile = new File(filename);
		 if(mediaFile.isAbsolute()) {
			 retVal = mediaFile;
		 } else {
			 /* 
			  * Look for media in the following location order:
			  * 
			  * <media folder>/<project>/<corpus>/<file>
			  * <media folder>/<project>/<file>
			  * <media folder>/<corpus>/<file>
			  * <media folder>/<file>
			  */
			 final List<String> checkList = new ArrayList<String>();
			 if(project != null && corpus != null) {
				 checkList.add(project.getName() + 
						 File.separator + corpus + File.separator + filename);
			 }
			 if(project != null) {
				 checkList.add(project.getName() + 
						 File.separator + filename);
			 }
			 if(corpus != null) {
				 checkList.add(corpus + File.separator + filename);
			 }
			 checkList.add(File.separator + filename);
			 
			 final List<String> mediaPaths = 
					 new ArrayList<String>();
			 mediaPaths.addAll(getMediaIncludePaths());
			 
			 if(project != null) {
				 // check resources
				 File resFile = new File(project.getLocation(), "__res");
				 File resMediaFile = new File(resFile, "media");
				 mediaPaths.add(0, resMediaFile.getAbsolutePath());
			 }
			 
			
			 // look in rest of search paths
			 for(String path:mediaPaths) {
				 for(String checkName:checkList) {
					 final File checkFile = new File(path, checkName);
					 if(checkFile.exists()) {
						 retVal = checkFile;
						 break;
					 }
				 }
			 }
			 
		 }
		 
		 return retVal;
	 }
}
