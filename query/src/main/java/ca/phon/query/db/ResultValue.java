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

package ca.phon.query.db;

import java.util.regex.Matcher;

import ca.phon.phonex.PhonexMatcher;
import ca.phon.util.Range;

/**
 * A reference to a piece of data in a result.
 */
public interface ResultValue {
	/**
	 * Gets the tier name for this result value.
	 * 
	 * @return the tier name
	 */
	public abstract String getTierName();

	/**
	 * Sets the tier name for this result value.
	 * 
	 * @param tierName  the tier name
	 */
	public abstract void setTierName(String tierName);

	/**
	 * Gets the range for this result value.
	 * 
	 * @return the range
	 */
	public abstract Range getRange();

	/**
	 * Sets the range for this result value.
	 * 
	 * @param range  the range
	 */
	public abstract void setRange(Range range);

	/**
	 * Gets the group index for this result value.
	 * 
	 * @return the group index
	 */
	public abstract int getGroupIndex();

	/**
	 * Sets the group index for this result value.
	 * 
	 * @param groupIndex  the group index
	 */
	public abstract void setGroupIndex(int groupIndex);

	/**
	 * Gets the data for this result value.
	 * 
	 * @return the data
	 */
	public abstract String getData();

	/**
	 * Sets the data for this result value.
	 * 
	 * @param data  the data
	 */
	public abstract void setData(String data);
	
	/**
	 * Returns the number of 'matcher' groups either that were produced
	 * by either regex {@link Matcher}s or {@link PhonexMatcher}s.
	 * 
	 * @return number of matcher groups
	 */
	public abstract int getMatcherGroupCount();
	
	/**
	 * Get the value of the specified matcher group.
	 * 
	 * @param index
	 * 
	 * @return value of the specified group
	 * 
	 */
	public abstract String getMatcherGroup(int index);

}