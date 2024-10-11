'use strict';

angular.module('myApp.dashboard', ['ngRoute'])
/**
 * Configures the route for the dashboard page
 * @param {Object} $routeProvider - AngularJS $routeProvider service
 * @returns {undefined} This method does not return a value
 */.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/', {
    templateUrl: 'dashboard/dashboard.html',
    controller: DashboardCtrl,
		resolve: DashboardCtrl.resolve
  });
}]);

/**
 * Controller for the Dashboard view. Manages authentication status, user information retrieval,
 * and API interactions for displaying user data.
 * @param {Object} $scope - Angular scope object for the controller
 * @param {Object} $rootScope - Angular root scope object
 * @param {Object} $http - Angular's $http service for making HTTP requests
 * @param {boolean} isAuthenticated - Flag indicating whether the user is authenticated
 * @param {Object} authService - Service providing authentication-related functionality
 * @returns {void} This controller does not return a value
 */function DashboardCtrl($scope, $rootScope, $http, isAuthenticated, authService) {
	$rootScope.authenticated = isAuthenticated;

	$scope.serverResponse = '';
	$scope.responseBoxClass = '';

	var setResponse = function(res, success) {
		$rootScope.authenticated = isAuthenticated;
		if (success) {
			$scope.responseBoxClass = 'alert-success';
		} else {
			$scope.responseBoxClass = 'alert-danger';
		}
		$scope.serverResponse = res;
		$scope.serverResponse.data = JSON.stringify(res.data, null, 2);
	}

	if ($rootScope.authenticated) {
		authService.getUser()
		.then(function(response) {
			$scope.user = response.data;
		});
	}

	$scope.getUserInfo = function() {
		authService.getUser()
		.then(function(response) {
			setResponse(response, true);
		})
		.catch(function(response) {
			setResponse(response, false);
		});
	}

	$scope.getAllUserInfo = function() {
    $http({
      headers: authService.createAuthorizationTokenHeader(),
      method: 'GET',
      url: 'api/user/all'
    })
		.then(function(res) {
			setResponse(res, true);
		})
		.catch(function(response) {
			setResponse(response, false);
		});
	}
}
DashboardCtrl.resolve = {
	/**
	 * Checks if the user is authenticated by attempting to refresh the JWT token.
	 * @param {Object} $q - Angular's promise service
	 * @param {Object} $http - Angular's HTTP client service
	 * @param {Object} AuthService - Service handling authentication operations
	 * @returns {Promise<boolean>} A promise that resolves to true if authentication is successful, false otherwise
	 */
	isAuthenticated : function($q, $http, AuthService) {
		var deferred = $q.defer();
		var oldToken = AuthService.getJwtToken();
		if (oldToken !== null) {
      $http({
        headers: AuthService.createAuthorizationTokenHeader(),
        method: 'POST',
        url: 'auth/refresh'
      })
      .success(function(res) {
        AuthService.setJwtToken(res.access_token);
        deferred.resolve(res.access_token !== null);
      })
      .error(function(err){
        AuthService.removeJwtToken();
        deferred.resolve(false); // you could optionally pass error data here
      });
		} else {
      deferred.resolve(false);
		}
		return deferred.promise;
	}
};

DashboardCtrl.$inject = ['$scope', '$rootScope', '$http', 'isAuthenticated', 'AuthService'];

