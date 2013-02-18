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
package ca.phon.session;

import ca.phon.util.Tuple;

/**
 * Helper class for holding location for sessions
 * in a project.
 */
public class SessionLocation extends Tuple<String, String> {

	/**
	 * Constructor
	 */
	public SessionLocation() {
		this("", "");
	}
	
	public SessionLocation(String corpus, String session) {
		super(corpus, session);
	}
	
	/**
	 * Get corpus
	 */
	public String getCorpus() {
		return getObj1();
	}
	
	/**
	 * Set corpus
	 */
	public void setCorpus(String corpus) {
		setObj1(corpus);
	}
	
	/**
	 * Get session
	 */
	public String getSession() {
		return getObj2();
	}
	
	/**
	 * Set session
	 */
	public void setSession(String session) {
		setObj2(session);
	}
	
	@Override
	public String toString() {
		return getCorpus() + "." + getSession();
	}
	
}
