/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.session.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.Rank;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.PrefHelper;

/**
 * Check IPA transcriptions for a session. 
 *
 */
@PhonPlugin(name="Check IPA Transcriptions", comments="Check IPA transcriptions and optionally reset syllabification")
@Rank(1)
public class CheckTranscripts implements SessionCheck, IPluginExtensionPoint<SessionCheck> {
	
	public final static String RESET_SYLLABIFICATION = CheckTranscripts.class.getName() + ".resetSyllabification";
	public final static boolean DEFAULT_RESET_SYLLABIFICATION = false;
	private boolean resetSyllabification = 
			PrefHelper.getBoolean(RESET_SYLLABIFICATION, DEFAULT_RESET_SYLLABIFICATION);
	
	public final static String SYLLABIFIER_LANG = CheckTranscripts.class.getName() + ".syllabifierLang";
	public final static String DEFAULT_SYLLABIFIER_LANG = SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString();
	private String syllabifierLang = PrefHelper.get(SYLLABIFIER_LANG, DEFAULT_SYLLABIFIER_LANG);
	
	public CheckTranscripts() {
		super();
	}
	
	public boolean isResetSyllabification() {
		return resetSyllabification;
	}

	public void setResetSyllabification(boolean resetSyllabification) {
		this.resetSyllabification = resetSyllabification;
	}

	public String getSyllabifierLang() {
		return syllabifierLang;
	}

	public void setSyllabifierLang(String syllabifierLang) {
		this.syllabifierLang = syllabifierLang;
	}

	@Override
	public boolean checkSession(SessionValidator validator, Session session) {
		boolean modified = false;
		Syllabifier syllabifier = null;
		if(isResetSyllabification()) {
			syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(getSyllabifierLang());
		}
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			for(Tier<IPATranscript> tier:r.getTiersOfType(IPATranscript.class)) {
				for(int gIdx = 0; gIdx < tier.numberOfGroups(); gIdx++) {
					final IPATranscript ipa = tier.getGroup(gIdx);
					final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
					if(uv != null) {
						// error in this transcription
						final ValidationEvent ve = new ValidationEvent(session, i, tier.getName(), gIdx,
								uv.getParseError().getMessage());
						validator.fireValidationEvent(ve);
					} else {
						if(isResetSyllabification() && syllabifier != null) {
							String prev = ipa.toString(true);
							ipa.resetSyllabification();
							syllabifier.syllabify(ipa.toList());
							
							boolean changed = !prev.equals(ipa.toString(true));
							
							if(changed) {
								ValidationEvent evt = new ValidationEvent(session, i, tier.getName(), gIdx,
										String.format("Reset syllabification (%s)", syllabifier.getName()));
								validator.fireValidationEvent(evt);
							}
							
							modified |= changed;
						}
					}
				}
			}
		}
		return modified;
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (Object ... args) -> this;
	}

	@Override
	public Properties getProperties() {
		Properties props = new Properties();
		
		props.put(RESET_SYLLABIFICATION, Boolean.toString(isResetSyllabification()));
		props.put(SYLLABIFIER_LANG, getSyllabifierLang());
		
		return props;
	}

	@Override
	public void loadProperties(Properties props) {
		if(props.containsKey(RESET_SYLLABIFICATION))
			setResetSyllabification(Boolean.parseBoolean(props.getProperty(RESET_SYLLABIFICATION)));
		setSyllabifierLang(props.getProperty(SYLLABIFIER_LANG, DEFAULT_SYLLABIFIER_LANG));
	}

}
