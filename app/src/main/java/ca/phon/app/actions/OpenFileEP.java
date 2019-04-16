package ca.phon.app.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginEntryPoint;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;

public class OpenFileEP extends HookableAction implements IPluginEntryPoint {

	public static String EP_NAME = "Open";
	
	public static String DESC = "Open file on disk...";
	
	public OpenFileEP() {
		super();
		
		putValue(NAME, EP_NAME + "...");
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		SwingUtilities.invokeLater( () -> {
			ActionEvent ae = new ActionEvent(null, 0, EP_NAME);
			hookableActionPerformed(ae);
		});
	}
	
	private FileFilter createFileFilter() {
		Set<String> supportedExtensions = new LinkedHashSet<>();
		
		List<IPluginExtensionPoint<OpenFileHandler>> fileHandlers = PluginManager.getInstance().getExtensionPoints(OpenFileHandler.class);
		for(var extPt:fileHandlers) {
			OpenFileHandler handler = extPt.getFactory().createObject();
			supportedExtensions.addAll(handler.supportedExtensions());
		}
		
		String extensions = supportedExtensions.stream().collect(Collectors.joining(";"));
		FileFilter retVal = new FileFilter("Supported files", extensions);
		return retVal;
	}
	
	public void dialogFinished(NativeDialogEvent evt) {
		if(evt.getDialogResult() != NativeDialogEvent.OK_OPTION) return;
		String selectedFile = evt.getDialogData().toString();
		openFile(new File(selectedFile));
	}
	
	public void openFile(File file) {
		List<IPluginExtensionPoint<OpenFileHandler>> fileHandlers = PluginManager.getInstance().getExtensionPoints(OpenFileHandler.class);
		for(var extPt:fileHandlers) {
			OpenFileHandler handler = extPt.getFactory().createObject();
			
			boolean canOpen = false;
			try {
				canOpen = handler.canOpen(file);
			} catch (IOException e) {
				// ignore
			}
			if(canOpen) {
				try {
					handler.openFile(file);
					break;
				} catch (IOException e) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(e);
				}
			}
		}
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(false);
		props.setCanCreateDirectories(false);
		props.setCanChooseFiles(true);
		props.setFileFilter(createFileFilter());
		props.setRunAsync(true);
		props.setListener(this::dialogFinished);
		
		NativeDialogs.showOpenDialog(props);
	}

}
