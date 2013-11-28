package ca.phon.app.session.editor;

import java.awt.Container;
import java.util.Set;

import javax.swing.JComponent;

/**
 * <p>View model for the {@link SessionEditor}.  This class
 * is responsible for creating and placing any {@link EditorView}s.</p>
 *
 */
public interface EditorViewModel {
	
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
	 * Get the list of view names handeled by this
	 * model
	 * 
	 * @return list of available view names
	 */
	public Set<String> getViewNames();
	
	/**
	 * Show the specified view.
	 * 
	 * @param viewName
	 */
	public void showView(String viewName);
	
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

}
