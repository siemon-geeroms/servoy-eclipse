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
package com.servoy.eclipse.designer.actions;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.SwingConstants;

import com.servoy.j2db.util.ComponentFactoryHelper;
import com.servoy.j2db.util.gui.SpecialMatteBorder;

/**
 * Add a label with some special settings so that it appears as a horizontal line.
 * 
 * @author rgansevles
 * 
 */

public class AddHorizontalLineActionDelegate extends AddLabelActionDelegate
{

	public AddHorizontalLineActionDelegate()
	{
		addExtendedData("text", "");
		addExtendedData("borderType", ComponentFactoryHelper.createBorderString(new SpecialMatteBorder(1, 0, 0, 0, Color.black, Color.black, Color.black,
			Color.black)));
		addExtendedData("horizontalAlignment", SwingConstants.RIGHT);
		addExtendedData("transparent", Boolean.TRUE);
		addExtendedData("size", new Dimension(40, 1));
	}
}
