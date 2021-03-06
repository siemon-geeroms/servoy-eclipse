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
package com.servoy.eclipse.ui.property;

import com.servoy.j2db.util.PersistHelper;

/**
 * Converter between swing (awt) fonts and font strings.
 * 
 * @author rgansevles
 */

public class PropertyFontConverter implements IPropertyConverter<java.awt.Font, String>
{
	public static final PropertyFontConverter INSTANCE = new PropertyFontConverter();

	private PropertyFontConverter()
	{
	}

	/**
	 * Convert AWT font to font string
	 */
	public String convertProperty(Object id, java.awt.Font awtfont)
	{
		if (awtfont == null) return null;
		return PersistHelper.createFontString(awtfont);
	}

	/**
	 * Convert font string to AWT font
	 */
	public java.awt.Font convertValue(Object id, String fontString)
	{
		if (fontString == null || fontString.trim().length() == 0) return null;
		return PersistHelper.createFont(fontString);
	}
}
