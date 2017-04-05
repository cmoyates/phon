/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.components.GraphOutline;
import ca.gedge.opgraph.app.components.OpGraphTreeModel;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;
import ca.phon.app.opgraph.nodes.PhonNodeLibrary;
import ca.phon.app.opgraph.wizard.GraphOutlineExtension;
import ca.phon.app.opgraph.wizard.NodeWizardReportTemplate;
import ca.phon.app.opgraph.wizard.ReportTemplateView;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.NodeWizardOptionalsEdit;
import ca.phon.app.opgraph.wizard.edits.NodeWizardSettingsEdit;
import ca.phon.app.project.ParticipantsPanel;
import ca.phon.project.Project;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.workspace.Workspace;

public class AnalysisOpGraphEditorModel extends OpgraphEditorModel {

	private JPanel debugSettings;

	private ReportTemplateView reportTemplateView;

	private JComboBox<Project> projectList;

	private ParticipantsPanel participantSelector;

	private AnalysisWizardExtension wizardExt;

	private final static Logger LOGGER = Logger.getLogger(AnalysisOpGraphEditorModel.class.getName());

	public AnalysisOpGraphEditorModel() {
		this(new OpGraph());
	}

	public AnalysisOpGraphEditorModel(OpGraph opgraph) {
		super(opgraph);

		WizardExtension ext = opgraph.getExtension(WizardExtension.class);
		if(ext != null && !(ext instanceof AnalysisWizardExtension)) {
			throw new IllegalArgumentException("Graph is not an analysis document.");
		}
		if(ext == null) {
			ext = new AnalysisWizardExtension(opgraph);
			opgraph.putExtension(WizardExtension.class, ext);
		}
		wizardExt = (AnalysisWizardExtension)ext;

		init();
	}

	private void init() {
		PhonNodeLibrary.install(getNodeLibrary().getLibrary());
		GraphOutlineExtension.install(getDocument(), getGraphOutline(), getWizardExtension());

		getDocument().getUndoSupport().addUndoableEditListener( (e) -> {
			final UndoableEdit edit = e.getEdit();
			if(edit instanceof AddNodeEdit) {
				final WizardExtension graphExtension = getWizardExtension();

				final OpNode addedNode = ((AddNodeEdit)edit).getNode();
				if(addedNode instanceof CompositeNode) {
					final OpGraph addedGraph = ((CompositeNode)addedNode).getGraph();

					final WizardExtension wizardExt = addedGraph.getExtension(WizardExtension.class);
					if(wizardExt != null && wizardExt instanceof AnalysisWizardExtension) {
						final OpGraph parentGraph = getDocument().getGraph();

						// attempt to setup links for project, selected session and selected participants
						final OpNode parentProjectNode = parentGraph.getNodesByName("Project").stream().findFirst().orElse(null);
						final OpNode parentSessionsNode = parentGraph.getNodesByName("Selected Sessions").stream().findFirst().orElse(null);
						final OpNode parentParticipantsNode = parentGraph.getNodesByName("Selected Participants").stream().findFirst().orElse(null);

						if(parentProjectNode != null) {
							try {
								final OpLink projectLink =
										new OpLink(parentProjectNode, "obj", addedNode, "project");
								parentGraph.add(projectLink);
							} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e1) {
								LOGGER.log(Level.WARNING, e1.getLocalizedMessage(), e1);
							}
						}

						if(parentSessionsNode != null) {
							try {
								final OpLink sessionsLink =
										new OpLink(parentSessionsNode, "obj", addedNode, "selectedSessions");
								parentGraph.add(sessionsLink);
							} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e1) {
								LOGGER.log(Level.WARNING, e1.getLocalizedMessage(), e1);
							}
						}

						if(parentParticipantsNode != null) {
							try {
								final OpLink participantsLink =
										new OpLink(parentParticipantsNode, "obj", addedNode, "selectedParticipants");
								parentGraph.add(participantsLink);
							} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e1) {
								LOGGER.log(Level.WARNING, e1.getLocalizedMessage(), e1);
							}
						}

						final AnalysisWizardExtension analysisExt = (AnalysisWizardExtension)wizardExt;

						for(OpNode node:analysisExt) {
							graphExtension.addNode(node);
							graphExtension.setNodeForced(node, analysisExt.isNodeForced(node));

							String nodeTitle = analysisExt.getWizardTitle();
							if(analysisExt.getNodeTitle(node).trim().length() > 0) {
								nodeTitle +=  " " + analysisExt.getNodeTitle(node);
							} else {
								nodeTitle += ( node.getName().equals("Parameters") || node.getName().equals(analysisExt.getWizardTitle()) ? "" : " " + node.getName());
							}
							graphExtension.setNodeTitle(node, nodeTitle);
						}

						for(OpNode optionalNode:analysisExt.getOptionalNodes()) {
							graphExtension.addOptionalNode(optionalNode);
							graphExtension.setOptionalNodeDefault(optionalNode, analysisExt.getOptionalNodeDefault(optionalNode));
						}

						// copy report template
						final NodeWizardReportTemplate prefixTemplate = graphExtension.getReportTemplate("Report Prefix");
						final NodeWizardReportTemplate suffixTemplate = graphExtension.getReportTemplate("Report Suffix");
						final NodeWizardReportTemplate pt =
								analysisExt.getReportTemplate("Report Prefix");
						if(pt != null) {
							if(!prefixTemplate.getTemplate().contains(pt.getTemplate())) {
								prefixTemplate.setTemplate(prefixTemplate.getTemplate() + "\n" + pt.getTemplate());
							}
						}

						final NodeWizardReportTemplate st =
								analysisExt.getReportTemplate("Report Suffix");
						if(st != null) {
							if(!suffixTemplate.getTemplate().contains(st.getTemplate())) {
								suffixTemplate.setTemplate(suffixTemplate.getTemplate() + "\n" + st.getTemplate());
							}
						}
					}
				}
			}
		});
	}

	public ParticipantsPanel getParticipantSelector() {
		return this.participantSelector;
	}

	protected WizardExtension getWizardExtension() {
		return getDocument().getRootGraph().getExtension(WizardExtension.class);
	}

	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("analysis", "analyses");
	}

	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Report Template", getReportTemplateView());
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}

	protected JComponent getReportTemplateView() {
		if(reportTemplateView == null) {
			reportTemplateView = new ReportTemplateView(getDocument());
		}
		return reportTemplateView;
	}

	protected JComponent getDebugSettings() {
		if(debugSettings == null) {
			debugSettings = new JPanel();

			final Workspace workspace = Workspace.userWorkspace();
			projectList = new JComboBox<Project>(workspace.getProjects().toArray(new Project[0]));
			projectList.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Project"),
					projectList.getBorder()));

			projectList.addItemListener( (e) -> {
				participantSelector.setProject((Project)projectList.getSelectedItem());
			} );

			participantSelector = new ParticipantsPanel();
			final JScrollPane sessionScroller = new JScrollPane(participantSelector);
			sessionScroller.setBorder(BorderFactory.createTitledBorder("Sessions & Participants"));

			debugSettings.setLayout(new BorderLayout());
			debugSettings.add(projectList, BorderLayout.NORTH);
			debugSettings.add(sessionScroller, BorderLayout.CENTER);
		}
		return debugSettings;
	}

	@Override
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = new Rectangle();
		switch(viewName) {
		case "Canvas":
			retVal.setBounds(200, 0, 600, 600);
			break;

		case "Debug Settings":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Report Template":
			retVal.setBounds(0, 0, 200, 200);
			break;

		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Debug":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Connections":
			retVal.setBounds(800, 200, 200, 200);
			break;

		case "Library":
			retVal.setBounds(0, 0, 200, 200);
			break;

		case "Settings":
			retVal.setBounds(800, 0, 200, 200);
			break;

		default:
			retVal.setBounds(0, 0, 200, 200);
			break;
		}
		return retVal;
	}

	@Override
	public boolean isViewVisibleByDefault(String viewName) {
		return super.isViewVisibleByDefault(viewName)
				|| viewName.equals("Debug Settings")
				|| viewName.equals("Report Template");
	}

	@Override
	public String getDefaultFolder() {
		return UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER;
	}

	@Override
	public String getTitle() {
		return "Composer (Analysis)";
	}

	@Override
	public boolean validate() {
		return super.validate();
	}

	@Override
	public void setupContext(OpContext context) {
		super.setupContext(context);

		context.put("_project", projectList.getSelectedItem());
		context.put("_selectedSessions", participantSelector.getSessionSelector().getSelectedSessions());
		context.put("_selectedParticipants", participantSelector.getParticipantSelector().getSelectedParticpants());
		context.put("_buffers", new MultiBufferPanel());
	}

}
