/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2015 Servoy BV

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

package com.servoy.eclipse.designer.editor.rfb.actions.handlers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import com.servoy.eclipse.core.ServoyModelManager;
import com.servoy.eclipse.designer.editor.BaseVisualFormEditor;
import com.servoy.eclipse.designer.editor.BaseVisualFormEditorDesignPage;
import com.servoy.eclipse.designer.editor.rfb.RfbVisualFormEditorDesignPage;
import com.servoy.eclipse.model.util.ModelUtils;
import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.eclipse.ui.property.PersistContext;
import com.servoy.eclipse.ui.util.ElementUtil;
import com.servoy.j2db.FlattenedSolution;
import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.IChildWebObject;
import com.servoy.j2db.persistence.IFlattenedPersistWrapper;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.IRepository;
import com.servoy.j2db.persistence.ISupportBounds;
import com.servoy.j2db.persistence.ISupportChilds;
import com.servoy.j2db.persistence.ISupportExtendsID;
import com.servoy.j2db.persistence.PositionComparator;
import com.servoy.j2db.util.PersistHelper;

/**
 * @author jcompagner
 *
 */
public class ChangeParentCommand extends Command
{
	private IPersist child;
	private final IPersist targetChild;
	private ISupportChilds newParent;
	private final Form form;
	private int oldIndex;
	private final boolean insertAfterTarget;

	private final boolean hasChildPositionSupport;
	private final Class< ? > childPositionClass;
	private ISupportChilds oldParent;
	private int insertIndex;
	private IPersist oldChild;


	public ChangeParentCommand(IPersist child, ISupportChilds newParent, IPersist targetChild, Form form, boolean insertAfterTarget)
	{
		super("Change Parent");

		this.form = form;
		this.child = child instanceof IFlattenedPersistWrapper< ? > ? ((IFlattenedPersistWrapper< ? >)child).getWrappedPersist() : child;
		this.targetChild = targetChild instanceof IFlattenedPersistWrapper< ? > ? ((IFlattenedPersistWrapper< ? >)targetChild).getWrappedPersist()
			: targetChild;

		this.newParent = newParent;
		this.insertAfterTarget = insertAfterTarget;

		this.hasChildPositionSupport = child instanceof ISupportBounds || child instanceof IChildWebObject;
		this.childPositionClass = child instanceof ISupportBounds ? ISupportBounds.class : IChildWebObject.class;
	}

	@Override
	public void execute()
	{
		ISupportChilds initialParent = ((ISupportExtendsID)child).getExtendsID() > 0 && child.getParent() instanceof Form
			? PersistHelper.getSuperPersist((ISupportExtendsID)child).getParent() : child.getParent();

		FlattenedSolution flattenedSolution = ModelUtils.getEditingFlattenedSolution(child.getParent());
		oldParent = PersistHelper.getFlattenedPersist(flattenedSolution, (Form)child.getAncestor(IRepository.FORMS), initialParent);
		this.newParent = PersistHelper.getFlattenedPersist(flattenedSolution, form,
			(ISupportChilds)getOverridePersist(newParent == null ? initialParent : newParent));
		IPersist superPersist = ((ISupportExtendsID)newParent).getExtendsID() > 0 ? PersistHelper.getSuperPersist((ISupportExtendsID)newParent) : null;

		if (hasChildPositionSupport)
		{
			ArrayList<IPersist> children = getChildrenSortedOnType(oldParent);
			oldIndex = children.indexOf(child);
		}

		if (!initialParent.equals(superPersist))//if it's an override we don't want to remove the child from its original parent
		{
			oldParent.removeChild(child);
		}

		oldChild = child;
		child = getOverridePersist(child);
		if (child.getParent() instanceof Form)
		{
			child.getParent().removeChild(child);
		}
		ArrayList<IPersist> children = null;
		if (hasChildPositionSupport)
		{
			children = getChildrenSortedOnType(newParent);
			newParent.removeChild(child);
			children.remove(oldChild);
			children.remove(child);
			insertIndex = childPositionClass.isInstance(targetChild) ? children.indexOf(targetChild) : -1;
			if (insertIndex == -1) children.add(child);
			else
			{
				if (insertAfterTarget) insertIndex++;
				if (insertIndex < children.size()) children.add(insertIndex, child);
				else children.add(child);
			}
		}

		newParent.addChild(child);
		if (hasChildPositionSupport) updateWithOrderedPosition(children);

		List<IPersist> changes = new ArrayList<>();
		if (initialParent.equals(superPersist))
		{
			IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
			for (IEditorReference editorRef : editorRefs)
			{
				IEditorPart editor = editorRef.getEditor(false);
				if (editor instanceof BaseVisualFormEditor)
				{
					BaseVisualFormEditorDesignPage activePage = ((BaseVisualFormEditor)editor).getGraphicaleditor();
					if (activePage instanceof RfbVisualFormEditorDesignPage) ((RfbVisualFormEditorDesignPage)activePage).refreshContent();
					break;
				}
			}
			oldParent = newParent;//if it's an override, then on undo we just want to change the position
			changes.add((((IFlattenedPersistWrapper< ? >)newParent).getWrappedPersist()));
			changes.addAll(getChildrenSortedOnType(newParent));
		}
		else
		{
			changes.add(child);
		}
		ServoyModelManager.getServoyModelManager().getServoyModel().firePersistsChanged(false, changes);
	}

	private IPersist getOverridePersist(IPersist persist)
	{
		if (form != null)
		{
			try
			{
				return ElementUtil.getOverridePersist(PersistContext.create(persist, form));
			}
			catch (Exception ex)
			{
				ServoyLog.logError(ex);
			}
		}
		return persist;
	}

	private void updateWithOrderedPosition(ArrayList<IPersist> children)
	{
		int counter = child instanceof ISupportBounds ? 1 : 0;
		for (IPersist p : children)
		{
			IPersist override = getOverridePersist(p);
			if (!children.contains(override))
			{
				if (override.getParent() instanceof Form)
				{
					override.getParent().removeChild(override);
				}
				newParent.removeChild(p);
				newParent.addChild(override);
				p = override;
			}
			if (child instanceof ISupportBounds)
			{
				((ISupportBounds)p).setLocation(new Point(counter, counter));
			}
			else if (child instanceof IChildWebObject)
			{
				((IChildWebObject)p).setIndex(counter);
			}
			counter++;
		}
	}

	private ArrayList<IPersist> getChildrenSortedOnType(ISupportChilds parent)
	{
		ArrayList<IPersist> children = new ArrayList<IPersist>();
		Iterator<IPersist> it = parent.getAllObjects();
		while (it.hasNext())
		{
			IPersist persist = it.next();

			if (childPositionClass.isInstance(persist))
			{
				children.add(persist instanceof IFlattenedPersistWrapper ? ((IFlattenedPersistWrapper< ? >)persist).getWrappedPersist() : persist);
			}
		}
		IPersist[] sortedChildArray = children.toArray(new IPersist[0]);
		if (childPositionClass == ISupportBounds.class)
		{
			Arrays.sort(sortedChildArray, PositionComparator.XY_PERSIST_COMPARATOR);
		}
		else if (childPositionClass == IChildWebObject.class)
		{
			Arrays.sort(sortedChildArray, new Comparator<IPersist>()
			{
				@Override
				public int compare(IPersist o1, IPersist o2)
				{
					return ((IChildWebObject)o1).getIndex() - ((IChildWebObject)o2).getIndex();
				}
			});
		}
		return new ArrayList<IPersist>(Arrays.asList(sortedChildArray));
	}

	@Override
	public void undo()
	{
		ArrayList<IPersist> changes = new ArrayList<IPersist>();
		changes.add(child.getParent());
		changes.add(oldParent);
		IPersist changedChild = child instanceof IFlattenedPersistWrapper< ? > ? ((IFlattenedPersistWrapper< ? >)child).getWrappedPersist() : child;
		changes.add(changedChild);
		child.getParent().removeChild(child);

		if (hasChildPositionSupport)
		{
			ArrayList<IPersist> children = getChildrenSortedOnType(oldParent);
			children.remove(insertIndex);
			if (oldIndex < children.size())
			{
				children.add(oldIndex, child);
			}
			else
			{
				children.add(child);
			}
			oldParent.addChild(child);
			updateWithOrderedPosition(children);
		}
		else
		{
			oldParent.addChild(child);
		}
		ServoyModelManager.getServoyModelManager().getServoyModel().firePersistsChanged(false, changes);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo()
	{
		ArrayList<IPersist> changes = new ArrayList<IPersist>();
		changes.add(newParent);
		changes.add(oldParent);
		super.redo();
		ServoyModelManager.getServoyModelManager().getServoyModel().firePersistsChanged(false, changes);
	}
}
