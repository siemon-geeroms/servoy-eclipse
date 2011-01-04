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
package com.servoy.eclipse.designer.editor.commands;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

import com.servoy.eclipse.designer.Activator;


/**
 * Factory for actions in form designer.
 * 
 * @author rgansevles
 */

@SuppressWarnings("nls")
public abstract class DesignerActionFactory extends ActionFactory
{
	public static final String GROUP_ANCHORING = "group.anchoring";
	public static final String GROUP_Z_ORDER = "group.z.order";
	public static final String GROUP_GROUPING = "group.grouping";
	public static final String GROUP_SAMESIZE = "group.samesize";

	protected DesignerActionFactory(String actionId)
	{
		super(actionId);
	}

	public static final String BRING_TO_FRONT_TEXT = "Bring to front";
	public static final String BRING_TO_FRONT_TOOLTIP = "Bring selected objects to front";
	public static final ImageDescriptor BRING_TO_FRONT_IMAGE = Activator.loadImageDescriptorFromBundle("bringtofront.gif");//$NON-NLS-1$
	public static final ActionFactory BRING_TO_FRONT = new ActionFactory("bring-to-front") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), BRING_TO_FRONT_TEXT);
			action.setToolTipText(BRING_TO_FRONT_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(BRING_TO_FRONT_IMAGE);
			return action;
		}
	};

	public static final String SEND_TO_BACK_TEXT = "Send to back";
	public static final String SEND_TO_BACK_TOOLTIP = "Send selected objects to back";
	public static final ImageDescriptor SEND_TO_BACK_IMAGE = Activator.loadImageDescriptorFromBundle("sendtoback.gif");//$NON-NLS-1$
	public static final ActionFactory SEND_TO_BACK = new ActionFactory("send-to-back") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SEND_TO_BACK_TEXT);
			action.setToolTipText(SEND_TO_BACK_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(SEND_TO_BACK_IMAGE);
			return action;
		}
	};

	public static final String SELECT_FEEDBACK_TEXT = "Select feedback mode";
	public static final String SELECT_FEEDBACK_TOOLTIP = SELECT_FEEDBACK_TEXT;
	public static final ImageDescriptor SELECT_FEEDBACK_IMAGE = Activator.loadImageDescriptorFromBundle("grid.gif");//$NON-NLS-1$
	public static final ActionFactory SELECT_FEEDBACK = new ActionFactory("select-feedback-mode") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SELECT_FEEDBACK_TEXT, IAction.AS_DROP_DOWN_MENU);
			action.setToolTipText(SELECT_FEEDBACK_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(SELECT_FEEDBACK_IMAGE);
			return action;
		}
	};

	public static final String TOGGLE_SHOW_ANCHOR_FEEDBACK_TEXT = "Show Anchor feedback";
	public static final String TOGGLE_SHOW_ANCHOR_FEEDBACK_TOOLTIP = "Show anchor feedback for selected elements";
	public static final ImageDescriptor TOGGLE_SHOW_ANCHOR_FEEDBACK_IMAGE = Activator.loadImageDescriptorFromBundle("anchor.gif");//$NON-NLS-1$ 
	public static final ActionFactory TOGGLE_SHOW_ANCHOR_FEEDBACK = new ActionFactory("toggle-show-anchor-feedback") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), TOGGLE_SHOW_ANCHOR_FEEDBACK_TEXT, IAction.AS_CHECK_BOX);
			action.setToolTipText(TOGGLE_SHOW_ANCHOR_FEEDBACK_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(TOGGLE_SHOW_ANCHOR_FEEDBACK_IMAGE);
			return action;
		}
	};

	public static final String TOGGLE_SHOW_SAME_SIZE_FEEDBACK_TEXT = "Show same-size feedback";
	public static final String TOGGLE_SHOW_SAME_SIZE_FEEDBACK_TOOLTIP = "Show same-size feedback for selected elements";
	public static final ImageDescriptor TOGGLE_SHOW_SAME_SIZE_FEEDBACK_IMAGE = Activator.loadImageDescriptorFromBundle("samesize.gif");//$NON-NLS-1$ 
	public static final ActionFactory TOGGLE_SHOW_SAME_SIZE_FEEDBACK = new ActionFactory("toggle-show-samesize-feedback") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), TOGGLE_SHOW_SAME_SIZE_FEEDBACK_TEXT, IAction.AS_CHECK_BOX);
			action.setToolTipText(TOGGLE_SHOW_SAME_SIZE_FEEDBACK_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(TOGGLE_SHOW_SAME_SIZE_FEEDBACK_IMAGE);
			return action;
		}
	};

	public static final String SELECT_SNAPMODE_TEXT = "Select snap mode";
	public static final String SELECT_SNAPMODE_TOOLTIP = SELECT_SNAPMODE_TEXT;
	public static final ImageDescriptor SELECT_SNAPTMODE_IMAGE = Activator.loadImageDescriptorFromBundle("snaptogrid.gif");//$NON-NLS-1$
	public static final ActionFactory SELECT_SNAPMODE = new ActionFactory("select-snapmode") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SELECT_SNAPMODE_TEXT, IAction.AS_DROP_DOWN_MENU);
			action.setToolTipText(SELECT_SNAPMODE_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(SELECT_SNAPTMODE_IMAGE);
			return action;
		}
	};

	public static final String DISTRIBUTE_HORIZONTAL_SPACING_TEXT = "Horizontal Spacing";
	public static final String DISTRIBUTE_HORIZONTAL_SPACING_TOOLTIP = DISTRIBUTE_HORIZONTAL_SPACING_TEXT;
	public static final ImageDescriptor DISTRIBUTE_HORIZONTAL_SPACING_IMAGE = Activator.loadImageDescriptorFromBundle("distribute_hspace.gif");//$NON-NLS-1$
	public static final ActionFactory DISTRIBUTE_HORIZONTAL_SPACING = new ActionFactory("distribute-horizontal-spacing") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), DISTRIBUTE_HORIZONTAL_SPACING_TEXT);
			action.setToolTipText(DISTRIBUTE_HORIZONTAL_SPACING_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(DISTRIBUTE_HORIZONTAL_SPACING_IMAGE);
			return action;
		}
	};

	public static final String DISTRIBUTE_HORIZONTAL_CENTER_TEXT = "Horizontal Centers";
	public static final String DISTRIBUTE_HORIZONTAL_CENTER_TOOLTIP = DISTRIBUTE_HORIZONTAL_CENTER_TEXT;
	public static final ImageDescriptor DISTRIBUTE_HORIZONTAL_CENTER_IMAGE = Activator.loadImageDescriptorFromBundle("distribute_hcenters.gif");//$NON-NLS-1$
	public static final ActionFactory DISTRIBUTE_HORIZONTAL_CENTER = new ActionFactory("distribute-horizontal-center") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), DISTRIBUTE_HORIZONTAL_CENTER_TEXT);
			action.setToolTipText(DISTRIBUTE_HORIZONTAL_CENTER_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(DISTRIBUTE_HORIZONTAL_CENTER_IMAGE);
			return action;
		}
	};

	public static final String DISTRIBUTE_HORIZONTAL_PACK_TEXT = "Leftward";
	public static final String DISTRIBUTE_HORIZONTAL_PACK_TOOLTIP = DISTRIBUTE_HORIZONTAL_PACK_TEXT;
	public static final ImageDescriptor DISTRIBUTE_HORIZONTAL_PACK_IMAGE = Activator.loadImageDescriptorFromBundle("distribute_leftward.gif");//$NON-NLS-1$
	public static final ActionFactory DISTRIBUTE_HORIZONTAL_PACK = new ActionFactory("distribute-horizontal-pack") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), DISTRIBUTE_HORIZONTAL_PACK_TEXT);
			action.setToolTipText(DISTRIBUTE_HORIZONTAL_PACK_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(DISTRIBUTE_HORIZONTAL_PACK_IMAGE);
			return action;
		}
	};

	public static final String DISTRIBUTE_VERTICAL_SPACING_TEXT = "Vertical Spacing";
	public static final String DISTRIBUTE_VERTICAL_SPACING_TOOLTIP = DISTRIBUTE_VERTICAL_SPACING_TEXT;
	public static final ImageDescriptor DISTRIBUTE_VERTICAL_SPACING_IMAGE = Activator.loadImageDescriptorFromBundle("distribute_vspace.gif");//$NON-NLS-1$
	public static final ActionFactory DISTRIBUTE_VERTICAL_SPACING = new ActionFactory("distribute-vertical-spacing") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), DISTRIBUTE_VERTICAL_SPACING_TEXT);
			action.setToolTipText(DISTRIBUTE_VERTICAL_SPACING_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(DISTRIBUTE_VERTICAL_SPACING_IMAGE);
			return action;
		}
	};

	public static final String DISTRIBUTE_VERTICAL_CENTER_TEXT = "Vertical Centers";
	public static final String DISTRIBUTE_VERTICAL_CENTER_TOOLTIP = DISTRIBUTE_VERTICAL_CENTER_TEXT;
	public static final ImageDescriptor DISTRIBUTE_VERTICAL_CENTER_IMAGE = Activator.loadImageDescriptorFromBundle("distribute_vcenters.gif");//$NON-NLS-1$
	public static final ActionFactory DISTRIBUTE_VERTICAL_CENTER = new ActionFactory("distribute-vertical-center") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), DISTRIBUTE_VERTICAL_CENTER_TEXT);
			action.setToolTipText(DISTRIBUTE_VERTICAL_CENTER_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(DISTRIBUTE_VERTICAL_CENTER_IMAGE);
			return action;
		}
	};

	public static final String DISTRIBUTE_VERTICAL_PACK_TEXT = "Upward";
	public static final String DISTRIBUTE_VERTICAL_PACK_TOOLTIP = DISTRIBUTE_VERTICAL_PACK_TEXT;
	public static final ImageDescriptor DISTRIBUTE_VERTICAL_PACK_IMAGE = Activator.loadImageDescriptorFromBundle("distribute_upward.gif");//$NON-NLS-1$
	public static final ActionFactory DISTRIBUTE_VERTICAL_PACK = new ActionFactory("distribute-vertical-pack") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), DISTRIBUTE_VERTICAL_PACK_TEXT);
			action.setToolTipText(DISTRIBUTE_VERTICAL_PACK_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(DISTRIBUTE_VERTICAL_PACK_IMAGE);
			return action;
		}
	};

	public static final String SET_TAB_SEQUENCE_TEXT = "Set tab sequence";
	public static final String SET_TAB_SEQUENCE_TOOLTIP = SET_TAB_SEQUENCE_TEXT;
	public static final ImageDescriptor SET_TAB_SEQUENCE_IMAGE = Activator.loadImageDescriptorFromBundle("th_horizontal.gif"); // TODO create proper icon
	public static final ActionFactory SET_TAB_SEQUENCE = new ActionFactory("set-tab-sequence") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SET_TAB_SEQUENCE_TEXT);
			action.setToolTipText(SET_TAB_SEQUENCE_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(SET_TAB_SEQUENCE_IMAGE);
			return action;
		}
	};

	public static final String GROUP_TEXT = "Group";
	public static final String GROUP_TOOLTIP = GROUP_TEXT;
	public static final ImageDescriptor GROUP_IMAGE = Activator.loadImageDescriptorFromBundle("group.gif"); //$NON-NLS-1$
	public static final ActionFactory GROUP = new ActionFactory("set-group-id") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), GROUP_TEXT);
			action.setToolTipText(GROUP_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(GROUP_IMAGE);
			return action;
		}
	};

	public static final String UNGROUP_TEXT = "Ungroup";
	public static final String UNGROUP_TOOLTIP = UNGROUP_TEXT;
	public static final ImageDescriptor UNGROUP_IMAGE = Activator.loadImageDescriptorFromBundle("ungroup.gif"); //$NON-NLS-1$
	public static final ActionFactory UNGROUP = new ActionFactory("clear-group-id") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), UNGROUP_TEXT);
			action.setToolTipText(UNGROUP_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(UNGROUP_IMAGE);
			return action;
		}
	};

	public static final String SAVE_AS_TEMPLATE_TEXT = "Save as template...";
	public static final String SAVE_AS_TEMPLATE_TOOLTIP = SAVE_AS_TEMPLATE_TEXT;
	public static final ImageDescriptor SAVE_AS_TEMPLATE_IMAGE = Activator.loadImageDescriptorFromBundle("template.gif");
	public static final ActionFactory SAVE_AS_TEMPLATE = new ActionFactory("save-as-template-id") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SAVE_AS_TEMPLATE_TEXT);
			action.setToolTipText(SAVE_AS_TEMPLATE_TOOLTIP);
			window.getPartService().addPartListener(action);
//			action.setActionDefinitionId("org.eclipse.ui.edit." + getId()); //$NON-NLS-1$
			action.setImageDescriptor(SAVE_AS_TEMPLATE_IMAGE);
			return action;
		}
	};

	public static final String SAME_WIDTH_TEXT = "Set same width";
	public static final String SAME_WIDTH_TOOLTIP = SAME_WIDTH_TEXT;
	public static final ImageDescriptor SAME_WIDTH_IMAGE = Activator.loadImageDescriptorFromBundle("same_width.gif");
	public static final ActionFactory SAME_WIDTH = new ActionFactory("same-width") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SAME_WIDTH_TEXT);
			action.setToolTipText(SAME_WIDTH_TOOLTIP);
			window.getPartService().addPartListener(action);
			action.setImageDescriptor(SAME_WIDTH_IMAGE);
			return action;
		}
	};

	public static final String SAME_HEIGHT_TEXT = "Set same height";
	public static final String SAME_HEIGHT_TOOLTIP = SAME_HEIGHT_TEXT;
	public static final ImageDescriptor SAME_HEIGHT_IMAGE = Activator.loadImageDescriptorFromBundle("same_height.gif");
	public static final ActionFactory SAME_HEIGHT = new ActionFactory("same-height") {//$NON-NLS-1$

		@Override
		public IWorkbenchAction create(IWorkbenchWindow window)
		{
			if (window == null)
			{
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), SAME_HEIGHT_TEXT);
			action.setToolTipText(SAME_HEIGHT_TOOLTIP);
			window.getPartService().addPartListener(action);
			action.setImageDescriptor(SAME_HEIGHT_IMAGE);
			return action;
		}
	};
}
