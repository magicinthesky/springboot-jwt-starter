'use strict';
// Declare app level module which depends on views, and components
angular.module('myApp', [
  'ngRoute',
  'myApp.dashboard',
  'myApp.login',
  'myApp.services'
]).
/**
 * Configures the AngularJS application with custom settings for HTTP requests and routing.
 * @param {Object} $locationProvider - Provider for configuring the AngularJS $location service.
 * @param {Object} $routeProvider - Provider for configuring routes in AngularJS.
 * @param {Object} $httpProvider - Provider for configuring the AngularJS $http service.
 * @returns {undefined} This method does not return a value.
 */
config(['$locationProvider', '$routeProvider', "$httpProvider", function($locationProvider, $routeProvider, $httpProvider) {
	$httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
  $routeProvider.otherwise({redirectTo: '/'});
}])
.controller('NavigationCtrl', ['$scope', '$rootScope', '$http', '$location', 'AuthService',
  /**
   * Initializes the controller and sets up authentication-related functionalities.
   * @param {Object} $scope - Angular scope object for the controller
   * @param {Object} $rootScope - Angular root scope object
   * @param {Object} $http - Angular $http service for making HTTP requests
   * @param {Object} $location - Angular $location service for working with URL
   * @param {Object} authService - Custom authentication service
   * @returns {undefined} This method does not return a value
   */
  function($scope, $rootScope, $http, $location, authService) {
    var self = this

    $rootScope.selectedTab = $location.path() || '/';

    $scope.logout = function() {
      authService.removeJwtToken();
      $rootScope.authenticated = false;
      $location.path("#/");
      $rootScope.selectedTab = "/";
    }

    $scope.setSelectedTab = function(tab) {
      $rootScope.selectedTab = tab;
    }

    $scope.tabClass = function(tab) {
      if ($rootScope.selectedTab == tab) {
        return "active";
      } else {
        return "";
      }
    }

    if ($rootScope.authenticated) {
      $location.path('/');
      $rootScope.selectedTab = '/';
      return;
    }
  }
]);