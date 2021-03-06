package com.servoy.eclipse.designer.editor.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import com.servoy.eclipse.designer.editor.BaseVisualFormEditor;
import com.servoy.eclipse.designer.util.DesignerUtil;
import com.servoy.eclipse.ui.property.PersistContext;
import com.servoy.j2db.persistence.IPersist;

public class DeleteCommand extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		List<IPersist> selection = getSelection();
		if (selection.size() > 0)
		{
			BaseVisualFormEditor editorPart = getEditorPart();
			if (editorPart != null) editorPart.getCommandStack().execute(new FormElementDeleteCommand(selection.toArray(new IPersist[selection.size()])));
		}
		return null;
	}

	/**
	 * @return
	 */
	private List<IPersist> getSelection()
	{
		ArrayList<IPersist> result = new ArrayList<IPersist>();
		for (Object next : getSelectionList())
		{
			if (next instanceof PersistContext)
			{
				PersistContext persistContext = (PersistContext)next;
				if (persistContext.getPersist() != null) result.add(persistContext.getPersist());
			}
			else if (next instanceof IPersist)
			{
				result.add((IPersist)next);
			}
		}
		return result;
	}

	private List<Object> getSelectionList()
	{
		ContentOutline contentOutline = DesignerUtil.getContentOutline();
		if (contentOutline != null)
		{
			return ((IStructuredSelection)contentOutline.getSelection()).toList();
		}
		return new ArrayList<Object>();
	}

	private BaseVisualFormEditor getEditorPart()
	{
		IWorkbenchWindow active = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (active == null) return null;
		IWorkbenchPage page = active.getActivePage();
		if (page == null) return null;
		IWorkbenchPart part = page.getActiveEditor();
		if (part instanceof BaseVisualFormEditor) return (BaseVisualFormEditor)part;
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled()
	{
		return getSelection().size() > 0 && !DesignerUtil.containsInheritedElement(getSelectionList());
	}

}
