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

package com.servoy.eclipse.exporter.apps.common;


/**
 * Stores and provides the export-app-relevant arguments (reads arguments from eclipse app. arguments).
 * @author acostescu
 */
public interface IArgumentChest
{

	public String getHelpMessage();

	public String getAppServerDir();

	public String getSettingsFileName();

	public boolean isInvalid();

	public boolean mustShowHelp();

	public String[] getSolutionNames();

	public String getSolutionNamesAsString();

	public void info(String message, int priority);

	public boolean isVerbose();

	public boolean shouldExportUsingDbiFileInfoOnly();

	public boolean shouldIgnoreBuildErrors();

	public String getExportFilePath();

}