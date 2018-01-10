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
package ca.phon.app.opgraph.editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoManager;

import org.apache.commons.io.FilenameUtils;

import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.perspective.*;
import bibliothek.util.Filter;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.gedge.opgraph.app.components.canvas.GridLayer;
import ca.phon.app.opgraph.editor.OpgraphEditorModel.ViewLocation;
import ca.phon.app.opgraph.editor.actions.debug.*;
import ca.phon.app.opgraph.editor.actions.file.*;
import ca.phon.app.opgraph.editor.actions.graph.*;
import ca.phon.app.opgraph.editor.actions.view.*;
import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.plugin.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.menu.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.RecentFiles;

/**
 * Generic opgragh editor.
 *
 * @author Greg
 *
 */
public class OpgraphEditor extends CommonModuleFrame {

	private static final long serialVersionUID = 311253647756696496L;

	/**
	 * Docking view controller
	 */
	private CControl dockControl;

	private OpgraphEditorModel model;

	private JMenuBar menuBar;

	private JToolBar toolBar;

	private NodeEditorStatusBar statusBar;

	public final static String RECENT_DOCS_PROP = OpgraphEditor.class.getName() + ".recentDocs";

	public OpgraphEditor() {
		this(new MacroOpgraphEditorModel());
	}

	public OpgraphEditor(OpgraphEditorModel model) {
		super();
		setModel(model);

		// rebuild menu so edit undo/redo commands work properly
		final JMenuBar menuBar = MenuManager.createWindowMenuBar(this);
		setJMenuBar(menuBar);

		initDockingView();
	}

	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
		setupMenu();
	}

	@Override
	public boolean hasUnsavedChanges() {
		return getModel().getDocument().hasModifications();
	}

	public OpgraphEditorModel getModel() {
		return this.model;
	}

	public void setModel(OpgraphEditorModel model) {
		if(this.model != null)
			this.model.getDocument().getUndoSupport().removeUndoableEditListener(undoListener);
		this.model = model;
		// set undo manager for edit menu commands
		putExtension(UndoManager.class, this.model.getDocument().getUndoManager());
		this.model.getDocument().getUndoSupport().addUndoableEditListener(undoListener);
		if(dockControl != null)
			resetView();
		else
			updateTitle();

		setJMenuBar(MenuManager.createWindowMenuBar(this));
	}

	public void resetView() {
		for(String view:model.getAvailableViewNames()) {
			dockControl.removeSingleDockable(view);
		}
		setupDefaultPerspective();
		updateTitle();
	}

	public File getCurrentFile() {
		return getModel().getDocument().getSource();
	}

	public void setCurrentFile(File source) {
		getModel().getDocument().setSource(source);
		updateTitle();

		// update node title for wizard
		final WizardExtension ext = getModel().getDocument().getRootGraph().getExtension(WizardExtension.class);
		if(ext != null) {
			final String name = FilenameUtils.getBaseName(source.getAbsolutePath());
			ext.setWizardTitle(name);
		}
	}

	public boolean isViewVisible(String viewName) {
		return true;
	}

	public void showView(String viewName) {

	}

	public void hideView(String viewName) {

	}

	public JToolBar getToolBar() {
		return this.toolBar;
	}

	public NodeEditorStatusBar getStatusBar() {
		return this.statusBar;
	}

	protected void updateTitle() {
		final StringBuffer sb = new StringBuffer();
		sb.append(getModel().getTitle());
		sb.append(" : ");
		if(getCurrentFile() != null)
			sb.append(getCurrentFile().getAbsolutePath());
		else
			sb.append("Untitled");
		if(hasUnsavedChanges()) {
			sb.append("*");
		}
		setWindowName(sb.toString());

		// also update modification status
		setModified(hasUnsavedChanges());
	}

	public boolean chooseFile() {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(this);
		props.setCanCreateDirectories(true);
		props.setFileFilter(new OpgraphFileFilter());
		props.setRunAsync(false);
		props.setTitle("Save graph");

		if(getCurrentFile() != null) {
			final File parentFolder = getCurrentFile().getParentFile();
			final String name = getCurrentFile().getName();

			props.setInitialFolder(parentFolder.getAbsolutePath());
			props.setInitialFile(name);
		} else {
			props.setInitialFolder(getModel().getDefaultFolder());
			props.setInitialFile("Untitled.xml");
		}

		final String saveAs = NativeDialogs.showSaveDialog(props);
		if(saveAs != null) {
			setCurrentFile(new File(saveAs));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean saveData() throws IOException {
		if(!getModel().validate()) return false;
		if(getCurrentFile() == null) {
			if(!chooseFile()) return false;
		}
		OpgraphIO.write(getModel().getDocument().getRootGraph(), getCurrentFile());
		getModel().getDocument().markAsUnmodified();
		updateTitle();
		return true;
	}

	protected void initDockingView() {
		setLayout(new BorderLayout());

		toolBar = new JToolBar();
		setupToolbar();
		add(toolBar, BorderLayout.NORTH);

		dockControl = new CControl(this);

		dockControl.addSingleDockableFactory(new DockableViewFilter(),
				new DockableViewFactory());
		dockControl.createWorkingArea("work");

		add(dockControl.getContentArea(), BorderLayout.CENTER);

		setupDefaultPerspective();
		setupStatusBar();
		add(statusBar, BorderLayout.SOUTH);
	}

	protected void setupStatusBar() {
		statusBar = new NodeEditorStatusBar();
	}

	protected void setupToolbar() {
		toolBar.removeAll();

		toolBar.add(new SaveAction(this));

		toolBar.addSeparator();
		toolBar.add(new MergeNodesAction(this));
		toolBar.add(new ExpandMacroAction(this));

		toolBar.addSeparator();
		toolBar.add(new DistributeNodesAction(this, SwingConstants.HORIZONTAL));
		toolBar.add(new DistributeNodesAction(this, SwingConstants.VERTICAL));
		toolBar.addSeparator();
		toolBar.add(new AlignNodesAction(this, SwingConstants.TOP));
		toolBar.add(new AlignNodesAction(this, SwingConstants.BOTTOM));
		toolBar.add(new AlignNodesAction(this, SwingConstants.LEFT));
		toolBar.add(new AlignNodesAction(this, SwingConstants.RIGHT));

		toolBar.addSeparator();
		toolBar.add(new StartAction(this));
		toolBar.add(new StopAction(this));
		toolBar.add(new StepAction(this));
		toolBar.add(new StepIntoAction(this));
		toolBar.add(new StepOutOfAction(this));
	}

	protected void setupMenu() {
		final MenuBuilder menuBuilder = new MenuBuilder(this.menuBar);

		final JMenu newMenu = menuBuilder.addMenu("File@^", "New");
		// add new actions
		final List<IPluginExtensionPoint<EditorModelInstantiator>> extPts =
			PluginManager.getInstance().getExtensionPoints(EditorModelInstantiator.class);
		for(IPluginExtensionPoint<EditorModelInstantiator> extPt:extPts) {
			final EditorModelInstantiator instantiator = extPt.getFactory().createObject();
			final NewAction act = new NewAction(this, instantiator);
			newMenu.add(new JMenuItem(act));
		}

		menuBuilder.addItem("File@New", new OpenAction(this));
		final JMenu recentsMenu = menuBuilder.addMenu("File@" + OpenAction.TXT, "Recent documents");
		recentsMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				// setup recents menu items
				final RecentFiles recentFiles = new RecentFiles(RECENT_DOCS_PROP);

				recentsMenu.removeAll();
				for(File recentFile:recentFiles) {
					// add menu item for graph file
					final OpenAction openRecentAct = new OpenAction(OpgraphEditor.this, recentFile);
					openRecentAct.putValue(Action.NAME, recentFile.getName());
					openRecentAct.putValue(Action.SHORT_DESCRIPTION, recentFile.getAbsolutePath());
					openRecentAct.putValue(Action.ACCELERATOR_KEY, null);
					recentsMenu.add(new JMenuItem(openRecentAct));
				}

				recentsMenu.addSeparator();
				final JMenuItem clearItem = new JMenuItem("Clear recent documents");
				clearItem.addActionListener( (evt) -> recentFiles.clearHistory() );
				recentsMenu.add(clearItem);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		menuBuilder.addSeparator("File@Recent documents", "sep1");

		menuBuilder.addItem("File@sep1", new SaveAction(this));
		menuBuilder.addItem("File@Save", new SaveAsAction(this));
		menuBuilder.addSeparator("File@Save as...", "sep2");

		menuBuilder.addMenu(".@Edit", "Graph");
		menuBuilder.addItem("Graph", new DeleteAction(this));
		menuBuilder.addSeparator("Graph", "sep1");
		menuBuilder.addItem("Graph", new MergeNodesAction(this));
		menuBuilder.addItem("Graph", new ExpandMacroAction(this));
		menuBuilder.addSeparator("Graph", "sep2");
		menuBuilder.addItem("Graph", new AutoLayoutAction(this));
		menuBuilder.addSeparator("Graph", "sep3");
		menuBuilder.addItem("Graph", new MoveNodeAction(this, 0, GridLayer.DEFAULT_GRID_SPACING / 2));
		menuBuilder.addItem("Graph", new MoveNodeAction(this, 0, -GridLayer.DEFAULT_GRID_SPACING / 2));
		menuBuilder.addItem("Graph", new MoveNodeAction(this, GridLayer.DEFAULT_GRID_SPACING/2, 0));
		menuBuilder.addItem("Graph", new MoveNodeAction(this, -GridLayer.DEFAULT_GRID_SPACING / 2, 0));
		menuBuilder.addSeparator("Graph", "sep4");
		menuBuilder.addItem("Graph", new DistributeNodesAction(this, SwingConstants.HORIZONTAL));
		menuBuilder.addItem("Graph", new DistributeNodesAction(this, SwingConstants.VERTICAL));
		menuBuilder.addSeparator("Graph", "sep5");
		menuBuilder.addItem("Graph", new AlignNodesAction(this, SwingConstants.TOP));
		menuBuilder.addItem("Graph", new AlignNodesAction(this, SwingConstants.BOTTOM));
		menuBuilder.addItem("Graph", new AlignNodesAction(this, SwingConstants.LEFT));
		menuBuilder.addItem("Graph", new AlignNodesAction(this, SwingConstants.RIGHT));

		final JMenu viewMenu = menuBuilder.addMenu(".@Graph", "View");
		viewMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				viewMenu.removeAll();

				viewMenu.add(new ResetViewAction(OpgraphEditor.this));
				viewMenu.addSeparator();

				for(String viewName:getModel().getAvailableViewNames()) {
					final ToggleViewAction viewAct = new ToggleViewAction(OpgraphEditor.this, viewName);
					viewAct.putValue(ToggleViewAction.SELECTED_KEY, isViewVisible(viewName));
					viewMenu.add(new JCheckBoxMenuItem(viewAct));
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		final JMenu nodeMenu = menuBuilder.addMenu(".@Graph", "Node");
		nodeMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				nodeMenu.removeAll();

				// build node menu from menu providers
				final Object context =
						(getModel().getDocument().getSelectionModel().getSelectedNode() != null
								? getModel().getDocument().getSelectionModel().getSelectedNode()
								: getModel().getDocument().getGraph());
				final PathAddressableMenuImpl addressable = new PathAddressableMenuImpl(nodeMenu);
				final ca.gedge.opgraph.app.MenuManager manager = new ca.gedge.opgraph.app.MenuManager();
				for(MenuProvider menuProvider : manager.getMenuProviders())
					menuProvider.installPopupItems(context,
							new MouseEvent(getModel().getCanvas(), -1, System.currentTimeMillis(), 0, 0, 0, 1, true, 1), getModel().getDocument(), addressable);
			}

			@Override
			public void menuDeselected(MenuEvent e) {

			}

			@Override
			public void menuCanceled(MenuEvent e) {

			}
		});

		menuBuilder.addMenu(".@Node", "Debug");
		menuBuilder.addItem("Debug", new StartAction(this));
		menuBuilder.addItem("Debug", new StopAction(this));
		menuBuilder.addSeparator("Debug", "sep1");
		menuBuilder.addItem("Debug", new StepAction(this));
		menuBuilder.addItem("Debug", new StepIntoAction(this));
		menuBuilder.addItem("Debug", new StepOutOfAction(this));
	}

	protected void setupDefaultPerspective() {
		final CControlPerspective perspectives = dockControl.getPerspectives();
		final CPerspective defaultPerspective = perspectives.createEmptyPerspective();

		final CWorkingPerspective workPerspective = (CWorkingPerspective)defaultPerspective.getStation("work");

		for(String viewName:getModel().getAvailableViewNames()) {
			final SingleCDockablePerspective dockablePerspective = new SingleCDockablePerspective(viewName);
			if(getModel().isViewVisibleByDefault(viewName)) {
				final ViewLocation viewLocation = getModel().getDefaultViewLocation(viewName);
				if(viewLocation == ViewLocation.CENTER) {
					final Rectangle bounds = getModel().getInitialViewBounds(viewName);
					workPerspective.gridAdd(bounds.x, bounds.y, bounds.width, bounds.height, dockablePerspective);
				} else if(viewLocation == ViewLocation.NORTH) {
					workPerspective.getPerspective().getContentArea().getNorth().add(dockablePerspective);
				} else if(viewLocation == ViewLocation.EAST) {
					workPerspective.getPerspective().getContentArea().getEast().add(dockablePerspective);
				} else if(viewLocation == ViewLocation.SOUTH) {
					workPerspective.getPerspective().getContentArea().getSouth().add(dockablePerspective);
				} else if(viewLocation == ViewLocation.WEST) {
					workPerspective.getPerspective().getContentArea().getWest().add(dockablePerspective);
				}
			}
		}

		final CGridPerspective center = defaultPerspective.getContentArea().getCenter();
		center.gridAdd( 0, 0, 600, 800, workPerspective );

		defaultPerspective.storeLocations();
		defaultPerspective.shrink();
		perspectives.setPerspective(defaultPerspective, true);
	}

	private final UndoableEditListener undoListener = (e) -> {
		updateTitle();
	};

	private class DockableViewFilter implements Filter<String> {

		@Override
		public boolean includes(String viewName) {
			return (model.getView(viewName) != null);
		}

	}

	private class DockableViewFactory implements SingleCDockableFactory {

		@Override
		public SingleCDockable createBackup(String viewName) {
			final JComponent view = model.getView(viewName);

			final DefaultSingleCDockable retVal = new DefaultSingleCDockable( viewName , view , new CAction[0] );
			retVal.setTitleText(viewName);
			return retVal;
		}

	}

}
