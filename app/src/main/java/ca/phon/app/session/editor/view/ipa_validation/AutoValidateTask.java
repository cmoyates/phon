/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.session.editor.view.ipa_validation;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.SyllabifierInfo;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;

/**
 * Auto-validate ipa fields for a session.
 * 
 * How data is validated:
 * 
 *  - if only one user has a transcription entered that transcription will be used
 *  - if all transcriptions are the same, the transcription is used
 *  - if transcriptions are different the transcriptions from the preferred transcriber
 *    (by default the first transcriber in the list) is used
 *
 */
public class AutoValidateTask extends ca.phon.worker.PhonTask {
	
	/**
	 * Include ipa target?
	 */
	private boolean validateTarget = true;
	
	/**
	 * Include ipa actual?
	 */
	private boolean validateActual = true;
	
	/**
	 * Overwrite data?
	 */
	private boolean overwriteData = false;
	
	/**
	 * Session
	 */
	private Session session;
	
	/**
	 * Preferred transcriber
	 */
	private Transcriber preferredTranscriber = null;
	
	/**
	 * Utterance filter
	 * 
	 */
	private RecordFilter recordFilter = null;
	
	/**
	 * Undoable edit
	 */
	private UndoableEdit undoableEdit = null;
	
	public AutoValidateTask(Session t) {
		this(t, null);
	}
	
	public AutoValidateTask(Session t, Transcriber tr) {
		super("Auto validate session");
		this.session = t;
		this.preferredTranscriber = tr;
	}
	
	public Transcriber getPreferredTranscriber() {
		Transcriber retVal = preferredTranscriber;
		
		if(retVal == null && session != null) {
			if(session.getTranscriberCount() > 0) {
				retVal = session.getTranscriber(0);
				preferredTranscriber = retVal;
			}
		}
		
		return retVal;
	}

	public void setPreferredTranscriber(Transcriber t) {
		this.preferredTranscriber = t;
	}
	
	/**
	 * Validate the given tier for a record
	 */
	private UndoableEdit validateIPA(Tier<IPATranscript> tier) {
		final CompoundEdit retVal = new CompoundEdit();
		
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			final IPATranscript grp = tier.getGroup(i);
			final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
			
			if(alts == null) continue;
			
			if(!overwriteData && grp.length() > 0) {
					continue;
			}
			
			IPATranscript setV = null;
			for(Transcriber t:session.getTranscribers()) {
				final IPATranscript ipa = alts.get(t.getUsername());
				
				if(ipa != null && ipa.length() > 0) {
					if(setV == null) {
						setV = ipa;
					} else {
						if(!setV.toString().equals(ipa.toString())) {
							final IPATranscript alt = alts.get(getPreferredTranscriber().getUsername());
							if(alt != null) {
								setV = alt;
							}
							break;
						}
					}
				}
			}
			
			if(setV != null) {
				setV.putExtension(AlternativeTranscript.class, alts);
				final SyllabifierInfo info = session.getExtension(SyllabifierInfo.class);
				final SyllabifierLibrary syllabifierLibrary = SyllabifierLibrary.getInstance();
				final Language lang = (info != null && info.getSyllabifierLanguageForTier(tier.getName()) != null ?
						info.getSyllabifierLanguageForTier(tier.getName()) : syllabifierLibrary.defaultSyllabifierLanguage());
				final Syllabifier syllabifier = syllabifierLibrary.getSyllabifierForLanguage(lang);
				if(syllabifier != null) {
					syllabifier.syllabify(setV.toList());
				}
				
				final TierEdit<IPATranscript> tierEdit = new TierEdit<IPATranscript>(null, tier, i, setV);
				tierEdit.doIt();
				retVal.addEdit(tierEdit);
			}
		}
		
		retVal.end();
		return retVal;
	}
	
	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		final CompoundEdit undoableEdit = new CompoundEdit();
		
		for(Record utt:session.getRecords()) {
			if(recordFilter != null && recordFilter.checkRecord(utt)) {
				if(validateTarget) {
					undoableEdit.addEdit(validateIPA(utt.getIPATarget()));
				}
				if(validateActual) {
					undoableEdit.addEdit(validateIPA(utt.getIPAActual()));
				}
			}
		}
		
		undoableEdit.end();
		this.undoableEdit = undoableEdit;
		super.setStatus(TaskStatus.FINISHED);
	}
	
	public UndoableEdit getUndoableEdit() {
		return this.undoableEdit;
	}

	public boolean isValidateTarget() {
		return validateTarget;
	}

	public void setValidateTarget(boolean validateTarget) {
		this.validateTarget = validateTarget;
	}

	public boolean isValidateActual() {
		return validateActual;
	}

	public void setValidateActual(boolean validateActual) {
		this.validateActual = validateActual;
	}

	public boolean isOverwriteData() {
		return overwriteData;
	}

	public void setOverwriteData(boolean overwriteData) {
		this.overwriteData = overwriteData;
	}

	public RecordFilter getRecordFilter() {
		return recordFilter;
	}

	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}

}
