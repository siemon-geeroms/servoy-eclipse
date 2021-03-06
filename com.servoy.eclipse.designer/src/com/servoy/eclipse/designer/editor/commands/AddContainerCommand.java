package com.servoy.eclipse.designer.editor.commands;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sablo.specification.PropertyDescription;
import org.sablo.specification.NGPackageSpecification;
import org.sablo.specification.WebComponentSpecProvider;
import org.sablo.specification.WebObjectSpecification;
import org.sablo.specification.WebLayoutSpecification;
import org.sablo.specification.property.ICustomType;
import org.sablo.websocket.utils.PropertyUtils;

import com.servoy.eclipse.core.ServoyModelManager;
import com.servoy.eclipse.designer.editor.BaseRestorableCommand;
import com.servoy.eclipse.designer.editor.BaseVisualFormEditor;
import com.servoy.eclipse.designer.editor.rfb.actions.handlers.PersistFinder;
import com.servoy.eclipse.designer.util.DesignerUtil;
import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.eclipse.ui.dialogs.FlatTreeContentProvider;
import com.servoy.eclipse.ui.dialogs.TreePatternFilter;
import com.servoy.eclipse.ui.dialogs.TreeSelectDialog;
import com.servoy.eclipse.ui.property.PersistContext;
import com.servoy.eclipse.ui.util.ElementUtil;
import com.servoy.j2db.persistence.AbstractBase;
import com.servoy.j2db.persistence.AbstractContainer;
import com.servoy.j2db.persistence.IBasicWebComponent;
import com.servoy.j2db.persistence.IChildWebObject;
import com.servoy.j2db.persistence.IDeveloperRepository;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.IRepository;
import com.servoy.j2db.persistence.ISupportChilds;
import com.servoy.j2db.persistence.ISupportFormElements;
import com.servoy.j2db.persistence.LayoutContainer;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.WebComponent;
import com.servoy.j2db.persistence.WebCustomType;
import com.servoy.j2db.util.Debug;


public class AddContainerCommand extends AbstractHandler implements IHandler
{
	public static final String COMMAND_ID = "com.servoy.eclipse.designer.rfb.add";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		try
		{
			final BaseVisualFormEditor activeEditor = DesignerUtil.getActiveEditor();
			if (activeEditor != null)
			{
				activeEditor.getCommandStack().execute(new BaseRestorableCommand("createLayoutContainer")
				{
					private IPersist persist;

					@Override
					public void execute()
					{
						try
						{
							PersistContext persistContext = DesignerUtil.getContentOutlineSelection();
							if (persistContext != null)
							{
								if (event.getParameter("com.servoy.eclipse.designer.editor.rfb.menu.customtype.property") != null)
								{
									if (persistContext.getPersist() instanceof IBasicWebComponent)
									{
										IBasicWebComponent parentBean = (IBasicWebComponent)ElementUtil.getOverridePersist(persistContext);
										addCustomType(parentBean, event.getParameter("com.servoy.eclipse.designer.editor.rfb.menu.customtype.property"), null,
											-1);
										persist = parentBean;
									}
								}
								else if (event.getParameter("com.servoy.eclipse.designer.editor.rfb.menu.add.spec") != null)
								{
									persist = addLayoutComponent(persistContext, event.getParameter("com.servoy.eclipse.designer.editor.rfb.menu.add.spec"),
										event.getParameter("com.servoy.eclipse.designer.editor.rfb.menu.add.package"),
										new JSONObject(event.getParameter("com.servoy.eclipse.designer.editor.rfb.menu.add.config")),
										computeNextLayoutContainerIndex(persistContext.getPersist()));
								}
								else
								{
									List<String> specs = new ArrayList<String>();
									WebObjectSpecification[] webComponentSpecifications = WebComponentSpecProvider.getInstance().getAllWebComponentSpecifications();
									for (WebObjectSpecification webComponentSpec : webComponentSpecifications)
									{
										if (!webComponentSpec.getPackageName().equals("servoydefault"))
										{
											specs.add(webComponentSpec.getName());
										}
									}
									Collections.sort(specs);
									TreeSelectDialog dialog = new TreeSelectDialog(new Shell(), true, true, TreePatternFilter.FILTER_LEAFS,
										FlatTreeContentProvider.INSTANCE, new LabelProvider(), null, null, SWT.NONE, "Select spec",
										specs.toArray(new String[0]), null, false, "SpecDialog", null);
									dialog.open();
									if (dialog.getReturnCode() == Window.OK)
									{
										if (persistContext.getPersist() instanceof AbstractContainer)
										{
											AbstractContainer parentPersist = (AbstractContainer)ElementUtil.getOverridePersist(persistContext);
											String componentName = (String)((StructuredSelection)dialog.getSelection()).getFirstElement();
											int index = componentName.indexOf("-");
											if (index != -1)
											{
												componentName = componentName.substring(index + 1);
											}
											componentName = componentName.replaceAll("-", "_");
											String baseName = componentName;
											int i = 1;
											while (!PersistFinder.INSTANCE.checkName(activeEditor, componentName))
											{
												componentName = baseName + "_" + i;
												i++;
											}
											persist = parentPersist.createNewWebComponent(componentName,
												(String)((StructuredSelection)dialog.getSelection()).getFirstElement());
										}
									}
								}
								if (persist != null)
								{
									ServoyModelManager.getServoyModelManager().getServoyModel().firePersistsChanged(false,
										Arrays.asList(new IPersist[] { persist }));
									Object[] selection = new Object[] { persist };
									IStructuredSelection structuredSelection = new StructuredSelection(selection);
									DesignerUtil.getContentOutline().setSelection(structuredSelection);
								}
							}
						}
						catch (Exception ex)
						{
							Debug.error(ex);
						}
					}

					@Override
					public void undo()
					{
						try
						{
							if (persist != null)
							{
								((IDeveloperRepository)persist.getRootObject().getRepository()).deleteObject(persist);
								ServoyModelManager.getServoyModelManager().getServoyModel().firePersistsChanged(false,
									Arrays.asList(new IPersist[] { persist }));
							}
						}
						catch (RepositoryException e)
						{
							ServoyLog.logError("Could not undo create layout container", e);
						}
					}
				});
			}
		}
		catch (NullPointerException npe)
		{
			Debug.log(npe);
		}
		return null;
	}

	private LayoutContainer addLayoutComponent(PersistContext parentPersist, String specName, String packageName, JSONObject configJson, int index)
	{
		LayoutContainer container;
		try
		{
			if (parentPersist != null && parentPersist.getPersist() instanceof AbstractBase && parentPersist.getPersist() instanceof ISupportChilds)
			{
				AbstractBase parent = (AbstractBase)ElementUtil.getOverridePersist(parentPersist);
				NGPackageSpecification<WebLayoutSpecification> specifications = WebComponentSpecProvider.getInstance().getLayoutSpecifications().get(
					packageName);
				container = (LayoutContainer)parent.getRootObject().getChangeHandler().createNewObject(((ISupportChilds)parent), IRepository.LAYOUTCONTAINERS);
				container.setSpecName(specName);
				container.setPackageName(packageName);
				parent.addChild(container);
				container.setLocation(new Point(index, index));
				if (configJson != null)
				{
					Iterator keys = configJson.keys();
					while (keys.hasNext())
					{
						String key = (String)keys.next();
						Object value = configJson.get(key);
						if ("children".equals(key))
						{
							// special key to create children instead of a attribute set.
							JSONArray array = (JSONArray)value;
							for (int i = 0; i < array.length(); i++)
							{
								JSONObject jsonObject = array.getJSONObject(i);
								if (jsonObject.has("layoutName"))
								{
									WebLayoutSpecification spec = specifications.getSpecification(jsonObject.getString("layoutName"));
									addLayoutComponent(PersistContext.create(container, parentPersist.getContext()), spec.getName(), packageName,
										jsonObject.optJSONObject("model"), i + 1);
								}
								else if (jsonObject.has("componentName"))
								{
									WebComponent component = (WebComponent)parent.getRootObject().getChangeHandler().createNewObject(((ISupportChilds)parent),
										IRepository.WEBCOMPONENTS);
									component.setLocation(new Point(i + 1, i + 1));
									component.setTypeName(jsonObject.getString("componentName"));
									((AbstractBase)container).addChild(component);
								}
							}
						} // children and layoutName are special
						else if (!"layoutName".equals(key)) container.putAttribute(key, value.toString());
					}
					return container;
				}
			}
		}
		catch (RepositoryException e)
		{
			Debug.log(e);
		}
		catch (JSONException e)
		{
			Debug.log(e);
		}
		return null;
	}

	private int computeNextLayoutContainerIndex(IPersist parent)
	{
		int i = 1;
		if (parent instanceof ISupportFormElements)
		{
			Iterator<IPersist> allObjects = ((ISupportFormElements)parent).getAllObjects();

			while (allObjects.hasNext())
			{
				IPersist child = allObjects.next();
				if (child instanceof LayoutContainer)
				{
					i++;
				}
			}
			return i;
		}
		return i;
	}

	public static WebCustomType addCustomType(IBasicWebComponent parentBean, String propertyName, String compName, int arrayIndex)
	{
		int index = arrayIndex;
		WebObjectSpecification spec = WebComponentSpecProvider.getInstance().getWebComponentSpecification(parentBean.getTypeName());
		boolean isArray = spec.isArrayReturnType(propertyName);
		PropertyDescription targetPD = spec.getProperty(propertyName);
		String typeName = PropertyUtils.getSimpleNameOfCustomJSONTypeProperty(targetPD.getType());
		IChildWebObject[] arrayValue = null;
		if (isArray)
		{
			targetPD = ((ICustomType< ? >)targetPD.getType()).getCustomJSONTypeDefinition();
			if (parentBean instanceof WebComponent)
			{
				arrayValue = (IChildWebObject[])((WebComponent)parentBean).getProperty(propertyName);
			}
			if (index == -1) index = arrayValue != null ? arrayValue.length : 0;
		}
		if (parentBean instanceof WebComponent)
		{
			WebCustomType bean = WebCustomType.createNewInstance(parentBean, targetPD, propertyName, index, true);
			bean.setName(compName);
			bean.setTypeName(typeName);
			if (isArray)
			{
				if (arrayValue == null)
				{
					arrayValue = new IChildWebObject[] { bean };
				}
				else
				{
					if (index > -1)
					{
						ArrayList<IChildWebObject> arrayList = new ArrayList<IChildWebObject>();
						arrayList.addAll(Arrays.asList(arrayValue));
						arrayList.add(index, bean);
						arrayValue = arrayList.toArray(new IChildWebObject[arrayList.size()]);
					}
					else
					{
						IChildWebObject[] newArrayValue = new IChildWebObject[arrayValue.length + 1];
						System.arraycopy(arrayValue, 0, newArrayValue, 0, arrayValue.length);
						newArrayValue[arrayValue.length] = bean;
						arrayValue = newArrayValue;
					}
				}
				((WebComponent)parentBean).setProperty(propertyName, arrayValue);
			}
			else((WebComponent)parentBean).setProperty(propertyName, bean);

			return bean;
		}
		return null;
	}
}
