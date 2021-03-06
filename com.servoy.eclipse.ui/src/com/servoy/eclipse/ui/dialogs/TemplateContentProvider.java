/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

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
package com.servoy.eclipse.ui.dialogs;

import java.util.List;

import com.servoy.eclipse.core.ServoyModelManager;
import com.servoy.eclipse.core.util.TemplateElementHolder;
import com.servoy.j2db.persistence.IRepository;
import com.servoy.j2db.persistence.IRootObject;
import com.servoy.j2db.persistence.Template;

/**
 * Content provider class for templates.
 * 
 * @author rgansevles
 * 
 */

public class TemplateContentProvider extends FlatTreeContentProvider
{
	public static final TemplateContentProvider DEFAULT = new TemplateContentProvider();

	public static final Object TEMPLATES_DUMMY_INPUT = new Object();

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement == TEMPLATES_DUMMY_INPUT)
		{
			List<IRootObject> templates = ServoyModelManager.getServoyModelManager().getServoyModel().getActiveRootObjects(IRepository.TEMPLATES);
			Object[] elements = new Object[templates.size()];
			for (int i = 0; i < templates.size(); i++)
			{
				elements[i] = new TemplateElementHolder((Template)templates.get(i));
			}
			return elements;
		}

		return new Object[0];
	}
}
