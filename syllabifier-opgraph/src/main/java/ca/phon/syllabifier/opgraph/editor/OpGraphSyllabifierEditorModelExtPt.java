package ca.phon.syllabifier.opgraph.editor;

import ca.gedge.opgraph.OpGraph;
import ca.phon.opgraph.editor.OpgraphEditorModel;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(author="Greg J. Hedlund <ghedlund@mun.ca>", minPhonVersion="2.1.0", name="Syllabifier Opgraph Editor Model")
public class OpGraphSyllabifierEditorModelExtPt implements IPluginExtensionPoint<OpgraphEditorModel> {

	@Override
	public Class<?> getExtensionType() {
		return OpgraphEditorModel.class;
	}

	@Override
	public IPluginExtensionFactory<OpgraphEditorModel> getFactory() {
		final IPluginExtensionFactory<OpgraphEditorModel> factory = (Object ... args) -> {
			if(args.length == 1 && args[0] instanceof OpGraph) {
				final OpGraph graph = (OpGraph)args[0];
				return new OpGraphSyllabifierEditorModel(graph);
			} else {
				return new OpGraphSyllabifierEditorModel();
			}
		};
		return factory;
	}

}
