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

package com.servoy.eclipse.model.war.exporter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.json.JSONException;
import org.sablo.specification.NGPackageSpecification;
import org.sablo.specification.WebComponentSpecProvider;
import org.sablo.specification.WebObjectSpecification;
import org.sablo.specification.WebServiceSpecProvider;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.servoy.eclipse.model.ServoyModelFinder;
import com.servoy.eclipse.model.nature.ServoyNGPackageProject;
import com.servoy.eclipse.model.nature.ServoyProject;
import com.servoy.eclipse.model.nature.ServoyResourcesProject;
import com.servoy.eclipse.model.ngpackages.BaseNGPackageManager;
import com.servoy.eclipse.model.repository.EclipseExportI18NHelper;
import com.servoy.eclipse.model.repository.EclipseExportUserChannel;
import com.servoy.eclipse.model.repository.SolutionSerializer;
import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.eclipse.model.util.TableDefinitionUtils;
import com.servoy.eclipse.model.util.WorkspaceFileAccess;
import com.servoy.j2db.ClientVersion;
import com.servoy.j2db.FlattenedSolution;
import com.servoy.j2db.IBeanManagerInternal;
import com.servoy.j2db.ILAFManagerInternal;
import com.servoy.j2db.persistence.AbstractRepository;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.Solution;
import com.servoy.j2db.persistence.SolutionMetaData;
import com.servoy.j2db.server.headlessclient.dataui.TemplateGenerator;
import com.servoy.j2db.server.ngclient.startup.resourceprovider.ComponentResourcesExporter;
import com.servoy.j2db.server.ngclient.startup.resourceprovider.ResourceProvider;
import com.servoy.j2db.server.ngclient.utils.NGUtils;
import com.servoy.j2db.server.shared.ApplicationServerRegistry;
import com.servoy.j2db.server.shared.IApplicationServerSingleton;
import com.servoy.j2db.server.shared.IUserManager;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.JarManager;
import com.servoy.j2db.util.JarManager.ExtensionResource;
import com.servoy.j2db.util.Pair;
import com.servoy.j2db.util.Settings;
import com.servoy.j2db.util.SortedProperties;
import com.servoy.j2db.util.Utils;
import com.servoy.j2db.util.xmlxport.IMetadataDefManager;
import com.servoy.j2db.util.xmlxport.ITableDefinitionsManager;
import com.servoy.j2db.util.xmlxport.IXMLExporter;


/**
 * Class that creates the WAR file.
 *
 * @author gboros
 * @since 8.0
 */
public class WarExporter
{

	private static final String COMPONENTS_DIR_NAME = "components";
	private static final String SERVICES_DIR_NAME = "services";
	private final IWarExportModel exportModel;
	private static final String[] EXCLUDE_FROM_NG_JAR = new String[] { "com/servoy/j2db/server/ngclient/startup", "war/", "META-INF/MANIFEST.", "META-INF/SERVOYCL." };
	private static final String[] NG_LIBS = new String[] { "org.slf4j.api_*.jar", "log4j_*.jar", "org.freemarker*.jar", "org.jsoup*.jar", "servoy_ngclient_" +
		ClientVersion.getBundleVersion() + ".jar", "sablo_" + ClientVersion.getBundleVersion() + ".jar", "commons-lang3_*.jar", "wro4j-core_*.jar" };

	public WarExporter(IWarExportModel exportModel)
	{
		this.exportModel = exportModel;
	}

	/**
	 * Export the solution as war.
	 * @param monitor
	 * @throws ExportException if export fails
	 */
	public void doExport(IProgressMonitor m) throws ExportException
	{
		SubMonitor monitor = SubMonitor.convert(m, "Creating War File", 36);
		File warFile = createNewWarFile();
		monitor.worked(2);
		File tmpWarDir = createTempDir();
		monitor.worked(2);
		String appServerDir = exportModel.getServoyApplicationServerDir();
		monitor.subTask("Copy root webapp files");
		copyRootWebappFiles(tmpWarDir, appServerDir);
		monitor.worked(2);
		monitor.subTask("Copy beans");
		copyBeans(tmpWarDir, appServerDir);
		monitor.worked(2);
		monitor.subTask("Copy plugins");
		copyPlugins(tmpWarDir, appServerDir);
		monitor.worked(2);
		monitor.subTask("Copy lafs");
		copyLafs(tmpWarDir, appServerDir);
		monitor.worked(2);
		monitor.subTask("Copy all standard libraries");
		final File targetLibDir = copyStandardLibs(tmpWarDir, appServerDir);
		monitor.worked(2);
		monitor.subTask("Copy Drivers");
		copyDrivers(appServerDir, targetLibDir);
		monitor.worked(2);
		monitor.subTask("Copy images");
		copyLibImages(tmpWarDir, appServerDir);
		monitor.worked(2);
		moveSlf4j(tmpWarDir, targetLibDir);
		monitor.worked(2);
		monitor.subTask("Creating web.xml");
		copyWebXml(tmpWarDir);
		monitor.worked(2);
		monitor.subTask("Creating context.xml");
		createTomcatContextXML(tmpWarDir);
		monitor.worked(2);
		addServoyProperties(tmpWarDir);
		monitor.worked(2);
		if (exportModel.isExportActiveSolution())
		{
			monitor.subTask("Copy the active solution");
			copyActiveSolution(monitor.newChild(2), tmpWarDir);
		}
		monitor.setWorkRemaining(8);
		monitor.subTask("Copying NGClient components/services...");
		copyComponentsAndServicesPlusLibs(monitor.newChild(2), tmpWarDir, targetLibDir);
		monitor.setWorkRemaining(6);
		monitor.subTask("Copy exported components");
		copyExportedComponentsAndServicesPropertyFile(tmpWarDir);
		monitor.worked(2);
		monitor.subTask("Creating/zipping the WAR file");
		zipDirectory(tmpWarDir, warFile);
		monitor.worked(2);
		deleteDirectory(tmpWarDir);
		monitor.worked(2);
		monitor.done();
		return;
	}

	/**
	 * Copy to the war the properties file containing the selected NG components and services.
	 * This is needed to optimize the references included in the index.html file.
	 * If no components and services are selected, then all references would be included in the index.
	 */
	private void copyExportedComponentsAndServicesPropertyFile(File tmpWarDir) throws ExportException
	{
		if ((exportModel.getExportedComponents() == null && exportModel.getExportedServices() == null) ||
			(exportModel.getExportedComponents().size() == WebComponentSpecProvider.getInstance().getWebComponentSpecifications().size() &&
				exportModel.getExportedServices().size() == NGUtils.getAllWebServiceSpecificationsThatCanBeUncheckedAtWarExport().length))
			return;

		File exported = new File(tmpWarDir, "WEB-INF/exported_web_objects.properties");
		Properties properties = new Properties();
		StringBuilder webObjects = new StringBuilder();
		for (String component : exportModel.getExportedComponents())
		{
			webObjects.append(component + ",");
		}

		TreeSet<String> allServices = new TreeSet<String>();
		// append internal servoy services
		NGPackageSpecification<WebObjectSpecification> servoyservices = WebServiceSpecProvider.getInstance().getServicesInPackage("servoyservices");
		if (servoyservices != null) allServices.addAll(servoyservices.getSpecifications().keySet());
		// append user services
		if (exportModel.getExportedServices() != null) allServices.addAll(exportModel.getExportedServices());
		for (String service : allServices)
		{
			webObjects.append(service + ",");
		}
		properties.put("usedWebObjects", webObjects.substring(0, webObjects.length() - 1));
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(exported);
			properties.store(fos, "");
		}
		catch (Exception e)
		{
			throw new ExportException("Couldn't generate the exported_web_objects.properties file", e);
		}
		finally
		{
			if (fos != null) try
			{
				fos.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	/**
	 * Copy to the war all NG components and services (default and user-defined), as well as the jars required by the NGClient.
	 */
	private void copyComponentsAndServicesPlusLibs(IProgressMonitor m, File tmpWarDir, final File targetLibDir) throws ExportException
	{
		SubMonitor monitor = SubMonitor.convert(m, 3);
		try
		{
			StringBuilder componentLocations = new StringBuilder();
			StringBuilder servicesLocations = new StringBuilder();

			ComponentResourcesExporter.copyDefaultComponentsAndServices(tmpWarDir, exportModel.getExcludedComponentPackages(),
				exportModel.getExcludedServicePackages());

			componentLocations.append(ComponentResourcesExporter.getComponentDirectoryNames(exportModel.getExcludedComponentPackages()));
			componentLocations.append(copyUserDefinedComponents(tmpWarDir, exportModel.getExcludedComponentPackages(), monitor.newChild(1)));


			servicesLocations.append(ComponentResourcesExporter.getServicesDirectoryNames(exportModel.getExcludedServicePackages()));
			servicesLocations.append(copyUserDefinedServices(tmpWarDir, exportModel.getExcludedServicePackages(), monitor.newChild(1)));

			copyAndAddLocationsOfNGPackageProjects(componentLocations, servicesLocations, tmpWarDir, exportModel.getExcludedComponentPackages(),
				exportModel.getExcludedServicePackages(), monitor.newChild(1));

			createSpecLocationsPropertiesFile(new File(tmpWarDir, "WEB-INF/components.properties"), componentLocations.toString());
			createSpecLocationsPropertiesFile(new File(tmpWarDir, "WEB-INF/services.properties"), servicesLocations.toString());

			copyNGLibs(targetLibDir);
		}
		catch (IOException e)
		{
			throw new ExportException("Could not copy the components", e);
		}
		monitor.done();
	}

	private void copyAndAddLocationsOfNGPackageProjects(StringBuilder componentLocations, StringBuilder servicesLocations, File tmpWarDir,
		List<String> excludedComponentPackages, List<String> excludedServicePackages, IProgressMonitor monitor) throws ExportException
	{
		monitor.subTask("Copying user-defined ng package projects services and components");

		BaseNGPackageManager ngPackageManager = ServoyModelFinder.getServoyModel().getNGPackageManager();
		if (ngPackageManager != null)
		{
			ServoyNGPackageProject[] ngPackageProjects = ngPackageManager.getReferencedNGPackageProjects();

			if (ngPackageProjects != null)
			{
				for (ServoyNGPackageProject ngp : ngPackageProjects)
				{
					String packageName = ngp.getProject().getName();
					if (ngPackageManager.isComponentPackageProject(packageName))
					{
						if (excludedComponentPackages == null || excludedComponentPackages.indexOf(packageName) == -1)
						{
							// if we are here then normally this package is already loaded and valid => we can just copy it directly
							componentLocations.append(";/" + packageName);
							File destDir = new File(tmpWarDir, packageName);
							copyDir(new File(ngp.getProject().getLocationURI()), destDir, true);

							// delete the eclipse .project file as it's not needed in war
							if (destDir.exists() && destDir.isDirectory())
							{
								File dotProjectFile = new File(destDir, ".project");
								if (dotProjectFile.exists() && dotProjectFile.isFile()) dotProjectFile.delete();
							}
						}
					}
					else if (ngPackageManager.isServicePackageProject(packageName))
					{
						if (excludedServicePackages == null || excludedServicePackages.indexOf(packageName) == -1)
						{
							// if we are here then normally this package is already loaded and valid => we can just copy it directly
							servicesLocations.append(";/" + packageName);
							File destDir = new File(tmpWarDir, ngp.getProject().getName());
							copyDir(new File(ngp.getProject().getLocationURI()), destDir, true);

							// delete the eclipse .project file as it's not needed in war
							if (destDir.exists() && destDir.isDirectory())
							{
								File dotProjectFile = new File(destDir, ".project");
								if (dotProjectFile.exists() && dotProjectFile.isFile()) dotProjectFile.delete();
							}
						}
					}
					else
					{
						ServoyLog.logError(new RuntimeException(
							"War export: Invalid referenced ngPackage project found; it is there but it is not loaded for some reason and it will not be exported: " +
								packageName));
					}
				}
			}
		}
		else
		{
			ServoyLog.logError(new RuntimeException("Cannot find ngPackage manager when exporting war..."));
		}
	}

	/**
	 * Add all NG_LIBS to the war.
	 * @param targetLibDir
	 * @throws ExportException
	 * @throws IOException
	 */
	private void copyNGLibs(File targetLibDir) throws ExportException, IOException
	{
		List<String> pluginLocations = exportModel.getPluginLocations();
		File parent = null;
		if (System.getProperty("eclipse.home.location") != null)
			parent = new File(URI.create(System.getProperty("eclipse.home.location").replaceAll(" ", "%20")));
		else parent = new File(System.getProperty("user.dir"));
		for (String libName : NG_LIBS)
		{
			int i = 0;
			boolean found = false;
			while (!found && i < pluginLocations.size())
			{
				File pluginLocation = new File(pluginLocations.get(i));
				if (!pluginLocation.isAbsolute())
				{
					pluginLocation = new File(parent, pluginLocations.get(i));
				}
				FileFilter filter = new WildcardFileFilter(libName);
				try
				{
					File[] libs = pluginLocation.listFiles(filter);

					if (libs == null)
					{
						System.err.println(pluginLocation.toString() + " is directory " + pluginLocation.isDirectory());
						System.err.println("missing lib name: " + libName);
						System.err.println("missing filter: " + filter.toString());
						System.out.println(pluginLocation.listFiles());
					}

					if (libs != null && libs.length > 0)
					{
						File file = libs[0];
						if (libName.contains("servoy_ngclient"))
						{
							copyNGClientJar(file, targetLibDir);
						}
						else
						{
							copyFile(file, new File(targetLibDir, file.getName()));
						}
						found = true;
					}
					i++;
				}
				catch (IOException e)
				{
					Debug.error(e);
				}
			}
			if (!found) throw new ExportException(libName + " was not found. Please specify location");
		}
	}

	/**
	 * Copy the servoy_ngclient jar to the libs folder in the .war.
	 * Exclude folders defined in EXCLUDE_FROM_NG_JAR.
	 * @param file
	 * @param targetLibDir
	 * @throws IOException
	 */
	private void copyNGClientJar(File file, File targetLibDir) throws IOException
	{
		File dest = new File(targetLibDir, file.getName());
		ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(dest));
		byte[] buf = new byte[1024];

		ZipEntry entry = zin.getNextEntry();
		while (entry != null)
		{
			String name = entry.getName();
			boolean toBeDeleted = false;
			for (String f : EXCLUDE_FROM_NG_JAR)
			{
				if (name.startsWith(f))
				{
					toBeDeleted = true;
					break;
				}
			}
			if (!toBeDeleted)
			{
				// Add ZIP entry to output stream.
				zout.putNextEntry(new ZipEntry(name));
				// Transfer bytes from the ZIP file to the output file
				int len;
				while ((len = zin.read(buf)) > 0)
				{
					zout.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		// Close the streams
		zin.close();
		// Compress the files
		// Complete the ZIP file
		zout.close();
	}

	/**
	 * @param tmpWarDir
	 * @param locations
	 * @throws ExportException
	 */
	private void createSpecLocationsPropertiesFile(File file, String locations) throws ExportException
	{
		Properties properties = new Properties();
		properties.put("locations", locations);
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(file);
			properties.store(fos, "");
		}
		catch (Exception e)
		{
			throw new ExportException("Couldn't generate the components.properties file", e);
		}
		finally
		{
			if (fos != null) try
			{
				fos.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private String copyUserDefinedComponentsOrServices(File tmpWarDir, IProgressMonitor monitor, String copyFrom, List<String> excludedPackages)
		throws ExportException
	{
		StringBuilder locations = new StringBuilder();
		ServoyResourcesProject activeResourcesProject = ServoyModelFinder.getServoyModel().getActiveResourcesProject();
		try
		{
			activeResourcesProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		catch (CoreException e1)
		{
			Debug.error(e1);
		}
		if (activeResourcesProject != null)
		{
			IFolder folder = activeResourcesProject.getProject().getFolder(copyFrom);
			if (folder.exists())
			{
				try
				{
					IResource[] members = folder.members();
					for (IResource resource : members)
					{
						String name = resource.getName();
						int index = name.lastIndexOf('.');
						if (index != -1)
						{
							name = name.substring(0, index);
						}
						if (resource instanceof IFolder)
						{
							IFolder resourceFolder = (IFolder)resource;
							if ((resourceFolder).getFile("META-INF/MANIFEST.MF").exists())
							{
								String packageName = resourceFolder.getName();
								if (excludedPackages == null || excludedPackages.indexOf(packageName) == -1)
								{
									locations.append(";/" + packageName);
									copyDir(new File(resource.getLocationURI()), new File(tmpWarDir, resourceFolder.getName()), true);
								}
							}
						}
						else if (resource instanceof IFile)
						{
							locations.append(";/" + name);
							extractJar(new File(resource.getLocationURI()), tmpWarDir);
						}
					}
				}
				catch (CoreException e)
				{
					ServoyLog.logError(e);
				}
			}
		}
		return locations.toString();
	}

	/**
	 * Copy all user-defined components from resources project to the war.
	 */
	private String copyUserDefinedComponents(File tmpWarDir, List<String> excludedComponentPackages, IProgressMonitor monitor) throws ExportException
	{
		monitor.subTask("Copy user-defined components from resources project");
		return copyUserDefinedComponentsOrServices(tmpWarDir, monitor, COMPONENTS_DIR_NAME, excludedComponentPackages);
	}

	/**
	 * Copy all user-defined services from resources/services to the war.
	 */
	private String copyUserDefinedServices(File tmpWarDir, List<String> excludedServicePackages, IProgressMonitor monitor) throws ExportException
	{
		monitor.subTask("Copy user-defined services from resources project");
		return copyUserDefinedComponentsOrServices(tmpWarDir, monitor, SERVICES_DIR_NAME, excludedServicePackages);
	}

	private void extractJar(File file, File tmpWarDir) throws ExportException
	{
		JarFile jarfile = null;
		try
		{
			jarfile = new JarFile(file);
			Enumeration<JarEntry> enu = jarfile.entries();
			while (enu.hasMoreElements())
			{
				String name = file.getName();
				int index = name.lastIndexOf('.');
				if (index != -1)
				{
					name = name.substring(0, index);
				}

				String destdir = tmpWarDir + "/" + name;
				JarEntry je = enu.nextElement();
				File fl = new File(destdir, je.getName());
				if (!fl.exists())
				{
					fl.getParentFile().mkdirs();
					fl = new File(destdir, je.getName());
				}
				if (je.isDirectory())
				{
					continue;
				}
				InputStream is = jarfile.getInputStream(je);
				FileOutputStream fo = new FileOutputStream(fl);
				while (is.available() > 0)
				{
					fo.write(is.read());
				}
				fo.close();
				is.close();
			}
		}
		catch (IOException e)
		{
			Debug.error("IO exception when extracting from file " + file.getAbsolutePath(), e);
		}
		finally
		{
			if (jarfile != null) try
			{
				jarfile.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private File createNewWarFile() throws ExportException
	{
		File warFile = new File(exportModel.getWarFileName());
		if (warFile.exists() && !warFile.delete())
		{
			throw new ExportException("Can't delete the existing war file " + exportModel.getWarFileName());
		}

		try
		{
			if (warFile.getParentFile() != null && !warFile.getParentFile().exists()) warFile.getParentFile().mkdirs();
			if (!warFile.createNewFile())
			{
				throw new ExportException("Can't create the file " + exportModel.getWarFileName());
			}
		}
		catch (IOException e)
		{
			throw new ExportException("Can't create the file " + exportModel.getWarFileName(), e);
		}
		return warFile;
	}

	private File createTempDir() throws ExportException
	{
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));

		File tmpWarDir = new File(tmpDir, "warexport/");
		if (tmpWarDir.exists())
		{
			if (!deleteDirectory(tmpWarDir))
			{
				throw new ExportException("Can't delete the temp dir  " + tmpWarDir);
			}
		}
		if (!tmpWarDir.mkdirs())
		{
			throw new ExportException("Can't create the temp dir  " + tmpWarDir);
		}
		return tmpWarDir;
	}

	protected void copyActiveSolution(IProgressMonitor monitor, File tmpWarDir) throws ExportException
	{
		try
		{
			FlattenedSolution solution = ServoyModelFinder.getServoyModel().getActiveProject().getFlattenedSolution();
			SolutionSerializer.writeRuntimeSolution(null, new File(tmpWarDir, "WEB-INF/solution.runtime"), solution.getSolution(),
				ApplicationServerRegistry.get().getDeveloperRepository(), solution.getModules());
			exportSolution(monitor, tmpWarDir.getCanonicalPath(), solution.getSolution(), false);

			File preImportFolder = new File(tmpWarDir, "WEB-INF/preImport");
			File postImportFolder = new File(tmpWarDir, "WEB-INF/postImport");
			for (ServoyProject sp : ServoyModelFinder.getServoyModel().getModulesOfActiveProjectWithImportHooks())
			{
				File destinationFolder = null;
				if (SolutionMetaData.isPreImportHook(sp.getSolution()))
				{
					destinationFolder = preImportFolder;
				}
				else if (SolutionMetaData.isPostImportHook(sp.getSolution()))
				{
					destinationFolder = postImportFolder;
				}

				if (destinationFolder != null)
				{
					if (!destinationFolder.exists()) destinationFolder.mkdir();
					SolutionSerializer.writeRuntimeSolution(null, new File(destinationFolder, sp.getSolution().getName() + ".runtime"), sp.getSolution(),
						ApplicationServerRegistry.get().getDeveloperRepository(), sp.getModules());
				}
			}

		}
		catch (Exception ex)
		{
			throw new ExportException("Cannot write the active solution in war file", ex);
		}

		try
		{
			File importProperties = new File(tmpWarDir, "WEB-INF/import.properties");
			Properties prop = new Properties();
			prop.setProperty("overwriteGroups", Boolean.toString(exportModel.isOverwriteGroups()));
			prop.setProperty("allowSQLKeywords", Boolean.toString(exportModel.isAllowSQLKeywords()));
			prop.setProperty("overrideSequenceTypes", Boolean.toString(exportModel.isOverrideSequenceTypes()));
			prop.setProperty("overrideDefaultValues", Boolean.toString(exportModel.isOverrideDefaultValues()));
			prop.setProperty("insertNewI18NKeysOnly", Boolean.toString(exportModel.isInsertNewI18NKeysOnly()));
			prop.setProperty("importUserPolicy", Integer.toString(exportModel.getImportUserPolicy()));
			prop.setProperty("addUsersToAdminGroup", Boolean.toString(exportModel.isAddUsersToAdminGroup()));
			prop.setProperty("allowDataModelChange", Boolean.toString(exportModel.isAllowDataModelChanges()));
			prop.setProperty("updateSequences", Boolean.toString(exportModel.isUpdateSequences()));
			prop.setProperty("automaticallyUpgradeRepository", Boolean.toString(exportModel.isAutomaticallyUpgradeRepository()));
			FileWriter writer = new FileWriter(importProperties);
			try
			{
				prop.store(writer, "import properties");
			}
			finally
			{
				writer.close();
			}
		}
		catch (Exception e)
		{
			ServoyLog.logError(e);
		}
	}

	protected void createTomcatContextXML(File tmpWarDir) throws ExportException
	{
		if (!exportModel.isCreateTomcatContextXML()) return;
		try
		{
			File metaDir = new File(tmpWarDir, "META-INF");
			metaDir.mkdir();
			File contextFile = new File(tmpWarDir, "META-INF/context.xml");
			contextFile.createNewFile();
			FileWriter writer = new FileWriter(contextFile);
			try
			{
				String fileContent = "<Context ";
				if (exportModel.isAntiResourceLocking()) fileContent += "antiResourceLocking=\"true\" ";
				if (exportModel.isClearReferencesStatic()) fileContent += "clearReferencesStatic=\"true\" ";
				if (exportModel.isClearReferencesStopThreads()) fileContent += "clearReferencesStopThreads=\"true\" ";
				if (exportModel.isClearReferencesStopTimerThreads()) fileContent += "clearReferencesStopTimerThreads=\"true\" ";
				fileContent += "></Context>";
				writer.write(fileContent);
			}
			finally
			{
				writer.close();
			}
		}
		catch (Exception e)
		{
			ServoyLog.logError(e);
		}
	}

	private void exportSolution(IProgressMonitor monitor, String tmpWarDir, Solution activeSolution, boolean exportSolution)
		throws CoreException, ExportException
	{
		int totalDuration = IProgressMonitor.UNKNOWN;
		if (exportModel.getModulesToExport() != null) totalDuration = (int)(1.42 * exportModel.getModulesToExport().length); // make the main export be 70% of the time, leave the rest for sample data
		monitor.beginTask("Exporting solution", totalDuration);

		final IApplicationServerSingleton as = ApplicationServerRegistry.get();
		AbstractRepository rep = (AbstractRepository)as.getDeveloperRepository();

		IUserManager sm = as.getUserManager();
		EclipseExportUserChannel eeuc = new EclipseExportUserChannel(exportModel, monitor);
		EclipseExportI18NHelper eeI18NHelper = new EclipseExportI18NHelper(new WorkspaceFileAccess(ResourcesPlugin.getWorkspace()));
		IXMLExporter exporter = as.createXMLExporter(rep, sm, eeuc, Settings.getInstance(), as.getDataServer(), as.getClientId(), eeI18NHelper);

		try
		{
			ITableDefinitionsManager tableDefManager = null;
			IMetadataDefManager metadataDefManager = null;
			if (exportModel.isExportUsingDbiFileInfoOnly())
			{
				Pair<ITableDefinitionsManager, IMetadataDefManager> defManagers = TableDefinitionUtils.getTableDefinitionsFromDBI(activeSolution,
					exportModel.isExportReferencedModules(), exportModel.isExportI18NData(), exportModel.isExportAllTablesFromReferencedServers(),
					exportModel.isExportMetaData());
				if (defManagers != null)
				{
					tableDefManager = defManagers.getLeft();
					metadataDefManager = defManagers.getRight();
				}
			}
			exporter.exportSolutionToFile(activeSolution, new File(tmpWarDir, "WEB-INF/solution.servoy"), ClientVersion.getVersion(),
				ClientVersion.getReleaseNumber(), exportModel.isExportMetaData(), exportModel.isExportSampleData(), exportModel.getNumberOfSampleDataExported(),
				exportModel.isExportI18NData(), exportModel.isExportUsers(), exportModel.isExportReferencedModules(), exportModel.isProtectWithPassword(),
				tableDefManager, metadataDefManager, exportSolution);

			monitor.done();
		}
		catch (RepositoryException e)
		{
			throw new ExportException("Repository exception", e);
		}
		catch (JSONException jsonex)
		{
			throw new ExportException("Bad JSON file structure.", jsonex);
		}
		catch (IOException ioex)
		{
			throw new ExportException("Exception getting files.", ioex);
		}
	}

	/**
	 * @param tmpWarDir
	 * @throws ExportException
	 */
	private void addServoyProperties(File tmpWarDir) throws ExportException
	{
		if (exportModel.getServoyPropertiesFileName() == null)
		{
			generatePropertiesFile(tmpWarDir);
		}
		else
		{
			File sourceFile = new File(exportModel.getServoyPropertiesFileName());
			if (exportModel.allowOverwriteSocketFactoryProperties())
			{
				changeAndWritePropertiesFile(tmpWarDir, sourceFile);
			}
			else
			{
				copyPropertiesFileToWar(tmpWarDir, sourceFile);
			}
		}
	}

	private void copyWebXml(File tmpWarDir) throws ExportException
	{
		// copy war web.xml
		File webXMLFile = new File(tmpWarDir, "WEB-INF/web.xml");
		BufferedOutputStream bos = null;
		InputStream webXmlIS = null;
		try
		{
			webXmlIS = WarExporter.class.getResourceAsStream("resources/web.xml");
			bos = new BufferedOutputStream(new FileOutputStream(webXMLFile));

			byte[] buffer = new byte[8096];
			int read = webXmlIS.read(buffer);
			while (read != -1)
			{
				bos.write(buffer, 0, read);
				read = webXmlIS.read(buffer);
			}
		}
		catch (Exception e)
		{
			throw new ExportException("Can't create the web.xml file: " + webXMLFile.getAbsolutePath(), e);
		}
		finally
		{
			if (bos != null) try
			{
				bos.close();
			}
			catch (IOException e)
			{
			}
			if (webXmlIS != null) try
			{
				webXmlIS.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private void moveSlf4j(File tmpWarDir, final File targetLibDir) throws ExportException
	{
		// move the slf4j outside of the WEB-INF/lib to /lib/, its only used in the client
		File slf4j = new File(targetLibDir, "slf4j-jdk14.jar");
		copyFile(slf4j, new File(tmpWarDir, "lib/slf4j-jdk14.jar"));
		slf4j.delete();
	}

	/**
	 * @param tmpWarDir
	 * @param appServerDir
	 * @throws ExportException
	 */
	private void copyLibImages(File tmpWarDir, String appServerDir) throws ExportException
	{
		// copy lib/images dir
		File libImagesDir = new File(appServerDir, "lib/images");
		File targetLibImagesDir = new File(tmpWarDir, "lib/images");
		targetLibImagesDir.mkdirs();
		copyDir(libImagesDir, targetLibImagesDir, false);
	}

	/**
	 * @param monitor
	 * @param appServerDir
	 * @param targetLibDir
	 * @throws ExportException
	 */
	private void copyDrivers(String appServerDir, final File targetLibDir) throws ExportException
	{
		List<String> drivers = exportModel.getDrivers();
		File srcDriverDir = new File(appServerDir, "drivers");
		for (String driverFileName : drivers)
		{
			copyFile(new File(srcDriverDir, driverFileName), new File(targetLibDir, driverFileName));
		}
	}

	private File copyStandardLibs(File tmpWarDir, String appServerDir) throws ExportException
	{
		// copy lib dir (excluding images)
		final File libDir = new File(appServerDir, "lib");
		final File targetLibDir = new File(tmpWarDir, "WEB-INF/lib");
		targetLibDir.mkdirs();
		copyDir(libDir, targetLibDir, false);

//		// copy the template handler
//		copyFile(new File(appServerDir, "server/lib/template-handler.jar"), new File(targetLibDir, "template-handler.jar"));

		// delete the servlet.jar that one isn't allowed.
		new File(targetLibDir, "servlet-api.jar").delete();
		new File(targetLibDir, "jsp-api.jar").delete();
		// delete the tomcat boostrapper, also not needed in a war file
		new File(targetLibDir, "server-bootstrap.jar").delete();
		new File(targetLibDir, "tomcat-juli.jar").delete();
		new File(targetLibDir, "tim-api.jar").delete();
		return targetLibDir;
	}

	private void copyLafs(File tmpWarDir, String appServerDir) throws ExportException
	{
		Writer fw;
		// copy the lafs
		File lafSourceDir = new File(appServerDir, "lafs");
		File lafTargetDir = new File(tmpWarDir, "lafs");
		lafTargetDir.mkdirs();
		ILAFManagerInternal lafManager = ApplicationServerRegistry.get().getLafManager();
		Map<String, List<ExtensionResource>> loadedLafDefs = lafManager.getLoadedLAFDefs();
		List<String> lafs = exportModel.getLafs();
		File lafProperties = new File(lafTargetDir, "lafs.properties");
		fw = null;
		try
		{
			fw = new FileWriter(lafProperties);
			Set<File> writtenFiles = new HashSet<File>();
			for (String lafName : lafs)
			{
				List<ExtensionResource> fileNames = JarManager.getExtensions(loadedLafDefs, lafName);
				ArrayList<String> files = new ArrayList<String>();
				if (fileNames != null)
				{
					for (ExtensionResource ext : fileNames)
					{
						files.add(ext.jarFileName);
					}
				}
				else
				{
					files.add(lafName);
				}
				for (String f : files)
				{
					File sourceFile = new File(lafSourceDir, f);
					copyFile(sourceFile, new File(lafTargetDir, f));
					writeFileEntry(fw, sourceFile, f, writtenFiles);
				}
			}
		}
		catch (IOException e2)
		{
			throw new ExportException("Error creating lafs properties file " + lafProperties.getAbsolutePath(), e2);
		}
		finally
		{
			if (fw != null) try
			{
				fw.close();
				fw = null;
			}
			catch (IOException e)
			{
			}
		}
	}

	private void copyPlugins(File tmpWarDir, String appServerDir) throws ExportException
	{
		// copy the plugins
		File pluginsDir = new File(tmpWarDir, "plugins");
		pluginsDir.mkdirs();
		List<String> plugins = exportModel.getPlugins();
		File pluginProperties = new File(pluginsDir, "plugins.properties");
		Writer fw = null;
		try
		{
			fw = new FileWriter(pluginProperties);
			Set<File> writtenFiles = new HashSet<File>();
			for (String plugin : plugins)
			{
				String pluginName = "plugins/" + plugin;
				File pluginJarFile = new File(appServerDir, pluginName);

				writeFileEntry(fw, pluginJarFile, plugin, writtenFiles);

				copyFile(pluginJarFile, new File(tmpWarDir, pluginName));

				copyJnlp(tmpWarDir, appServerDir, pluginName + ".jnlp", fw, writtenFiles);

				if (pluginName.toLowerCase().endsWith(".jar") || pluginName.toLowerCase().endsWith(".zip"))
				{
					String pluginLibDir = pluginName.substring(0, pluginName.length() - 4);
					File pluginLibDirFile = new File(appServerDir, pluginLibDir);
					if (pluginLibDirFile.exists() && pluginLibDirFile.isDirectory())
					{
						Set<File> copiedFiles = copyDir(pluginLibDirFile, new File(tmpWarDir, pluginLibDir), false);
						for (File file : copiedFiles)
						{
							String fileName = file.getAbsolutePath().replace('\\', '/');
							int index = fileName.indexOf("plugins/");
							if (index != -1)
							{
								fileName = fileName.substring(index + "plugins/".length());
							}
							writeFileEntry(fw, file, fileName, writtenFiles);
						}
					}
				}

			}
		}
		catch (IOException e1)
		{
			throw new ExportException("Error creating plugins dir", e1);
		}
		finally
		{
			if (fw != null) try
			{
				fw.close();
				fw = null;
			}
			catch (IOException e)
			{
			}
		}
	}

	private void copyBeans(File tmpWarDir, String appServerDir) throws ExportException
	{
		// copy the beans
		File beanSourceDir = new File(appServerDir, "beans");
		File beanTargetDir = new File(tmpWarDir, "beans");
		beanTargetDir.mkdirs();
		IBeanManagerInternal beanManager = ApplicationServerRegistry.get().getBeanManager();
		Map<String, List<ExtensionResource>> loadedBeanDefs = beanManager.getLoadedBeanDefs();
		List<String> beans = exportModel.getBeans();
		File beanProperties = new File(beanTargetDir, "beans.properties");
		Writer fw = null;
		try
		{
			fw = new FileWriter(beanProperties);
			Set<File> writtenFiles = new HashSet<File>();
			for (String beanName : beans)
			{
				List<ExtensionResource> fileNames = JarManager.getExtensions(loadedBeanDefs, beanName);
				if (fileNames != null)
				{
					for (ExtensionResource ext : fileNames)
					{
						File sourceFile = new File(beanSourceDir, ext.jarFileName);
						copyFile(sourceFile, new File(beanTargetDir, ext.jarFileName));
						writeFileEntry(fw, sourceFile, ext.jarFileName, writtenFiles);
					}
				}
			}
		}
		catch (IOException e2)
		{
			throw new ExportException("Error creating beans dir", e2);
		}
		finally
		{
			if (fw != null) try
			{
				fw.close();
				fw = null;
			}
			catch (IOException e)
			{
			}
		}
	}

	private void copyRootWebappFiles(File tmpWarDir, String appServerDir) throws ExportException
	{
		File webAppDir = new File(appServerDir, "server/webapps/ROOT");
		// copy first the standard webapp dir of the app server
		copyDir(webAppDir, tmpWarDir, true);

		File defaultCss = new File(tmpWarDir, "/servoy-webclient/templates/default/servoy_web_client_default.css");
		if (!defaultCss.exists())
		{
			try
			{
				String styleCSS = TemplateGenerator.getStyleCSS("servoy_web_client_default.css");
				OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(defaultCss), "utf8");
				fw.write(styleCSS);
				fw.close();
			}
			catch (Exception e)
			{
				throw new ExportException("Error default servoy css file (servoy_web_client_default.css)", e);
			}
		}
	}

	private void changeAndWritePropertiesFile(File tmpWarDir, File sourceFile) throws ExportException
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try
		{
			fis = new FileInputStream(sourceFile);
			Properties properties = new SortedProperties();
			properties.load(fis);

			properties.setProperty("SocketFactory.rmiServerFactory", "com.servoy.j2db.server.rmi.tunnel.ServerTunnelRMISocketFactoryFactory");
			properties.setProperty("SocketFactory.tunnelConnectionMode", "http&socket");
			if (properties.containsKey("SocketFactory.useTwoWaySocket")) properties.remove("SocketFactory.useTwoWaySocket");

			fos = new FileOutputStream(new File(tmpWarDir, "WEB-INF/servoy.properties"));
			properties.store(fos, "");
		}
		catch (IOException e)
		{
			throw new ExportException("Failed to overwrite properties file", e);
		}
		finally
		{
			try
			{
				if (fis != null) fis.close();
				if (fos != null) fos.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
	}

	private void copyPropertiesFileToWar(File tmpWarDir, File sourceFile) throws ExportException
	{
		File destFile = new File(tmpWarDir, "WEB-INF/servoy.properties");
		try
		{
			if (destFile.createNewFile())
			{
				FileInputStream fis = null;
				FileOutputStream fos = null;
				FileChannel sourceChannel = null;
				FileChannel destinationChannel = null;


				try
				{
					fis = new FileInputStream(sourceFile);
					fos = new FileOutputStream(destFile);
					sourceChannel = fis.getChannel();
					destinationChannel = fos.getChannel();
					// Copy source file to destination file
					destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
				}
				finally
				{
					if (sourceChannel != null) sourceChannel.close();
					if (destinationChannel != null) destinationChannel.close();
					if (fis != null) fis.close();
					if (fos != null) fos.close();
				}
			}
		}
		catch (IOException e)
		{
			throw new ExportException("Couldn't copy the servoy properties file", e);
		}
	}

	private void generatePropertiesFile(File tmpWarDir) throws ExportException
	{
		// create the (sorted) properties file.
		Properties properties = new SortedProperties();
		properties.setProperty("SocketFactory.rmiServerFactory", "com.servoy.j2db.server.rmi.tunnel.ServerTunnelRMISocketFactoryFactory");
		properties.setProperty("SocketFactory.tunnelConnectionMode", "http&socket");
		properties.setProperty("SocketFactory.compress", "true");
		properties.setProperty("java.rmi.server.hostname", "127.0.0.1");

		// TODO ask for a keystore?
		properties.setProperty("SocketFactory.useSSL", "true");
		properties.setProperty("SocketFactory.tunnelUseSSLForHttp", "false");
		//{ "SocketFactory.SSLKeystorePath", "", "The SSL keystore path on the server", "text" }, //
		//{ "SocketFactory.SSLKeystorePassphrase", "", "The SSL passphrase to access the keystore", "password" }, //

		properties.setProperty("servoy.use.client.timezone", "true");

		// TODO ask for all kinds of other stuff like branding?
		properties.setProperty("servoy.server.start.rmi", Boolean.toString(exportModel.getStartRMI()));
		properties.setProperty("servoy.rmiStartPort", exportModel.getStartRMIPort());

		// store the servers
		SortedSet<String> selectedServerNames = exportModel.getSelectedServerNames();
		properties.setProperty("ServerManager.numberOfServers", Integer.toString(selectedServerNames.size()));
		int i = 0;
		for (String serverName : selectedServerNames)
		{
			ServerConfiguration sc = exportModel.getServerConfiguration(serverName);

			properties.put("server." + i + ".serverName", sc.getName());
			properties.put("server." + i + ".userName", sc.getUserName());
			properties.put("server." + i + ".password", sc.getPassword());
			properties.put("server." + i + ".URL", sc.getServerUrl());
//			Map<String, String> connectionProperties = sc.getConnectionProperties();
//			if (connectionProperties == null)
//			{
//				Settings.removePrefixedProperties(properties, "server." + i + ".property.");
//			}
//			else
//			{
//				for (Entry<String, String> entry : connectionProperties.entrySet())
//				{
//					properties.put("server." + i + ".property." + entry.getKey(), entry.getValue());
//				}
//			}
			properties.put("server." + i + ".driver", sc.getDriver());
			properties.put("server." + i + ".skipSysTables", "" + sc.isSkipSysTables());
			String catalog = sc.getCatalog();
			if (catalog == null)
			{
				catalog = "<none>";
			}
			else if (catalog.trim().length() == 0)
			{
				catalog = "<empty>";
			}
			properties.put("server." + i + ".catalog", catalog);
			String schema = sc.getSchema();
			if (schema == null)
			{
				schema = "<none>";
			}
			else if (schema.trim().length() == 0)
			{
				schema = "<empty>";
			}
			properties.put("server." + i + ".schema", schema);
			properties.put("server." + i + ".maxConnectionsActive", String.valueOf(sc.getMaxActive()));
			properties.put("server." + i + ".maxConnectionsIdle", String.valueOf(sc.getMaxIdle()));
			properties.put("server." + i + ".maxPreparedStatementsIdle", String.valueOf(sc.getMaxPreparedStatementsIdle()));
			properties.put("server." + i + ".connectionValidationType", String.valueOf(sc.getConnectionValidationType()));
			if (sc.getValidationQuery() != null)
			{
				properties.put("server." + i + ".validationQuery", sc.getValidationQuery());
			}
			if (sc.getDataModelCloneFrom() != null && !"".equals(sc.getDataModelCloneFrom()))
			{
				properties.put("server." + i + ".dataModelCloneFrom", sc.getDataModelCloneFrom());
			}
			properties.put("server." + i + ".enabled", Boolean.toString(true));
//			if (sc.getDialectClass() != null)
//			{
//				properties.put("server." + i + ".dialect", sc.getDialectClass());
//			}
//			else
//			{
//				properties.remove("server." + i + ".dialect");
//			}
			i++;
		}

		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(new File(tmpWarDir, "WEB-INF/servoy.properties"));
			properties.store(fos, "");
		}
		catch (FileNotFoundException fileNotFoundException)
		{
			throw new ExportException("Couldn't find file " + tmpWarDir.getAbsolutePath() + "/WEB-INF/servoy.properties", fileNotFoundException);
		}
		catch (IOException e)
		{
			throw new ExportException("Couldn't generate the properties file", e);
		}
		finally
		{
			if (fos != null) try
			{
				fos.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private void writeFileEntry(Writer fw, File file, String name, Set<File> writtenFiles) throws IOException
	{

		if (writtenFiles.add(file) && file.exists())
		{
			fw.write(name);
			fw.write('=');
			fw.write(Long.toString(file.lastModified()));
			fw.write(':');
			fw.write(Long.toString(file.length()));
			fw.write('\n');
		}
	}

	private void zipDirectory(File directory, File zip) throws ExportException
	{
		ZipOutputStream zos = null;
		try
		{
			zos = new ZipOutputStream(new FileOutputStream(zip));
			zip(directory, directory, zos);
		}
		catch (Exception e)
		{
			throw new ExportException("Can't create the war file " + zip, e);
		}
		finally
		{
			if (zos != null) try
			{
				zos.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private void zip(File directory, File base, ZipOutputStream zos) throws IOException
	{
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (File file : files)
		{
			if (file.isDirectory())
			{
				zip(file, base, zos);
			}
			else
			{
				FileInputStream in = new FileInputStream(file);
				ZipEntry entry = new ZipEntry(file.getPath().substring(base.getPath().length() + 1).replace('\\', '/'));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer)))
				{
					zos.write(buffer, 0, read);
				}
				zos.closeEntry();
				in.close();
			}
		}
	}

	private void copyJnlp(File tmpWarDir, String appServerDir, String pluginJnlpName, Writer fw, Set<File> writtenFiles) throws ExportException, IOException
	{
		if (pluginJnlpName.startsWith("/servoy-client/"))
		{
			pluginJnlpName = pluginJnlpName.substring(15);
		}
		File pluginJarJnlpFile = new File(appServerDir, pluginJnlpName);
		if (pluginJarJnlpFile.exists())
		{
			copyFile(pluginJarJnlpFile, new File(tmpWarDir, pluginJnlpName));
			// parse the jnlp and copy all the referenced jars over.
			Document document = getDocument(pluginJarJnlpFile);
			if (document != null)
			{
				List<String> jarNames = new ArrayList<String>();
				List<String> jnlpNames = new ArrayList<String>();
				parseJarNames(document.getChildNodes(), jarNames, jnlpNames);

				for (String jarName : jarNames)
				{
					// ignore everything copied from lib, those are moved to WEB-INF/lib later on
					if (jarName.startsWith("/lib/")) continue;
					File jarFile = new File(appServerDir, jarName);
					File jarTargetFile = new File(tmpWarDir, jarName);
					jarTargetFile.getParentFile().mkdirs();
					copyFile(jarFile, jarTargetFile);
					int index = jarName.indexOf("plugins/");
					if (index != -1)
					{
						jarName = jarName.substring(index + "plugins/".length());
					}
					writeFileEntry(fw, jarFile, jarName, writtenFiles);
				}

				for (String jnlpName : jnlpNames)
				{
					copyJnlp(tmpWarDir, appServerDir, jnlpName, fw, writtenFiles);
				}
			}
			else
			{
				Debug.error("jnlp file " + pluginJarJnlpFile + " couldn't be parsed, nothing copied");
			}
		}
	}

	private void parseJarNames(NodeList childNodes, List<String> jarNames, List<String> jnlpNames)
	{
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node node = childNodes.item(i);
			if (node.getNodeName().equalsIgnoreCase("jar"))
			{
				NamedNodeMap attributes = node.getAttributes();
				Node href = attributes.getNamedItem("href");
				if (href != null)
				{
					jarNames.add(href.getNodeValue());
				}
			}
			else if (node.getNodeName().equalsIgnoreCase("extension"))
			{
				NamedNodeMap attributes = node.getAttributes();
				Node href = attributes.getNamedItem("href");
				if (href != null)
				{
					jnlpNames.add(href.getNodeValue());
				}
			}
			parseJarNames(node.getChildNodes(), jarNames, jnlpNames);
		}
	}

	private Document getDocument(File jnlpFile)
	{
		try
		{
			String contents = Utils.getTXTFileContent(jnlpFile);
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			return docBuilder.parse(new InputSource(new StringReader(contents)));
		}
		catch (Exception e)
		{
			Debug.error("Error creating parsing the jnlp file: " + jnlpFile, e);
		}
		return null;
	}

	private static boolean deleteDirectory(File path)
	{
		if (path.exists())
		{
			File[] files = path.listFiles();
			for (File file : files)
			{
				if (file.isDirectory())
				{
					deleteDirectory(file);
				}
				else
				{
					file.delete();
				}
			}
		}
		return (path.delete());
	}

	private static Set<File> copyDir(File sourceDir, File destDir, boolean recusive) throws ExportException
	{
		Set<File> writtenFiles = new HashSet<File>();
		copyDir(sourceDir, destDir, recusive, writtenFiles);
		return writtenFiles;
	}

	private static void copyDir(File sourceDir, File destDir, boolean recusive, Set<File> writtenFiles) throws ExportException
	{
		if (!destDir.exists() && !destDir.mkdirs()) throw new ExportException("Can't create destination dir: " + destDir);
		File[] listFiles = sourceDir.listFiles();
		for (File file : listFiles)
		{
			if (file.isDirectory())
			{
				if (recusive) copyDir(file, new File(destDir, file.getName()), recusive, writtenFiles);
			}
			else
			{
				copyFile(file, new File(destDir, file.getName()));
				writtenFiles.add(file);
			}
		}
	}

	private static void copyFile(File sourceFile, File destFile) throws ExportException
	{
		if (!sourceFile.exists())
		{
			return;
		}
		try
		{
			if (!destFile.getParentFile().exists())
			{
				destFile.getParentFile().mkdirs();
			}
			if (!destFile.exists())
			{
				destFile.createNewFile();
			}
			FileChannel source = null;
			FileChannel destination = null;
			String compileLessWithNashorn = null;
			if (sourceFile.getName().endsWith(".less") &&
				(compileLessWithNashorn = ResourceProvider.compileLessWithNashorn(sourceFile.toURI().toURL())) != null)
			{
				File compiledLessFile = destFile;
				PrintWriter printWriter = new PrintWriter(compiledLessFile);
				printWriter.println(compileLessWithNashorn);
				printWriter.close();
			}
			else
			{
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				if (destination != null && source != null)
				{
					destination.transferFrom(source, 0, source.size());
				}
				if (source != null)
				{
					source.close();
				}
				if (destination != null)
				{
					destination.close();
				}
			}
		}
		catch (Exception e)
		{
			throw new ExportException("Cant'copy file from " + sourceFile + " to " + destFile, e);
		}

	}

	/**
	 * Check if all NG_LIBS can be found in the specified plugin locations.
	 * @return message to add path to the missing jar
	 */
	public String searchExportedPlugins()
	{
		File parent = null;
		if (System.getProperty("eclipse.home.location") != null)
			parent = new File(URI.create(System.getProperty("eclipse.home.location").replaceAll(" ", "%20")));
		else parent = new File(System.getProperty("user.dir"));

		List<String> pluginLocations = exportModel.getPluginLocations();
		for (String libName : NG_LIBS)
		{
			int i = 0;
			boolean found = false;
			while (!found && i < pluginLocations.size())
			{
				File pluginLocation = new File(pluginLocations.get(i));
				if (!pluginLocation.isAbsolute())
				{
					pluginLocation = new File(parent, pluginLocations.get(i));
				}
				FileFilter filter = new WildcardFileFilter(libName);
				File[] libs = pluginLocation.listFiles(filter);
				if (libs != null && libs.length > 0)
				{
					found = true;
					break;
				}
				i++;
			}
			if (!found) return libName;
		}
		return null;
	}
}
