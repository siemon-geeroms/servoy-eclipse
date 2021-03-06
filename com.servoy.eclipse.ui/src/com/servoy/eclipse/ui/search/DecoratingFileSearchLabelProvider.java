/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.eclipse.ui.search;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for the search results.
 *  
 * @author jcompagner
 * @since 6.0
 */
public class DecoratingFileSearchLabelProvider extends DecoratingStyledCellLabelProvider implements IPropertyChangeListener, ILabelProvider
{

	private static final String HIGHLIGHT_BG_COLOR_NAME = "org.eclipse.jdt.ui.ColoredLabels.match_highlight";

	public static final Styler HIGHLIGHT_STYLE = StyledString.createColorRegistryStyler(null, HIGHLIGHT_BG_COLOR_NAME);

	public DecoratingFileSearchLabelProvider(FileLabelProvider provider)
	{
		super(provider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null);
	}

	@Override
	public void initialize(ColumnViewer viewer, ViewerColumn column)
	{
		PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
		JFaceResources.getColorRegistry().addListener(this);

		setOwnerDrawEnabled(showColoredLabels());

		super.initialize(viewer, column);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		PlatformUI.getPreferenceStore().removePropertyChangeListener(this);
		JFaceResources.getColorRegistry().removeListener(this);
	}

	private void refresh()
	{
		ColumnViewer viewer = getViewer();

		if (viewer == null)
		{
			return;
		}
		boolean showColoredLabels = showColoredLabels();
		if (showColoredLabels != isOwnerDrawEnabled())
		{
			setOwnerDrawEnabled(showColoredLabels);
			viewer.refresh();
		}
		else if (showColoredLabels)
		{
			viewer.refresh();
		}
	}

	@Override
	protected StyleRange prepareStyleRange(StyleRange styleRange, boolean applyColors)
	{
		if (!applyColors && styleRange.background != null)
		{
			StyleRange sr = super.prepareStyleRange(styleRange, applyColors);
			sr.borderStyle = SWT.BORDER_DOT;
			return sr;
		}
		return super.prepareStyleRange(styleRange, applyColors);
	}

	public static boolean showColoredLabels()
	{
		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
	}

	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals(JFacePreferences.QUALIFIER_COLOR) || property.equals(JFacePreferences.COUNTER_COLOR) ||
			property.equals(JFacePreferences.DECORATIONS_COLOR) || property.equals(HIGHLIGHT_BG_COLOR_NAME) ||
			property.equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS))
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					refresh();
				}
			});
		}
	}

	public String getText(Object element)
	{
		return getStyledText(element).getString();
	}


}
