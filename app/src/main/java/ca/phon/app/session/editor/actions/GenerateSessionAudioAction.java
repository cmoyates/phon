package ca.phon.app.session.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.EditorEvent;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.media.export.VLCWavExporter;
import ca.phon.media.util.MediaLocator;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonWorker;

/**
 * Generate session audio file
 * 
 */
public class GenerateSessionAudioAction extends SessionEditorAction {

	private final static String TXT = "Generate/re-encode session audio";
	
	private final static String DESC = "Generate or re-encode session audio file";
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("misc/oscilloscope", IconSize.SMALL);
	
	private final static String ORIGINAL_FILE_SUFFIX = "-orig";
	
	private final static String WAV_EXT = ".wav";
	
	private final Collection<PhonTaskListener> customListeners = new ArrayList<>();
	
	public GenerateSessionAudioAction(SessionEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(SMALL_ICON, ICON);
	}
	
	public void addTaskListener(PhonTaskListener listener) {
		customListeners.add(listener);
	}
	
	public void removeTaskListener(PhonTaskListener listener) {
		customListeners.remove(listener);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		PhonTask exportTask = generateExportAudioTask();
		if(exportTask != null) {
			getEditor().getStatusBar().watchTask(exportTask);
			
			PhonWorker worker = PhonWorker.createWorker();
			worker.setName("Generate session audio");
			worker.setFinishWhenQueueEmpty(true);
			worker.invokeLater(exportTask);
			worker.start();
		}
	}
	
	/**
	 * Provide access to the internal task for generating
	 * session audio file.
	 * 
	 */
	public PhonTask generateExportAudioTask() {
		PhonTaskListener taskListener = new PhonTaskListener() {

			@Override
			public void statusChanged(PhonTask task, TaskStatus oldStatus,
					TaskStatus newStatus) {
				if(newStatus == TaskStatus.FINISHED) {
					getEditor().getMediaModel().resetAudioCheck();

					if(getEditor().getMediaModel().isSessionAudioAvailable()) {
						// tell the editor session audio is now available
						EditorEvent ee = new EditorEvent(SessionMediaModel.SESSION_AUDIO_AVAILABLE, GenerateSessionAudioAction.this, 
								getEditor().getMediaModel().getSessionAudioFile());
						getEditor().getEventManager().queueEvent(ee);
					}
				}
			}

			@Override
			public void propertyChanged(PhonTask task, String property,
					Object oldValue, Object newValue) {
			}

		};
		
		PhonTask exportTask = generateAudioFileTask();
		if(exportTask != null) {
			exportTask.addTaskListener(taskListener);
			customListeners.forEach(exportTask::addTaskListener);
		}
		
		return exportTask;
	}
	
	/**
	 * Create, if possible, a new task for generating session audio file.
	 *  
	 * @return the new task or <code>null</code> if task creation failed.
	 */
	private PhonTask generateAudioFileTask() {
		final Session session = getEditor().getSession();
		final SessionMediaModel mediaModel = getEditor().getMediaModel();
		
		if(mediaModel.isSessionMediaAvailable()) {
			File movFile = MediaLocator.findMediaFile(
				session.getMediaLocation(), getEditor().getProject(), session.getCorpus());
			int lastDot = movFile.getName().lastIndexOf(".");
			if(lastDot > 0) {
				String audioFileName = movFile.getName().substring(0, lastDot) + WAV_EXT;
				File parentFile = movFile.getParentFile();
				File resFile = new File(parentFile, audioFileName);
				String movExt = movFile.getName().substring(lastDot);

				if(movExt.equalsIgnoreCase(WAV_EXT)) {
					String originalFileName = movFile.getName().substring(0, movFile.getName().lastIndexOf('.')) + 
							ORIGINAL_FILE_SUFFIX + movExt;
					final File originalFile = new File(movFile.getParentFile(), originalFileName);
					// already a wav, do nothing!
					int selectedOption = 
							getEditor().showMessageDialog("Re-encode wav", "Original file will be renamed " + 
									originalFileName, MessageDialogProperties.okCancelOptions);
					if(selectedOption == 1) {
						return null;
					}
					if(!movFile.renameTo(originalFile)) {
						LogUtil.warning("Unable to rename media to " + originalFile.getAbsolutePath());
						return null;
					} else {
						// ensure originalFile exists
						if(!originalFile.exists()) {
							LogUtil.warning("Unable to move original media to " + originalFile.getAbsolutePath());
							return null;
						}
						movFile = originalFile;
					}
				}

				if(resFile.exists()) {
					// ask to overwrite
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setParentWindow(CommonModuleFrame.getCurrentFrame());
					props.setTitle("Generate Wav");
					props.setHeader("Overwrite file?");
					props.setMessage("Wav file already exists, overwrite?");
					props.setRunAsync(false);
					props.setOptions(MessageDialogProperties.yesNoOptions);
					int retVal = NativeDialogs.showMessageDialog(props);
					if(retVal != 0) return null;
				}
				
				final VLCWavExporter exporter = new VLCWavExporter(movFile, resFile);
				return exporter;
			}
		}
		return null;
	}

}
