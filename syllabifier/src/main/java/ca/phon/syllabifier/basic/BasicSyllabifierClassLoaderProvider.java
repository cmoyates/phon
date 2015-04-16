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
package ca.phon.syllabifier.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierProvider;
import ca.phon.syllabifier.basic.io.ObjectFactory;
import ca.phon.syllabifier.basic.io.SyllabifierDef;
import ca.phon.util.resources.ClassLoaderHandler;

/**
 * Loader for Phon 1.6 syllabifier files.
 */
public class BasicSyllabifierClassLoaderProvider extends ClassLoaderHandler<Syllabifier> implements SyllabifierProvider {
	
	private final static Logger LOGGER = Logger.getLogger(BasicSyllabifierClassLoaderProvider.class.getName());

	private final static String LIST = "syllabifier/basic.list";
	
	public BasicSyllabifierClassLoaderProvider() {
		super();
		super.loadResourceFile(LIST);
	}
	
	@Override
	public Syllabifier loadFromURL(URL url) throws IOException {
		final InputStream is = url.openStream();
		
		
		try {
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			
			final JAXBElement<SyllabifierDef> jaxbEle = 
					unmarshaller.unmarshal(new StreamSource(is), SyllabifierDef.class);
			return new BasicSyllabifier(jaxbEle.getValue());
		} catch (JAXBException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return null;
	}
	
}
