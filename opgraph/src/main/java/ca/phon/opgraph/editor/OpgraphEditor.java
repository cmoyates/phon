package ca.phon.opgraph.editor;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JComponent;
import javax.swing.JMenuBar;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.perspective.CControlPerspective;
import bibliothek.gui.dock.common.perspective.CGridPerspective;
import bibliothek.gui.dock.common.perspective.CPerspective;
import bibliothek.gui.dock.common.perspective.CWorkingPerspective;
import bibliothek.gui.dock.common.perspective.SingleCDockablePerspective;
import bibliothek.util.Filter;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.phon.ui.CommonModuleFrame;

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

	public OpgraphEditor() {
		this(new OpgraphEditorModel());
	}
	
	public OpgraphEditor(OpgraphEditorModel model) {
		super();
		setWindowName("Opgraph Editor");
		this.model = model;
		
		initDockingView();
		addWindowFocusListener(focusListener);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
	}

	public OpgraphEditorModel getModel() {
		return this.model;
	}
	
	public void setModel(OpgraphEditorModel model) {
		this.model = model;
	}

	protected void initDockingView() {
		setLayout(new BorderLayout());
		dockControl = new CControl(this);
		add(dockControl.getContentArea(), BorderLayout.CENTER);
			
		setupDefaultPerspective();
		setupMenu();
	}
	
	protected void setupMenu() {
		GraphEditorModel.setActiveEditorModel(model);
		this.menuBar.removeAll();
		final PathAddressableMenu pmenu = 
				new PathAddressableMenuImpl(this.menuBar);
		for(MenuProvider menuProvider:model.getMenuProviders()) {
			menuProvider.installItems(model, pmenu);
		}
	}
	
	protected void setupDefaultPerspective() {
		dockControl.addSingleDockableFactory(new DockableViewFilter(), 
				new DockableViewFactory());
		dockControl.createWorkingArea("work");
		
		final CControlPerspective perspectives = dockControl.getPerspectives();
		final CPerspective defaultPerspective = perspectives.createEmptyPerspective();
		
		defaultPerspective.getContentArea().getWest().add(
				new SingleCDockablePerspective("Library"));
		
		final CWorkingPerspective workPerspective = (CWorkingPerspective)defaultPerspective.getStation("work");
		workPerspective.gridAdd( 0, 0, 600, 600, new SingleCDockablePerspective("Canvas"));
		workPerspective.gridAdd( 600, 0, 200, 200, new SingleCDockablePerspective("Settings"));
		workPerspective.gridAdd( 600, 200, 200, 200, new SingleCDockablePerspective("Defaults"));
		
		final CGridPerspective center = defaultPerspective.getContentArea().getCenter();
		center.gridAdd(0, 0, 600, 800, workPerspective);
		
		defaultPerspective.storeLocations();
		defaultPerspective.shrink();
		perspectives.setPerspective(defaultPerspective, true);
	}
	
	private final WindowFocusListener focusListener = new WindowFocusListener() {
		
		@Override
		public void windowLostFocus(WindowEvent e) {
			
		}
		
		@Override
		public void windowGainedFocus(WindowEvent e) {
			GraphEditorModel.setActiveEditorModel(model);
		}
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
	
	public static void main(String[] args) {
		OpgraphEditor editor = new OpgraphEditor();
		editor.pack();
		editor.setVisible(true);
	}
	
}
