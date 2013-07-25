package ca.phon.syllabifier.opgraph.nodes;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.ParseException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.ipa.IPATranscript;

/**
 * IPA source node for the syllabifier.
 *
 */
@OpNodeInfo(
		name="IPA Source",
		description="IPA source node for syllabifier and stages.",
		category="Syllabifier")
public class IPASourceNode extends OpNode implements NodeSettings {
	
	// context value
	private final static String IPA_KEY = "__ipa__";
	
	// single output
	private final OutputField ipaOut = 
			new OutputField("ipa", "ipa source", true, IPATranscript.class);
	
	public IPASourceNode() {
		super();
		putField(ipaOut);
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		// map context value to output
		if(getIpa() != null) {
			IPATranscript ipa;
			try {
				ipa = IPATranscript.parseTranscript(getIpa());
				context.put(ipaOut, ipa);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (context.containsKey(IPA_KEY)) {
			context.put(ipaOut, (IPATranscript)context.get(IPA_KEY));
		}
	}
	
	public String getIpa() {
		String retVal = null;
		if(settings != null && settings.ipaArea.getText().length() > 0) {
			retVal = settings.ipaArea.getText();
		}
		return retVal;
	}

	private final Settings settings = new Settings();
	@Override
	public Component getComponent(GraphDocument document) {
		return settings;
	}

	// don't save any settings
	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
	}
	
	/**
	 * Settings panel
	 */
	private class Settings extends JPanel {
		
		private static final long serialVersionUID = -2070336588978115879L;
		
		private final JTextArea ipaArea = new JTextArea();
		
		public Settings() {
			super();
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			add(new JLabel("Enter IPA:"), BorderLayout.NORTH);
			add(new JScrollPane(ipaArea), BorderLayout.CENTER);
		}
		
	}

}
