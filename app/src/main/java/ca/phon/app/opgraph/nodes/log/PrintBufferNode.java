package ca.phon.app.opgraph.nodes.log;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.BufferWindow;
import ca.phon.formatter.Formatter;
import ca.phon.formatter.FormatterFactory;

@OpNodeInfo(
		name="Print to Buffer",
		category="Report",
		description="Print given data to the buffer specified in settings.",
		showInLibrary=true
)
public class PrintBufferNode extends OpNode implements NodeSettings {
	
	private InputField dataField =
			new InputField("data", "Data to print", false, true, Object.class);
	
	private InputField bufferNameField = 
			new InputField("buffer", "Buffer name", true, true, String.class);
	
	private InputField appendField = 
			new InputField("append", "Append to buffer", true, true, Boolean.class);
	
	public PrintBufferNode() {
		super();
		
		putField(dataField);
		putField(bufferNameField);
		putField(appendField);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Object data = context.get(dataField);
		if(data == null) throw new ProcessingException(null, "Data cannot be null");
		
		final String bufferName = getBufferName(context);
		final boolean append = isAppendToBuffer(context);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Runnable onEDT = () -> {
			final BufferWindow bufferWindow = BufferWindow.getInstance();
			
			BufferPanel bufferPanel = bufferWindow.getBuffer(bufferName);
			if(bufferPanel == null) {
				bufferPanel = bufferWindow.createBuffer(bufferName);
			}
			
			if(!append) {
				bufferPanel.getLogBuffer().setText("");
			}

			StringBuffer bufferValue = new StringBuffer(bufferPanel.getLogBuffer().getText());
			
			if(data instanceof String) {
				bufferValue.append((String)data);
			} else {
				final Formatter formatter = FormatterFactory.createFormatter(data.getClass());
				if(formatter == null) {
					bufferValue.append(data.toString());
				} else {
					bufferValue.append(formatter.format(data));
				}
			}
			
			bufferPanel.getLogBuffer().setText(bufferValue.toString());
			
			if(!bufferWindow.isVisible()) {
				bufferWindow.setSize(600, 800);
				bufferWindow.centerWindow();
				bufferWindow.setVisible(true);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			onEDT.run();
		else {
			try {
				SwingUtilities.invokeAndWait(onEDT);
			} catch (InvocationTargetException | InterruptedException e) {
				throw new ProcessingException(null, e);
			}
		}
	}

	public String getBufferName(OpContext ctx) {
		return (ctx.containsKey(bufferNameField) ? ctx.get(bufferNameField).toString() : "default");
	}
	
	public boolean isAppendToBuffer(OpContext ctx) {
		return (ctx.containsKey(appendField) ? (Boolean)ctx.get(appendField) : false);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		return null;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}
	
}
