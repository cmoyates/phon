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

package ca.phon.query.db;

import java.util.Map;

/**
 * Interface for the result of a Phon query. 
 */
public interface Result extends Iterable<ResultValue> {
	/**
	 * Gets the record index for this result.
	 * 
	 * @return the record index
	 */
	public abstract int getRecordIndex();

	/**
	 * Sets the record index for this result.
	 * 
	 * @param index  the record index
	 */
	public abstract void setRecordIndex(int index);

	/**
	 * Gets the map of metadata for this result. 
	 * 
	 * @return the metadata map 
	 */
	public abstract Map<String, String> getMetadata();

	/**
	 * Gets the schema of this result.
	 *       
	 * @return the schema
	 */
	public abstract String getSchema();

	/**
	 * Sets the schema of this result.
	 * 
	 * @param schema  the format 
	 */
	public abstract void setSchema(String schema);
	
	/**
	 * Is this result excluded from reports
	 * 
	 * @return <code>true</code> if this result should be excluded
	 *  <code>false</code> otherwise.
	 */
	public abstract boolean isExcluded();
	
	/**
	 * Set the excluded status of this result.
	 * 
	 * @param excluded
	 */
	public abstract void setExcluded(boolean excluded);
	
	/**
	 * Get the number of result values.
	 * 
	 * @return number of result values
	 */
	public abstract int getNumberOfResultValues();
	
	/**
	 * Get the specified result value
	 * 
	 * @param idx
	 * @return result value
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public abstract ResultValue getResultValue(int idx);
	
	/**
	 * Remove the specified result value
	 * 
	 * @param idx
	 * @return the removed result value
	 */
	public abstract ResultValue removeResultValue(int idx);
	
	/**
	 * Add the given result value.
	 * 
	 * @param resultValue
	 * @return index of added result value
	 */
	public abstract int addResultValue(ResultValue resultValue);
	
}
