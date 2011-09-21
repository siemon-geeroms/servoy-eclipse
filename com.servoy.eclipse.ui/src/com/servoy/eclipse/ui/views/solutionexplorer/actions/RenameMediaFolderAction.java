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

package com.servoy.eclipse.ui.views.solutionexplorer.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;

import com.servoy.eclipse.core.ServoyModelManager;
import com.servoy.eclipse.model.nature.ServoyProject;
import com.servoy.eclipse.model.repository.EclipseRepository;
import com.servoy.eclipse.model.util.ServoyLog;
import com.servoy.eclipse.ui.node.SimpleUserNode;
import com.servoy.eclipse.ui.node.UserNodeType;
import com.servoy.eclipse.ui.util.EditorUtil;
import com.servoy.eclipse.ui.util.MediaNode;
import com.servoy.eclipse.ui.views.solutionexplorer.SolutionExplorerView;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.Media;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.Solution;

/**
 * Action to rename media folder.
 * 
 * @author gboros
 */
public class RenameMediaFolderAction extends Action implements ISelectionChangedListener
{
	private final SolutionExplorerView viewer;
	private Solution solution;
	private SimpleUserNode selection;

	public RenameMediaFolderAction(SolutionExplorerView viewer)
	{
		this.viewer = viewer;
		setText("Rename");
		setToolTipText(getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event)
	{
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();
		solution = null;
		selection = null;
		if (sel.size() == 1 && (((SimpleUserNode)sel.getFirstElement()).getType() == UserNodeType.MEDIA_FOLDER))
		{
			SimpleUserNode node = ((SimpleUserNode)sel.getFirstElement());
			SimpleUserNode solutionNode = node.getAncestorOfType(Solution.class);
			if (solutionNode != null)
			{
				// make sure you have the in-memory version of the solution
				solution = ServoyModelManager.getServoyModelManager().getServoyModel().getServoyProject(((Solution)solutionNode.getRealObject()).getName()).getEditingSolution();
				selection = node;
			}
		}
		setEnabled(solution != null);
	}

	@Override
	public void run()
	{
		if (solution == null || selection == null) return;


		InputDialog renameFolderNameDlg = new InputDialog(viewer.getSite().getShell(), "Rename media folder", "Specify a new folder name", selection.getName(),
			new IInputValidator()
			{
				public String isValid(String newText)
				{
					return newText.length() < 1 ? "Name cannot be empty" : newText.equalsIgnoreCase(selection.getName()) ? "Please enter a different name"
						: null;
				}
			});

		renameFolderNameDlg.setBlockOnOpen(true);
		renameFolderNameDlg.open();
		if (renameFolderNameDlg.getReturnCode() == Window.OK)
		{
			ServoyProject servoyProject = ServoyModelManager.getServoyModelManager().getServoyModel().getServoyProject(solution.getName());
			EclipseRepository repository = (EclipseRepository)solution.getRepository();
			String replaceName = ((MediaNode)selection.getRealObject()).getPath();
			int replaceStartIdx = replaceName.substring(0, replaceName.length() - 1).lastIndexOf('/');

			String newName = (replaceStartIdx == -1) ? renameFolderNameDlg.getValue() + "/" : replaceName.substring(0, replaceStartIdx) + "/" +
				renameFolderNameDlg.getValue() + "/";

			ArrayList<IPersist> newMedias = new ArrayList<IPersist>();
			ArrayList<IPersist> removedMedias = new ArrayList<IPersist>();
			Iterator<Media> mediaIte = solution.getMedias(false);
			Media media, movedMedia;

			try
			{
				while (mediaIte.hasNext())
				{
					media = mediaIte.next();
					if (media.getName().startsWith(replaceName))
					{
						movedMedia = solution.createNewMedia(ServoyModelManager.getServoyModelManager().getServoyModel().getNameValidator(),
							media.getName().replaceFirst(replaceName, newName));
						movedMedia.setMimeType(media.getMimeType());
						movedMedia.setPermMediaData(media.getMediaData());
						movedMedia.flagChanged();
						newMedias.add(movedMedia);
						removedMedias.add(servoyProject.getEditingPersist(media.getUUID()));
					}
				}

				for (IPersist m : newMedias)
				{
					repository.copyPersistIntoSolution(m, solution, true);
				}

				for (IPersist m : removedMedias)
				{
					repository.deleteObject(m);
					EditorUtil.closeEditor(m);
				}

				ArrayList<IPersist> changedMedias = new ArrayList<IPersist>(newMedias);
				changedMedias.addAll(removedMedias);
				ServoyModelManager.getServoyModelManager().getServoyModel().getServoyProject(solution.getName()).saveEditingSolutionNodes(
					changedMedias.toArray(new Media[0]), false);
			}
			catch (RepositoryException ex)
			{
				ServoyLog.logError(ex);
			}
		}
	}
}