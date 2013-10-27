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

package ca.phon.app.session.editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

/**
 * Action class for editor events.  This class will call
 * the specified method of the given object when an event occurs.
 * The specified method must take a single parameter of type EditorEvent.
 *
 * The action will be executed on the calling thread by default.
 * To invoke the action on the AWT event queue use the method
 * setRunOnEDT(true)
 */
public class DelegateEditorAction implements EditorAction {

	/** The delegate class (static method) */
	private Class<?> clazz;

	/** The delegate object (non-static method) */
	private Object object;

	/** The method name we are looking for */
	private String methodId;
	
	/**
	 * Constructors
	 */
	public DelegateEditorAction(Object delegate, String methodId) {
		super();

		this.object = delegate;
		this.methodId = methodId;
	}

	public DelegateEditorAction(Class<?> clazz, String methodId) {
		super();

		this.clazz = clazz;
		this.methodId = methodId;
	}

	/**
	 * If both clazz and object are defined - we default to static.
	 * @return
	 */
	public final boolean isStaticAction() {
		boolean retVal = true;

		if(object != null)
			retVal = false;

		return retVal;
	}
	
	@Override
	public void eventOccured(EditorEvent ee) {
		try {
			Method m = getMethod();

			if(isStaticAction())
				m.invoke(null, ee);
			else
				m.invoke(object, ee);
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			
		}
	}

	/*
	 * Get the delegate method reference.
	 */
	private Method getMethod() throws NoSuchMethodException {
		Class<?> clazz = null;

		if(isStaticAction()) {
			clazz = this.clazz;
		} else {
			clazz = this.object.getClass();
		}

		if(clazz == null)
			throw new NullPointerException("Class not found");

		Method retVal =
				clazz.getMethod(methodId, EditorEvent.class);
		return retVal;
	}

//	private class DelegateRunner implements Runnable {
//
//		// the event to dispatch
//		private EditorEvent ee;
//		
//		public DelegateRunner(EditorEvent ee) {
//			this.ee = ee;
//		}
//
//		@Override
//		public void run() {
//			try {
//				Method m = getMethod();
//
//				if(isStaticAction())
//					m.invoke(null, ee);
//				else
//					m.invoke(object, ee);
//
//			} catch (InvocationTargetException e) {
//				PhonLogger.severe(DelegateEditorAction.class, e.toString());
//				if(e.getCause() != null)
//					PhonLogger.severe(DelegateEditorAction.class, "Caused by:\n\t" + e.getCause().toString());
//				
//				e.printStackTrace();
//			} catch (Exception e) {
//				PhonLogger.severe(DelegateEditorAction.class, e.toString());
//			}
//		}
//
//	}

}
