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
package com.servoy.eclipse.ui.property;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.beans.IntrospectionException;
import java.beans.PropertyEditor;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.servoy.eclipse.core.Activator;
import com.servoy.eclipse.core.DesignComponentFactory;
import com.servoy.eclipse.core.ServoyModel;
import com.servoy.eclipse.core.ServoyModelManager;
import com.servoy.eclipse.core.repository.I18NMessagesUtil;
import com.servoy.eclipse.model.nature.ServoyProject;
import com.servoy.eclipse.model.repository.EclipseMessages;
import com.servoy.eclipse.model.repository.EclipseRepository;
import com.servoy.eclipse.model.util.ModelUtils;
import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.eclipse.model.util.TableWrapper;
import com.servoy.eclipse.ui.Messages;
import com.servoy.eclipse.ui.dialogs.DataProviderTreeViewer;
import com.servoy.eclipse.ui.dialogs.DataProviderTreeViewer.DataProviderOptions;
import com.servoy.eclipse.ui.dialogs.DataProviderTreeViewer.DataProviderOptions.INCLUDE_RELATIONS;
import com.servoy.eclipse.ui.dialogs.FormContentProvider;
import com.servoy.eclipse.ui.dialogs.FormContentProvider.FormListOptions;
import com.servoy.eclipse.ui.dialogs.MethodDialog.MethodListOptions;
import com.servoy.eclipse.ui.dialogs.TableContentProvider;
import com.servoy.eclipse.ui.dialogs.TableContentProvider.TableListOptions;
import com.servoy.eclipse.ui.editors.BeanCustomCellEditor;
import com.servoy.eclipse.ui.editors.DataProviderCellEditor;
import com.servoy.eclipse.ui.editors.DataProviderCellEditor.DataProviderValueEditor;
import com.servoy.eclipse.ui.editors.FontCellEditor;
import com.servoy.eclipse.ui.editors.FormatCellEditor;
import com.servoy.eclipse.ui.editors.ListSelectCellEditor;
import com.servoy.eclipse.ui.editors.PageFormatEditor;
import com.servoy.eclipse.ui.editors.SortCellEditor;
import com.servoy.eclipse.ui.editors.TabSeqDialogCellEditor;
import com.servoy.eclipse.ui.editors.TabSeqDialogCellEditor.TabSeqDialogValueEditor;
import com.servoy.eclipse.ui.editors.TagsAndI18NTextCellEditor;
import com.servoy.eclipse.ui.labelproviders.ArrayLabelProvider;
import com.servoy.eclipse.ui.labelproviders.DataProviderLabelProvider;
import com.servoy.eclipse.ui.labelproviders.DatasourceLabelProvider;
import com.servoy.eclipse.ui.labelproviders.FontLabelProvider;
import com.servoy.eclipse.ui.labelproviders.FormContextDelegateLabelProvider;
import com.servoy.eclipse.ui.labelproviders.FormLabelProvider;
import com.servoy.eclipse.ui.labelproviders.MediaLabelProvider;
import com.servoy.eclipse.ui.labelproviders.PageFormatLabelProvider;
import com.servoy.eclipse.ui.labelproviders.PersistInheritenceDelegateLabelProvider;
import com.servoy.eclipse.ui.labelproviders.SolutionContextDelegateLabelProvider;
import com.servoy.eclipse.ui.labelproviders.TextCutoffLabelProvider;
import com.servoy.eclipse.ui.labelproviders.ValidvalueDelegatelabelProvider;
import com.servoy.eclipse.ui.labelproviders.ValuelistLabelProvider;
import com.servoy.eclipse.ui.property.ComplexProperty.ComplexPropertyConverter;
import com.servoy.eclipse.ui.property.MethodWithArguments.UnresolvedMethodWithArguments;
import com.servoy.eclipse.ui.resource.FontResource;
import com.servoy.eclipse.ui.util.DocumentValidatorVerifyListener;
import com.servoy.eclipse.ui.util.EditorUtil;
import com.servoy.eclipse.ui.util.ElementUtil;
import com.servoy.eclipse.ui.util.IDefaultValue;
import com.servoy.eclipse.ui.util.MediaNode;
import com.servoy.eclipse.ui.util.VerifyingTextCellEditor;
import com.servoy.eclipse.ui.views.properties.IMergeablePropertyDescriptor;
import com.servoy.eclipse.ui.views.properties.IMergedPropertyDescriptor;
import com.servoy.j2db.FlattenedSolution;
import com.servoy.j2db.FormController;
import com.servoy.j2db.FormManager;
import com.servoy.j2db.IForm;
import com.servoy.j2db.component.ComponentFactory;
import com.servoy.j2db.component.ComponentFormat;
import com.servoy.j2db.dataui.PropertyEditorClass;
import com.servoy.j2db.dataui.PropertyEditorHint;
import com.servoy.j2db.dataui.PropertyEditorOption;
import com.servoy.j2db.persistence.AbstractBase;
import com.servoy.j2db.persistence.AbstractRepository;
import com.servoy.j2db.persistence.AggregateVariable;
import com.servoy.j2db.persistence.Bean;
import com.servoy.j2db.persistence.Column;
import com.servoy.j2db.persistence.ColumnWrapper;
import com.servoy.j2db.persistence.ContentSpec.Element;
import com.servoy.j2db.persistence.Field;
import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.GraphicalComponent;
import com.servoy.j2db.persistence.IColumnTypes;
import com.servoy.j2db.persistence.IDataProvider;
import com.servoy.j2db.persistence.IDataProviderLookup;
import com.servoy.j2db.persistence.IDeveloperRepository;
import com.servoy.j2db.persistence.IFormElement;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.IRepository;
import com.servoy.j2db.persistence.IRootObject;
import com.servoy.j2db.persistence.IScriptProvider;
import com.servoy.j2db.persistence.ISupportDataProviderID;
import com.servoy.j2db.persistence.ISupportExtendsID;
import com.servoy.j2db.persistence.ISupportName;
import com.servoy.j2db.persistence.ISupportUpdateableName;
import com.servoy.j2db.persistence.ITableDisplay;
import com.servoy.j2db.persistence.Media;
import com.servoy.j2db.persistence.Part;
import com.servoy.j2db.persistence.Portal;
import com.servoy.j2db.persistence.RectShape;
import com.servoy.j2db.persistence.Relation;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.RepositoryHelper;
import com.servoy.j2db.persistence.RootObjectMetaData;
import com.servoy.j2db.persistence.RuntimeProperty;
import com.servoy.j2db.persistence.ScriptCalculation;
import com.servoy.j2db.persistence.ScriptMethod;
import com.servoy.j2db.persistence.ScriptVariable;
import com.servoy.j2db.persistence.Solution;
import com.servoy.j2db.persistence.SolutionMetaData;
import com.servoy.j2db.persistence.StaticContentSpecLoader;
import com.servoy.j2db.persistence.Style;
import com.servoy.j2db.persistence.Tab;
import com.servoy.j2db.persistence.TabPanel;
import com.servoy.j2db.persistence.Table;
import com.servoy.j2db.persistence.ValidatorSearchContext;
import com.servoy.j2db.persistence.ValueList;
import com.servoy.j2db.query.ISQLJoin;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.smart.dataui.InvisibleBean;
import com.servoy.j2db.util.ComponentFactoryHelper;
import com.servoy.j2db.util.DataSourceUtilsBase;
import com.servoy.j2db.util.IDelegate;
import com.servoy.j2db.util.PersistHelper;
import com.servoy.j2db.util.SafeArrayList;
import com.servoy.j2db.util.Utils;

/**
 * Property source for IPersist objects.
 * This class manages contents of the properties view by returning a list of 
 * property descriptors depending on value and context of the persist object.
 * 
 * @author rgansevles
 */

@SuppressWarnings("nls")
public class PersistPropertySource implements IPropertySource, IAdaptable, IModelSavePropertySource
{
	public static final IPropertyController<Integer, Integer> HORIZONTAL_ALIGNMENT_CONTROLLER;
	public static final IPropertyController<Integer, Integer> SELECTION_MODE_CONTROLLER;
	public static final IPropertyController<Integer, Integer> VERTICAL_ALIGNMENT_CONTROLLER;
	public static final IPropertyController<Integer, Integer> ROLLOVER_CURSOR_CONTROLLER;
	public static final IPropertyController<Integer, Integer> ROTATION_CONTROLLER;
	public static final IPropertyController<Integer, Integer> SHAPE_TYPE_CONTOLLER;
	public static final IPropertyController<Integer, Integer> VIEW_TYPE_CONTOLLER;
	public static final IPropertyController<Integer, Integer> DISPLAY_TYPE_CONTOLLER;
	public static final IPropertyController<Integer, Integer> TAB_ORIENTATION_CONTROLLER;
	public static final IPropertyController<String, String> MNEMONIC_CONTROLLER;
	public static final IPropertyController<Integer, Integer> TEXT_ORIENTATION_CONTROLLER;
	public static final IPropertyController<Integer, Integer> SOLUTION_TYPE_CONTROLLER;
	public static final IPropertyController<Integer, Integer> JOIN_TYPE_CONTROLLER;
	public static final IPropertyController<Integer, Object> SLIDING_OPTIONS_CONTROLLER;
	public static final IPropertyController<Integer, Object> ANCHOR_CONTROLLER;
	public static final IPropertyController<Integer, Object> ENCAPSULATION_CONTROLLER;
	public static final IPropertyController<String, PageFormat> PAGE_FORMAT_CONTROLLER;
	public static final IPropertyController<String, String> LOGIN_SOLUTION_CONTROLLER;
	public static final IPropertyController<Map<String, Object>, Map<String, Object>> DESIGN_PROPERTIES_CONTROLLER;
	public static final IPropertyController<String, Object[]> COMMA_SEPARATED_CONTROLLER;

	public static final IPropertyConverter<String, Border> BORDER_STRING_CONVERTER;

	public static final Comparator<java.beans.PropertyDescriptor> BEANS_PROPERTY_COMPARATOR;

	static
	{
		HORIZONTAL_ALIGNMENT_CONTROLLER = new ComboboxPropertyController<Integer>(
			"horizontalAlignment",
			RepositoryHelper.getDisplayName("horizontalAlignment", Field.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(-1), Integer.valueOf(SwingConstants.LEFT), Integer.valueOf(SwingConstants.CENTER), Integer.valueOf(SwingConstants.RIGHT) },
				new String[] { Messages.LabelDefault, Messages.AlignLeft, Messages.AlignCenter, Messages.AlignRight }), Messages.LabelUnresolved);
		SELECTION_MODE_CONTROLLER = new ComboboxPropertyController<Integer>(
			"selectionMode",
			RepositoryHelper.getDisplayName("selectionMode", Form.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(IForm.SELECTION_MODE_DEFAULT), Integer.valueOf(IForm.SELECTION_MODE_SINGLE), Integer.valueOf(IForm.SELECTION_MODE_MULTI) },
				new String[] { Messages.LabelDefault, Messages.SelectionModeSingle, Messages.SelectionModeMulti }), Messages.LabelUnresolved);
		SHAPE_TYPE_CONTOLLER = new ComboboxPropertyController<Integer>(
			"shapeType",
			RepositoryHelper.getDisplayName("shapeType", RectShape.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(RectShape.BORDER_PANEL), Integer.valueOf(RectShape.RECTANGLE), Integer.valueOf(RectShape.ROUNDED_RECTANGLE), Integer.valueOf(RectShape.OVAL) },
				new String[] { "BORDER_PANEL", "RECTANGLE", "ROUNDED_RECTANGLE", "OVAL" }), Messages.LabelUnresolved);
		TAB_ORIENTATION_CONTROLLER = new ComboboxPropertyController<Integer>(
			"tabOrientation",
			RepositoryHelper.getDisplayName("tabOrientation", TabPanel.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(TabPanel.DEFAULT), Integer.valueOf(SwingConstants.TOP), Integer.valueOf(SwingConstants.RIGHT), Integer.valueOf(SwingConstants.BOTTOM), Integer.valueOf(SwingConstants.LEFT), Integer.valueOf(TabPanel.HIDE), Integer.valueOf(TabPanel.SPLIT_HORIZONTAL), Integer.valueOf(TabPanel.SPLIT_VERTICAL), Integer.valueOf(TabPanel.ACCORDION_PANEL) },
				new String[] { Messages.LabelDefault, Messages.AlignTop, Messages.AlignRight, Messages.AlignBottom, Messages.AlignLeft, "HIDE", "SPLIT HORIZONTAL", "SPLIT VERTICAL", "ACCORDION PANE" }),
			Messages.LabelUnresolved);
		DISPLAY_TYPE_CONTOLLER = new ComboboxPropertyController<Integer>(
			"displayType",
			RepositoryHelper.getDisplayName("displayType", Field.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(Field.TEXT_FIELD), Integer.valueOf(Field.TEXT_AREA), Integer.valueOf(Field.RTF_AREA), Integer.valueOf(Field.HTML_AREA), Integer.valueOf(Field.TYPE_AHEAD), Integer.valueOf(Field.COMBOBOX), Integer.valueOf(Field.RADIOS), Integer.valueOf(Field.CHECKS), Integer.valueOf(Field.CALENDAR), Integer.valueOf(Field.IMAGE_MEDIA), Integer.valueOf(Field.PASSWORD), Integer.valueOf(Field.LIST_BOX), Integer.valueOf(Field.MULTISELECT_LISTBOX), Integer.valueOf(Field.SPINNER) },
				new String[] { "TEXT_FIELD", "TEXT_AREA", "RTF_AREA", "HTML_AREA", "TYPE_AHEAD", "COMBOBOX", "RADIOS", "CHECK", "CALENDAR", "IMAGE_MEDIA", "PASSWORD", "LISTBOX", "MULTISELECT_LISTBOX", "SPINNER" }),
			Messages.LabelUnresolved);

		BORDER_STRING_CONVERTER = new IPropertyConverter<String, Border>()
		{
			public Border convertProperty(Object property, String value)
			{
				return ComponentFactoryHelper.createBorder(value, true);
			}

			public String convertValue(Object property, Border value)
			{
				return ComponentFactoryHelper.createBorderString(value);
			}
		};
		JOIN_TYPE_CONTROLLER = new ComboboxPropertyController<Integer>("joinType", RepositoryHelper.getDisplayName("joinType", Relation.class),
			new ComboboxPropertyModel<Integer>(

			new Integer[] { Integer.valueOf(ISQLJoin.INNER_JOIN), Integer.valueOf(ISQLJoin.LEFT_OUTER_JOIN) },
				new String[] { ISQLJoin.JOIN_TYPES_NAMES[ISQLJoin.INNER_JOIN], ISQLJoin.JOIN_TYPES_NAMES[ISQLJoin.LEFT_OUTER_JOIN] }), Messages.LabelUnresolved);

		Integer[] ia = new Integer[SolutionMetaData.solutionTypes.length];
		for (int i = 0; i < ia.length; i++)
		{
			ia[i] = Integer.valueOf(SolutionMetaData.solutionTypes[i]);
		}
		SOLUTION_TYPE_CONTROLLER = new ComboboxPropertyController<Integer>("solutionType", RepositoryHelper.getDisplayName("solutionType", Solution.class),
			new ComboboxPropertyModel<Integer>(ia, SolutionMetaData.solutionTypeNames), Messages.LabelUnresolved);

		MNEMONIC_CONTROLLER = new EditableComboboxPropertyController("mnemonic", RepositoryHelper.getDisplayName("mnemonic", GraphicalComponent.class),
			new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" });

		VIEW_TYPE_CONTOLLER = new ComboboxPropertyController<Integer>(
			"view",
			RepositoryHelper.getDisplayName("view", Form.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(IForm.RECORD_VIEW), Integer.valueOf(IForm.LIST_VIEW), Integer.valueOf(IForm.LOCKED_RECORD_VIEW), Integer.valueOf(FormController.LOCKED_LIST_VIEW), Integer.valueOf(FormController.LOCKED_TABLE_VIEW) },
				new String[] { "Record view", "List view", "Record view (locked)", "List view (locked)", "Table view (locked)" }), Messages.LabelUnresolved);

		ROTATION_CONTROLLER = new ComboboxPropertyController<Integer>("rotation", RepositoryHelper.getDisplayName("rotation", GraphicalComponent.class),
			new ComboboxPropertyModel<Integer>(new Integer[] { Integer.valueOf(0), Integer.valueOf(90), Integer.valueOf(180), Integer.valueOf(270) }),
			Messages.LabelUnresolved);

		ROLLOVER_CURSOR_CONTROLLER = new ComboboxPropertyController<Integer>("rolloverCursor", RepositoryHelper.getDisplayName("rolloverCursor",
			GraphicalComponent.class), new ComboboxPropertyModel<Integer>(
			new Integer[] { Integer.valueOf(Cursor.DEFAULT_CURSOR), Integer.valueOf(Cursor.HAND_CURSOR) },
			new String[] { Messages.LabelDefault, Messages.CursorHand }), Messages.LabelUnresolved);
		VERTICAL_ALIGNMENT_CONTROLLER = new ComboboxPropertyController<Integer>(
			"verticalAlignment",
			RepositoryHelper.getDisplayName("verticalAlignment", Field.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(-1), Integer.valueOf(SwingConstants.TOP), Integer.valueOf(SwingConstants.CENTER), Integer.valueOf(SwingConstants.BOTTOM) },
				new String[] { Messages.LabelDefault, Messages.AlignTop, Messages.AlignCenter, Messages.AlignBottom }), Messages.LabelUnresolved);

		TEXT_ORIENTATION_CONTROLLER = new ComboboxPropertyController<Integer>(
			"textOrientation",
			RepositoryHelper.getDisplayName("textOrientation", Solution.class),
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(Solution.TEXT_ORIENTATION_DEFAULT), Integer.valueOf(Solution.TEXT_ORIENTATION_LEFT_TO_RIGHT), Integer.valueOf(Solution.TEXT_ORIENTATION_RIGHT_TO_LEFT), Integer.valueOf(Solution.TEXT_ORIENTATION_LOCALE_SPECIFIC) },
				new String[] { Messages.LabelDefault, Messages.OrientationLeftToRight, Messages.OrientationRightToLeft, Messages.OrientationLocaleSpecific }),
			Messages.LabelUnresolved);

		SLIDING_OPTIONS_CONTROLLER = new SlidingoptionsPropertyController("printSliding", RepositoryHelper.getDisplayName("printSliding",
			GraphicalComponent.class));

		ANCHOR_CONTROLLER = new AnchorPropertyController("anchors", RepositoryHelper.getDisplayName("anchors", GraphicalComponent.class));
		ENCAPSULATION_CONTROLLER = new EncapsulationPropertyController("encapsulation", RepositoryHelper.getDisplayName("encapsulation", Form.class));

		PAGE_FORMAT_CONTROLLER = new PropertyController<String, PageFormat>("defaultPageFormat", RepositoryHelper.getDisplayName("defaultPageFormat",
			Form.class), new IPropertyConverter<String, PageFormat>()
		{
			public PageFormat convertProperty(Object id, String value)
			{
				return PersistHelper.createPageFormat(value);
			}

			public String convertValue(Object id, PageFormat value)
			{
				return PersistHelper.createPageFormatString(value);
			}

		}, PageFormatLabelProvider.INSTANCE, new ICellEditorFactory()
		{
			public CellEditor createPropertyEditor(Composite parent)
			{
				return new PageFormatEditor(parent, "Page Setup", PageFormatLabelProvider.INSTANCE);
			}
		});

		LOGIN_SOLUTION_CONTROLLER = new LoginSolutionPropertyController("loginSolutionName", RepositoryHelper.getDisplayName("loginSolutionName",
			Solution.class));

		DESIGN_PROPERTIES_CONTROLLER = new PropertySetterDelegatePropertyController<Map<String, Object>, PersistPropertySource>(
			new MapEntriesPropertyController("designTimeProperties", RepositoryHelper.getDisplayName("designTimeProperties", Form.class)),
			"designTimeProperties")
		{
			@Override
			public Map<String, Object> getProperty(PersistPropertySource propSource)
			{
				IPersist persist = propSource.getPersist();
				if (persist instanceof AbstractBase)
				{
					return ((AbstractBase)persist).getMergedCustomDesignTimeProperties(); // returns non-null map with copied/merged values, may be written to
				}
				return null;
			}

			public void setProperty(PersistPropertySource propSource, Map<String, Object> value)
			{
				IPersist persist = propSource.getPersist();
				if (persist instanceof AbstractBase)
				{
					((AbstractBase)persist).setUnmergedCustomDesignTimeProperties(value);
				}
			}
		};

		COMMA_SEPARATED_CONTROLLER = new PropertyController<String, Object[]>("groupbyDataProviderIDs", RepositoryHelper.getDisplayName(
			"groupbyDataProviderIDs", Part.class), new StringTokenizerConverter(",", true), null, null);

		BEANS_PROPERTY_COMPARATOR = new Comparator<java.beans.PropertyDescriptor>()
		{
			public int compare(java.beans.PropertyDescriptor p1, java.beans.PropertyDescriptor p2)
			{
				Object hint1 = p1.getValue(PropertyEditorHint.PROPERTY_EDITOR_HINT);
				Object hint2 = p2.getValue(PropertyEditorHint.PROPERTY_EDITOR_HINT);

				Object comp1 = null;
				Object comp2 = null;
				if (hint1 instanceof PropertyEditorHint)
				{
					comp1 = ((PropertyEditorHint)hint1).getOption(PropertyEditorOption.propertyOrder);
					if (comp1 instanceof Comparable< ? >)
					{
						if (hint2 instanceof PropertyEditorHint)
						{
							comp2 = ((PropertyEditorHint)hint2).getOption(PropertyEditorOption.propertyOrder);
						}
						if (!(comp2 instanceof Comparable< ? >))
						{
							// not both comparable, compare on displayName
							comp1 = comp2 = null;
						}
					}
				}
				if (!(comp1 instanceof Comparable< ? >))
				{
					comp1 = p1.getDisplayName();
					comp2 = p2.getDisplayName();
				}
				return comp1 == null ? (comp2 == null ? 0 : -1) : (comp2 == null ? 1 : ((Comparable)comp1).compareTo(comp2));
			}
		};
	}

	private static final String BEAN_PROPERTY_PREFIX_DOT = "bean.";

	public static IPropertyConverter<String, String> NULL_STRING_CONVERTER = new IPropertyConverter<String, String>()
	{
		public String convertProperty(Object id, String value)
		{
			return (value == null) ? "" : value;
		}

		public String convertValue(Object id, String value)
		{
			return "".equals(value) ? null : value;
		}
	};

	// remember the font that was used last time the element was painted, use this as default for font cell editors.
	public static final RuntimeProperty<java.awt.Font> LastPaintedFontProperty = new RuntimeProperty<java.awt.Font>()
	{
		private static final long serialVersionUID = 1L;
	};

	// remember the background set for the object when painted
	public static final RuntimeProperty<java.awt.Color> LastPaintedBackgroundProperty = new RuntimeProperty<java.awt.Color>()
	{
		private static final long serialVersionUID = 1L;
	};

	private PersistContext persistContext;
	private boolean readOnly;

	// Property Descriptors
	private Map<Object, IPropertyDescriptor> propertyDescriptors;
	private Map<Object, IPropertyDescriptor> hiddenPropertyDescriptors;
	private Map<Object, PropertyDescriptorWrapper> beansProperties;

	public static PersistPropertySource createPersistPropertySource(IPersist persist, IPersist context, boolean readonly)
	{
		return createPersistPropertySource(PersistContext.create(persist, context), readonly);
	}

	public static PersistPropertySource createPersistPropertySource(IPersist persist, boolean readonly)
	{
		return createPersistPropertySource(PersistContext.create(persist), readonly);
	}

	public static PersistPropertySource createPersistPropertySource(PersistContext persistContext, boolean readonly)
	{
		PersistPropertySource persistPropertySource = (PersistPropertySource)Platform.getAdapterManager().getAdapter(persistContext, IPropertySource.class);
		if (persistPropertySource != null) persistPropertySource.setReadOnly(readonly);
		return persistPropertySource;
	}

	/*
	 * This constructor should only be called from the adapter factory, all other code should use createPersistPropertySource()
	 */
	public PersistPropertySource(PersistContext persistContext, boolean readonly)
	{
		this.persistContext = persistContext;
		this.readOnly = readonly;
	}

	public void setReadOnly(boolean readOnly)
	{
		if (this.readOnly != readOnly)
		{
			this.readOnly = readOnly;
			propertyDescriptors = null;
		}
	}

	public IPersist getPersist()
	{
		return persistContext.getPersist();
	}

	public IPersist getContext()
	{
		return persistContext.getContext();
	}

	public Object getSaveModel()
	{
		return persistContext.getPersist();
	}

	public Object getAdapter(Class adapter)
	{
		if (adapter == IPersist.class)
		{
			return persistContext.getPersist();
		}
		return null;
	}

	public Object getEditableValue()
	{
		return null;
	}

	protected void init()
	{
		if (propertyDescriptors == null)
		{
			FlattenedSolution flattenedEditingSolution = ModelUtils.getEditingFlattenedSolution(persistContext.getPersist(), persistContext.getContext());
			Form form = (Form)(ModelUtils.isInheritedFormElement(persistContext.getPersist(), persistContext.getContext()) ? persistContext.getContext()
				: persistContext.getPersist()).getAncestor(IRepository.FORMS);

			java.beans.BeanInfo info = null;
			Object valueObject = null;
			try
			{
				if (persistContext.getPersist() instanceof Bean) //step into the bean instance
				{
					valueObject = DesignComponentFactory.getBeanDesignInstance(Activator.getDefault().getDesignClient(), flattenedEditingSolution,
						(Bean)persistContext.getPersist(), form);
					if (valueObject instanceof InvisibleBean) //step into the invisible bean instance
					{
						valueObject = ((InvisibleBean)valueObject).getDelegate();
					}
				}
				else
				{
					valueObject = persistContext.getPersist();
				}
				if (valueObject != null)
				{
					info = java.beans.Introspector.getBeanInfo(valueObject.getClass());
				}
			}
			catch (java.beans.IntrospectionException e)
			{
				ServoyLog.logError(e);
			}
			java.beans.PropertyDescriptor[] properties;
			if (info != null)
			{
				properties = sortBeansPropertyDescriptors(info.getPropertyDescriptors());
			}
			else
			{
				properties = new java.beans.PropertyDescriptor[0];
			}

			propertyDescriptors = new LinkedHashMap<Object, IPropertyDescriptor>();// defines order
			hiddenPropertyDescriptors = new HashMap<Object, IPropertyDescriptor>();
			beansProperties = new HashMap<Object, PropertyDescriptorWrapper>();

			for (java.beans.PropertyDescriptor element : properties)
			{
				try
				{
					registerProperty(new PropertyDescriptorWrapper(element, valueObject), flattenedEditingSolution, form);
				}
				catch (RepositoryException e)
				{
					ServoyLog.logError("Could not register property " + element.getName(), e);
				}
			}
			if (valueObject == persistContext.getPersist())
			{
				// check for pseudo properties
				String[] pseudoPropertyNames = getPseudoPropertyNames(persistContext.getPersist().getClass());
				if (pseudoPropertyNames != null)
				{
					for (String propName : pseudoPropertyNames)
					{
						IPropertyDescriptor desc;
						try
						{
							desc = getPropertiesPropertyDescriptor(this, propName, getDisplayName(propName), propName, flattenedEditingSolution, form);
							if (desc != null)
							{
								setCategory(desc, PropertyCategory.createPropertyCategory(propName));
								propertyDescriptors.put(propName, desc);
							}
						}
						catch (RepositoryException e)
						{
							ServoyLog.logError(e);
						}
					}
				}
			}
			else
			{
				// bean: use size, location and name descriptors from the persist (Bean)
				try
				{
					for (java.beans.PropertyDescriptor element : java.beans.Introspector.getBeanInfo(persistContext.getPersist().getClass()).getPropertyDescriptors())
					{
						try
						{
							registerProperty(new PropertyDescriptorWrapper(element, persistContext.getPersist()), flattenedEditingSolution, form);
						}
						catch (RepositoryException e)
						{
							ServoyLog.logError("Could not register property " + element.getName(), e);
						}
						if (propertyDescriptors.containsKey(element.getName()))
						{
							// if same property is defined in bean and persist, override with persist version
							propertyDescriptors.remove(BEAN_PROPERTY_PREFIX_DOT + element.getName());
						}
					}
				}
				catch (IntrospectionException e)
				{
					ServoyLog.logError(e);
				}
			}
		}
	}

	protected String[] getPseudoPropertyNames(Class< ? > clazz)
	{
		if (Solution.class == clazz)
		{
			return new String[] { "loginSolutionName" };
		}
		if (Form.class == clazz || IFormElement.class.isAssignableFrom(clazz))
		{
			return new String[] { "designTimeProperties" };
		}
		return null;
	}

	private void registerProperty(PropertyDescriptorWrapper propertyDescriptor, FlattenedSolution flattenedEditingSolution, Form form)
		throws RepositoryException
	{
		if (persistContext.getPersist() == propertyDescriptor.valueObject // for beans we show all
			&&
			!shouldShow(propertyDescriptor))
		{
			return;
		}

		PropertyCategory category;
		String id;
		if (propertyDescriptor.valueObject == persistContext.getPersist())
		{
			category = PropertyCategory.createPropertyCategory(propertyDescriptor.propertyDescriptor.getName());
			id = propertyDescriptor.propertyDescriptor.getName();
		}
		else
		{
			category = PropertyCategory.Beans;
			id = BEAN_PROPERTY_PREFIX_DOT + propertyDescriptor.propertyDescriptor.getName();
		}

		IPropertyDescriptor pd = createPropertyDescriptor(propertyDescriptor, flattenedEditingSolution, form, category, id);
		IPropertyDescriptor combinedPropertyDesciptor = null;
		if (propertyDescriptor.valueObject == persistContext.getPersist())
		{
			combinedPropertyDesciptor = createCombinedPropertyDescriptor(propertyDescriptor, form);
		}
		beansProperties.put(pd != null ? pd.getId() : propertyDescriptor.propertyDescriptor.getName(), propertyDescriptor);
		if (pd != null)
		{
			if (readOnly)
			{
				// Add a wrapper to enforce readonly unless the IPropertyController handles read-only self
				if (!(pd instanceof IPropertyController) || !((IPropertyController< ? , ? >)pd).supportsReadonly())
				{
					pd = new ReadonlyPropertyController(pd);
				}
			}
			else
			{
				// Add a wrapper that checks readonly of the other pd in isCompatibleWith()
				pd = new DelegatePropertyController(pd, pd.getId());
			}

			if (propertyDescriptor.valueObject == persistContext.getPersist() && hideForProperties(propertyDescriptor))
			{
				hiddenPropertyDescriptors.put(pd.getId(), pd);
			}
			else
			{
				propertyDescriptors.put(pd.getId(), pd);
			}

			if (combinedPropertyDesciptor != null)
			{
				propertyDescriptors.put(combinedPropertyDesciptor.getId(), combinedPropertyDesciptor);
			}
		}
	}

	/**
	 * @param propertyDescriptor
	 * @param flattenedEditingSolution
	 * @param form
	 * @param category
	 * @param id
	 * @return
	 * @throws RepositoryException
	 */
	protected IPropertyDescriptor createPropertyDescriptor(PropertyDescriptorWrapper propertyDescriptor, FlattenedSolution flattenedEditingSolution, Form form,
		PropertyCategory category, String id) throws RepositoryException
	{
		return createPropertyDescriptor(this, id, persistContext, readOnly, propertyDescriptor,
			RepositoryHelper.getDisplayName(propertyDescriptor.propertyDescriptor.getName(), persistContext.getPersist().getClass()), category,
			flattenedEditingSolution, form);
	}

	public IPropertyDescriptor[] getPropertyDescriptors()
	{
		init();
		IPropertyDescriptor[] descs = propertyDescriptors.values().toArray(new IPropertyDescriptor[propertyDescriptors.size()]);
		if (persistContext.getPersist() instanceof Bean)
		{
			return PropertyController.applySequencePropertyComparator(descs);
		}
		return descs;
	}

	public IPropertyDescriptor getPropertyDescriptor(Object id)
	{
		init();

		// recursively process a.b.c properties
		if (id instanceof String && !((String)id).startsWith(BEAN_PROPERTY_PREFIX_DOT))
		{
			String propId = (String)id;
			int dot = propId.indexOf('.');
			if (dot > 0)
			{
				Object propertyValue = getPropertyValue(propId.substring(0, dot));
				if (propertyValue instanceof IAdaptable)
				{
					IPropertySource propertySource = (IPropertySource)((IAdaptable)propertyValue).getAdapter(IPropertySource.class);
					if (propertySource != null)
					{
						String subProp = propId.substring(dot + 1);
						IPropertyDescriptor[] descs = propertySource.getPropertyDescriptors();
						if (descs == null)
						{
							return null;
						}
						for (IPropertyDescriptor desc : descs)
						{
							if (subProp.equals(desc.getId()))
							{
								return desc;
							}
						}
					}
				}
			}
		}

		IPropertyDescriptor propertyDescriptor = propertyDescriptors.get(id);
		if (propertyDescriptor == null)
		{
			propertyDescriptor = hiddenPropertyDescriptors.get(id);
		}
		return propertyDescriptor;
	}

	private Map<Object, PropertyDescriptorWrapper> getBeansProperties()
	{
		init();
		return beansProperties;
	}

	public static IPropertyDescriptor createPropertyDescriptor(IPropertySource propertySource, final String id, final PersistContext persistContext,
		boolean readOnly, PropertyDescriptorWrapper propertyDescriptor, String displayName, PropertyCategory category,
		FlattenedSolution flattenedEditingSolution, Form form) throws RepositoryException
	{

		if ((propertyDescriptor.propertyDescriptor.getReadMethod() == null) || propertyDescriptor.propertyDescriptor.getWriteMethod() == null ||
			propertyDescriptor.propertyDescriptor.isExpert() || propertyDescriptor.propertyDescriptor.getPropertyType().equals(Object.class) ||
			propertyDescriptor.propertyDescriptor.isHidden())
		{
			return null;
		}

		IPropertyDescriptor desc = createPropertyDescriptor(propertySource, persistContext, readOnly, propertyDescriptor, id, displayName, category,
			flattenedEditingSolution, form);
		setCategory(desc, category);
		if (desc != null //
			&&
			persistContext != null &&
			persistContext.getPersist() != null &&
			persistContext.getPersist().getParent() == persistContext.getContext() // only show overrides when element is shown in its 'own' form
			&&
			// skip some specific properties
			!(persistContext.getPersist() instanceof Form && StaticContentSpecLoader.PROPERTY_NAME.getPropertyName().equals(id)) &&
			!StaticContentSpecLoader.PROPERTY_EXTENDSID.getPropertyName().equals(id))
		{
			return new DelegatePropertyController(desc, id)
			{
				@Override
				public ILabelProvider getLabelProvider()
				{
					String propertyId = id;
					if (persistContext.getPersist() instanceof Form && StaticContentSpecLoader.PROPERTY_WIDTH.getPropertyName().equals(id))
					{
						propertyId = StaticContentSpecLoader.PROPERTY_SIZE.getPropertyName();
					}
					return new PersistInheritenceDelegateLabelProvider(persistContext.getPersist(), super.getLabelProvider(), propertyId);
				}
			};
		}
		return desc;
	}

	private static void setCategory(IPropertyDescriptor desc, PropertyCategory category)
	{
		if (category != null)
		{
			if (desc instanceof org.eclipse.ui.views.properties.PropertyDescriptor)
			{
				((org.eclipse.ui.views.properties.PropertyDescriptor)desc).setCategory(category.name());
			}
			else if (desc instanceof DelegatePropertyController)
			{
				((DelegatePropertyController)desc).setCategory(category.name());
			}
		}
	}

	protected IPropertyDescriptor createCombinedPropertyDescriptor(PropertyDescriptorWrapper propertyDescriptor, Form form)
	{
		PropertyCategory category = PropertyCategory.createPropertyCategory(propertyDescriptor.propertyDescriptor.getName());
		IPropertyDescriptor desc = getCombinedPropertyDescriptor(propertyDescriptor, getDisplayName(propertyDescriptor.propertyDescriptor.getName()), category,
			form);
		if (desc instanceof org.eclipse.ui.views.properties.PropertyDescriptor)
		{
			((org.eclipse.ui.views.properties.PropertyDescriptor)desc).setCategory(category.name());
		}
		else if (desc instanceof DelegatePropertyController)
		{
			((DelegatePropertyController)desc).setCategory(category.name());
		}
		return desc;
	}

	private String getDisplayName(String name)
	{
		return RepositoryHelper.getDisplayName(name, persistContext.getPersist().getClass());
	}

	private static IPropertyDescriptor createPropertyDescriptor(IPropertySource propertySource, final PersistContext persistContext, final boolean readOnly,
		final PropertyDescriptorWrapper propertyDescriptor, final String id, String displayName, PropertyCategory category,
		final FlattenedSolution flattenedEditingSolution, final Form form) throws RepositoryException
	{
		/*
		 * Category based property controllers.
		 */
		if (category == PropertyCategory.Events || category == PropertyCategory.Commands)
		{
			final Table table = form == null ? null : form.getTable();
			return new MethodPropertyController<Integer>(id, displayName, persistContext, new MethodListOptions(true, category == PropertyCategory.Commands,
				form != null, true, allowFoundsetMethods(persistContext, id) && table != null, table))
			{
				@Override
				protected IPropertyConverter<Integer, Object> createConverter()
				{
					IPropertyConverter<Integer, MethodWithArguments> id2MethodsWithArgumentConverter = new IPropertyConverter<Integer, MethodWithArguments>()
					{
						public MethodWithArguments convertProperty(Object id, Integer value)
						{
							SafeArrayList<Object> args = null;
							if (persistContext != null && persistContext.getPersist() instanceof AbstractBase)
							{
								List<Object> instanceArgs = ((AbstractBase)persistContext.getPersist()).getInstanceMethodArguments(id.toString());
								if (instanceArgs != null)
								{
									args = new SafeArrayList<Object>(instanceArgs);
								}
							}
							return new MethodWithArguments(value.intValue(), args, table);
						}

						public Integer convertValue(Object id, MethodWithArguments value)
						{
							if (persistContext != null) setInstancMethodArguments(persistContext.getPersist(), id, value.arguments);
							return Integer.valueOf(value.methodId);
						}
					};

					if (persistContext != null && persistContext.getContext() instanceof Form && ((Form)persistContext.getContext()).getExtendsID() > 0)
					{
						// convert to the actual method called according to form inheritance
						id2MethodsWithArgumentConverter = new ChainedPropertyConverter<Integer, MethodWithArguments, MethodWithArguments>(
							id2MethodsWithArgumentConverter, new FormInheritenceMethodConverter(persistContext));
					}

					ComplexProperty.ComplexPropertyConverter<MethodWithArguments> mwa2complexConverter = new ComplexProperty.ComplexPropertyConverter<MethodWithArguments>()
					{
						@Override
						public Object convertProperty(Object id, MethodWithArguments value)
						{
							return new ComplexProperty<MethodWithArguments>(value)
							{
								@Override
								public IPropertySource getPropertySource()
								{
									return new MethodPropertySource(this, persistContext, table, getId().toString(), isReadOnly());
								}
							};
						}

						@Override
						public MethodWithArguments convertValue(Object id, Object value)
						{
							if (value == null || value instanceof MethodWithArguments)
							{
								return (MethodWithArguments)value;
							}
							return ((ComplexProperty<MethodWithArguments>)value).getValue();
						}
					};

					return new ChainedPropertyConverter<Integer, MethodWithArguments, Object>(id2MethodsWithArgumentConverter, mwa2complexConverter);
				}
			};
		}

		/*
		 * Property editors defined by beans
		 */

		if (category == PropertyCategory.Beans)
		{
			Class< ? > propertyEditorClass = propertyDescriptor.propertyDescriptor.getPropertyEditorClass();
			try
			{
				if (propertyEditorClass != null && PropertyEditor.class.isAssignableFrom(propertyEditorClass))
				{
					if (propertyDescriptor.getPropertyEditor() == null)
					{
						propertyDescriptor.setPropertyEditor((PropertyEditor)propertyEditorClass.newInstance());
					}

					String[] tags = propertyDescriptor.getPropertyEditor().getTags();
					if (tags != null && tags.length > 0)
					{
						// list of values
						return new BeanTagsPropertyController(id, displayName, propertyDescriptor.getPropertyEditor(), Messages.LabelUnresolved);
					}

					if (propertyDescriptor.getPropertyEditor().supportsCustomEditor())
					{
						// has its own editor
						final LabelProvider labelProvider = new LabelProvider()
						{
							@Override
							public String getText(Object element)
							{
								String text = null;
								try
								{
									propertyDescriptor.getPropertyEditor().setValue(element);
									text = propertyDescriptor.getPropertyEditor().getAsText();
								}
								catch (RuntimeException e)
								{
									ServoyLog.logError("Could not create get property value " + id, e);
								}
								if (text == null)
								{
									return super.getText(element);
								}
								return text;
							}
						};
						PropertyDescriptor beanPropertyDescriptor = new PropertyDescriptor(id, displayName)
						{
							@Override
							public CellEditor createPropertyEditor(Composite parent)
							{
								return new BeanCustomCellEditor(parent, propertyDescriptor.getPropertyEditor(), labelProvider,
									propertyDescriptor.propertyDescriptor.getName(), "Bean custom property editor", readOnly);
							}
						};
						beanPropertyDescriptor.setLabelProvider(labelProvider);
						return beanPropertyDescriptor;
					}

					try
					{
						if (propertyDescriptor.getPropertyEditor().getAsText() != null)
						{
							// can be edited as text
							return new PropertyController<Object, String>(id, displayName, new BeanAsTextPropertyConverter(
								propertyDescriptor.getPropertyEditor()), null, new ICellEditorFactory()
							{
								public CellEditor createPropertyEditor(Composite parent)
								{
									return new TextCellEditor(parent);
								}
							});
						}
					}
					catch (RuntimeException e)
					{
						// can not be edited as text
						// TODO: trace message ?
					}
				}
			}
			catch (InstantiationException e)
			{
				ServoyLog.logError("Could not create property editor " + propertyEditorClass.getName(), e);
			}
			catch (IllegalAccessException e)
			{
				ServoyLog.logError("Could not create property editor " + propertyEditorClass.getName(), e);
			}

			// property editors for beans, use property editor hints if given
			Object hintValue = propertyDescriptor.propertyDescriptor.getValue(PropertyEditorHint.PROPERTY_EDITOR_HINT);
			final PropertyEditorHint propertyEditorHint;
			if (hintValue instanceof PropertyEditorHint)
			{
				propertyEditorHint = (PropertyEditorHint)hintValue;
			}
			else if (propertyDescriptor.propertyDescriptor.getPropertyType() == FunctionDefinition.class)
			{
				// use defaults for FunctionDefinition property without specified hint
				propertyEditorHint = new PropertyEditorHint(PropertyEditorClass.method);
			}
			else
			{
				propertyEditorHint = null;
			}

			if (propertyEditorHint != null && form != null)
			{
				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.method)
				{
					// accept String or FunctionDefinition properties

					Class< ? > propertyType = propertyDescriptor.propertyDescriptor.getPropertyType();
					// convert a FunctionDefinition to MethodWithArguments
					IPropertyConverter<FunctionDefinition, MethodWithArguments> methodsWithArgumentConverter = new IPropertyConverter<FunctionDefinition, MethodWithArguments>()
					{
						public MethodWithArguments convertProperty(Object id, FunctionDefinition value)
						{
							if (value == null) return null;

							FlattenedSolution flattenedSolution = ModelUtils.getEditingFlattenedSolution(persistContext.getPersist(),
								persistContext.getContext());
							Iterator<ScriptMethod> scriptMethods = null;
							String scopeName = value.getScopeName();
							if (scopeName != null)
							{
								// global method
								scriptMethods = flattenedSolution.getScriptMethods(scopeName, false);
							}
							else
							{
								// form method
								Form methodForm = flattenedSolution.getForm(value.getContextName());
								if (methodForm != null)
								{
									scriptMethods = methodForm.getScriptMethods(false);
								}
							}
							if (scriptMethods != null)
							{
								ScriptMethod scriptMethod = AbstractBase.selectByName(scriptMethods, value.getMethodName());
								if (scriptMethod != null)
								{
									return MethodWithArguments.create(scriptMethod, null);
								}
							}
							return new UnresolvedMethodWithArguments(value.toMethodString());
						}

						public FunctionDefinition convertValue(Object id, MethodWithArguments value)
						{
							IScriptProvider scriptMethod = ModelUtils.getScriptMethod(persistContext.getPersist(), persistContext.getContext(), value.table,
								value.methodId);
							if (scriptMethod != null)
							{
								String formName = (scriptMethod.getParent() instanceof Form) ? ((Form)scriptMethod.getParent()).getName() : null;
								return new FunctionDefinition(formName, scriptMethod.getName());
							}
							return null;
						}
					};

					if (persistContext.getContext() instanceof Form && ((Form)persistContext.getContext()).getExtendsID() > 0)
					{
						// convert to the actual method called according to form inheritance
						methodsWithArgumentConverter = new ChainedPropertyConverter<FunctionDefinition, MethodWithArguments, MethodWithArguments>(
							methodsWithArgumentConverter, new FormInheritenceMethodConverter(persistContext));
					}

					final IPropertyConverter<FunctionDefinition, Object> functionDefinitionPropertyConverter = new ChainedPropertyConverter<FunctionDefinition, MethodWithArguments, Object>(
						methodsWithArgumentConverter, PropertyCastingConverter.<MethodWithArguments, Object> propertyCastingConverter());

					if (propertyType == FunctionDefinition.class)
					{
						return new MethodPropertyController<FunctionDefinition>(id, displayName, persistContext, new MethodListOptions(
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)), false,
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeForm)),
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeGlobal)),
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeFoundset)), form == null ? null : form.getTable()))
						{
							@Override
							protected IPropertyConverter<FunctionDefinition, Object> createConverter()
							{
								return functionDefinitionPropertyConverter;
							}
						};
					}

					if (propertyType == String.class)
					{
						// chain with String to FunctionDefinition converter
						return new MethodPropertyController<String>(id, displayName, persistContext, new MethodListOptions(
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)), false,
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeForm)),
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeGlobal)),
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeFoundset)), form == null ? null : form.getTable()))
						{
							@Override
							protected IPropertyConverter<String, Object> createConverter()
							{
								IPropertyConverter<String, FunctionDefinition> string2FunctionDefinitionConverter = new IPropertyConverter<String, FunctionDefinition>()
								{
									public String convertValue(Object id, FunctionDefinition value)
									{
										if (value == null) return null;
										return value.toMethodString();
									}

									public FunctionDefinition convertProperty(Object id, String value)
									{
										if (value == null) return null;
										return new FunctionDefinition(value);
									}
								};
								return new ChainedPropertyConverter<String, FunctionDefinition, Object>(string2FunctionDefinitionConverter,
									functionDefinitionPropertyConverter);
							}
						};
					}

					// not String or FunctionDefinition
					return null;
				}

				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.dataprovider &&
					propertyDescriptor.propertyDescriptor.getPropertyType() == String.class)
				{
					// String property, select a data provider
					Table table = null;
					try
					{
						table = flattenedEditingSolution.getFlattenedForm(form).getTable();
					}
					catch (RepositoryException ex)
					{
						ServoyLog.logInfo("Table form not accessible: " + ex.getMessage());
						return null;
					}

					INCLUDE_RELATIONS includeRelations;
					if (Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeRelations)))
					{
						if (Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNestedRelations)))
						{
							includeRelations = INCLUDE_RELATIONS.NESTED;
						}
						else
						{
							includeRelations = INCLUDE_RELATIONS.YES;
						}
					}
					else
					{
						includeRelations = INCLUDE_RELATIONS.NO;
					}
					boolean includeGlobalRelations;
					if (propertyEditorHint.getOption(PropertyEditorOption.includeGlobalRelations) != null)
					{
						includeGlobalRelations = Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeGlobalRelations));
					}
					else
					{
						includeGlobalRelations = Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeGlobal));
					}


					final DataProviderOptions options = new DataProviderTreeViewer.DataProviderOptions(
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)), table != null &&
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeColumns)), table != null &&
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeCalculations)), table != null &&
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeRelatedCalculations)),
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeForm)),
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeGlobal)), table != null &&
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeAggregates)), table != null &&
							Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeRelatedAggregates)), includeRelations,
						includeGlobalRelations, true, null);
					final DataProviderConverter converter = new DataProviderConverter(flattenedEditingSolution, persistContext.getPersist(), table);
					DataProviderLabelProvider showPrefix = new DataProviderLabelProvider(false);
					showPrefix.setConverter(converter);
					DataProviderLabelProvider hidePrefix = new DataProviderLabelProvider(true);
					hidePrefix.setConverter(converter);

					ILabelProvider labelProviderShowPrefix = new SolutionContextDelegateLabelProvider(new FormContextDelegateLabelProvider(showPrefix,
						persistContext.getContext()), persistContext.getContext());
					final ILabelProvider labelProviderHidePrefix = new SolutionContextDelegateLabelProvider(new FormContextDelegateLabelProvider(hidePrefix,
						persistContext.getContext()), persistContext.getContext());
					PropertyController<String, String> propertyController = new PropertyController<String, String>(id, displayName, null,
						labelProviderShowPrefix, new ICellEditorFactory()
						{
							public CellEditor createPropertyEditor(Composite parent)
							{
								return new DataProviderCellEditor(parent, labelProviderHidePrefix, new DataProviderValueEditor(converter), form,
									flattenedEditingSolution, readOnly, options, converter);
							}
						});
					propertyController.setSupportsReadonly(true);
					return propertyController;
				}

				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.relation &&
					propertyDescriptor.propertyDescriptor.getPropertyType() == String.class)
				{
					// String property, select a relation
					Table primaryTable = null;
					if (form != null)
					{
						try
						{
							primaryTable = flattenedEditingSolution.getFlattenedForm(form).getTable();
						}
						catch (RepositoryException ex)
						{
							ServoyLog.logInfo("Table form not accessible: " + ex.getMessage());
						}
					}

					return new RelationPropertyController(id, displayName, persistContext, primaryTable, null /* foreignTable */,
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)),
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNestedRelations)));
				}

				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.form &&
					propertyDescriptor.propertyDescriptor.getPropertyType() == String.class)
				{
					// String property, select a form
					final ILabelProvider formLabelProvider = new SolutionContextDelegateLabelProvider(new FormLabelProvider(flattenedEditingSolution, false),
						persistContext.getContext());
					PropertyDescriptor pd = new PropertyController<String, Integer>(id, displayName)
					{
						@Override
						public CellEditor createPropertyEditor(Composite parent)
						{
							return new ListSelectCellEditor(parent, "Select form",
								new FormContentProvider(flattenedEditingSolution, null /* persist is solution */), formLabelProvider, new FormValueEditor(
									flattenedEditingSolution), readOnly, new FormContentProvider.FormListOptions(FormListOptions.FormListType.FORMS, null,
									Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)), false, false), SWT.NONE, null,
								"Select form dialog");
						}

						@Override
						protected IPropertyConverter<String, Integer> createConverter()
						{
							// convert between form ids and form name
							return new IPropertyConverter<String, Integer>()
							{
								public Integer convertProperty(Object id, String value)
								{
									Form f = flattenedEditingSolution.getForm(value);
									return Integer.valueOf(f == null ? Form.NAVIGATOR_NONE : f.getID());
								}

								public String convertValue(Object id, Integer value)
								{
									int formId = value.intValue();
									if (formId == Form.NAVIGATOR_NONE) return null;
									Form f = flattenedEditingSolution.getForm(formId);
									return f == null ? null : f.getName();
								}
							};
						}
					};
					pd.setLabelProvider(formLabelProvider);
					return pd;
				}

				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.valuelist &&
					propertyDescriptor.propertyDescriptor.getPropertyType() == String.class)
				{
					// String property, select a value list
					return new ValuelistPropertyController<String>(id, displayName, persistContext,
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)))
					{
						@Override
						protected IPropertyConverter<String, Integer> createConverter()
						{
							// convert between value list ids and value list name
							return new IPropertyConverter<String, Integer>()
							{
								public Integer convertProperty(Object id, String value)
								{
									ValueList vl = flattenedEditingSolution.getValueList(value);
									return Integer.valueOf(vl == null ? ValuelistLabelProvider.VALUELIST_NONE : vl.getID());
								}

								public String convertValue(Object id, Integer value)
								{
									int vlId = value.intValue();
									if (vlId == ValuelistLabelProvider.VALUELIST_NONE) return null;
									ValueList vl = flattenedEditingSolution.getValueList(vlId);
									return vl == null ? null : vl.getName();
								}
							};
						}
					};
				}

				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.media &&
					propertyDescriptor.propertyDescriptor.getPropertyType() == String.class)
				{
					// String property, select an image
					return new MediaPropertyController<String>(id, displayName, persistContext,
						Boolean.TRUE.equals(propertyEditorHint.getOption(PropertyEditorOption.includeNone)))
					{
						@Override
						protected IPropertyConverter<String, MediaNode> createConverter()
						{
							// convert between media node and image name
							return new IPropertyConverter<String, MediaNode>()
							{
								public MediaNode convertProperty(Object id, String value)
								{
									Media media = flattenedEditingSolution.getMedia(value);
									if (media != null)
									{
										String mediaName = media.getName();
										return new MediaNode(mediaName, mediaName, MediaNode.TYPE.IMAGE, flattenedEditingSolution.getSolution(), null, media);
									}
									else
									{
										return MediaLabelProvider.MEDIA_NODE_NONE;
									}
								}

								public String convertValue(Object id, MediaNode value)
								{
									return value == null ? null : value.getName();
								}
							};
						}
					};
				}

				if (propertyEditorHint.getPropertyEditorClass() == PropertyEditorClass.styleclass &&
					propertyDescriptor.propertyDescriptor.getPropertyType() == String.class)
				{
					// String property, select a style class
					Object option = propertyEditorHint.getOption(PropertyEditorOption.styleLookupName);
					if (option instanceof String)
					{
						return createStyleClassPropertyController(persistContext.getPersist(), id, displayName, (String)option, form);
					}
				}
			}
		}

		/*
		 * Type based property controllers.
		 */

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == java.awt.Dimension.class)
		{
			final java.awt.Dimension defaultDimension = propertyDescriptor.propertyDescriptor.getName().equals("intercellSpacing") ? new Dimension(1, 1)
				: new Dimension(0, 0);
			return new PropertyController<java.awt.Dimension, Object>(id, displayName, new ComplexPropertyConverter<java.awt.Dimension>()
			{
				@Override
				public Object convertProperty(Object id, java.awt.Dimension value)
				{
					return new ComplexProperty<java.awt.Dimension>(value)
					{
						@Override
						public IPropertySource getPropertySource()
						{
							DimensionPropertySource dimensionPropertySource = new DimensionPropertySource(this, defaultDimension);
							dimensionPropertySource.setReadonly(readOnly);
							return dimensionPropertySource;
						}
					};
				}

			}, DimensionPropertySource.getLabelProvider(), new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return DimensionPropertySource.createPropertyEditor(parent);
				}
			});
		}

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == java.awt.Point.class)
		{
			return new PropertyController<java.awt.Point, Object>(id, displayName, new ComplexPropertyConverter<java.awt.Point>()
			{
				@Override
				public Object convertProperty(Object id, java.awt.Point value)
				{
					return new ComplexProperty<java.awt.Point>(value)
					{
						@Override
						public IPropertySource getPropertySource()
						{
							PointPropertySource pointPropertySource = new PointPropertySource(this);
							pointPropertySource.setReadonly(readOnly);
							return pointPropertySource;
						}
					};
				}
			}, PointPropertySource.getLabelProvider(), new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return PointPropertySource.createPropertyEditor(parent);
				}
			});
		}

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == java.awt.Insets.class)
		{
			return new PropertyController<java.awt.Insets, Object>(id, displayName, new ComplexPropertyConverter<java.awt.Insets>()
			{
				@Override
				public Object convertProperty(Object property, java.awt.Insets value)
				{
					return new ComplexProperty<Insets>(value)
					{
						@Override
						public IPropertySource getPropertySource()
						{
							InsetsPropertySource insetsPropertySource = new InsetsPropertySource(this);
							insetsPropertySource.setReadonly(readOnly);
							return insetsPropertySource;
						}
					};
				}
			}, InsetsPropertySource.getLabelProvider(), new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return InsetsPropertySource.createPropertyEditor(parent);
				}
			});
		}

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == java.awt.Color.class)
		{
			return new ColorPropertyController(id, displayName);
		}

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == java.awt.Font.class)
		{
			final IDefaultValue<String> getLastPaintedFont;
			if (persistContext != null && persistContext.getPersist() instanceof AbstractBase)
			{
				getLastPaintedFont = new IDefaultValue<String>()
				{
					public String getValue()
					{
						return PropertyFontConverter.INSTANCE.convertProperty(id,
							((AbstractBase)persistContext.getPersist()).getRuntimeProperty(LastPaintedFontProperty));
					}
				};
			}
			else
			{
				getLastPaintedFont = null;
			}
			return new PropertyController<java.awt.Font, String>(id, displayName, PropertyFontConverter.INSTANCE, new LabelProvider(), new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new FontCellEditor(parent, getLastPaintedFont);
				}
			});
		}

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == Border.class)
		{
			return new BorderPropertyController(id, displayName, propertySource, persistContext);
		}

		if (propertyDescriptor.propertyDescriptor.getPropertyType() == boolean.class ||
			propertyDescriptor.propertyDescriptor.getPropertyType() == Boolean.class)
		{
			return new CheckboxPropertyDescriptor(id, displayName);
		}


		/*
		 * Name based property controllers.
		 */

		String name = propertyDescriptor.propertyDescriptor.getName();
		IPropertyDescriptor retval = getGeneralPropertyDescriptor(persistContext, readOnly, id, displayName, name, flattenedEditingSolution);
		if (retval != null)
		{
			return retval;
		}
		if (category == PropertyCategory.Properties) // name based props for IPersists only (not beans)
		{
			retval = getPropertiesPropertyDescriptor(propertySource, persistContext, readOnly, id, displayName, name, flattenedEditingSolution, form);
			if (retval != null)
			{
				return retval;
			}
		}

		//  defaults, determine editor based on type
		final Class< ? > clazz = propertyDescriptor.propertyDescriptor.getPropertyType();
		if (clazz != null)
		{
			if (clazz == String.class)
			{
				return new PropertyController<String, String>(id, displayName, NULL_STRING_CONVERTER, null, new ICellEditorFactory()
				{
					public CellEditor createPropertyEditor(Composite parent)
					{
						return new TextCellEditor(parent);
					}
				});
			}

			final int type;
			if (clazz == byte.class || clazz == Byte.class)
			{
				type = NumberCellEditor.BYTE;
			}
			else if (clazz == double.class || clazz == Double.class)
			{
				type = NumberCellEditor.DOUBLE;
			}
			else if (clazz == float.class || clazz == Float.class)
			{
				type = NumberCellEditor.FLOAT;
			}
			else if (clazz == int.class || clazz == Integer.class)
			{
				type = NumberCellEditor.INTEGER;
			}
			else if (clazz == long.class || clazz == Long.class)
			{
				type = NumberCellEditor.LONG;
			}
			else if (clazz == short.class || clazz == Short.class)
			{
				type = NumberCellEditor.SHORT;
			}
			else
			{
				type = -1;
			}
			if (type != -1)
			{
				return new PropertyDescriptor(id, displayName)
				{
					@Override
					public CellEditor createPropertyEditor(Composite parent)
					{
						NumberCellEditor editor = new NumberCellEditor(parent);
						editor.setType(type);
						return editor;
					}
				};
			}

			// other bean properties
			if (category == PropertyCategory.Beans)
			{
				return new PropertyController<Object, Object>(id, displayName, new ComplexPropertyConverter<Object>()
				{
					@Override
					public Object convertProperty(Object id, Object value)
					{
						return new ComplexProperty<Object>(value)
						{
							@Override
							public IPropertySource getPropertySource()
							{
								BeanSubpropertyPropertySource ps = new BeanSubpropertyPropertySource(this, propertyDescriptor.valueObject,
									propertyDescriptor.propertyDescriptor, flattenedEditingSolution, form);
								ps.setReadonly(readOnly);
								return ps;
							}
						};
					}

				}, null, new DummyCellEditorFactory(NullDefaultLabelProvider.LABEL_DEFAULT));
			}
		}

		return null;
	}

	/**
	 * For which properties should foundset methods be allowed.
	 * 
	 * @param persistContext 
	 * @param id
	 * @return
	 */
	private static boolean allowFoundsetMethods(PersistContext persistContext, String id)
	{
		if (persistContext.getPersist() instanceof Form && StaticContentSpecLoader.PROPERTY_ONLOADMETHODID.getPropertyName().equals(id))
		{
			return false; // in onload the foundset is not initialized yet
		}
		return true;
	}

	/**
	 * Create a property controller for selecting a style class in Properties view.
	 * 
	 * @param id
	 * @param displayName
	 * @param styleLookupname
	 * @return
	 */
	public static ComboboxPropertyController<String> createStyleClassPropertyController(IPersist persist, String id, String displayName,
		final String styleLookupname, Form form)
	{
		StyleClassesComboboxModel model = new StyleClassesComboboxModel(form, styleLookupname);
		return new ComboboxPropertyController<String>(id, displayName, model, Messages.LabelUnresolved, new ComboboxDelegateValueEditor<String>(
			new StyleClassValueEditor(form, persist), model))
		{
			@Override
			protected String getWarningMessage()
			{
				if (getModel().getRealValues().length == 1)
				{
					// only 1 value (DEFAULT)
					return "No style classes available for lookup '" + styleLookupname + "'";
				}
				return null;
			}
		};
	}

	public static ComboboxPropertyController<String> createNamedFoundsetPropertyController(String id, String displayName, Form form)
	{
		NamedFoundsetComboboxModel model = new NamedFoundsetComboboxModel(form);
		return new ComboboxPropertyController<String>(id, displayName, model, Messages.LabelUnresolved, new ComboboxDelegateValueEditor<String>(
			new NamedFoundsetRelationValueEditor(form), model));
	}

	/**
	 * Get property descriptor that maps multiple properties into 1
	 * 
	 * @param propertyDescriptor
	 * @param displayName
	 * @param category
	 * @return
	 */
	private IPropertyDescriptor getCombinedPropertyDescriptor(PropertyDescriptorWrapper propertyDescriptor, String displayName, PropertyCategory category,
		Form form)
	{
		if (propertyDescriptor.valueObject == persistContext.getPersist()) // name based props for IPersists only
		{
			String name = propertyDescriptor.propertyDescriptor.getName();

			if (name.equals("containsFormID"))
			{
				return new RelatedTabController("containsForm", "containsForm", "Select tab form", readOnly, form,
					ModelUtils.getEditingFlattenedSolution(persistContext.getPersist()));
			}
		}
		return null;
	}

	/**
	 * Get all solution names other than yourself.
	 * 
	 * @return
	 */
	private static List<String> getSolutionNames(IPersist persist)
	{
		List<String> lst = new ArrayList<String>();

		if (persist instanceof Solution)
		{
			String name = ((Solution)persist).getName();
			RootObjectMetaData[] solutionMetaDatas;
			try
			{
				solutionMetaDatas = ServoyModel.getDeveloperRepository().getRootObjectMetaDatasForType(IRepository.SOLUTIONS);
				for (RootObjectMetaData element : solutionMetaDatas)
				{
					if (!element.getName().equals(name))
					{
						lst.add(element.getName());
					}
				}
			}
			catch (RemoteException e)
			{
				ServoyLog.logError(e);
			}
			catch (RepositoryException e)
			{
				ServoyLog.logError(e);
			}
		}
		return lst;
	}

	/**
	 * Get the style names usable for the current form.
	 * 
	 * @param persist
	 * @return
	 * @throws RepositoryException
	 */
	protected static String[] getStyleNames() throws RepositoryException
	{
		List<String> styleNames = new ArrayList<String>();
		Iterator<Style> it = ServoyModel.getDeveloperRepository().getActiveRootObjects(IRepository.STYLES).iterator();
		while (it.hasNext())
		{
			Style style = it.next();
			styleNames.add(style.getName());
		}

		Collections.sort(styleNames);
		styleNames.add(0, null);
		return styleNames.toArray(new String[styleNames.size()]);
	}

	//remove some properties based on the 'known' name
	protected boolean shouldShow(PropertyDescriptorWrapper propertyDescriptor) throws RepositoryException
	{
		String name = propertyDescriptor.propertyDescriptor.getName();

		// check for content spec element.
		EclipseRepository repository = (EclipseRepository)persistContext.getPersist().getRootObject().getRepository();
		Element element = repository.getContentSpec().getPropertyForObjectTypeByName(persistContext.getPersist().getTypeID(), name);


		if (!RepositoryHelper.shouldShow(name, element, persistContext.getPersist().getClass()))
		{
			return false;
		}

		if (name.equals("labelFor") && persistContext.getPersist() instanceof GraphicalComponent)
		{
			GraphicalComponent gc = (GraphicalComponent)persistContext.getPersist();
			if (gc.getOnActionMethodID() >= 1 && gc.getShowClick())
			{
				//if it's a button, then we only show the property if it has a value (probably to be cleared via quickfix)
				return gc.getLabelFor() != null && !gc.getLabelFor().equals("");
			}
			else return true;
		}
		if (name.endsWith("printSliding") && !(persistContext.getPersist().getParent() instanceof Form))
		{
			return false;//if not directly on form it can not slide
		}
		if ((name.equals("onTabChangeMethodID") || name.equals("scrollTabs")) &&
			persistContext.getPersist() instanceof TabPanel &&
			(((TabPanel)persistContext.getPersist()).getTabOrientation() == TabPanel.SPLIT_HORIZONTAL || ((TabPanel)persistContext.getPersist()).getTabOrientation() == TabPanel.SPLIT_VERTICAL))
		{
			return false; // not applicable for splitpanes
		}

		if (name.equals("loginFormID") && persistContext.getPersist() instanceof Solution && ((Solution)persistContext.getPersist()).getLoginFormID() <= 0)
		{
			try
			{
				if (((Solution)persistContext.getPersist()).getLoginSolutionName() != null)
				{
					// there is a login solution, do not show the deprecated login form setting
					return false;
				}
			}
			catch (RepositoryException e)
			{
				ServoyLog.logError(e);
			}
		}


		return true;
	}

	protected boolean hideForProperties(PropertyDescriptorWrapper propertyDescriptor)
	{
		return RepositoryHelper.hideForProperties(propertyDescriptor.propertyDescriptor.getName(), persistContext.getPersist().getClass(),
			persistContext.getPersist());
	}

	public Object getPersistPropertyValue(Object id)
	{
		init();
		PropertyDescriptorWrapper beanPropertyDescriptor = getBeansProperties().get(id);
		if (beanPropertyDescriptor != null)
		{
			try
			{
				return beanPropertyDescriptor.propertyDescriptor.getReadMethod().invoke(beanPropertyDescriptor.valueObject, new Object[0]);
			}
			catch (Exception e)
			{
				ServoyLog.logError("Could not get property value for id " + id + " on object " + beanPropertyDescriptor.valueObject, e);
			}
		}
		return null;
	}

	public Object getPropertyValue(Object id)
	{
		// recursively process a.b.c properties
		if (id instanceof String && !((String)id).startsWith(BEAN_PROPERTY_PREFIX_DOT))
		{
			String propId = (String)id;
			int dot = propId.indexOf('.');
			if (dot > 0)
			{
				Object propertyValue = getPropertyValue(propId.substring(0, dot));
				if (propertyValue instanceof IAdaptable)
				{
					IPropertySource propertySource = (IPropertySource)((IAdaptable)propertyValue).getAdapter(IPropertySource.class);
					if (propertySource != null)
					{
						return propertySource.getPropertyValue(propId.substring(dot + 1));
					}
				}
			}
		}

		IPropertyDescriptor propertyDescriptor = getPropertyDescriptor(id);
		while (propertyDescriptor instanceof IDelegate && !(propertyDescriptor instanceof IPropertySetter))
		{
			Object delegate = ((IDelegate)propertyDescriptor).getDelegate();
			if (delegate instanceof IPropertyDescriptor)
			{
				propertyDescriptor = (IPropertyDescriptor)delegate;
			}
			else
			{
				break;
			}
		}
		Object propertyValue;
		if (propertyDescriptor instanceof IPropertySetter)
		{
			// a combined property
			propertyValue = ((IPropertySetter)propertyDescriptor).getProperty(this);
		}
		else
		{
			propertyValue = getPersistPropertyValue(id);
		}
		return convertGetPropertyValue(id, propertyDescriptor, propertyValue);
	}

	public static Object convertGetPropertyValue(Object id, IPropertyDescriptor propertyDescriptor, Object rawValue)
	{
		Object value = rawValue;
		if (propertyDescriptor instanceof IPropertyController)
		{
			IPropertyConverter propertyConverter = ((IPropertyController)propertyDescriptor).getConverter();
			if (propertyConverter != null)
			{
				return propertyConverter.convertProperty(id, value);
			}
		}
		return value;
	}

	public static Object convertSetPropertyValue(Object id, IPropertyDescriptor propertyDescriptor, Object rawValue)
	{
		if (propertyDescriptor instanceof IPropertyController)
		{
			IPropertyConverter propertyConverter = ((IPropertyController)propertyDescriptor).getConverter();
			if (propertyConverter != null)
			{
				return propertyConverter.convertValue(id, rawValue);
			}
		}
		return rawValue;
	}

	public Object getDefaultPersistValue(Object id)
	{
		init();
		PropertyDescriptorWrapper beanPropertyDescriptor = getBeansProperties().get(id);
		if (beanPropertyDescriptor == null)
		{
			ServoyLog.logError("Unknown property id " + id, null);
			return null;

		}
		if (id instanceof String)
		{
			AbstractRepository repository = (EclipseRepository)persistContext.getPersist().getRootObject().getRepository();
			try
			{
				Element element = repository.getContentSpec().getPropertyForObjectTypeByName(persistContext.getPersist().getTypeID(), (String)id);
				if (element == null)
				{
					// no content spec (example: form.width), try based on property descriptor
					PropertyDescriptorWrapper propertyDescriptor = beansProperties.get(id);
					if (propertyDescriptor != null)
					{
						if (propertyDescriptor.propertyDescriptor.getPropertyType() == int.class)
						{
							return repository.convertArgumentStringToObject(IRepository.INTEGER, null);
						}
						else if (propertyDescriptor.propertyDescriptor.getPropertyType() == boolean.class)
						{
							return repository.convertArgumentStringToObject(IRepository.BOOLEAN, null);
						}
					}
				}
				else
				{
					return repository.convertArgumentStringToObject(element.getTypeID(), element.getDefaultTextualClassValue());
				}
			}
			catch (RepositoryException e)
			{
				ServoyLog.logError("Could not get default value for id " + id, e);
			}
		}
		if (persistContext.getPersist() == beanPropertyDescriptor.valueObject)
		{
			ServoyLog.logError("Could not find content spec value for id " + id + " type = " + persistContext.getPersist().getTypeID(), null);
		}
		// else it is a bean property
		return null;
	}


	public boolean isPropertySet(Object id)
	{
		if (readOnly) return false;

		// recursively process a.b.c properties
		if (id instanceof String && !((String)id).startsWith(BEAN_PROPERTY_PREFIX_DOT))
		{
			String propId = (String)id;
			int dot = propId.indexOf('.');
			if (dot > 0)
			{
				Object propertyValue = getPropertyValue(propId.substring(0, dot));
				if (propertyValue instanceof IAdaptable)
				{
					IPropertySource propertySource = (IPropertySource)((IAdaptable)propertyValue).getAdapter(IPropertySource.class);
					if (propertySource != null)
					{
						return propertySource.isPropertySet(propId.substring(dot + 1));
					}
				}
			}
		}

		init();
		IPropertyDescriptor pd = propertyDescriptors.get(id);
		if (pd == null)
		{
			pd = hiddenPropertyDescriptors.get(id);
		}
		while (pd instanceof IDelegate && !(pd instanceof IPropertySetter))
		{
			Object delegate = ((IDelegate)pd).getDelegate();
			if (delegate instanceof IPropertyDescriptor)
			{
				pd = (IPropertyDescriptor)delegate;
			}
			else
			{
				break;
			}
		}
		if (pd instanceof IPropertySetter)
		{
			return ((IPropertySetter)pd).isPropertySet(this);
		}

		if (persistContext.getContext() instanceof Form && (persistContext.getPersist().getAncestor(IRepository.FORMS) != persistContext.getContext()))
		{
			return false;
		}

		if (((AbstractBase)persistContext.getPersist()).hasProperty((String)id))
		{
			return true;
		}

		Object defaultValue = getDefaultPersistValue(id);
		Object propertyValue = getPersistPropertyValue(id);
		return defaultValue != propertyValue && (defaultValue == null || !defaultValue.equals(propertyValue));
	}

	public void resetPropertyValue(Object id)
	{
		if (readOnly)
		{
			throw new RuntimeException("Cannot reset read-only property");
		}
		try
		{
			init();

			// recursively process a.b.c properties
			if (id instanceof String && !((String)id).startsWith(BEAN_PROPERTY_PREFIX_DOT))
			{
				String propId = (String)id;
				int dot = propId.indexOf('.');
				if (dot > 0)
				{
					Object propertyValue = getPropertyValue(propId.substring(0, dot));
					if (propertyValue instanceof IAdaptable)
					{
						IPropertySource propertySource = (IPropertySource)((IAdaptable)propertyValue).getAdapter(IPropertySource.class);
						if (propertySource != null)
						{
							propertySource.resetPropertyValue(propId.substring(dot + 1));
							return;
						}
					}
				}
			}

			IPropertyDescriptor pd = propertyDescriptors.get(id);
			if (pd == null)
			{
				pd = hiddenPropertyDescriptors.get(id);
			}
			while (pd instanceof IDelegate && !(pd instanceof IPropertySetter))
			{
				Object delegate = ((IDelegate)pd).getDelegate();
				if (delegate instanceof IPropertyDescriptor)
				{
					pd = (IPropertyDescriptor)delegate;
				}
				else
				{
					break;
				}
			}
			if (pd instanceof IPropertySetter)
			{
				((IPropertySetter)pd).resetPropertyValue(this);
				return;
			}

			PropertyDescriptorWrapper beanPropertyDescriptor = getBeansProperties().get(id);
			if (beanPropertyDescriptor != null)
			{
				createOverrideElementIfNeeded();

				if (persistContext.getPersist() instanceof AbstractBase && beanPropertyDescriptor.valueObject == persistContext.getPersist() /*
																																			 * not a bean
																																			 * property
																																			 */)
				{
					((AbstractBase)persistContext.getPersist()).clearProperty((String)id);
					if (persistContext.getPersist() instanceof ISupportExtendsID &&
						PersistHelper.isOverrideElement((ISupportExtendsID)persistContext.getPersist()) &&
						!((AbstractBase)persistContext.getPersist()).hasOverrideProperties())
					{
						// last property was reset, remove overriding persist
						IPersist superPersist = PersistHelper.getSuperPersist((ISupportExtendsID)persistContext.getPersist());
						try
						{
							((IDeveloperRepository)((AbstractBase)persistContext.getPersist()).getRootObject().getRepository()).deleteObject(persistContext.getPersist());
							persistContext.getPersist().getParent().removeChild(persistContext.getPersist());
							persistContext = PersistContext.create(superPersist, persistContext.getContext());
							beansProperties = null;
							propertyDescriptors = null;
							hiddenPropertyDescriptors = null;
						}
						catch (RepositoryException e)
						{
							ServoyLog.logError(e);
						}
					}

					if (persistContext.getPersist() instanceof Bean && "size".equals(id))
					{
						// size, location and name are set on persist, not on bean instance
						Object beanDesignInstance = ModelUtils.getEditingFlattenedSolution(persistContext.getPersist()).getBeanDesignInstance(
							(Bean)persistContext.getPersist());
						if (beanDesignInstance instanceof Component)
						{
							((Component)beanDesignInstance).setSize((Dimension)getPersistPropertyValue(id));
						}
					}
				}
				else
				{
					setPersistPropertyValue(id, getDefaultPersistValue(id));
				}

				if ("groupID".equals(id))
				{
					ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, persistContext.getPersist().getParent(), true);
				}
				else
				{
					// fire persist change recursively if the style is changed
					ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, persistContext.getPersist(),
						"styleName".equals(id) || "extendsID".equals(id));
				}
			}
		}
		catch (Exception e)
		{
			ServoyLog.logError("Could not rest set property value for id " + id + " on object " + persistContext.getPersist(), e);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Could not reset property", e.getMessage());
		}
	}

	private static boolean isQuoted(String text)
	{
		return ((text.startsWith("'") && text.endsWith("'")) || (text.startsWith("\"") && text.endsWith("\"")));
	}

	public void setPersistPropertyValue(Object id, Object value)
	{
		init();
		PropertyDescriptorWrapper beanPropertyDescriptor = getBeansProperties().get(id);
		if (beanPropertyDescriptor != null)
		{
			try
			{
				if (createOverrideElementIfNeeded())
				{
					beanPropertyDescriptor = getBeansProperties().get(id);
				}
				boolean isInheritedValue = (value == null && persistContext.getPersist() instanceof ISupportExtendsID &&
					beanPropertyDescriptor.valueObject == persistContext.getPersist() &&
					PersistHelper.getSuperPersist((ISupportExtendsID)persistContext.getPersist()) != null && ((AbstractBase)PersistHelper.getSuperPersist((ISupportExtendsID)persistContext.getPersist())).getProperty((String)id) != null);
				if (beanPropertyDescriptor.valueObject instanceof AbstractBase && value == null && !isInheritedValue)
				{
					((AbstractBase)beanPropertyDescriptor.valueObject).clearProperty((String)id);
				}
				else if ("name".equals(id) && beanPropertyDescriptor.valueObject instanceof ISupportUpdateableName)
				{
					if (value instanceof String || value == null)
					{
						((ISupportUpdateableName)beanPropertyDescriptor.valueObject).updateName(
							ServoyModelManager.getServoyModelManager().getServoyModel().getNameValidator(), (String)value);
					}
					else
					{
						// value not a string
						ServoyLog.logWarning(
							"Cannot set " + id + " property on object " + beanPropertyDescriptor.valueObject + " with type " + value.getClass(), null);
					}
				}
				else
				{
					beanPropertyDescriptor.propertyDescriptor.getWriteMethod().invoke(beanPropertyDescriptor.valueObject, new Object[] { value });
					if ("i18nDataSource".equals(id))
					{
						I18NMessagesUtil.showDatasourceWarning();
						ServoyProject servoyProject = ServoyModelManager.getServoyModelManager().getServoyModel().getActiveProject();
						EclipseMessages.writeProjectI18NFiles(servoyProject, false, false);
					}
				}
				if (persistContext.getPersist() instanceof Bean)
				{
					if (beanPropertyDescriptor.valueObject == persistContext.getPersist())
					{
						// size, location and name are set on persist, not on bean instance
						if ("size".equals(id) && (value == null || value instanceof Dimension))
						{
							Object beanDesignInstance = ModelUtils.getEditingFlattenedSolution(persistContext.getPersist()).getBeanDesignInstance(
								(Bean)persistContext.getPersist());
							if (beanDesignInstance instanceof Component)
							{
								((Component)beanDesignInstance).setSize((Dimension)value);
							}
						}
					}
					else
					{
						ComponentFactory.updateBeanWithItsXML((Bean)persistContext.getPersist(), beanPropertyDescriptor.valueObject);
					}
				}


				if (id.equals("textOrientation"))
				{
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Orientation change",
								"Running clients need to be restarted to make text orientation changes effective");
						}
					});
				}

			}
			catch (Exception e)
			{
				ServoyLog.logError("Could not set property value for id " + id + " on object " + beanPropertyDescriptor.valueObject, e);
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Could not update property", e.getMessage());
			}

			if ("groupID".equals(id))
			{
				ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, persistContext.getPersist().getParent(), true);
			}
			else
			{
				// fire persist change recursively if the style (or other ui related property) is changed
				ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, persistContext.getPersist(),
					"styleName".equals(id) || "extendsID".equals(id));
			}
		}
	}

	/**
	 * Create an override element of the current element if this is the first override
	 * @throws RepositoryException 
	 */
	private boolean createOverrideElementIfNeeded() throws RepositoryException
	{
		IPersist overridePersist = ElementUtil.getOverridePersist(persistContext);
		if (overridePersist == persistContext.getPersist())
		{
			return false;
		}

		persistContext = PersistContext.create(overridePersist, persistContext.getContext());
		beansProperties = null;
		propertyDescriptors = null;
		hiddenPropertyDescriptors = null;
		init();
		return true;
	}

	public void setPropertyValue(Object id, Object value)
	{
		if (readOnly)
		{
			throw new RuntimeException("Cannot save read-only property");
		}

		try
		{
			createOverrideElementIfNeeded();
			init();

			// recursively process a.b.c properties
			if (id instanceof String && !((String)id).startsWith(BEAN_PROPERTY_PREFIX_DOT))
			{
				String propId = (String)id;
				int dot = propId.indexOf('.');
				if (dot > 0)
				{
					String subPropId = propId.substring(0, dot);
					Object propertyValue = getPropertyValue(subPropId);
					if (propertyValue instanceof IAdaptable)
					{
						IPropertySource propertySource = (IPropertySource)((IAdaptable)propertyValue).getAdapter(IPropertySource.class);
						if (propertySource != null)
						{
							propertySource.setPropertyValue(propId.substring(dot + 1), value);
							setPropertyValue(subPropId, propertyValue);
							return;
						}
					}
				}
			}

			Object val = value;
			IPropertyDescriptor propertyDescriptor = propertyDescriptors.get(id);
			if (propertyDescriptor == null)
			{
				propertyDescriptor = hiddenPropertyDescriptors.get(id);
			}
			while (propertyDescriptor instanceof IDelegate && !(propertyDescriptor instanceof IPropertySetter))
			{
				Object delegate = ((IDelegate)propertyDescriptor).getDelegate();
				if (delegate instanceof IPropertyDescriptor)
				{
					propertyDescriptor = (IPropertyDescriptor)delegate;
				}
				else
				{
					break;
				}
			}
			Object convertedValue = convertSetPropertyValue(id, propertyDescriptor, val);
			if (propertyDescriptor instanceof IPropertySetter)
			{
				((IPropertySetter)propertyDescriptor).setProperty(this, convertedValue);
			}
			else
			{
				setPersistPropertyValue(id, convertedValue);
			}
		}
		catch (Exception e)
		{
			ServoyLog.logError("Could not set property value for id " + id + " on object " + persistContext.getPersist(), e);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Could not update property", e.getMessage());
		}
	}

	@Override
	public String toString()
	{
		StringBuilder retval = new StringBuilder();
		if (persistContext.getPersist() instanceof Portal)
		{
			retval.append("Portal (relation: ").append(((Portal)persistContext.getPersist()).getRelationName()).append(')');
		}
		else if (persistContext.getPersist() instanceof GraphicalComponent)
		{
			retval.append("Label / Button");
		}
		else if (persistContext.getPersist() instanceof RectShape)
		{
			retval.append("Rectangle");
		}
		else
		{
			retval.append(persistContext.getPersist().getClass().getName().replaceFirst(".*\\.", ""));
		}

		if (persistContext.getPersist() instanceof Tab)
		{
			String relationName = ((Tab)persistContext.getPersist()).getRelationName();
			if (relationName != null)
			{
				retval.append(" (relation: ").append(relationName).append(')');
			}
			else
			{
				retval.append(" (relationless)");
			}
		}
		else if (persistContext.getPersist() instanceof Bean)
		{
			retval.append(" (class: ");
			String beanClassName = ((Bean)persistContext.getPersist()).getBeanClassName();
			if (beanClassName != null)
			{
				retval.append(beanClassName.replaceFirst(".*\\.", ""));
			}
			retval.append(')');
		}

		String name = getActualComponentName(persistContext.getPersist());
		if (name != null)
		{
			retval.append(" - ").append(name);
		}
		if (persistContext.getPersist().getParent() instanceof Portal)
		{
			retval.append(" - (parent: portal)");
		}
		if (persistContext.getPersist() instanceof ISupportDataProviderID)
		{
			try
			{
				String dataprovider = ((ISupportDataProviderID)persistContext.getPersist()).getDataProviderID();
				FlattenedSolution editingFlattenedSolution = ModelUtils.getEditingFlattenedSolution(persistContext.getPersist());
				Form flattenedForm = editingFlattenedSolution.getFlattenedForm(persistContext.getPersist());
				IDataProviderLookup dataproviderLookup = editingFlattenedSolution.getDataproviderLookup(null, flattenedForm);
				IDataProvider dp = dataproviderLookup.getDataProvider(dataprovider);
				if (dp != null)
				{
					retval.append(" - ");
					if (dp instanceof ColumnWrapper)
					{
						dp = ((ColumnWrapper)dp).getColumn();
					}
					if (dp instanceof Column)
					{
						retval.append("Column");
					}
					else if (dp instanceof ScriptCalculation)
					{
						retval.append("Calculation");
					}
					else if (dp instanceof AggregateVariable)
					{
						retval.append("Aggregate[");
						retval.append(((AggregateVariable)dp).getTypeAsString());
						retval.append('(');
						retval.append(((AggregateVariable)dp).getColumnNameToAggregate());
						retval.append(")]");
					}
					else if (dp instanceof ScriptVariable)
					{
						if (((ScriptVariable)dp).getParent() instanceof Form)
						{
							retval.append("FormVariable");
						}
						else
						{
							retval.append("Global[").append(((ScriptVariable)dp).getScopeName()).append(']');
						}
					}
					retval.append('(');
					retval.append(dataprovider);
					retval.append(',');
					// use dataprovider type as defined by converter
					ComponentFormat componentFormat = ComponentFormat.getComponentFormat(null, dp, Activator.getDefault().getDesignClient());
					retval.append(Column.getDisplayTypeString(componentFormat.dpType));
					Column column = null;
					if (dp instanceof ScriptCalculation)
					{
						column = ((ScriptCalculation)dp).getTable().getColumn(dataprovider);
					}
					else if (dp instanceof AggregateVariable)
					{
						column = ((AggregateVariable)dp).getTable().getColumn(dataprovider);
					}
					else if (dp instanceof Column)
					{
						column = (Column)dp;
					}
					if (dp.getLength() > 0)
					{
						switch (componentFormat.dpType)
						{
							case IColumnTypes.TEXT :
							case IColumnTypes.MEDIA :
								retval.append(',');
								retval.append(dp.getLength());
								break;
							case IColumnTypes.NUMBER :
								if (column != null)
								{
									retval.append(',');
									retval.append(column.getLength());
									retval.append(',');
									retval.append(column.getScale());
								}
						}
					}
					if (column != null && !column.getAllowNull())
					{
						retval.append(",not nullable");
					}
					retval.append(')');
//				retval.append(",");
//				if (dp.getColumnWrapper().getColumn().get)
//				retval.append("nullable");

				}
			}
			catch (RepositoryException e)
			{
				ServoyLog.logError(e);
			}
		}
		return retval.toString();
	}

	public static String getActualComponentName(IPersist persist)
	{
		String name = null;
		if (persist instanceof IFormElement)
		{
			name = ((IFormElement)persist).getName();
		}

		if (persist instanceof ISupportDataProviderID && (name == null || name.length() == 0))
		{
			name = ((ISupportDataProviderID)persist).getDataProviderID();
		}

		if (persist instanceof ISupportName && (name == null || name.length() == 0))
		{
			name = ((ISupportName)persist).getName();
		}

		if (persist instanceof IRootObject && (name == null || name.length() == 0))
		{
			name = ((IRootObject)persist).getName();
		}

		if (persist instanceof Part && (name == null || name.length() == 0))
		{
			name = ((Part)persist).getEditorName();
		}

		if (name != null && !(persist instanceof Media))
		{
			int index = name.indexOf('.');
			if (index != -1)
			{
				name = name.substring(index + 1);
			}
		}
		return name;
	}

	/**
	 * Wrapper class for PropertyDescriptor and value object.
	 * 
	 * @author rgansevles
	 * 
	 */
	public static class PropertyDescriptorWrapper
	{
		public final java.beans.PropertyDescriptor propertyDescriptor;
		public Object valueObject;
		private PropertyEditor propertyEditor;

		PropertyDescriptorWrapper(java.beans.PropertyDescriptor propertyDescriptor, Object valueObject)
		{
			this.propertyDescriptor = propertyDescriptor;
			this.valueObject = valueObject;
		}

		public void setPropertyEditor(PropertyEditor propertyEditor)
		{
			this.propertyEditor = propertyEditor;
		}

		public PropertyEditor getPropertyEditor()
		{
			return propertyEditor;
		}
	}

	private static IPropertyDescriptor getGeneralPropertyDescriptor(PersistContext persistContext, final boolean readOnly, String id, String displayName,
		String name, FlattenedSolution flattenedEditingSolution)
	{
		// Some properties apply to both persists and beans

		if (name.equals("dataSource"))
		{
			// cannot change table when we have a super-form that already has a data source
			boolean propertyReadOnly = false;
			if (!readOnly && persistContext != null && persistContext.getPersist() instanceof Form && ((Form)persistContext.getPersist()).getExtendsID() > 0)
			{
				Form flattenedSuperForm = flattenedEditingSolution.getFlattenedForm(flattenedEditingSolution.getForm(((Form)persistContext.getPersist()).getExtendsID()));
				propertyReadOnly = flattenedSuperForm == null /* superform not found? make readonly for safety */
					|| flattenedSuperForm.getDataSource() != null; /* superform has a data source */

				if (propertyReadOnly && flattenedSuperForm != null && ((Form)persistContext.getPersist()).getDataSource() != null &&
					!((Form)persistContext.getPersist()).getDataSource().equals(flattenedSuperForm.getDataSource()))
				{
					// current form has invalid data source (overrides with different value), allow user to correct
					String[] dbServernameTablename = DataSourceUtilsBase.getDBServernameTablename(flattenedSuperForm.getDataSource());
					if (dbServernameTablename != null)
					{
						return new DatasourceController(id, displayName, "Select table", false,
							new Object[] { TableContentProvider.TABLE_NONE, new TableWrapper(dbServernameTablename[0], dbServernameTablename[1],
								EditorUtil.isViewTypeTable(dbServernameTablename[0], dbServernameTablename[1])) },
							DatasourceLabelProvider.INSTANCE_NO_IMAGE_FULLY_QUALIFIED);
					}
				}
			}
			return new DatasourceController(id, displayName, "Select table", readOnly || propertyReadOnly, new TableContentProvider.TableListOptions(
				TableListOptions.TableListType.ALL, true), DatasourceLabelProvider.INSTANCE_NO_IMAGE_FULLY_QUALIFIED);
		}

		if (name.equals("i18nDataSource"))
		{
			return new DatasourceController(id, displayName, "Select I18N table", readOnly, new TableContentProvider.TableListOptions(
				TableListOptions.TableListType.I18N, true), DatasourceLabelProvider.INSTANCE_NO_IMAGE_FULLY_QUALIFIED);
		}

		if (name.equals("primaryDataSource"))
		{
			return new DatasourceController(id, displayName, "Select Primary table", readOnly, new TableContentProvider.TableListOptions(
				TableListOptions.TableListType.ALL, false), DatasourceLabelProvider.INSTANCE_NO_IMAGE_FULLY_QUALIFIED);
		}

		if (name.equals("foreignDataSource"))
		{
			return new DatasourceController(id, displayName, "Select Foreign table", readOnly, new TableContentProvider.TableListOptions(
				TableListOptions.TableListType.ALL, false), DatasourceLabelProvider.INSTANCE_NO_IMAGE_FULLY_QUALIFIED);
		}

		if (name.equals("scrollbars"))
		{
			return new PropertyController<Integer, Object>(id, displayName, new ComplexProperty.ComplexPropertyConverter<Integer>()
			{
				@Override
				public Object convertProperty(Object property, Integer value)
				{
					return new ComplexProperty<Integer>(value)
					{
						@Override
						public IPropertySource getPropertySource()
						{
							ScrollbarSettingPropertySource scrollbarSettingPropertySource = new ScrollbarSettingPropertySource(this);
							scrollbarSettingPropertySource.setReadonly(readOnly);
							return scrollbarSettingPropertySource;
						}
					};
				}
			}, ScrollbarSettingLabelProvider.INSTANCE, new DummyCellEditorFactory(ScrollbarSettingLabelProvider.INSTANCE));
		}

		if ("horizontalAlignment".equals(name))
		{
			return HORIZONTAL_ALIGNMENT_CONTROLLER;
		}

		return null;
	}

	protected IPropertyDescriptor getPropertiesPropertyDescriptor(IPropertySource propertySource, String id, String displayName, String name,
		FlattenedSolution flattenedEditingSolution, Form form) throws RepositoryException
	{
		return getPropertiesPropertyDescriptor(propertySource, persistContext, readOnly, id, displayName, name, flattenedEditingSolution, form);
	}

	private static IPropertyDescriptor getPropertiesPropertyDescriptor(IPropertySource propertySource, final PersistContext persistContext,
		final boolean readOnly, final String id, String displayName, String name, final FlattenedSolution flattenedEditingSolution, final Form form)
		throws RepositoryException
	{
		if ("tabSeq".equals(name))
		{
			return new PropertyController<String, String>(id, displayName, null, null, new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					if (persistContext.getContext() instanceof Form) return new TabSeqDialogCellEditor(parent, null, new TabSeqDialogValueEditor(
						(Form)persistContext.getContext()), true, SWT.NONE);
					else return new TabSeqDialogCellEditor(parent, null, new TabSeqDialogValueEditor(null), true, SWT.NONE);
				}
			});
		}
		if ("verticalAlignment".equals(name))
		{
			return VERTICAL_ALIGNMENT_CONTROLLER;
		}

		if (name.equals("borderType"))
		{
			BorderPropertyController borderPropertyController = new BorderPropertyController(id, displayName, propertySource, persistContext);
			borderPropertyController.setReadonly(readOnly);

			// BorderPropertyController handles Border objects, the property is a String.
			return new PropertyController<String, Object>(id, displayName, new ChainedPropertyConverter<String, Border, Object>(BORDER_STRING_CONVERTER,
				borderPropertyController.getConverter()), borderPropertyController.getLabelProvider(), borderPropertyController);
		}

		if (name.equals("mediaOptions"))
		{
			return new MediaoptionsPropertyController(id, displayName, propertySource);
		}

		if (name.endsWith("printSliding"))
		{
			return SLIDING_OPTIONS_CONTROLLER;
		}

		if (name.equals("fontType"))
		{
			final IDefaultValue<String> getLastPaintedFont;
			if (persistContext.getPersist() instanceof AbstractBase)
			{
				getLastPaintedFont = new IDefaultValue<String>()
				{
					public String getValue()
					{
						return PropertyFontConverter.INSTANCE.convertProperty(id,
							((AbstractBase)persistContext.getPersist()).getRuntimeProperty(LastPaintedFontProperty));
					}
				};
			}
			else
			{
				getLastPaintedFont = null;
			}
			// Both property (P) and edit (E) types are font strings, parse the string as awt font and convert back so that guessed fonts are mapped to 
			// the correct string for that font
			return new PropertyController<String, String>(id, displayName, new ChainedPropertyConverter<String, java.awt.Font, String>(
				new InversedPropertyConverter<String, java.awt.Font>(PropertyFontConverter.INSTANCE), PropertyFontConverter.INSTANCE),
				FontLabelProvider.INSTANCE, new ICellEditorFactory()
				{
					public CellEditor createPropertyEditor(Composite parent)
					{
						return new FontCellEditor(parent, getLastPaintedFont);
					}
				});
		}

		if (name.endsWith("dataProviderID"))
		{
			Table table = null;
			Portal portal = (Portal)persistContext.getPersist().getAncestor(IRepository.PORTALS);
			final DataProviderOptions options;
			if (portal != null)
			{
				Relation[] relations = flattenedEditingSolution.getRelationSequence(portal.getRelationName());
				if (relations == null)
				{
					return null;
				}
				options = new DataProviderTreeViewer.DataProviderOptions(true, false, false, true /* related calcs */, false, false, false, false,
					INCLUDE_RELATIONS.NESTED, false, true, relations);
			}
			else
			{
				if (form == null) return null;
				try
				{
					table = flattenedEditingSolution.getFlattenedForm(form).getTable();
				}
				catch (RepositoryException ex)
				{
					ServoyLog.logInfo("Table form not accessible: " + ex.getMessage());
					return null;
				}
				options = new DataProviderTreeViewer.DataProviderOptions(true, table != null, table != null, table != null, true, true, table != null,
					table != null, INCLUDE_RELATIONS.NESTED, true, true, null);
			}
			final DataProviderConverter converter = new DataProviderConverter(flattenedEditingSolution, persistContext.getPersist(), table);
			DataProviderLabelProvider showPrefix = new DataProviderLabelProvider(false);
			showPrefix.setConverter(converter);
			DataProviderLabelProvider hidePrefix = new DataProviderLabelProvider(true);
			hidePrefix.setConverter(converter);

			ILabelProvider labelProviderShowPrefix = new SolutionContextDelegateLabelProvider(new FormContextDelegateLabelProvider(showPrefix,
				persistContext.getContext()));
			final ILabelProvider labelProviderHidePrefix = new SolutionContextDelegateLabelProvider(new FormContextDelegateLabelProvider(hidePrefix,
				persistContext.getContext()));
			PropertyController<String, String> propertyController = new PropertyController<String, String>(id, displayName, null, labelProviderShowPrefix,
				new ICellEditorFactory()
				{
					public CellEditor createPropertyEditor(Composite parent)
					{
						return new DataProviderCellEditor(parent, labelProviderHidePrefix, new DataProviderValueEditor(converter), form,
							flattenedEditingSolution, readOnly, options, converter);
					}
				});
			propertyController.setSupportsReadonly(true);
			return propertyController;
		}

		if (name.equals("relationName"))
		{
			Table primaryTable = null;
			if (form != null)
			{
				try
				{
					primaryTable = flattenedEditingSolution.getFlattenedForm(form).getTable();
				}
				catch (RepositoryException ex)
				{
					ServoyLog.logInfo("Table form not accessible: " + ex.getMessage());
				}
			}
			Table foreignTable = null;
			boolean incudeNone = false;
			if (persistContext.getPersist() instanceof Tab)
			{
				Form tabForm = flattenedEditingSolution.getForm(((Tab)persistContext.getPersist()).getContainsFormID());
				if (tabForm != null)
				{
					try
					{
						foreignTable = tabForm.getTable();
					}
					catch (RepositoryException ex)
					{
						ServoyLog.logInfo("Table form not accessible: " + ex.getMessage());
					}
				}
				incudeNone = true; // unrelated tabs
			}
			else if (persistContext.getPersist() instanceof Portal)
			{
				// only show relations that go to the same table
				Relation[] relationSequence = flattenedEditingSolution.getRelationSequence(((Portal)persistContext.getPersist()).getRelationName());
				if (relationSequence != null)
				{
					foreignTable = relationSequence[relationSequence.length - 1].getForeignTable();
				}
			}
			return new RelationPropertyController(id, displayName, persistContext, primaryTable, foreignTable, incudeNone, true);
		}

		if (name.endsWith("rowBGColorCalculation"))
		{
			if (form == null) return null;
			Table table = null;
			if (persistContext.getPersist() instanceof Portal)
			{
				Relation[] relations = flattenedEditingSolution.getRelationSequence(((Portal)persistContext.getPersist()).getRelationName());
				if (relations != null)
				{
					table = relations[relations.length - 1].getForeignTable();
				}
			}
			else
			{
				try
				{
					table = flattenedEditingSolution.getFlattenedForm(form).getTable();
				}
				catch (RepositoryException e)
				{
					ServoyLog.logInfo("Table form not accessible: " + e.getMessage());
				}
			}
			return new ScriptProviderPropertyController(id, displayName, table, persistContext);
		}

		if ("variableType".equals(name) && persistContext.getPersist() instanceof ScriptVariable && propertySource instanceof PersistPropertySource)
		{
			int[] iTypes = Column.allDefinedTypes;
			Integer[] integerTypes = new Integer[iTypes.length];
			String[] stringTypes = new String[iTypes.length];
			for (int i = 0; i < iTypes.length; i++)
			{
				integerTypes[i] = Integer.valueOf(iTypes[i]);
				stringTypes[i] = Column.getDisplayTypeString(iTypes[i]);
			}
			return new PropertySetterDelegatePropertyController<Integer, PersistPropertySource>(new ComboboxPropertyController<Integer>(id, displayName,
				new ComboboxPropertyModel<Integer>(integerTypes, stringTypes), Messages.LabelUnresolved), id)
			{
				// handle setting of this property via a IPropertySetter so that we can do additional stuff when the property is set.
				public void setProperty(PersistPropertySource propertySource, Integer value)
				{
					propertySource.setPersistPropertyValue(id, value);

					// the variable type is being set; the default value should change if incompatible
					ScriptVariable variable = (ScriptVariable)propertySource.getPersist();
					String defaultValue = variable.getDefaultValue();
					if (defaultValue != null)
					{
						String newDefault = null;
						int type = value.intValue();
						if (type == IColumnTypes.TEXT)
						{
							if (!isQuoted(defaultValue))
							{
								newDefault = Utils.makeJSExpression(defaultValue);
							}
						}
						else if (type == IColumnTypes.INTEGER)
						{
							if (isQuoted(defaultValue))
							{
								newDefault = defaultValue.substring(1, defaultValue.length() - 1);
							}
							else
							{
								newDefault = defaultValue;
							}
							try
							{
								Integer.parseInt(newDefault);
								newDefault = (defaultValue == newDefault) ? null : newDefault;
							}
							catch (NumberFormatException e)
							{
								newDefault = "";
							}
						}
						else if (type == IColumnTypes.NUMBER)
						{
							if (isQuoted(defaultValue))
							{
								newDefault = defaultValue.substring(1, defaultValue.length() - 1);
							}
							else
							{
								newDefault = defaultValue;
							}
							try
							{
								Double.parseDouble(newDefault);
								newDefault = (defaultValue == newDefault) ? null : newDefault;
							}
							catch (NumberFormatException e)
							{
								newDefault = "";
							}
						}

						if (newDefault != null)
						{
							variable.setDefaultValue(newDefault);
						}
					}
				}
			};
		}

		if ("rolloverCursor".equals(name))
		{
			return ROLLOVER_CURSOR_CONTROLLER;
		}

		if (name.endsWith("shapeType"))
		{
			return SHAPE_TYPE_CONTOLLER;
		}

		if (name.equals("view"))
		{
			return VIEW_TYPE_CONTOLLER;
		}

		if ("selectionMode".equals(name))
		{
			return SELECTION_MODE_CONTROLLER;
		}

		if (name.equals("textOrientation"))
		{
			return TEXT_ORIENTATION_CONTROLLER;
		}

		if (name.equals("solutionType"))
		{
			return SOLUTION_TYPE_CONTROLLER;
		}

		if (name.equals("joinType"))
		{
			return JOIN_TYPE_CONTROLLER;
		}

		if (name.equals("displayType"))
		{
			return DISPLAY_TYPE_CONTOLLER;
		}

		if (name.equals("tabOrientation"))
		{
			return TAB_ORIENTATION_CONTROLLER;
		}

		if (name.equals("format"))
		{
			return new PropertyDescriptor(id, displayName)
			{
				@Override
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new FormatCellEditor(parent, persistContext.getPersist());
				}
			};
		}
		if (name.equals("labelFor"))
		{
			if (form != null)
			{
				Form flattenedForm = flattenedEditingSolution.getFlattenedForm(form);
				int bodyStart = -1, bodyEnd = -1;
				if (flattenedForm.getView() == FormController.TABLE_VIEW || flattenedForm.getView() == FormController.LOCKED_TABLE_VIEW)
				{
					bodyStart = 0;
					Iterator<Part> parts = flattenedForm.getParts();
					while (parts.hasNext())
					{
						Part part = parts.next();
						if (part.getPartType() == Part.BODY)
						{
							bodyEnd = part.getHeight();
							break;
						}
						bodyStart = part.getHeight();
					}
				}

				List<String> names = new ArrayList<String>();
				for (IPersist object : flattenedForm.getAllObjectsAsList())
				{
					if (object instanceof IFormElement && ((IFormElement)object).getName() != null && ((IFormElement)object).getName().length() > 0)
					{
						boolean add = ((IFormElement)object).getTypeID() == IRepository.FIELDS;
						if (!add && bodyStart >= 0 && (!(object instanceof GraphicalComponent) || ((GraphicalComponent)object).getLabelFor() == null))
						{
							// TableView, add elements in the body
							Point location = ((IFormElement)object).getLocation();
							add = (location != null && location.y >= bodyStart && location.y < bodyEnd);
						}
						if (add)
						{
							names.add(((IFormElement)object).getName());
						}
					}
				}
				String[] array = names.toArray(new String[names.size()]);
				Arrays.sort(array, String.CASE_INSENSITIVE_ORDER);
				return new EditableComboboxPropertyController(id, displayName, array);
			}
		}
		if (name.equals("mnemonic"))
		{
			return MNEMONIC_CONTROLLER;
		}
		if (name.endsWith("navigatorID") && persistContext != null && persistContext.getPersist() instanceof Form)
		{
			final ILabelProvider formLabelProvider = new SolutionContextDelegateLabelProvider(new FormLabelProvider(flattenedEditingSolution, false),
				persistContext.getContext());
			PropertyDescriptor pd = new PropertyDescriptor(id, displayName)
			{
				@Override
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new ListSelectCellEditor(parent, "Select navigator form", new FormContentProvider(flattenedEditingSolution,
						(Form)persistContext.getPersist()), formLabelProvider, new FormValueEditor(flattenedEditingSolution), readOnly,
						new FormContentProvider.FormListOptions(FormListOptions.FormListType.FORMS, Boolean.FALSE, true, true, true), SWT.NONE, null,
						"navigatorFormDialog");
				}
			};
			pd.setLabelProvider(formLabelProvider);
			return pd;
		}

		if (name.equals("firstFormID") || name.equals("loginFormID"))
		{
			final ILabelProvider formLabelProvider = new SolutionContextDelegateLabelProvider(new FormLabelProvider(flattenedEditingSolution, false),
				persistContext.getContext());
			PropertyDescriptor pd = new PropertyDescriptor(id, displayName)
			{
				@Override
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new ListSelectCellEditor(parent, "Select form", new FormContentProvider(flattenedEditingSolution, null /* persist is solution */),
						formLabelProvider, new FormValueEditor(flattenedEditingSolution), readOnly, new FormContentProvider.FormListOptions(
							FormListOptions.FormListType.FORMS, null, true, false, false), SWT.NONE, null, "Select form dialog");
				}
			};
			pd.setLabelProvider(formLabelProvider);
			return pd;
		}

		if (name.equals("extendsID") && persistContext != null && persistContext.getPersist() instanceof Form)
		{
			if (form == null) return null;
			final ILabelProvider formLabelProvider = new SolutionContextDelegateLabelProvider(new FormLabelProvider(flattenedEditingSolution, true),
				persistContext.getContext());
			PropertyDescriptor pd = new PropertyDescriptor(id, displayName)
			{
				@Override
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new ListSelectCellEditor(parent, "Select parent form", new FormContentProvider(flattenedEditingSolution, form), formLabelProvider,
						new FormValueEditor(flattenedEditingSolution), readOnly, new FormContentProvider.FormListOptions(
							FormListOptions.FormListType.HIERARCHY, null, true, false, false), SWT.NONE, null, "parentFormDialog")
					{
						@Override
						protected Object openDialogBox(Control cellEditorWindow)
						{
							Object returnValue = super.openDialogBox(cellEditorWindow);
							if (returnValue != null)
							{
								Form f = (Form)persistContext.getPersist();
								if (!Utils.equalObjects(Integer.valueOf(f.getExtendsID()), returnValue))
								{
									List<IPersist> overridePersists = new ArrayList<IPersist>();
									for (IPersist child : f.getAllObjectsAsList())
									{
										if (child instanceof ISupportExtendsID && PersistHelper.isOverrideElement((ISupportExtendsID)child)) overridePersists.add(child);
									}
									if (overridePersists.size() > 0)
									{
										if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Delete override elements",
											"You are about to change the parent form, this will also delete all override elements (this action cannot be undone). Are you sure you want to proceed?"))
										{
											for (IPersist child : overridePersists)
											{
												f.removeChild(child);
											}
										}
										else
										{
											returnValue = null;
										}
									}
									if (returnValue != null)
									{
										// here we decide what happens to the parts that are declared in this form; if new extended form
										// has those parts, mark them as overriden; if we wouldn't do this you could end up with 2 body parts in the same form: one overriden and one not�
										// an alternative would be to simply delete these parts
										ArrayList<Part> inheritedParts = new ArrayList<Part>();
										int newExtendsId = ((Integer)returnValue).intValue();
										if (newExtendsId != Form.NAVIGATOR_NONE && newExtendsId != Form.NAVIGATOR_DEFAULT) // NAVIGATOR_NONE (0 default value as in contentspec) should be used but in the past either one or the other was used
										{
											Form newSuperForm = flattenedEditingSolution.getForm(((Integer)returnValue).intValue());
											if (newSuperForm != null)
											{
												Iterator<Part> it = newSuperForm.getParts();
												while (it.hasNext())
													inheritedParts.add(it.next());
												List<Form> ancestorForms = flattenedEditingSolution.getFormHierarchy(newSuperForm);
												for (Form frm : ancestorForms)
												{
													it = frm.getParts();
													while (it.hasNext())
														inheritedParts.add(it.next());
												}
											}
										}

										Iterator<Part> it = f.getParts();
										while (it.hasNext())
										{
											Part p = it.next();
											Part ancestorP = null;
											// find first ancestor of the same part type for parts that cannot be duplicate in a form
											for (Part aP : inheritedParts)
											{
												if (aP.getPartType() == p.getPartType() && !Part.canBeMoved(p.getPartType()))
												{
													ancestorP = aP;
													break;
												}
											}
											if (ancestorP != null)
											{
												// remove part type as it is inherited
												Map<String, Object> pMap = p.getPropertiesMap();
												pMap.remove(StaticContentSpecLoader.PROPERTY_PARTTYPE.getPropertyName());
												p.copyPropertiesMap(pMap, true);

												p.setExtendsID(ancestorP.getID());
											}
										}
									}
								}
							}
							return returnValue;
						}
					};
				}
			};

			pd.setLabelProvider(formLabelProvider);
			return pd;
		}

		if (name.endsWith("valuelistID"))
		{
			return new ValuelistPropertyController<Integer>(id, displayName, persistContext, true);
		}

		if (name.equals("rolloverImageMediaID") || name.equals("imageMediaID"))
		{
			return new MediaPropertyController<Integer>(id, displayName, persistContext, true)
			{
				@Override
				protected IPropertyConverter<Integer, MediaNode> createConverter()
				{
					// convert between media node and image name
					return new IPropertyConverter<Integer, MediaNode>()
					{
						public MediaNode convertProperty(@SuppressWarnings("hiding")
						Object id, Integer value)
						{
							if (value != null && value.intValue() > 0)
							{
								Media media = flattenedEditingSolution.getMedia(value.intValue());
								if (media != null)
								{
									String mediaName = media.getName();
									return new MediaNode(mediaName, mediaName, MediaNode.TYPE.IMAGE, flattenedEditingSolution.getSolution(), null, media);
								}
								return MediaLabelProvider.MEDIA_NODE_UNRESOLVED;
							}
							return MediaLabelProvider.MEDIA_NODE_NONE;
						}

						public Integer convertValue(@SuppressWarnings("hiding")
						Object id, MediaNode value)
						{
							return value == null || value == MediaLabelProvider.MEDIA_NODE_NONE ? Integer.valueOf(0)
								: Integer.valueOf(value.getMedia().getID());
						}
					};
				}
			};
		}

		if (name.equals("styleClass"))
		{
			return createStyleClassPropertyController(persistContext.getPersist(), id, displayName, ModelUtils.getStyleLookupname(persistContext.getPersist()),
				form);
		}

		if (name.equals("namedFoundSet"))
		{
			return createNamedFoundsetPropertyController(id, displayName, form);
		}

		if (name.equals("styleName"))
		{
			ComboboxPropertyModel<String> model = new ComboboxPropertyModel<String>(getStyleNames(), NullDefaultLabelProvider.LABEL_DEFAULT);
			return new ComboboxPropertyController<String>(id, displayName, model, Messages.LabelUnresolved, new ComboboxDelegateValueEditor<String>(
				StyleValueEditor.INSTANCE, model));
		}

		if (name.equals("anchors"))
		{
			return ANCHOR_CONTROLLER;
		}
		if (name.equals("encapsulation"))
		{
			return ENCAPSULATION_CONTROLLER;
		}

		if (name.equals("rotation"))
		{
			return ROTATION_CONTROLLER;
		}

		if (name.equals("groupbyDataProviderIDs"))
		{
			return COMMA_SEPARATED_CONTROLLER;
		}

		if (name.equals("modulesNames"))
		{
			// list the available modules as well as unknown modules that are currently set.
			List<String> availableSolutions = getSolutionNames(persistContext.getPersist());
			StringTokenizerConverter converter = new StringTokenizerConverter(",", true);

			String[] modulesNames = null;
			if (persistContext.getPersist() instanceof Solution)
			{
				modulesNames = converter.convertProperty(id, ((Solution)persistContext.getPersist()).getModulesNames());
			}

			final List<String> allSolutions = new ArrayList<String>(availableSolutions);
			if (modulesNames != null)
			{
				for (String module : modulesNames)
				{
					if (!allSolutions.contains(module))
					{
						allSolutions.add(module);
					}
				}
			}

			final ILabelProvider labelProvider;
			if (allSolutions.size() == availableSolutions.size())
			{
				// no 'unknown' modules
				labelProvider = new ArrayLabelProvider(converter);
			}
			else
			{
				// found 'unknown' modules in current value, mark them with italic font
				labelProvider = new ValidvalueDelegatelabelProvider(new ArrayLabelProvider(converter), availableSolutions, null, FontResource.getDefaultFont(
					SWT.ITALIC, 0));
				Collections.sort(allSolutions);
			}

			return new PropertyController<String, Object[]>(id, displayName, converter, labelProvider, new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new ListSelectCellEditor(parent, "Select modules", labelProvider, null, readOnly, allSolutions.toArray(), SWT.MULTI | SWT.CHECK,
						null, "selectModules");
				}
			});
		}

		if (name.endsWith("initialSort"))
		{
			final ITableDisplay tableDisplay;
			if (persistContext != null && persistContext.getPersist() instanceof Portal)
			{
				tableDisplay = new ITableDisplay()
				{
					public String getInitialSort()
					{
						return ((Portal)persistContext.getPersist()).getInitialSort();
					}

					public Table getTable() throws RepositoryException
					{
						Table table = null;
						Relation[] relations = flattenedEditingSolution.getRelationSequence(((Portal)persistContext.getPersist()).getRelationName());
						if (relations != null)
						{
							table = relations[relations.length - 1].getForeignTable();
						}
						return table;
					}
				};
			}
			else if (form != null)
			{
				tableDisplay = flattenedEditingSolution.getFlattenedForm(form);
			}
			else if (persistContext.getPersist() instanceof Relation)
			{
				tableDisplay = new ITableDisplay()
				{
					public String getInitialSort()
					{
						return ((Relation)persistContext.getPersist()).getInitialSort();
					}

					public Table getTable() throws RepositoryException
					{
						return ((Relation)persistContext.getPersist()).getForeignTable();
					}
				};
			}
			else
			{
				tableDisplay = null;
			}
			if (tableDisplay == null)
			{
				return null;
			}

			return new PropertyController<String, String>(id, displayName, NULL_STRING_CONVERTER, NullDefaultLabelProvider.LABEL_DEFAULT,
				new ICellEditorFactory()
				{
					public CellEditor createPropertyEditor(Composite parent)
					{
						return new SortCellEditor(parent, flattenedEditingSolution, tableDisplay, "Select sorting fields",
							NullDefaultLabelProvider.LABEL_DEFAULT);
					}
				});
		}

		if (name.equals("text") || name.equals("toolTipText") || name.equals("titleText"))
		{
			Table table = null;
			if (form != null)
			{
				try
				{
					table = flattenedEditingSolution.getFlattenedForm(form).getTable();
				}
				catch (RepositoryException ex)
				{
					ServoyLog.logInfo("Table form not accessible: " + ex.getMessage());
				}
			}
			final Table finalTable = table;
			return new PropertyController<String, String>(id, displayName, new IPropertyConverter<String, String>()
			{
				public String convertProperty(Object id, String value)
				{
					return value;
				}

				public String convertValue(Object id, String value)
				{
					if ("titleText".equals(id) && value != null && value.length() > 0 && value.trim().length() == 0)
					{
						return FormManager.NO_TITLE_TEXT;
					}
					return value;
				}

			}, TextCutoffLabelProvider.DEFAULT, new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return new TagsAndI18NTextCellEditor(parent, persistContext, flattenedEditingSolution, TextCutoffLabelProvider.DEFAULT, finalTable,
						"Edit text property", Activator.getDefault().getDesignClient());
				}
			});
		}

		if (name.equals("name"))
		{
			return new DelegatePropertyController<String, String>(new PropertyController<String, String>(id, displayName, NULL_STRING_CONVERTER, null,
				new ICellEditorFactory()
				{
					public CellEditor createPropertyEditor(Composite parent)
					{
						VerifyingTextCellEditor cellEditor = new VerifyingTextCellEditor(parent);
						cellEditor.addVerifyListener(DocumentValidatorVerifyListener.IDENT_SERVOY_VERIFIER);
						cellEditor.setValidator(new ICellEditorValidator()
						{
							public String isValid(Object value)
							{
								if (ModelUtils.isInheritedFormElement(persistContext.getPersist(), persistContext.getContext()))
								{
									return "Cannot change name of an override element.";
								}
								if (value instanceof String && ((String)value).length() > 0)
								{
									try
									{
										if (persistContext.getPersist() instanceof IFormElement)
										{
											ServoyModelManager.getServoyModelManager().getServoyModel().getNameValidator().checkName(
												(String)value,
												persistContext.getPersist().getID(),
												new ValidatorSearchContext(flattenedEditingSolution.getFlattenedForm(persistContext.getPersist()),
													IRepository.ELEMENTS), false);

										}
										else if (persistContext.getPersist() instanceof Form)
										{
											ServoyModelManager.getServoyModelManager().getServoyModel().getNameValidator().checkName((String)value,
												persistContext.getPersist().getID(),
												new ValidatorSearchContext(persistContext.getPersist(), IRepository.FORMS), false);
										}
									}
									catch (RepositoryException e)
									{
										return e.getMessage();
									}
								}
								return null;
							}
						});

						return cellEditor;
					}
				}))
			{
				@Override
				public IMergedPropertyDescriptor createMergedPropertyDescriptor(IMergeablePropertyDescriptor pd)
				{
					return null;
				}

				@Override
				public boolean isMergeableWith(IMergeablePropertyDescriptor pd)
				{
					return true;
				}
			};
		}

		if (name.equals("defaultPageFormat"))
		{
			return PAGE_FORMAT_CONTROLLER;
		}

		if (name.equals("loginSolutionName"))
		{
			return LOGIN_SOLUTION_CONTROLLER;
		}

		if (name.equals("designTimeProperties"))
		{
			return DESIGN_PROPERTIES_CONTROLLER;
		}

		if ("location".equals(name))
		{
			return new PropertyController<java.awt.Point, Object>(id, displayName, new ComplexPropertyConverter<java.awt.Point>()
			{
				@Override
				public Object convertProperty(Object id, java.awt.Point value)
				{
					return new ComplexProperty<java.awt.Point>(value)
					{
						@Override
						public IPropertySource getPropertySource()
						{
							PointPropertySource pointPropertySource = new PointPropertySource(this);
							pointPropertySource.setReadonly(readOnly);
							return pointPropertySource;
						}
					};
				}
			}, PointPropertySource.getLabelProvider(), new ICellEditorFactory()
			{
				public CellEditor createPropertyEditor(Composite parent)
				{
					return PointPropertySource.createPropertyEditor(parent);
				}
			});
		}

		return null;
	}

	public static class NullDefaultLabelProvider extends LabelProvider implements IFontProvider
	{
		public static final NullDefaultLabelProvider LABEL_NONE = new NullDefaultLabelProvider(Messages.LabelNone);
		public static final NullDefaultLabelProvider LABEL_DEFAULT = new NullDefaultLabelProvider(Messages.LabelDefault);
		private final String defaultLabel;

		private NullDefaultLabelProvider(String defaultLabel)
		{
			this.defaultLabel = defaultLabel;
		}

		@Override
		public String getText(Object element)
		{
			return (element == null || "".equals(element)) ? defaultLabel : element.toString();
		}

		public Font getFont(Object element)
		{
			if (element == null || "".equals(element))
			{
				return FontResource.getDefaultFont(SWT.BOLD, -1);
			}
			return FontResource.getDefaultFont(SWT.NONE, 0);
		}
	}

	/** Sort property descriptors by PropertyEditorOption.propertyOrder if defined, else by displayName.
	 * 
	 * @param propertyDescriptors2
	 * @return
	 */
	public static java.beans.PropertyDescriptor[] sortBeansPropertyDescriptors(java.beans.PropertyDescriptor[] descs)
	{
		if (descs != null && descs.length > 1)
		{
			Arrays.sort(descs, BEANS_PROPERTY_COMPARATOR);
		}
		return descs;
	}
}
