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
package com.servoy.eclipse.jsunit;

import org.eclipse.jdt.internal.junit.runner.RemoteTestRunner;

import com.servoy.eclipse.model.util.ServoyLog;

/**
 * A remote test runnner who's class loader is able to access Servoy classes.
 * @author acostescu
 */
public class SolutionRemoteTestRunner extends RemoteTestRunner
{

	public static void main(String[] args)
	{
		try
		{
			SolutionRemoteTestRunner testRunServer = new SolutionRemoteTestRunner();
			testRunServer.init(args);
			testRunServer.run();
		}
		catch (Throwable e)
		{
			ServoyLog.logError(e);
		}
	}

	// the reason this class exists - so that the com.servoy.eclipse.jsunit class loader is used
	// instead of the class loader for the jUnit plugins - that does not have access to classes in Servoy proj.
	@Override
	protected ClassLoader getTestClassLoader()
	{
		return getClass().getClassLoader();
	}

}
