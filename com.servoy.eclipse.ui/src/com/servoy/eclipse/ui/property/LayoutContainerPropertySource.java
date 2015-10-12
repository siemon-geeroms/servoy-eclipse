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

package com.servoy.eclipse.ui.property;

import java.util.Map;

import org.sablo.specification.PropertyDescription;

import com.servoy.j2db.persistence.AbstractBase;
import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.LayoutContainer;
import com.servoy.j2db.persistence.RepositoryHelper;
import com.servoy.j2db.util.Utils;

/**
 * @author gganea
 *
 */
public class LayoutContainerPropertySource extends PersistPropertySource
{

	/**
	 * @param persistContext
	 * @param readonly
	 */
	public LayoutContainerPropertySource(PersistContext persistContext, boolean readonly)
	{
		super(persistContext, readonly);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.servoy.eclipse.ui.property.PersistPropertySource#createPropertyHandlers(java.lang.Object)
	 */
	@Override
	protected IPropertyHandler[] createPropertyHandlers(Object valueObject)
	{
		IPropertyHandler[] tmp1 = super.createPropertyHandlers(valueObject);
		IPropertyHandler[] tmp2 = createLayoutContainerPropertyHandlers();
		return Utils.arrayJoin(tmp1, tmp2);
	}

	/**
	 * @return
	 */
	private IPropertyHandler[] createLayoutContainerPropertyHandlers()
	{

		IPropertyHandler attributesPropertyHandler = new WebComponentPropertyHandler(
			new PropertyDescription("attributes", null, new PropertySetterDelegatePropertyController<Map<String, Object>, PersistPropertySource>(
				new MapEntriesPropertyController("attributes", RepositoryHelper.getDisplayName("attributes", Form.class)), "attributes")
			{
				@SuppressWarnings("unchecked")
				@Override
				public Map<String, Object> getProperty(PersistPropertySource propSource)
				{
					IPersist persist = propSource.getPersist();
					if (persist instanceof LayoutContainer)
					{
						return (Map<String, Object>)((LayoutContainer)persist).getCustomProperty(new String[] { "attributes" }); // returns non-null map with copied/merged values, may be written to
					}
					return null;
				}

				public void setProperty(PersistPropertySource propSource, Map<String, Object> value)
				{
					IPersist persist = propSource.getPersist();
					if (persist instanceof AbstractBase)
					{
						((LayoutContainer)persist).putCustomProperty(new String[] { "attributes" }, value);
					}
				}
			}));
		return new IPropertyHandler[] { attributesPropertyHandler };
	}
}
