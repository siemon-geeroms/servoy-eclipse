angular.module('editorContent',[])
 .controller("ContentController", function($scope, $window, $timeout, $webSocket,$rootScope,$compile,$editorService,$editorContentService){
     
     	 $editorService.setEditorContentRootScope($rootScope);
	 $rootScope.createComponent = function(html,model) {
			 var compScope = $scope.$new(true);
			 compScope.model = model;
			 compScope.api = {};
			 compScope.handlers = {};
			 var el = $compile(html)(compScope);
			 $('body').append(el); 
			 return el;
		  }
	$rootScope.highlight = false;
//	$solutionSettings.enableAnchoring = false; 
//	$scope.solutionSettings = $solutionSettings; 
	var realConsole = $window.console;
	$window.console = {
			log: function(msg) {
				if (typeof(consoleLog) != "undefined") {
					consoleLog("log",msg)
				}
				else if (realConsole) {
					realConsole.log(msg)
				}
				else alert(msg);
				
			},
			error: function(msg) {
				if (typeof(consoleLog) != "undefined") {
					consoleLog("error",msg)
				}
				else if (realConsole) {
					realConsole.error(msg)
				}				
				else alert(msg);
			}
	}
	
	$editorService.connect().then(function() {
	    $editorContentService.refreshGhosts();
	});
	
	
	
	if (typeof(WebSocket) == 'undefined' || $webSocket.getURLParameter("replacewebsocket")=='true') {
		
		WebSocket = SwtWebSocket;
		 
		function SwtWebSocket(url)  
		{
			this.id = parent.window.addWebSocket(this);
			var me = this;
			function onopenCaller(){
				parent.window.SwtWebsocketBrowserFunction('open', url, me.id)
				me.onopen()
			}
			setTimeout(onopenCaller, 0);
		}
		
		SwtWebSocket.prototype.send = function(str)
		{
			parent.window.SwtWebsocketBrowserFunction('send', str, this.id)
		}
	}
//	 $servoyInternal.connect();
//	 var formName = $webSocket.getURLParameter("f");
//	 var high = $webSocket.getURLParameter("highlight");
//	 $scope.getUrl = function() {
//		 if ($webSocket.isConnected()) {
//			 var url = $windowService.getFormUrl(formName);
//			 // this main url is in design (the template must have special markers)
//			 return url?url+"&design=true"+"&highlight="+$rootScope.highlight:null;
//		 }
//	 }
 }).factory("$editorContentService", function($editorService, EDITOR_CONSTANTS, EDITOR_EVENTS, $timeout) {
	 
	 return  {
		 refreshDecorators: function() {
		  // TODO this is now in a timeout to let the editor-content be able to reload the form.
			// could we have an event somewhere from the editor-content that the form is reloaded and ready?
			// maybe the form controllers code could call $evalAsync as last thing in its controller when it is in design.
		     var editorScope = $editorService.getEditor();
		     var selection = editorScope.getSelection();
		     
			if (selection.length > 0) {
				var ghost = editorScope.getGhost(selection[0].getAttribute("svy-id"));
				if(ghost && (ghost.type === EDITOR_CONSTANTS.GHOST_TYPE_FORM)) {						
					editorScope.setContentSizes();
				}
				else {
					var promise = $editorService.getGhostComponents();//no parameter, then the ghosts are not repositioned
					promise.then(function (result){
						editorScope.setGhosts(result);
						$timeout(function() {
							var nodes = Array.prototype.slice.call(editorScope.glasspane.querySelectorAll("[svy-id]"));
							var matchedElements = []
							for (var i = 0; i < nodes.length; i++) {
								var element = nodes[i]
								matchedElements.push(element);
							}	
							selection = matchedElements;
							if(selection.length != matchedElements.length) {
							    editorScope.$broadcast(EDITOR_EVENTS.SELECTION_CHANGED,selection);
							}
							else {
							    editorScope.$broadcast(EDITOR_EVENTS.RENDER_DECORATORS, selection);
							}
						}, 100);
					});
				}
			}
			else {
				editorScope.setContentSizes();
			}
		 },
		 refreshGhosts: function() {
		     var promise = $editorService.getGhostComponents();//no parameter, then the ghosts are not repositioned
			promise.then(function (result){
			    $editorService.getEditor().setGhosts(result);
			});
		 },
		 updateForm: function(name, uuid, w, h) {
			 updateForm({name:name, uuid:uuid, w:w, h:h});
		 }
	 }
 }).factory("loadingIndicator",function() {
	//the loading indicator should not be shown in the editor
	return {
		showLoading: function() {},
		hideLoading: function() {}
	}
});
