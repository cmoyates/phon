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
package ca.phon.app.session.editor;

import java.awt.Container;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.MenuElement;

/**
 * <p>View model for the {@link SessionEditor}.  This class
 * is responsible for creating and placing any {@link EditorView}s.</p>
 *
 */
public interface EditorViewModel {
	
	
	/**
	 * Remove all view references and cleanup resources.
	 * 
	 * 
	 */
	public void cleanup();
	
	/**
	 * Get the view root for the editor.  This is the component
	 * that will be displayed in the root pane of the editor
	 * window.
	 * 
	 * @return the root container for the editor
	 */
	public Container getRoot();
	
	/**
	 * Get the view specified by the given name.
	 * 
	 * @param viewName
	 */
	public EditorView getView(String viewName);
	
	/**
	 * Get the icon associated with the view.  This information
	 * comes from the view itself if the view is loaded, otherwise
	 * information is obtained from the EditorViewInfo annotation
	 * on the extension point.
	 * 
	 * @return view icon
	 */
	public ImageIcon getViewIcon(String viewName);

	/**
	 * Get the list of view names handeled by this
	 * model
	 * 
	 * @return list of available view names
	 */
	public Set<String> getViewNames();
	
	/**
	 * Get the set of available views organized by
	 * category.
	 * 
	 * @return editor views by category
	 */
	public Map<EditorViewCategory, List<String>> getViewsByCategory();
	
	/**
	 * Show the specified view.
	 * 
	 * @param viewName
	 */
	public void showView(String viewName);
	
	/**
	 * Get the close action for a view
	 * 
	 * @param viewName
	 * 
	 * @return the close action for the view
	 */
	public Action getCloseAction(String viewView);
	
	/**
	 * Is the specified view showing
	 * 
	 * @return <code>true</code> if the given view is part
	 *  of the current dock control, <code>false</code> otherwise
	 */
	public boolean isShowing(String viewName);
	
	/**
	 * Show the specified view as a new dynamic floating
	 * view.  These views are <b>not</b> saved in layouts.
	 *
	 * @param title
	 * @param comp
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void showDynamicFloatingDockable(String title, JComponent comp, int x, int y, int w, int h);
	
	/**
	 * Hide the specified view
	 * 
	 * @param viewName
	 */
	public void hideView(String viewName);
	
	/**
	 * Setup windows using the given perspective
	 * 
	 * @param editorPerspective
	 */
	public void setupWindows(RecordEditorPerspective editorPerspective);
	
	/**
	 * Setup views based on the given perspective 
	 * 
	 * @param editorPerspective
	 */
	public void applyPerspective(RecordEditorPerspective editorPerspective);
	
	/**
	 * Save the current view perspective as the specified editor 
	 * perspective
	 * 
	 * @param editorPerspective
	 */
	public void savePerspective(RecordEditorPerspective editorPerspective);
	
	/**
	 * Remove prespective from dock control
	 * 
	 * @param editorPerspective
	 */
	public void removePrespective(RecordEditorPerspective editorPerspective);
	
	/**
	 * Setup view menu
	 * 
	 * @param menuElement
	 */
	public void setupViewMenu(MenuElement menuElement);
	
	/**
	 * Setup perspective menu.
	 * 
	 * @param menuElement
	 */
	public void setupPerspectiveMenu(MenuElement menuElement);

}
