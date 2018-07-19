package ca.phon.query.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.params.history.ParamHistoryManager;
import ca.phon.script.params.history.ParamHistoryType;
import ca.phon.util.PrefHelper;

/**
 * Responsible for loading and saving query history files. Also includes
 * utility methods for some useful query history functions like adding
 * to the history.
 */
public class QueryHistoryManager extends ParamHistoryManager {
	
	private final static Logger LOGGER = Logger.getLogger(QueryHistoryManager.class.getName());

	public final static String QUERY_HISTORY_FOLDER = QueryHistoryManager.class.getName() + ".queryHistoryFolder";
	public final static String DEFAULT_HISTORY_FOLDER = PrefHelper.getUserDataFolder() + File.separator + "query_history";
	
	public static QueryHistoryManager newInstance(QueryScript script) throws IOException {
		final QueryName qn = script.getExtension(QueryName.class);
		if(qn != null) {
			return newInstance(qn.getName());
		} else {
			// use hash of script as name
			final String scriptText = script.getScript();
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA1");
				byte[] hash = digest.digest(scriptText.getBytes());
	
				StringBuffer buffer = new StringBuffer();
				for(int i = 0; i < hash.length; i++) {
					if((0xff & hash[i]) < 0x10) {
						buffer.append('0');
					}
					buffer.append(Integer.toHexString(0xff & hash[i]));
				}
							
				return newInstance(buffer.toString());
			} catch (NoSuchAlgorithmException e) {
				throw new IOException(e);
			}
		}
	}
	
	public static QueryHistoryManager newInstance(String scriptName) throws IOException {
		final File queryHistoryFile = 
				new File(PrefHelper.get(QUERY_HISTORY_FOLDER, DEFAULT_HISTORY_FOLDER), scriptName + ".xml");
		return new QueryHistoryManager(queryHistoryFile);
	}
	
	public QueryHistoryManager(File paramHistoryFile) throws IOException {
		super(paramHistoryFile);
	}
	
	public QueryHistoryManager(InputStream inputStream) throws IOException {
		super(inputStream);
	}
	
	public QueryHistoryManager(ParamHistoryType paramHistory) {
		super(paramHistory);
	}
	
}
