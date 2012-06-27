package ca.phon.util.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import ca.phon.util.StringUtils;

/**
 * Loads resources from a specified class loader (or the
 * default class loader if not specified.)
 * 
 * Resources are located using {@link ClassLoader#getResources(String)}
 * for each specified resource.
 */
public abstract class ClassLoaderHandler<T> extends URLHandler<T> {
	
	private static final Logger LOGGER = 
			Logger.getLogger(ClassLoaderHandler.class.getName());
	
	/**
	 * Class loader
	 */
	private ClassLoader classLoader = getClass().getClassLoader();
	
	/**
	 * Resources to load
	 */
	public List<String> resourcePaths = new ArrayList<String>();
	
	/**
	 * Constructor
	 */
	public ClassLoaderHandler() {}
	
	/**
	 * Constructor
	 * 
	 * @param cl the classloader to use
	 */
	public ClassLoaderHandler(ClassLoader cl) {
		this.classLoader = cl;
	}
	
	/**
	 * Add a resource to locate
	 * 
	 * @param res the resource to locate.  Use the naming rules
	 *  outline by {@link ClassLoader#getResource(String)}.
	 */
	public void addResource(String res) {
		this.resourcePaths.add(res);
	}
	
	/**
	 * Load resource list from the give resource file.
	 * 
	 * @param file
	 */
	public void loadResourceFile(String resFile) {
		try {
			final Enumeration<URL> resURLS = getClassLoader().getResources(resFile);
			while(resURLS.hasMoreElements()) {
				final URL resURL = resURLS.nextElement();
				
				// open file and read in one url at a time
				final BufferedReader in =
						new BufferedReader(new InputStreamReader(resURL.openStream()));
				String line = null;
				while((line = in.readLine()) != null) {
					final String resVal = StringUtils.strip(line);
					if(resVal.length() > 0) {
						addResource(resVal);
					}
				}
				in.close();
			}
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}
	
	/**
	 * Remove resource
	 * 
	 * @param res
	 */
	public void removeResource(String res) {
		this.resourcePaths.remove(res);
	}

	@Override
	public Iterator<T> iterator() {
		super.getURLS().clear();
		for(String res:resourcePaths) {
			
			try {
				Enumeration<URL> resURLS = getClassLoader().getResources(res);
				while(resURLS.hasMoreElements()) {
					super.add(resURLS.nextElement());
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.severe(e.getMessage());
			}
			
		}
		
		return super.iterator();
	}
	
	/**
	 * Get the class loader used by this handler.
	 * 
	 * @return ClassLoader
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}
	
	/**
	 * Set the class loader used by this handler.
	 * 
	 * @param cl the new class loader to use, if <code>null</code>
	 *  the default system classloader is used
	 */
	public void setClassLoader(ClassLoader cl) {
		this.classLoader = 
				(cl == null ? getClass().getClassLoader() : cl);
	}

}
