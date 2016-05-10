angular.module('app', ['ngMaterial'])
.config(function($mdIconProvider) {
  $mdIconProvider
    .iconSet('social', 'img/icons/sets/social-icons.svg', 24)
    .iconSet('device', 'img/icons/sets/device-icons.svg', 24)
    .iconSet('communication', 'img/icons/sets/communication-icons.svg', 24)
    .defaultIconSet('img/icons/sets/core-icons.svg', 24);
})
.controller('appPackages', function($scope,$window) {
    var loc = $window.location;
    var self = this;
		  
	var uri = "ws://"+loc.host+"/wpm/angular2/websocket";
//		var uri = "ws://localhost:8080/wpm/angular2/websocket";
	  
	var ws = new WebSocket(uri);
	  
	ws.onopen = function (event) {
	      var command = {"method":"requestAllInstalledPackages"};
	      ws.send(JSON.stringify(command)); 
	      command = {"method":"getSolutionList"};
	      ws.send(JSON.stringify(command)); 
	};
	  
	ws.onmessage = function (msg){
		$scope.$apply(function() {
			var receivedJson = JSON.parse(msg.data);
			var method = receivedJson["method"];
			self[method](receivedJson["result"]);
		})
	}
	
	$scope.solutionList = []
	$scope.componentPackages = []
	$scope.servicePackages = []
	$scope.layoutPackages = []
	  
	this.requestAllInstalledPackages = function(packagesArray){
		for(i=0;i<packagesArray.length;i++) {
			if (packagesArray[i].packageType == "Web-Component") {
				$scope.componentPackages.push(packagesArray[i]);
			}
			else if (packagesArray[i].packageType == "Web-Service") {
				$scope.servicePackages.push(packagesArray[i]);
			}
			else if (packagesArray[i].packageType == "Web-Layout") {
				$scope.layoutPackages.push(packagesArray[i]);
			}
		}
	};
	this.getSolutionList = function(solutionList) {
		$scope.solutionList  = solutionList;
	}
    //function called via json "message":"updatePackage"
    this.updatePackage = function (pack) {
	  	var i;
	  	for (i=0; i<this.items.length; i++){
	  	    if (this.items[i].name === pack.name) {
	  		this.items[i].version = pack.version;
	  		this.items[i].latestVersion = pack.latestVersion;
	  	    }
	  	}
    }

	$scope.install = function (item) {
    	  var command = {"method":"install",
    		  	 "args": item};
    	  ws.send(JSON.stringify(command));
  	}


    $scope.tabSelected = 1;
}).directive('packages',  function () {
	return {
		restrict: 'E',
		templateUrl: 'packages.html',
		scope: {
			packages: "=packages",
			solutionList: "=solutionList"
		},
		link:function($scope, $element, $attrs) {
		  $scope.getActionText = function(index) {
		    if ($scope.packages[index].installed) { 
		      return "Upgrade"
		    } else {
		      return "Add to solution";
		    }
		  }

		   $scope.getSelectedRelease = function(index) {
			if (angular.isUndefined($scope.packages[index].selected)) {
				$scope.packages[index].selected = $scope.packages[index].installed; 
			}
		    if ($scope.packages[index].selected == $scope.packages[index].releases[0].version) {
		      return $scope.packages[index].selected + " Latest";
		    } else {
		      return $scope.packages[index].selected;
		    }
		  }

		  $scope.uninstall = function(index) {
		    $scope.packages[index].installed = null;
		  }

		  $scope.isLatestRelease = function (index) {
		    return $scope.packages[index].installed == $scope.packages[index].releases[0];
		  }

		  $scope.upgradeEnabled = function (index) {
		    return !$scope.packages[index].installed || (!$scope.isLatestRelease(index) && $scope.packages[index].selected > $scope.packages[index].installed )
		  }

		  $scope.showDemo = function(value) {
		    $scope.demoVisible = value
		  }
		}
	}
});