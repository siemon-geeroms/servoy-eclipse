package com.servoy.eclipse.designer.editor.rfb.menu;

import java.util.Map;

import org.eclipse.core.expressions.PropertyTester;
import org.sablo.specification.PropertyDescription;
import org.sablo.specification.WebComponentSpecProvider;
import org.sablo.specification.WebComponentSpecification;

import com.servoy.eclipse.designer.editor.BaseVisualFormEditor;
import com.servoy.eclipse.designer.editor.rfb.actions.handlers.GhostHandler;
import com.servoy.eclipse.designer.outline.FormOutlineContentProvider;
import com.servoy.eclipse.designer.util.DesignerUtil;
import com.servoy.eclipse.ui.property.PersistContext;
import com.servoy.j2db.persistence.LayoutContainer;
import com.servoy.j2db.persistence.WebComponent;

public class IsContainerPropertyTester extends PropertyTester
{

	public IsContainerPropertyTester()
	{
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		if (receiver == FormOutlineContentProvider.ELEMENTS)
		{
			BaseVisualFormEditor activeEditor = DesignerUtil.getActiveEditor();
			if (activeEditor != null) return activeEditor.getForm().isResponsiveLayout();
		}
		if (receiver instanceof PersistContext)
		{
			PersistContext persistContext = (PersistContext)receiver;
			if (persistContext.getPersist() instanceof WebComponent)
			{
				WebComponentSpecification spec = WebComponentSpecProvider.getInstance().getWebComponentSpecification(
					((WebComponent)persistContext.getPersist()).getTypeName());
				Map<String, PropertyDescription> properties = spec.getProperties();
				for (PropertyDescription propertyDescription : properties.values())
				{
					if (GhostHandler.isDroppable(propertyDescription, propertyDescription.getConfig()))
					{
						return true;
					}
				}
			}
			return persistContext.getPersist() instanceof LayoutContainer;
		}
		else return receiver instanceof LayoutContainer;
	}

}
