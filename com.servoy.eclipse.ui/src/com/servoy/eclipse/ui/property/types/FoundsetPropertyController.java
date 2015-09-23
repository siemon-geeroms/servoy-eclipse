/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2014 Servoy BV

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

package com.servoy.eclipse.ui.property.types;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.json.JSONException;
import org.json.JSONObject;

import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.eclipse.ui.dialogs.DataProviderTreeViewer;
import com.servoy.eclipse.ui.dialogs.DataProviderTreeViewer.DataProviderOptions;
import com.servoy.eclipse.ui.dialogs.DataProviderTreeViewer.DataProviderOptions.INCLUDE_RELATIONS;
import com.servoy.eclipse.ui.editors.DataProviderCellEditor;
import com.servoy.eclipse.ui.editors.DataProviderCellEditor.DataProviderValueEditor;
import com.servoy.eclipse.ui.labelproviders.DataProviderLabelProvider;
import com.servoy.eclipse.ui.labelproviders.FormContextDelegateLabelProvider;
import com.servoy.eclipse.ui.labelproviders.RelationLabelProvider;
import com.servoy.eclipse.ui.labelproviders.SolutionContextDelegateLabelProvider;
import com.servoy.eclipse.ui.property.CheckboxPropertyDescriptor;
import com.servoy.eclipse.ui.property.ComplexProperty;
import com.servoy.eclipse.ui.property.ComplexProperty.ComplexPropertyConverter;
import com.servoy.eclipse.ui.property.ComplexPropertySource;
import com.servoy.eclipse.ui.property.DataProviderConverter;
import com.servoy.eclipse.ui.property.ICellEditorFactory;
import com.servoy.eclipse.ui.property.IPropertyConverter;
import com.servoy.eclipse.ui.property.PersistContext;
import com.servoy.eclipse.ui.property.PropertyController;
import com.servoy.eclipse.ui.property.RelationPropertyController;
import com.servoy.j2db.FlattenedSolution;
import com.servoy.j2db.persistence.Relation;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.Table;
import com.servoy.j2db.server.ngclient.property.FoundsetPropertyTypeConfig;
import com.servoy.j2db.util.ServoyJSONObject;
import com.servoy.j2db.util.Utils;


/**
 *
 * Property controller for foundset properties.
 * @author gboros
 *
 */
public class FoundsetPropertyController extends PropertyController<JSONObject, Object>
{
	private static final String USE_FORM_FOUNDSET = "useFormFoundset";
	private static final String FOUNDSET_RELATION = "foundsetRelation";

	private static final String FOUNDSET_SELECTOR = "foundsetSelector";
	private static final String FOUNDSET_DP_COUNT = "foundsetDataprovidersCount";

	private static final String FORM_FOUNDSET = "formFoundset";

	private final FlattenedSolution flattenedSolution;
	private final PersistContext persistContext;
	private final FoundsetPropertyTypeConfig config;

	/**
	 * @param id
	 * @param displayName
	 */
	public FoundsetPropertyController(Object id, String displayName, FlattenedSolution flattenedSolution, PersistContext persistContext,
		FoundsetPropertyTypeConfig foundsetPropertyTypeConfig)
	{
		super(id, displayName);
		this.flattenedSolution = flattenedSolution;
		this.persistContext = persistContext;
		this.config = foundsetPropertyTypeConfig;
	}

	@Override
	protected IPropertyConverter<JSONObject, Object> createConverter()
	{
		return new FoundsetPropertyConverter();
	}

	private ILabelProvider labelProvider;

	@Override
	public ILabelProvider getLabelProvider()
	{
		if (labelProvider == null)
		{
			labelProvider = new LabelProvider()
			{
				@Override
				public String getText(Object element)
				{
					if (element != null)
					{
						try
						{
							JSONObject elementJSON = (JSONObject)element;
							if (elementJSON.has(FOUNDSET_SELECTOR))
							{
								StringBuilder sb = new StringBuilder();
								String fs = elementJSON.optString(FOUNDSET_SELECTOR);
								sb.append("".equals(fs) ? FORM_FOUNDSET : fs);
								if (elementJSON.has(FoundsetPropertyTypeConfig.DATAPROVIDERS))
								{
									sb.append('[');
									JSONObject dataproviders = elementJSON.optJSONObject(FoundsetPropertyTypeConfig.DATAPROVIDERS);
									if (dataproviders != null)
									{
										Iterator< ? > dpKeysIte = dataproviders.keys();
										while (dpKeysIte.hasNext())
										{
											String key = dpKeysIte.next().toString();
											sb.append(key).append(':').append(dataproviders.getString(key));
											if (dpKeysIte.hasNext()) sb.append(',');
										}
									}
									sb.append(']');
								}

								return sb.toString();
							}
						}
						catch (JSONException ex)
						{
							ServoyLog.logError(ex);
						}
					}
					return "";
				}
			};
		}

		return labelProvider;
	}

	class FoundsetPropertyConverter extends ComplexPropertyConverter<JSONObject>
	{
		@Override
		public Object convertProperty(Object id, JSONObject value)
		{
			ServoyJSONObject copy = null;
			try
			{
				if (value != null) copy = new ServoyJSONObject(value, ServoyJSONObject.getNames(value), true, true);
			}
			catch (Exception ex)
			{
				ServoyLog.logError(ex);
			}
			return new ComplexProperty<JSONObject>(copy)
			{
				@Override
				public IPropertySource getPropertySource()
				{
					FoundsetPropertySource foundsetPropertySource = new FoundsetPropertySource(this, flattenedSolution, persistContext, config.dataproviders,
						config.hasDynamicDataproviders);
					foundsetPropertySource.setReadonly(FoundsetPropertyController.this.isReadOnly());
					return foundsetPropertySource;
				}
			};
		}
	}

	static class FoundsetPropertySource extends ComplexPropertySource<JSONObject>
	{
		private final FlattenedSolution flattenedSolution;
		private final PersistContext persistContext;
		private final String[] dataproviders;
		private final boolean hasDynamicDataproviders;

		private RelationPropertyController relationPropertyController;
		private Table table;

		private final ComplexProperty<JSONObject> complexProperty;

		public FoundsetPropertySource(ComplexProperty<JSONObject> complexProperty, FlattenedSolution flattenedSolution, PersistContext persistContext,
			String[] dataproviders, boolean hasDynamicDataproviders)
		{
			super(complexProperty);
			this.complexProperty = complexProperty;
			this.flattenedSolution = flattenedSolution;
			this.persistContext = persistContext;
			this.dataproviders = dataproviders;
			this.hasDynamicDataproviders = hasDynamicDataproviders;

			try
			{
				table = flattenedSolution.getFlattenedForm(persistContext.getPersist()).getTable();
			}
			catch (RepositoryException ex)
			{
				ServoyLog.logError(ex);
			}
		}

		@Override
		public IPropertyDescriptor[] createPropertyDescriptors()
		{
			ArrayList<IPropertyDescriptor> propertyDescriptors = new ArrayList<IPropertyDescriptor>();

			propertyDescriptors.add(new CheckboxPropertyDescriptor(USE_FORM_FOUNDSET, USE_FORM_FOUNDSET));

			relationPropertyController = new RelationPropertyController(FOUNDSET_RELATION, FOUNDSET_RELATION, persistContext, table, null /* foreignTable */,
				true, false);
			relationPropertyController.setLabelProvider(new SolutionContextDelegateLabelProvider(RelationLabelProvider.INSTANCE_ALL_NO_IMAGE,
				persistContext.getContext())
			{
				@Override
				public String getText(Object value)
				{
					return "".equals(value) ? FORM_FOUNDSET : super.getText(value);
				}
			});
			propertyDescriptors.add(relationPropertyController);

			if (dataproviders != null)
			{
				if (getEditableValue() != null)
				{
					for (String dp : dataproviders)
					{
						propertyDescriptors.add(createDataproviderPropertyDescriptor(dp, dp));
					}
				}
			}
			else if (hasDynamicDataproviders)
			{
				JSONObject v = getEditableValue();
				if (v != null)
				{
					propertyDescriptors.add(new TextPropertyDescriptor(FOUNDSET_DP_COUNT, FOUNDSET_DP_COUNT));
					int foundsetDPCount = v.optInt(FOUNDSET_DP_COUNT);
					for (int i = 1; i <= foundsetDPCount; i++)
					{
						propertyDescriptors.add(createDataproviderPropertyDescriptor("dp" + i, "dp" + i));
					}
				}
			}

			return PropertyController.applySequencePropertyComparator(propertyDescriptors.toArray(new IPropertyDescriptor[propertyDescriptors.size()]));
		}

		IPropertyDescriptor createDataproviderPropertyDescriptor(Object id, String displayName)
		{
			JSONObject v = getEditableValue();
			Relation[] relations = flattenedSolution.getRelationSequence(v.optString(FOUNDSET_SELECTOR));
			final DataProviderOptions options;
			if (relations != null)
			{
				options = new DataProviderTreeViewer.DataProviderOptions(true, false, false, true /* related calcs */, false, false, false, false,
					INCLUDE_RELATIONS.NESTED, false, true, relations);
			}
			else
			{
				options = new DataProviderTreeViewer.DataProviderOptions(true, table != null, table != null, table != null, true, true, table != null,
					table != null, INCLUDE_RELATIONS.NESTED, true, true, null);
			}


			final DataProviderConverter converter = new DataProviderConverter(flattenedSolution, persistContext.getPersist(), table);
			DataProviderLabelProvider showPrefix = new DataProviderLabelProvider(false);
			showPrefix.setConverter(converter);
			DataProviderLabelProvider hidePrefix = new DataProviderLabelProvider(true);
			hidePrefix.setConverter(converter);

			final ILabelProvider labelProviderHidePrefix = new SolutionContextDelegateLabelProvider(new FormContextDelegateLabelProvider(hidePrefix,
				persistContext.getContext()));
			PropertyController<String, String> propertyController = new PropertyController<String, String>(id, displayName, null, labelProviderHidePrefix,
				new ICellEditorFactory()
				{
					public CellEditor createPropertyEditor(Composite parent)
					{
						return new DataProviderCellEditor(parent, labelProviderHidePrefix, new DataProviderValueEditor(converter),
							flattenedSolution.getFlattenedForm(persistContext.getPersist()), flattenedSolution, readOnly, options, converter, null);
					}
				});
			propertyController.setSupportsReadonly(true);
			return propertyController;
		}

		@Override
		public Object getPropertyValue(Object id)
		{
			JSONObject v = getEditableValue();

			if (USE_FORM_FOUNDSET.equals(id))
			{
				return v == null ? Boolean.FALSE : Boolean.valueOf("".equals(v.optString(FOUNDSET_SELECTOR)));
			}
			else if (FOUNDSET_RELATION.equals(id))
			{
				if (v != null)
				{
					String foundsetSelector = v.optString(FOUNDSET_SELECTOR);
					return "".equals(foundsetSelector) ? null : relationPropertyController.getConverter().convertProperty(FOUNDSET_SELECTOR, foundsetSelector);
				}
			}
			else if (FOUNDSET_DP_COUNT.equals(id))
			{
				return v == null ? "0" : String.valueOf(v.optInt(FOUNDSET_DP_COUNT));
			}
			else
			{
				if (v != null)
				{
					JSONObject dataprovidersValues = v.optJSONObject(FoundsetPropertyTypeConfig.DATAPROVIDERS);
					if (dataprovidersValues != null && dataprovidersValues.has(id.toString()))
					{
						String foundsetSelector = v.optString(FOUNDSET_SELECTOR);
						String dp = dataprovidersValues.optString(id.toString());
						if (dp != null && foundsetSelector.length() > 0)
						{
							dp = foundsetSelector + "." + dp;
						}
						return dp;
					}
				}
			}

			return null;
		}

		@Override
		public JSONObject setComplexPropertyValue(Object id, Object v)
		{
			JSONObject editableValue = getEditableValue();
			if (editableValue == null)
			{
				editableValue = new JSONObject();
				complexProperty.setValue(editableValue);
			}
			try
			{
				if (USE_FORM_FOUNDSET.equals(id))
				{
					if (((Boolean)v).booleanValue())
					{
						editableValue.put(FOUNDSET_SELECTOR, "");
					}
					else
					{
						editableValue = null;
						complexProperty.setValue(editableValue);
					}
				}
				else if (FOUNDSET_RELATION.equals(id))
				{
					String foundsetSelector = relationPropertyController.getConverter().convertValue(FOUNDSET_SELECTOR, v);
					if (foundsetSelector == null)
					{
						editableValue.remove(FOUNDSET_SELECTOR);
					}
					else
					{
						editableValue.put(FOUNDSET_SELECTOR, foundsetSelector);
					}
				}
				else if (FOUNDSET_DP_COUNT.equals(id))
				{
					int dpCount;
					try
					{
						dpCount = Integer.parseInt(v.toString());
					}
					catch (NumberFormatException ex)
					{
						dpCount = 0;
					}
					int oldValue = editableValue.optInt(FOUNDSET_DP_COUNT);
					if (dpCount > -1 && dpCount < 51)
					{
						editableValue.put(FOUNDSET_DP_COUNT, v.toString());
					}
					if (dpCount < oldValue)
					{
						JSONObject dataprovidersValues = editableValue.optJSONObject(FoundsetPropertyTypeConfig.DATAPROVIDERS);
						if (dataprovidersValues != null)
						{
							Iterator< ? > it = dataprovidersValues.keys();
							while (it.hasNext())
							{
								String dp = (String)it.next();
								int dpid = Utils.getAsInteger(dp.substring(2));
								if (dpid > dpCount)
								{
									it.remove();
								}
							}
						}
					}
				}
				else
				{
					JSONObject dataprovidersValues = editableValue.optJSONObject(FoundsetPropertyTypeConfig.DATAPROVIDERS);
					if (dataprovidersValues == null)
					{
						dataprovidersValues = new JSONObject();
						editableValue.put(FoundsetPropertyTypeConfig.DATAPROVIDERS, dataprovidersValues);

					}
					if (v != null)
					{
						String dpValue = v.toString();
						String foundsetSelector = editableValue.optString(FOUNDSET_SELECTOR);
						if (dpValue.startsWith(foundsetSelector + "."))
						{
							dpValue = dpValue.substring(foundsetSelector.length() + 1);
						}
						dataprovidersValues.put(id.toString(), dpValue);
					}
					else
					{
						dataprovidersValues.remove(id.toString());
					}
				}
			}
			catch (JSONException ex)
			{
				ServoyLog.logError(ex);
			}

			return editableValue;
		}
	}
}