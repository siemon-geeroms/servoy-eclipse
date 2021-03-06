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
package com.servoy.eclipse.team;

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.servoy.eclipse.core.ServoyModel;
import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.j2db.IApplication;
import com.servoy.j2db.dataprocessing.IDataServer;
import com.servoy.j2db.persistence.AbstractRepository;
import com.servoy.j2db.persistence.ITeamRepository;
import com.servoy.j2db.server.shared.ApplicationServerRegistry;
import com.servoy.j2db.server.shared.IApplicationServer;
import com.servoy.j2db.server.shared.IApplicationServerAccess;
import com.servoy.j2db.server.shared.IUserManager;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;
import com.servoy.j2db.util.rmi.IRMIClientFactoryProvider;
import com.servoy.j2db.util.rmi.IRMIClientSocketFactoryFactory;
import com.servoy.j2db.util.rmi.IReconnectListener;

public class RepositoryAccessPoint
{
	public static final String LOCALHOST = "localhost";

	private static final AtomicReference<RepositoryAccessPoint> instanceRef = new AtomicReference<RepositoryAccessPoint>();

	private final String serverAddress;
	private final String user;
	private final String password;

	private int usedRMIPort;

	private String clientID;
	private IApplicationServerAccess applicationServerAccess;
	private ITeamRepository repository;
	private boolean isInprocessApplicationServer;

	private IRMIClientSocketFactoryFactory rmiFactoryFactory;

	private RepositoryAccessPoint(String serverAddress, String user, String password)
	{
		String serverAddr = serverAddress;
		if (serverAddr == null) serverAddr = LOCALHOST;
		else serverAddr = serverAddr.trim();
		int dblDotIdx = serverAddr.indexOf(":");
		if (dblDotIdx != -1)
		{
			this.serverAddress = serverAddr.substring(0, dblDotIdx);
			if (dblDotIdx < serverAddr.length() - 1)
			{
				String port = serverAddr.substring(dblDotIdx + 1);
				try
				{
					this.usedRMIPort = Integer.parseInt(port);
				}
				catch (NumberFormatException ex)
				{
					// ignore;
				}
			}
		}
		else this.serverAddress = serverAddr;
		this.user = user;
		this.password = password;


		if (usedRMIPort == 0)
		{
			Properties settings = getServoySettings();
			usedRMIPort = Utils.getAsInteger(settings.getProperty("usedRMIRegistryPort", "1099"));
		}

		if (serverAddr.equals(LOCALHOST))
		{
			isInprocessApplicationServer = true;
		}
	}

	public static RepositoryAccessPoint getInstance(String serverAddress, String user, String password)
	{
		synchronized (instanceRef)
		{
			RepositoryAccessPoint rap = new RepositoryAccessPoint(serverAddress, user, password);
			RepositoryAccessPoint oldRAp = instanceRef.get();
			if (rap.equals(oldRAp))
			{
				return oldRAp;
			}
			if (oldRAp != null)
			{
				oldRAp.close();
			}
			instanceRef.set(rap);
			return rap;
		}
	}

	public static synchronized void clear()
	{
		synchronized (instanceRef)
		{
			RepositoryAccessPoint rap = instanceRef.getAndSet(null);
			if (rap != null)
			{
				rap.close();
			}
		}
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((serverAddress == null) ? 0 : serverAddress.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RepositoryAccessPoint other = (RepositoryAccessPoint)obj;
		if (password == null)
		{
			if (other.password != null) return false;
		}
		else if (!password.equals(other.password)) return false;
		if (serverAddress == null)
		{
			if (other.serverAddress != null) return false;
		}
		else if (!serverAddress.equals(other.serverAddress)) return false;
		if (user == null)
		{
			if (other.user != null) return false;
		}
		else if (!user.equals(other.user)) return false;
		return true;
	}

	protected void close()
	{
		if (rmiFactoryFactory != null)
		{
			rmiFactoryFactory.close();
			rmiFactoryFactory = null;
		}

	}


	private boolean checkParameters(String srv, String usr, String pwdHash)
	{
		return Utils.stringSafeEquals(serverAddress, srv) && Utils.stringSafeEquals(user, usr) && Utils.stringSafeEquals(password, pwdHash);
	}

	public ITeamRepository getRepository() throws ApplicationServerAccessException, RepositoryAccessException, RemoteException
	{
		if (repository == null)
		{
			repository = createRepository();
		}
		if (repository == null) throw new RepositoryAccessException("Cannot access team repository.");

		return repository;
	}

	protected ITeamRepository createRepository() throws ApplicationServerAccessException, RepositoryAccessException, RemoteException
	{
		IApplicationServerAccess asa = getApplicationServerAccess();
		if (asa != null)
		{
			return asa.getTeamRepository();
		}
		return null;
	}

	public boolean isInprocessApplicationServer()
	{
		return isInprocessApplicationServer;
	}

	public IUserManager getUserManager() throws RepositoryAccessException, ApplicationServerAccessException
	{
		getApplicationServerAccess(); // checks login

		try
		{
			return getApplicationServerAccess().getUserManager(clientID);
		}
		catch (Exception e)
		{
			Debug.error("Could not get user manager", e);
			return null;
		}
	}

	public IDataServer getDataServer() throws RepositoryAccessException
	{
		try
		{
			return getApplicationServerAccess().getDataServer();
		}
		catch (RepositoryAccessException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			ServoyLog.logError("Cannot get data server access", ex);
			throw new RepositoryAccessException("Cannot get data server access.\n" + ex.getMessage());
		}
	}

	public String getClientID()
	{
		return clientID;
	}

	private IApplicationServerAccess getApplicationServerAccess() throws ApplicationServerAccessException, RepositoryAccessException
	{
		if (applicationServerAccess == null)
		{
			applicationServerAccess = createApplicationServerAccess();
		}

		return applicationServerAccess;
	}

	/*
	 * Checks if the remote repository version is the same with the eclipse repository version
	 */
	public void checkRemoteRepositoryVersion() throws RemoteException, RepositoryAccessException, ApplicationServerAccessException
	{
		int remoteRepositoryVersion = getRepository().getRepositoryVersion();
		int localRepository = AbstractRepository.repository_version;
		if (localRepository != remoteRepositoryVersion) throw new RepositoryAccessException("Incompatible repository versions, on the server: " +
			remoteRepositoryVersion + " locally: " + localRepository);
	}

	private IApplicationServerAccess createApplicationServerAccess() throws ApplicationServerAccessException, RepositoryAccessException
	{
		IApplicationServerAccess asa = null;

		if (serverAddress.equals(LOCALHOST))//try inproces 
		{
			IApplicationServer as = ApplicationServerRegistry.getService(IApplicationServer.class);
			try
			{
				clientID = ApplicationServerRegistry.get().getClientId();//only works for inprocess!
				asa = as.getApplicationServerAccess(clientID); // LocalApplicationServer
			}
			catch (RemoteException e)
			{
				throw new ApplicationServerAccessException("Error getting localhost repository", e);
			}
		}
		if (asa == null)
		{
			try
			{
				URL url = new URL("http", serverAddress, "");
				rmiFactoryFactory = createRMIClientSocketFactoryFactory(url, null, getServoySettings(), null);

				IApplicationServer as = (IApplicationServer)LocateRegistry.getRegistry(serverAddress, usedRMIPort,
					rmiFactoryFactory.getRemoteClientSocketFactory()).lookup(IApplicationServer.class.getName());

				// TODO this getClientID expects a password not a hash!!!!
				clientID = as.getClientID(user, password); // RemoteApplicationServer
				if (clientID != null)
				{
					asa = as.getApplicationServerAccess(clientID);
				}
			}
			catch (Exception e)
			{
				Debug.error("Error getting the repository from host " + serverAddress + ":" + usedRMIPort, e);
				throw new ApplicationServerAccessException("Error getting the repository from host " + serverAddress + ":" + usedRMIPort, e);
			}
			if (clientID == null) throw new RepositoryAccessException("Invalid username or password.");
		}

		return asa;
	}

	public IRMIClientSocketFactoryFactory createRMIClientSocketFactoryFactory(URL url, IApplication application, Properties settings,
		IReconnectListener reconnectListener) throws ApplicationServerAccessException
	{
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint ep = reg.getExtensionPoint(IRMIClientFactoryProvider.EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();

		if (extensions == null || extensions.length == 0)
		{
			ServoyLog.logWarning("Could not find rmi client factory provider server starter plugin (extension point " +
				IRMIClientFactoryProvider.EXTENSION_ID + ")", null);
			return null;
		}
		if (extensions.length > 1)
		{
			ServoyLog.logWarning("Multiple rmi client factory plugins found (extension point " +
				IRMIClientFactoryProvider.EXTENSION_ID + ")", null);
		}
		IConfigurationElement[] ce = extensions[0].getConfigurationElements();
		if (ce == null || ce.length == 0)
		{
			ServoyLog.logWarning("Could not read rmi client factory provider plugin (extension point " + IRMIClientFactoryProvider.EXTENSION_ID + ")", null);
			return null;
		}
		if (ce.length > 1)
		{
			ServoyLog.logWarning("Multiple extensions for rmi client factory plugins found (extension point " +
				IRMIClientFactoryProvider.EXTENSION_ID + ")", null);
		}
		IRMIClientFactoryProvider rmiClientFactoryProvider;
		try
		{
			rmiClientFactoryProvider = (IRMIClientFactoryProvider)ce[0].createExecutableExtension("class");
		}
		catch (CoreException e)
		{
			ServoyLog.logWarning("Could not create rmi client factory provider plugin (extension point " + IRMIClientFactoryProvider.EXTENSION_ID + ")", e);
			return null;
		}
		try
		{
			return rmiClientFactoryProvider.createRMIClientSocketFactoryFactory(url, application, settings, reconnectListener);
		}
		catch (Exception e)
		{
			Debug.error("couldn't instantiate the rmi socketfactory", e);
			throw new ApplicationServerAccessException("Error getting remote repository", e);

		}
	}


	private Properties getServoySettings()
	{
		return ServoyModel.getSettings();
	}

}
