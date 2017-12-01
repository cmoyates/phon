/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.log.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.BufferPanelContainer;

public class CloseAllBuffersAction extends HookableAction {

	private static final long serialVersionUID = -8345491546953230785L;

	private final static String CMD_NAME = "Close all buffers";
	
	private final static String SHORT_DESC = "Close all open buffers";
	
	private BufferPanelContainer container;
	
	public CloseAllBuffersAction(BufferPanelContainer container) {
		super();
		
		this.container = container;
		
		putValue(HookableAction.NAME, CMD_NAME);
		putValue(HookableAction.SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		this.container.closeAllBuffers();
	}

}
