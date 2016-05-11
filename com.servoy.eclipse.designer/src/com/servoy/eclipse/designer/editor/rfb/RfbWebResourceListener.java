/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2014 Servoy BV

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

import com.servoy.eclipse.model.ServoyModelFinder;
import com.servoy.eclipse.model.nature.ServoyNGPackageProject;
import com.servoy.eclipse.model.ngpackages.INGPackageChangeListener;

/**
 * Notify the editor about component changes to reload the palette.
 * @author emera
 */
public class RfbWebResourceListener implements INGPackageChangeListener
{

	private EditorWebsocketSession editorWebsocketSession;

	public RfbWebResourceListener()
	{
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.servoy.eclipse.core.IWebResourceChangedListener#changed()
	 */
	@Override
	public void ngPackageChanged(boolean components, boolean services)
	{
		if (components)
		{
			editorWebsocketSession.getEventDispatcher().addEvent(new Runnable()
			{
				@Override
				public void run()
				{
					editorWebsocketSession.getClientService(EditorWebsocketSession.EDITOR_SERVICE).executeAsyncServiceCall("reloadPalette", new Object[] { });
				}
			});
		}
	}

	/**
	 * @param editorWebsocketSession the editorWebsocketSession to set
	 */
	public void setEditorWebsocketSession(EditorWebsocketSession editorWebsocketSession)
	{
		if (this.editorWebsocketSession == null) ServoyModelFinder.getServoyModel().getNGPackageManager().addNGPackagesChangedListener(this);
		this.editorWebsocketSession = editorWebsocketSession;
	}

	@Override
	public void ngPackageProjectListChanged(ServoyNGPackageProject[] referencedNGPackageProjects)
	{
		// not used right now
	}

}
