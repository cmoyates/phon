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
package ca.phon.ipadictionary.exceptions;

/**
 * Generic IPADictionaryException.  All other dictionary exceptions
 * sub-class this.
 * 
 */
public class IPADictionaryExecption extends RuntimeException {

	private static final long serialVersionUID = 6826688313535456691L;

	public IPADictionaryExecption() {
		super();
	}

	public IPADictionaryExecption(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public IPADictionaryExecption(String arg0) {
		super(arg0);
	}

	public IPADictionaryExecption(Throwable arg0) {
		super(arg0);
	}

}
