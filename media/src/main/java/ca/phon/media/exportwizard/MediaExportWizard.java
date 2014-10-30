/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

package ca.phon.media.exportwizard;

import java.awt.BorderLayout;
import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.phon.media.FFMpegMediaExporter;
import ca.phon.ui.PhonLoggerConsole;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.MsFormatter;
import ca.phon.worker.PhonWorker;

/**
 * Wizard for exporting a segment of media.
 */
public class MediaExportWizard extends WizardFrame {
	
	private static final Logger LOGGER = Logger
			.getLogger(MediaExportWizard.class.getName());

	private Map<MediaExportWizardProp, Object> wizardProps;
	
	/* Steps */
	private ExportSetupStep setupStep;

	private PhonLoggerConsole exportConsole;
	private WizardStep exportStep;

	public MediaExportWizard() {
		super("Export Media");

		wizardProps = new HashMap<MediaExportWizardProp, Object>();

		init();
	}

	public MediaExportWizard(Map<MediaExportWizardProp, Object> props) {
		super("Export Media");

		wizardProps = props;

		init();
	}

	private void init() {
		
		super.btnCancel.setText("Close");
		super.btnFinish.setVisible(false);
		
		setupStep = new ExportSetupStep(wizardProps);
		setupStep.setNextStep(1);
		addWizardStep(setupStep);

		exportConsole = new PhonLoggerConsole();
		final JScrollPane scroller = new JScrollPane(exportConsole);

		JPanel exportPanel = new JPanel(new BorderLayout());
		DialogHeader exportHeader = new DialogHeader("Export media", "Export media using ffmpeg");
		exportPanel.add(exportHeader, BorderLayout.NORTH);
		exportPanel.add(scroller, BorderLayout.CENTER);

		exportStep = super.addWizardStep(exportPanel);
		exportStep.setPrevStep(0);
	}

	@Override
	public void next() {
		if(getCurrentStep() == setupStep) {
			// create exporter(s) and start thread
			FFMpegMediaExporter exporter =
					new FFMpegMediaExporter();
			exporter.setInputFile(setupStep.getInputFileLabel().getFile().getAbsolutePath());
			exporter.setOutputFile(setupStep.getOutputFileLabel().getFile().getAbsolutePath());
			
			if(exporter.getInputFile().equals(exporter.getOutputFile())) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(this);
				props.setTitle("Media export");
				props.setHeader("Failed to export media");
				props.setMessage("Source and destination file are the same.");
				props.setRunAsync(false);
				props.setOptions(MessageDialogProperties.okOptions);
				NativeDialogs.showMessageDialog(props);
				return;
			}

			if((new File(exporter.getOutputFile()).exists())) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(this);
				props.setTitle("Media export");
				props.setHeader("Overwrite file?");
				props.setMessage("Overwrite file " + exporter.getOutputFile() + "?");
				props.setRunAsync(false);
				props.setOptions(MessageDialogProperties.yesNoOptions);
				int retVal = NativeDialogs.showMessageDialog(props);
				if(retVal != 0) 
					return;
			}
			
			exporter.setIncludeVideo(setupStep.getEncodeVideoBox().isSelected());
			exporter.setVideoCodec(setupStep.getVideoCodecField().getText());

			exporter.setIncludeAudio(setupStep.getEncodeAudioBox().isSelected());
			exporter.setAudioCodec(setupStep.getAudioCodecField().getText());

			if(setupStep.getPartialExtractBox().isSelected()) {
				String segment = setupStep.getSegmentField().getText();

				String vals[] = segment.split("-");
				try {
					long startTime = MsFormatter.displayStringToMs(vals[0]);
					long endTime = MsFormatter.displayStringToMs(vals[1]);
					long duration = (endTime-startTime);
	
					if(duration > 0L) {
						exporter.setStartTime(startTime);
						exporter.setDuration(duration);
					}
				} catch (ParseException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			exporter.setOtherArgs(setupStep.getOtherArgsField().getText());

			PhonWorker worker = PhonWorker.createWorker();
			worker.setFinishWhenQueueEmpty(true);

			exportConsole.addLogger(Logger.getLogger("ca.phon"));
//			exportConsole.addReportThread(worker);
//			exportConsole.startLogging();
			
			worker.invokeLater(exporter);
			worker.start();

			super.next();
		}
	}
	
}
