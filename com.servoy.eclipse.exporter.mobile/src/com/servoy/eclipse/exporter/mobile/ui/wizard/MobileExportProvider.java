/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

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

package com.servoy.eclipse.exporter.mobile.ui.wizard;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.servoy.eclipse.exporter.mobile.Activator;
import com.servoy.eclipse.model.ServoyModelFinder;
import com.servoy.eclipse.model.nature.ServoyProject;
import com.servoy.eclipse.ui.views.solutionexplorer.actions.OpenWizardAction;
import com.servoy.eclipse.ui.wizards.IExportSolutionWizardProvider;
import com.servoy.j2db.persistence.SolutionMetaData;

/**
 * @author lvostinar
 *
 */
public class MobileExportProvider implements IExportSolutionWizardProvider
{
	public IAction getExportAction()
	{
		ServoyProject activeProject = ServoyModelFinder.getServoyModel().getActiveProject();
		if (activeProject != null && activeProject.getSolutionMetaData().getSolutionType() == SolutionMetaData.MOBILE)
		{
			return new OpenWizardAction(ExportMobileWizard.class, AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/mobile.png"),
				"Mobile Export");
		}
		return null;
	}

}
