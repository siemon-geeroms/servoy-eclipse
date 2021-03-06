package com.servoy.eclipse.model.war.exporter;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.servoy.eclipse.model.export.IExportSolutionModel;


/**
 * WAR export model
 *
 * @author gboros
 * @since 8.0
 */
public interface IWarExportModel extends IExportSolutionModel
{
	public boolean isExportActiveSolution();

	public String getWarFileName();

	public String getServoyPropertiesFileName();

	public String getServoyApplicationServerDir();

	public boolean allowOverwriteSocketFactoryProperties();

	public List<String> getDrivers();

	public List<String> getLafs();

	public List<String> getPlugins();

	public List<String> getBeans();

	public boolean getStartRMI();

	public String getStartRMIPort();

	public SortedSet<String> getSelectedServerNames();

	public ServerConfiguration getServerConfiguration(String serverName);

	public List<String> getPluginLocations();

	public Set<String> getExportedComponents();

	public Set<String> getExportedServices();

	public Set<String> getUsedComponents();

	public Set<String> getUsedServices();

	public List<String> getExcludedComponentPackages();

	public List<String> getExcludedServicePackages();

	public boolean isOverwriteGroups();

	public boolean isAllowSQLKeywords();

	public boolean isOverrideSequenceTypes();

	public boolean isInsertNewI18NKeysOnly();

	public boolean isOverrideDefaultValues();

	public int getImportUserPolicy();

	public boolean isAddUsersToAdminGroup();

	public boolean isAllowDataModelChanges();

	public boolean isUpdateSequences();

	boolean isAutomaticallyUpgradeRepository();

	boolean isCreateTomcatContextXML();

	boolean isAntiResourceLocking();

	boolean isClearReferencesStatic();

	boolean isClearReferencesStopThreads();

	boolean isClearReferencesStopTimerThreads();

	/**
	 * admin properties
	 */
	public String getDefaultAdminUser();

	public String getDefaultAdminPassword();

}
