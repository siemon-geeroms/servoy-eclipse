angular.module('contextmenuactions',['contextmenu','editor'])
.value("SHORTCUT_IDS",
{	
	SET_TAB_SEQUENCE_ID: "com.servoy.eclipse.designer.settabseq",
	SAME_WIDTH_ID: "com.servoy.eclipse.designer.samewidth",
	SAME_HEIGHT_ID: "com.servoy.eclipse.designer.sameheight",
	TOGGLE_ANCHORING_TOP_ID: "com.servoy.eclipse.designer.anchorTop",
	TOGGLE_ANCHORING_RIGHT_ID: "com.servoy.eclipse.designer.anchorRight",
	TOGGLE_ANCHORING_BOTTOM_ID: "com.servoy.eclipse.designer.anchorBottom",
	TOGGLE_ANCHORING_LEFT_ID: "com.servoy.eclipse.designer.anchorLeft",
	BRING_TO_FRONT_ONE_STEP_ID: "com.servoy.eclipse.designer.bringtofrontonestep",
	SEND_TO_BACK_ONE_STEP_ID: "com.servoy.eclipse.designer.sendtobackonestep",
	BRING_TO_FRONT_ID: "com.servoy.eclipse.designer.bringtofront", 
	SEND_TO_BACK_ID: "com.servoy.eclipse.designer.sendtoback",
	OPEN_SCRIPT_ID: "com.servoy.eclipse.ui.OpenFormJsAction"
})
.run(function($rootScope, $pluginRegistry,$contextmenu, $editorService,EDITOR_EVENTS,SHORTCUT_IDS){
	$pluginRegistry.registerPlugin(function(editorScope) {
		var selection = null;
		var beanAnchor = 0;
		$rootScope.$on(EDITOR_EVENTS.SELECTION_CHANGED, function(event, sel) {
			selection = sel;
			if (selection && selection.length == 1)
			{
				var node = selection[0];
				var beanModel = editorScope.getBeanModel(node);
				if (beanModel)
				{
					beanAnchor = beanModel.anchors;
				}
			}
		});
		var hasSelection = function(selectionSize)
		{
			if (selection && selection.length > 0 && (selectionSize == undefined || selection.length == selectionSize))
				return true;
			return false;
		};
		
		var isAnchored = function(anchor)
		{
			if (selection && selection.length == 1)
			{
				if(beanAnchor == 0)
					 beanAnchor = 1 + 8; // top left
				if ((beanAnchor & anchor) == anchor)
				{
					return true;
				}
			}
			return false;
		}
		var setAnchoring = function(anchor){
			var selection = editorScope.getSelection();
			if (selection && selection.length == 1)
			{
				var obj = {};
				var node = selection[0];
				var beanModel = editorScope.getBeanModel(node);
				if (beanModel)
				{
					var beanAnchor = beanModel.anchors;
					if(beanAnchor == 0)
						 beanAnchor = 1 + 8; // top left
					if ((beanAnchor & anchor) == anchor)
					{
						// already exists, remove it
						beanAnchor = beanAnchor - anchor;
					}
					else
					{
						beanAnchor = beanAnchor + anchor;
					}
					beanModel.anchors = beanAnchor;
					obj[node.getAttribute("svy-id")] = {anchors:beanModel.anchors}
					$editorService.sendChanges(obj);
				}
			}
		}
		
		var promise = $editorService.getShortcuts();
		promise.then(function (shortcuts){
		
			$contextmenu.add({
				text: "Set Tab Sequence",
				getIconStyle: function(){ return {'background-image':"url(images/th_horizontal.gif)"};},
				shortcut: shortcuts[SHORTCUT_IDS.SET_TAB_SEQUENCE_ID],
				getItemClass: function() { if (!editorScope.getSelection() || editorScope.getSelection().length < 2) return "disabled";},
				execute:function()
				{
					$editorService.executeAction('setTabSequence');
				}
			});
			
			// sizing
			var sizingActions = [];
			
			sizingActions.push(
					{
						text: "Same Width",
						getIconStyle: function(){ return {'background-image':"url(images/same_width.gif)"};},
						shortcut: shortcuts[SHORTCUT_IDS.SAME_WIDTH_ID],
						getItemClass: function() { if (!editorScope.getSelection() || editorScope.getSelection().length < 2) return "disabled";},
						execute:function()
						{
							$editorService.sameSize(true);
						}
					}
				);
			
			sizingActions.push(
					{
						text: "Same Height",
						getIconStyle: function(){ return {'background-image':"url(images/same_height.gif)"};},
						shortcut: shortcuts[SHORTCUT_IDS.SAME_HEIGHT_ID],
						getItemClass: function() { if (!editorScope.getSelection() || editorScope.getSelection().length < 2) return "disabled";},
						execute:function()
						{
							$editorService.sameSize(false);
						}
					}
				);
			
			$contextmenu.add(
					{
						text: "Sizing",
						subMenu: sizingActions,
						getItemClass: function() { return "dropdown-submenu";}
					}
				);
			
			// anchoring
			var anchoringActions = [];
			
			anchoringActions.push(
					{
						text: "Top",
						getIconStyle: function(){
							if(isAnchored(1))
							{
								return {'background-image':"url(images/check.png)"};
							}
							return null;
						},
						shortcut: shortcuts[SHORTCUT_IDS.TOGGLE_ANCHORING_TOP_ID],
						getItemClass: function() { if (!hasSelection(1) || !editorScope.isAbsoluteFormLayout()) return "disabled";},
						execute:function()
						{
							setAnchoring(1);
						}
					}
				);
			
			anchoringActions.push(
					{
						text: "Right",
						getIconStyle: function(){ if(isAnchored(2)) return {'background-image':"url(images/check.png)"};},
						shortcut: shortcuts[SHORTCUT_IDS.TOGGLE_ANCHORING_RIGHT_ID],
						getItemClass: function() { if (!hasSelection(1) || !editorScope.isAbsoluteFormLayout()) return "disabled";},
						execute:function()
						{
							setAnchoring(2);
						}
					}
				);
			
			anchoringActions.push(
					{
						text: "Bottom",
						getIconStyle: function(){ if(isAnchored(4)) return {'background-image':"url(images/check.png)"};},
						shortcut: shortcuts[SHORTCUT_IDS.TOGGLE_ANCHORING_BOTTOM_ID],
						getItemClass: function() {  if (!hasSelection(1) || !editorScope.isAbsoluteFormLayout()) return "disabled";},
						execute:function()
						{
							setAnchoring(4);
						}
					}
				);
			
			anchoringActions.push(
					{
						text: "Left",
						getIconStyle: function(){ if(isAnchored(8)) return {'background-image':"url(images/check.png)"};},
						shortcut: shortcuts[SHORTCUT_IDS.TOGGLE_ANCHORING_LEFT_ID],
						getItemClass: function() { if (!hasSelection(1) || !editorScope.isAbsoluteFormLayout()) return "disabled";},
						execute:function()
						{
							setAnchoring(8);
						}
					}
				);
			
			$contextmenu.add(
					{
						text: "Anchoring",
						subMenu: anchoringActions,
						getItemClass: function() { return "dropdown-submenu";}
					}
				);
			
			//arrange
			var arrangeActions = [];
			
			arrangeActions.push(
					{
						text: "Bring forward",
						getIconStyle: function(){ return {'background-image':"url(images/bring_forward.png)"}},
						shortcut: shortcuts[SHORTCUT_IDS.BRING_TO_FRONT_ONE_STEP_ID],
						getItemClass: function() { if (!hasSelection()) return "disabled";},
						execute:function()
						{
							$("#contextMenu").hide();
							$editorService.executeAction('z_order_bring_to_front_one_step');
						}
					}
				);
			
			arrangeActions.push(
					{
						text: "Send backward",
						getIconStyle: function(){ return {'background-image':"url(images/send_backward.png)"}},
						shortcut: shortcuts[SHORTCUT_IDS.SEND_TO_BACK_ONE_STEP_ID],
						getItemClass: function() { if (!hasSelection()) return "disabled";},
						execute:function()
						{
							$("#contextMenu").hide();
							$editorService.executeAction('z_order_send_to_back_one_step');
						}
					}
				);
			
			arrangeActions.push(
					{
						text: "Bring to front",
						getIconStyle: function(){ return {'background-image':"url(images/bring_to_front.png)"}},
						shortcut: shortcuts[SHORTCUT_IDS.BRING_TO_FRONT_ID],
						getItemClass: function() { if (!hasSelection()) return "disabled";},
						execute:function()
						{
							$("#contextMenu").hide();
							$editorService.executeAction('z_order_bring_to_front');
						}
					}
				);
			
			arrangeActions.push(
					{
						text: "Send to back",
						getIconStyle: function(){ return {'background-image':"url(images/send_to_back.png)"}},
						shortcut: shortcuts[SHORTCUT_IDS.SEND_TO_BACK_ID],
						getItemClass: function() { if (!hasSelection()) return "disabled";},
						execute:function()
						{
							$("#contextMenu").hide();
							$editorService.executeAction('z_order_send_to_back');
						}
					}
				);
			
			$contextmenu.add(
					{
						text: "Arrange",
						subMenu: arrangeActions,
						getItemClass: function() { return "dropdown-submenu"}
					}
				);
			
			$contextmenu.add(
					{
						getItemClass: function() { return "divider"}
					}
				);
			
			$contextmenu.add(
				{
					text: "Save as template ...",
					getIconStyle: function(){ return {'background-image':"url(toolbaractions/icons/template.gif)"}},
					execute:function()
					{
						$("#contextMenu").hide();
						$editorService.openElementWizard('saveastemplate');
					}
				}
			);			
			
			$contextmenu.add(
				{
					text: "Open in Script Editor",
					getIconStyle: function(){ return {'background-image':"url(images/js.gif)"}},
					shortcut: shortcuts[SHORTCUT_IDS.OPEN_SCRIPT_ID],
					execute:function()
					{
						$("#contextMenu").hide();
						$editorService.executeAction('openScript');
					}
				}
			);
		});
	});
	
});