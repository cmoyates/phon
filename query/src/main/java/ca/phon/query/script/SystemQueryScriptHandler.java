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
package ca.phon.query.script;

import java.io.IOException;
import java.net.URL;

import ca.phon.util.resources.ClassLoaderHandler;

public class SystemQueryScriptHandler extends ClassLoaderHandler<QueryScript>{
	
	private final static String LIST = "ca/phon/query/script/query.list";
	
	public SystemQueryScriptHandler() {
		super();
		super.loadResourceFile(LIST);
	}
	
	@Override
	public QueryScript loadFromURL(URL url) throws IOException {
		return new QueryScript(url);
	}

}
