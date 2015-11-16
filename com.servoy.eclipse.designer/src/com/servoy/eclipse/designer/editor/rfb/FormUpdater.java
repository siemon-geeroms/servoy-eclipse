/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2015 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */

package com.servoy.eclipse.designer.editor.rfb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sablo.Container;
import org.sablo.WebComponent;
import org.sablo.websocket.IWebsocketSession;

import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.IFormElement;
import com.servoy.j2db.server.ngclient.WebFormComponent;
import com.servoy.j2db.server.ngclient.design.DesignNGClientWebsocketSession;

public class FormUpdater implements Runnable
{
	private final Map<Form, List<IFormElement>> frms;
	private final List<Form> changedForms;
	private final IWebsocketSession websocketSession;

	/**
	 * @param frms
	 * @param changedForm
	 * @param changedSolution
	 */
	FormUpdater(IWebsocketSession websocketSession, Map<Form, List<IFormElement>> frms, List<Form> changedForms)
	{
		this.websocketSession = websocketSession;
		this.frms = frms;
		this.changedForms = changedForms;
	}

	@Override
	public void run()
	{
		websocketSession.getClientService(DesignNGClientWebsocketSession.EDITOR_CONTENT_SERVICE).executeAsyncServiceCall("refreshGhosts", new Object[] { });
		websocketSession.getClientService(DesignNGClientWebsocketSession.EDITOR_CONTENT_SERVICE).executeAsyncServiceCall("refreshDecorators", new Object[] { });

	}

	/**
	 * @param components
	 * @param name
	 * @return
	 */
	private static WebFormComponent findWebComponent(Collection<WebComponent> components, String name)
	{
		if (components == null) return null;
		for (WebComponent webComponent : components)
		{
			if (webComponent.getName().equals(name) && webComponent instanceof WebFormComponent) return (WebFormComponent)webComponent;
			if (webComponent instanceof Container)
			{
				WebFormComponent comp = findWebComponent(((Container)webComponent).getComponents(), name);
				if (comp != null) return comp;
			}
		}
		return null;
	}
}