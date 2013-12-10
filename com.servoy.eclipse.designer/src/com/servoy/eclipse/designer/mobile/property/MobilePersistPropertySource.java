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

package com.servoy.eclipse.designer.mobile.property;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.base.persistence.PersistUtils;
import com.servoy.base.persistence.constants.IColumnTypeConstants;
import com.servoy.base.persistence.constants.IFieldConstants;
import com.servoy.base.persistence.constants.IPartConstants;
import com.servoy.eclipse.core.ServoyModelManager;
import com.servoy.eclipse.core.util.DatabaseUtils;
import com.servoy.eclipse.ui.Messages;
import com.servoy.eclipse.ui.property.CheckboxPropertyDescriptor;
import com.servoy.eclipse.ui.property.ComboboxPropertyController;
import com.servoy.eclipse.ui.property.ComboboxPropertyModel;
import com.servoy.eclipse.ui.property.DelegatePropertySetterController;
import com.servoy.eclipse.ui.property.PersistContext;
import com.servoy.eclipse.ui.property.PersistPropertySource;
import com.servoy.eclipse.ui.property.PropertyCategory;
import com.servoy.eclipse.ui.property.PropertyController;
import com.servoy.j2db.FlattenedSolution;
import com.servoy.j2db.component.ComponentFactory;
import com.servoy.j2db.documentation.ClientSupport;
import com.servoy.j2db.persistence.AbstractBase;
import com.servoy.j2db.persistence.Bean;
import com.servoy.j2db.persistence.Field;
import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.GraphicalComponent;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.IRepository;
import com.servoy.j2db.persistence.Part;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.RepositoryHelper;
import com.servoy.j2db.persistence.StaticContentSpecLoader;
import com.servoy.j2db.scripting.annotations.AnnotationManagerReflection;
import com.servoy.j2db.util.Utils;

/**
 * PersistPropertySource to filter out properties not used in mobile solutions.
 * 
 * @author rgansevles
 *
 */
public class MobilePersistPropertySource extends PersistPropertySource
{
	public static final PropertyController<Integer, Integer> MOBILE_LABEL_HEADERSIZE_CONTROLLER = new DelegatePropertySetterController<Integer, MobilePersistPropertySource>(
		new ComboboxPropertyController<Integer>(IMobileProperties.HEADER_SIZE.propertyName, IMobileProperties.HEADER_SIZE.propertyName,
			new ComboboxPropertyModel<Integer>(
				new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6) },
				new String[] { "h1", "h2", "h3", "h4", "h5", "h6" }), Messages.LabelDefault), IMobileProperties.HEADER_SIZE.propertyName)
	{
		public void setProperty(MobilePersistPropertySource propertySource, Integer value)
		{
			((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.HEADER_SIZE.propertyName, value);
			ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, propertySource.getPersist(), false);
		}

		public Integer getProperty(MobilePersistPropertySource propertySource)
		{
			return (Integer)((AbstractBase)propertySource.getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_SIZE.propertyName);
		}
	};

	public static final String ALIGN_RIGHT_NAME = "alignRight"; //$NON-NLS-1$
	public static final PropertyController<Boolean, Boolean> ALIGN_RIGHT_CONTROLLER = new DelegatePropertySetterController<Boolean, MobilePersistPropertySource>(
		new CheckboxPropertyDescriptor(ALIGN_RIGHT_NAME, ALIGN_RIGHT_NAME), ALIGN_RIGHT_NAME)
	{
		public void setProperty(MobilePersistPropertySource propertySource, Boolean value)
		{
			if (Boolean.TRUE.equals(value))
			{
				((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.HEADER_RIGHT_BUTTON.propertyName, Boolean.TRUE);
				((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.HEADER_LEFT_BUTTON.propertyName, null);
			}
			else
			{
				((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.HEADER_RIGHT_BUTTON.propertyName, null);
				((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.HEADER_LEFT_BUTTON.propertyName, Boolean.TRUE);
			}
			ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, propertySource.getPersist(), false);
		}

		public Boolean getProperty(MobilePersistPropertySource propertySource)
		{
			return Boolean.valueOf(((AbstractBase)propertySource.getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_RIGHT_BUTTON.propertyName) != null);
		}
	};

	public static final String RADIO_STYLE_NAME = "horizontal"; //$NON-NLS-1$
	public static final PropertyController<Boolean, Boolean> MOBILE_RADIO_STYLE_CONTROLLER = new DelegatePropertySetterController<Boolean, MobilePersistPropertySource>(
		new CheckboxPropertyDescriptor(RADIO_STYLE_NAME, RADIO_STYLE_NAME), RADIO_STYLE_NAME)
	{
		// 0: vertical (default)
		// 1: horizontal
		// ... future
		public void setProperty(MobilePersistPropertySource propertySource, Boolean value)
		{
			((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.RADIO_STYLE.propertyName, Boolean.TRUE.equals(value)
				? IMobileProperties.RADIO_STYLE_HORIZONTAL : null);
			ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, propertySource.getPersist(), false);
		}

		public Boolean getProperty(MobilePersistPropertySource propertySource)
		{
			return Boolean.valueOf(IMobileProperties.RADIO_STYLE_HORIZONTAL.equals(((AbstractBase)propertySource.getPersist()).getCustomMobileProperty(IMobileProperties.RADIO_STYLE.propertyName)));
		}
	};

	public static String[] DATA_ICONS = new String[] { //
	"alert", //$NON-NLS-1$
	"arrow-d", //$NON-NLS-1$
	"arrow-l", //$NON-NLS-1$
	"arrow-r", //$NON-NLS-1$
	"arrow-u", //$NON-NLS-1$
	"back", //$NON-NLS-1$
	"bars", //$NON-NLS-1$
	"check", //$NON-NLS-1$
//	"custom", //$NON-NLS-1$
	"delete", //$NON-NLS-1$
	"edit", //$NON-NLS-1$
	"forward", //$NON-NLS-1$
	"gear", //$NON-NLS-1$
	"grid", //$NON-NLS-1$
	"home", //$NON-NLS-1$
	"info", //$NON-NLS-1$
	"minus", //$NON-NLS-1$
	"plus", //$NON-NLS-1$
	"refresh", //$NON-NLS-1$
	"search", //$NON-NLS-1$
	"star" //$NON-NLS-1$
	};

	public static final PropertyController<String, String> MOBILE_ICONS_CONTROLLER = new DelegatePropertySetterController<String, MobilePersistPropertySource>(
		new ComboboxPropertyController<String>(IMobileProperties.DATA_ICON.propertyName, IMobileProperties.DATA_ICON.propertyName,
			new ComboboxPropertyModel<String>(DATA_ICONS).addDefaultValue(), Messages.LabelUnresolved), IMobileProperties.DATA_ICON.propertyName)
	{
		public void setProperty(MobilePersistPropertySource propertySource, String value)
		{
			((AbstractBase)propertySource.getPersist()).putCustomMobileProperty(IMobileProperties.DATA_ICON.propertyName, value);
			ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, propertySource.getPersist(), false);
		}

		public String getProperty(MobilePersistPropertySource propertySource)
		{
			return (String)((AbstractBase)propertySource.getPersist()).getCustomMobileProperty(IMobileProperties.DATA_ICON.propertyName);
		}
	};

	public static final ComboboxPropertyController<Integer> MOBILE_DISPLAY_TYPE_CONTROLLER = new ComboboxPropertyController<Integer>(
		"displayType",
		RepositoryHelper.getDisplayName("displayType", Field.class),
		new ComboboxPropertyModel<Integer>(
			new Integer[] { Integer.valueOf(Field.TEXT_FIELD), Integer.valueOf(Field.TEXT_AREA), Integer.valueOf(Field.CALENDAR), Integer.valueOf(Field.COMBOBOX), Integer.valueOf(Field.RADIOS), Integer.valueOf(Field.CHECKS), Integer.valueOf(Field.PASSWORD) },
			new String[] { "TEXT_FIELD", "TEXT_AREA", "CALENDAR", "COMBOBOX", "RADIOS", "CHECKS", "PASSWORD" }), Messages.LabelUnresolved);

	public static final String STICKY_PART_NAME = "sticky"; //$NON-NLS-1$

	public static final PropertyController<Boolean, Boolean> MOBILE_STICKY_PART_CONTROLLER = new DelegatePropertySetterController<Boolean, MobilePersistPropertySource>(
		new CheckboxPropertyDescriptor(STICKY_PART_NAME, STICKY_PART_NAME), STICKY_PART_NAME)
	{
		private Part getPart(MobilePersistPropertySource propertySource)
		{
			if (propertySource.getPersist() instanceof Part)
			{
				return (Part)propertySource.getPersist();
			}
			if (propertySource.getPersist() instanceof GraphicalComponent &&
				((GraphicalComponent)propertySource.getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName) != null)
			{
				// header text, find header part
				Form form = (Form)propertySource.getContext().getAncestor(IRepository.FORMS);
				// TODO: check flattened form?
				for (Part part : Utils.iterate(form.getParts()))
				{
					if (PersistUtils.isHeaderPart(part.getPartType()))
					{
						return part;
					}
				}
			}
			return null;
		}

		public void setProperty(MobilePersistPropertySource propertySource, Boolean value)
		{
			Part part = getPart(propertySource);
			if (part != null)
			{
				int partType = part.getPartType();
				if (value.booleanValue())
				{
					if (partType == IPartConstants.HEADER) part.setPartType(IPartConstants.TITLE_HEADER);
					else if (partType == IPartConstants.FOOTER) part.setPartType(IPartConstants.TITLE_FOOTER);
				}
				else
				{
					if (partType == IPartConstants.TITLE_HEADER) part.setPartType(IPartConstants.HEADER);
					else if (partType == IPartConstants.TITLE_FOOTER) part.setPartType(IPartConstants.FOOTER);
				}

				ServoyModelManager.getServoyModelManager().getServoyModel().firePersistChanged(false, part, false);
			}
		}

		public Boolean getProperty(MobilePersistPropertySource propertySource)
		{
			Part part = getPart(propertySource);
			if (part == null)
			{
				return Boolean.FALSE;
			}
			int partType = part.getPartType();
			return Boolean.valueOf(partType == IPartConstants.TITLE_HEADER || partType == IPartConstants.TITLE_FOOTER);
		}
	};

	/**
	 * @param persistContext
	 * @param readonly
	 */
	public MobilePersistPropertySource(PersistContext persistContext, boolean readonly)
	{
		super(persistContext, readonly);
	}

	@Override
	protected boolean shouldShow(PropertyDescriptorWrapper propertyDescriptor) throws RepositoryException
	{
		if (isInternalProperty(propertyDescriptor.propertyDescriptor.getName()))
		{
			// Special case: add as hidden property, needed for setting value via palette
			return true;
		}
		if (!AnnotationManagerReflection.getInstance().hasSupportForClientType(propertyDescriptor.propertyDescriptor.getReadMethod(), getPersist().getClass(),
			ClientSupport.mc, ClientSupport.Default))
		{
			// do not show the property if the read-method is not flagged for mobile client
			return false;
		}
		if (propertyDescriptor.propertyDescriptor.getName().equals(StaticContentSpecLoader.PROPERTY_FORMAT.getPropertyName()))
		{
			int dataproviderType = DatabaseUtils.getDataproviderType(getPersist());
			if (dataproviderType == IColumnTypeConstants.INTEGER || dataproviderType == IColumnTypeConstants.NUMBER ||
				dataproviderType == IColumnTypeConstants.DATETIME)
			{
				return getPersist() instanceof GraphicalComponent ||
					(getPersist() instanceof Field && ((((Field)getPersist()).getDisplayType() == IFieldConstants.TEXT_FIELD) || ((Field)getPersist()).getDisplayType() == IFieldConstants.CALENDAR));
			}
			return false;
		}
		if (propertyDescriptor.propertyDescriptor.getName().equals(StaticContentSpecLoader.PROPERTY_VALUELISTID.getPropertyName()))
		{
			if ((getPersist() instanceof Field && ((Field)getPersist()).getDisplayType() == IFieldConstants.CALENDAR))
			{
				return false;
			}
		}
		if (propertyDescriptor.propertyDescriptor.getName().equals("loginFormID"))
		{
			return true;
		}
		if (propertyDescriptor.propertyDescriptor.getName().equals("innerHTML") && (persistContext.getPersist() instanceof Bean))
		{
			return true;
		}
		return super.shouldShow(propertyDescriptor);
	}

	@Override
	protected boolean hideForProperties(PropertyDescriptorWrapper propertyDescriptor)
	{
		if (isInternalProperty(propertyDescriptor.propertyDescriptor.getName()))
		{
			// Special case: add as hidden property, needed for setting value via palette
			return true;
		}
		if (StaticContentSpecLoader.PROPERTY_STYLECLASS.getPropertyName().equals(propertyDescriptor.propertyDescriptor.getName()) &&
			getPersist() instanceof GraphicalComponent &&
			Boolean.TRUE.equals(((GraphicalComponent)getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName)) &&
			getContext() instanceof Form)
		{
			// Special case: allow setting of header style class via headertext element
			return false;
		}

		if ((StaticContentSpecLoader.PROPERTY_ENABLED.getPropertyName().equals(propertyDescriptor.propertyDescriptor.getName()) ||
			StaticContentSpecLoader.PROPERTY_LOCATION.getPropertyName().equals(propertyDescriptor.propertyDescriptor.getName()) || StaticContentSpecLoader.PROPERTY_NAME.getPropertyName().equals(
			propertyDescriptor.propertyDescriptor.getName())) &&
			getPersist() instanceof GraphicalComponent &&
			Boolean.TRUE.equals(((GraphicalComponent)getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName)) &&
			getContext() instanceof Form)
		{
			return true;
		}

		return RepositoryHelper.hideForMobileProperties(propertyDescriptor.propertyDescriptor.getName(), getPersist().getClass(),
			(getPersist() instanceof Field) ? ((Field)getPersist()).getDisplayType() : 0, isButton(getPersist())) ||

		super.hideForProperties(propertyDescriptor);
	}

	private boolean isInternalProperty(String propertyDescriptor)
	{
		if (StaticContentSpecLoader.PROPERTY_EDITABLE.getPropertyName().equals(propertyDescriptor))
		{
			return true;
		}
		if (StaticContentSpecLoader.PROPERTY_SIZE.getPropertyName().equals(propertyDescriptor))
		{
			return true;
		}
		if (StaticContentSpecLoader.PROPERTY_VIEW.getPropertyName().equals(propertyDescriptor))
		{
			return true;
		}
		return false;
	}

	@Override
	protected String[] getPseudoPropertyNames(Class< ? > clazz)
	{
		if (GraphicalComponent.class == clazz && isButton(getPersist()))
		{
			// button
			GraphicalComponent gc = (GraphicalComponent)getPersist();
			if (gc.getCustomMobileProperty(IMobileProperties.HEADER_ITEM.propertyName) != null)
			{
				return new String[] { ALIGN_RIGHT_NAME, IMobileProperties.DATA_ICON.propertyName };
			}
			return new String[] { IMobileProperties.DATA_ICON.propertyName };
		}

		if (GraphicalComponent.class == clazz && !isButton(getPersist()))
		{
			if (((AbstractBase)getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName) == null)
			{
				// script label
				return new String[] { IMobileProperties.HEADER_SIZE.propertyName };
			}
			else
			{
				// header text, add header properties
				return new String[] { STICKY_PART_NAME };
			}
		}

		if (Field.class == clazz)
		{
			Field field = (Field)getPersist();
			if (field.getDisplayType() == Field.RADIOS)
			{
				// radios check
				return new String[] { RADIO_STYLE_NAME };
			}
		}

		if (Part.class == clazz)
		{
			return new String[] { STICKY_PART_NAME };
		}

		return null;
	}

	/**
	 * @param persist
	 * @return
	 */
	private static boolean isButton(IPersist persist)
	{
		return persist instanceof GraphicalComponent && ComponentFactory.isButton((GraphicalComponent)persist);
	}

	@Override
	protected IPropertyDescriptor getPropertiesPropertyDescriptor(IPropertySource propertySource, String id, String displayName, String name,
		FlattenedSolution flattenedEditingSolution, Form form) throws RepositoryException
	{
		if (name.equals(IMobileProperties.HEADER_SIZE.propertyName))
		{
			return MOBILE_LABEL_HEADERSIZE_CONTROLLER;
		}

		if (name.equals(RADIO_STYLE_NAME))
		{
			return MOBILE_RADIO_STYLE_CONTROLLER;
		}

		if (name.equals(IMobileProperties.DATA_ICON.propertyName))
		{
			return MOBILE_ICONS_CONTROLLER;
		}

		if (name.equals(STICKY_PART_NAME))
		{
			return MOBILE_STICKY_PART_CONTROLLER;
		}

		if (name.equals(ALIGN_RIGHT_NAME))
		{
			return ALIGN_RIGHT_CONTROLLER;
		}

		return super.getPropertiesPropertyDescriptor(propertySource, id, displayName, name, flattenedEditingSolution, form);
	}

	@Override
	protected IPropertyDescriptor createPropertyDescriptor(PropertyDescriptorWrapper propertyDescriptor, FlattenedSolution flattenedEditingSolution, Form form,
		PropertyCategory category, String id) throws RepositoryException
	{

		if (id.equals(StaticContentSpecLoader.PROPERTY_DISPLAYTYPE.getPropertyName()))
		{
			return MOBILE_DISPLAY_TYPE_CONTROLLER;
		}

		return super.createPropertyDescriptor(propertyDescriptor, flattenedEditingSolution, form, category, id);
	}

	@Override
	public void setPersistPropertyValue(Object id, Object value)
	{
		super.setPersistPropertyValue(id, value);

		// set style on header text, copy to header part for FormList (not for InsetList)
		if (StaticContentSpecLoader.PROPERTY_STYLECLASS.getPropertyName().equals(id) && getPersist() instanceof GraphicalComponent &&
			Boolean.TRUE.equals(((GraphicalComponent)getPersist()).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName)) &&
			getContext() instanceof Form)
		{
			Form form = (Form)getContext();
			for (Part part : Utils.iterate(form.getParts()))
			{
				if (PersistUtils.isHeaderPart(part.getPartType()))
				{
					new PersistPropertySource(PersistContext.create(part, form), false).setPersistPropertyValue(id, value);
				}
			}
		}
	}
}