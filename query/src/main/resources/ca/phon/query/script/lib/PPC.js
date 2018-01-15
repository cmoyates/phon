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
/*
 * PCC/PVC Library functions for query scripts
 */
importPackage(Packages.ca.phon.ipa.features)

var PatternFilter = require("lib/PatternFilter").PatternFilter;
var PatternType = require("lib/PatternFilter").PatternType;

exports.PPC = {
	/**
	 * Perform PPC (aligned) calculation for an aligned pair of
	 * IPA values
	 *
	 * @param word
	 * @param features
	 * @param ignoreDiacritics
	 *
	 * @return {
	target: numTarget,
	correct: numCorrect,
	deleted: numDeleted,
	epen: numEpenthesized
	};
	 */
	calc_ppc_aligned: function (group, filter, ignoreDiacritics) {
		var numTarget = 0;
		var numDeleted = 0;
		var numActual = 0;
		var numSubstituted = 0;
		var numEpenthesized = 0;
		var numCorrect = 0;

		var targetGroup = (group.getIPATarget() == null ? new IPATranscript(): group.getIPATarget());
		var actualGroup = (group.getIPAActual() == null ? new IPATranscript(): group.getIPAActual());
		var alignment = group.getPhoneAlignment();

		// check target side for numTarget, numDeleted and numCorrect
		for (pIdx = 0; pIdx < targetGroup.length();
		pIdx++) {
			var phone = targetGroup.elementAt(pIdx);
			var testIPA = (new IPATranscriptBuilder()).append(phone).toIPATranscript();

			if (filter.check_filter(testIPA)) {
				numTarget++;

				// check aligned phone
				var alignedData = alignment[ "getAligned(java.lang.Iterable)"]([phone]);
				if (alignedData.size() > 0) {
					var actualPhone = alignedData.get(0);
					if (actualPhone != null) {
						var targetPhoneString =
						(ignoreDiacritics ? (new IPATranscript([phone])).removePunctuation(true).stripDiacritics().toString(): phone.toString());
						var actualPhoneString =
						(ignoreDiacritics ? (new IPATranscript([actualPhone])).removePunctuation(true).stripDiacritics().toString(): actualPhone.toString());

						if (targetPhoneString == actualPhoneString) {
							numCorrect++;
						} else {
							numSubstituted++;
						}
					} else {
						numDeleted++;
					}
				} else {
					numDeleted++;
				}
			}
		}

		// check actual side for numActual, numEpenthesized
		// check target side for numTarget, numDeleted and numCorrect
		for (pIdx = 0; pIdx < actualGroup.length();
		pIdx++) {
			var phone = actualGroup.elementAt(pIdx);
			var testIPA = (new IPATranscriptBuilder()).append(phone).toIPATranscript();

			if (filter.check_filter(testIPA)) {
				numActual++;

				// check aligned phone
				var alignedData = alignment[ "getAligned(java.lang.Iterable)"]([phone]);
				if (alignedData.size() > 0) {
					var targetPhone = alignedData.get(0);
					if (targetPhone == null) {
						numEpenthesized++;
					}
				} else {
					numEpenthesized++;
				}
			}
		}

		var retVal = {
			target: numTarget,
			actual: numActual,
			correct: numCorrect,
			substituted: numSubstituted,
			deleted: numDeleted,
			epen: numEpenthesized
		};
		return retVal;
	}

};

exports.PPCOptions = function (id, aligned) {

	this.pattern = new PatternFilter(id + ".pattern");
	
	var ppcTypeParamInfo = {
		"id": id + ".ppcType",
		"title": "Report type:",
		"choices": ["Percent Phones Correct (PPC)", "Percent Consonants Correct (PCC)", "Percent Vowels Correct (PVC)", "Percent Correct (custom)"],
		"colnames": ["PPC", "PCC", "PVC", "PC"],
		"phonex": ["\\w", "\\c", "\\v" ],
		"def": 0,
		"cols": 1,
		"type": "radiobutton"
	};
	var ppcTypeParam;
	this.ppcTypeParameter;
	this.ppcType = { index:0, toString: function() { return ppcTypeParamInfo.choices[0]; } };
	
	var ignoreDiacriticsParamInfo = {
		"id": id +(".ignoreDiacritics"),
		"title": "",
		"desc": "Ignore diacritics",
		"def": true
	};
	var ignoreDiacriticsParam;
	this.ignoreDicacritics = ignoreDiacriticsParamInfo.def;
	
	this.getColumnName = function () {
		return ppcTypeParamInfo.colnames[ppcTypeParam.getValue(ppcTypeParamInfo.id).index];
	};
	
	this.param_setup = function (params) {
		ppcTypeParam = new EnumScriptParam(
			ppcTypeParamInfo.id,
			ppcTypeParamInfo.title,
			ppcTypeParamInfo.def,
			ppcTypeParamInfo.choices,
			ppcTypeParamInfo.type,
			ppcTypeParamInfo.cols);
		this.ppcTypeParameter = ppcTypeParam;
	
		ignoreDiacriticsParam = new BooleanScriptParam(
			ignoreDiacriticsParamInfo.id,
			ignoreDiacriticsParamInfo.desc,
			ignoreDiacriticsParamInfo.title,
			ignoreDiacriticsParamInfo.def);
			
		params.add(ppcTypeParam);
		
		var patternParams = new java.util.ArrayList();
		this.pattern.setSelectedPatternType(PatternType.PHONEX);
		this.pattern.param_setup(patternParams);
		this.pattern.setExactMatch(true);
		this.pattern.set_required(true);
		params.add(patternParams.get(1));	
		
		// setup listeners
		var patternFilter = this.pattern;
		patternFilter.setEnabled(false);
		patternFilter.setPattern("\\w");
		ppcTypeParam.addPropertyChangeListener(ppcTypeParamInfo.id, new java.beans.PropertyChangeListener() {
			propertyChange: function(e) {
				var idx = e.source.getValue(e.source.paramId).index;
				
				if(idx < 3) {
					patternFilter.setPattern(ppcTypeParamInfo.phonex[idx]);
					patternFilter.setEnabled(false);
				} else
					patternFilter.setEnabled(true);
			}
		});
		params.add(ignoreDiacriticsParam);
	};
	
};