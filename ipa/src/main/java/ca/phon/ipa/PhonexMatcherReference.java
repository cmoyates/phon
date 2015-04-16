/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * A special type of IPAElement which represents a
 * reference to a phonex group.  This is used during
 * phonex replacement only.
 *
 */
public class PhonexMatcherReference extends IPAElement {
	
	private Integer groupIndex;
	
	private String groupName;
	
	public PhonexMatcherReference(Integer groupIndex) {
		this.groupIndex = groupIndex;
	}
	
	public PhonexMatcherReference(String groupName) {
		this.groupName = groupName;
	}
	
	public int getGroupIndex() {
		return (groupIndex == null ? -1 : groupIndex);
	}
	
	public String getGroupName() {
		return groupName;
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return "$" + 
				(groupName != null ? "{" + groupName + "}" : groupIndex);
	}

}
