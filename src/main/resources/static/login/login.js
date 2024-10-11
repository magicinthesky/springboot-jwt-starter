angular.module('myApp.login', ['ngRoute'])

/**
 * Configures the route for the login page using AngularJS $routeProvider.
 * @param {Object} $routeProvider - AngularJS $routeProvider service for configuring routes.
 * @returns {undefined} This method does not return a value.
 */.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/login', {
    templateUrl: 'login/login.html',
    controller: 'LoginCtrl'
  });
}])

.controller('LoginCtrl', ['$scope', '$rootScope', '$http', '$location', 'AuthService',
  /**
   * Handles user login authentication and updates application state accordingly.
   * @param {Object} $scope - Angular scope for the login controller.
   * @param {Object} $rootScope - Angular root scope for application-wide data.
   * @param {Object} $http - Angular's $http service for making HTTP requests.
   * @param {Object} $location - Angular's $location service for URL manipulation.
   * @param {Object} authService - Custom authentication service.
   * @returns {undefined} This function does not return a value.
   */
  function($scope, $rootScope, $http, $location, authService) {
  $scope.error = false;
  $rootScope.selectedTab = $location.path() || '/';

  $scope.credentials = {};

  $scope.login = function() {
    // We are using formLogin in our backend, so here we need to serialize our form data
    $http({
      url: 'auth/login',
      method: 'POST',
      data: $scope.credentials,
      headers: authService.createAuthorizationTokenHeader()
    })
    .success(function(res) {
      $rootScope.authenticated = true;
      authService.setJwtToken(res.access_token);
      $location.path("#/");
      $rootScope.selectedTab = "/";
      $scope.error = false;
    })
    .catch(function() {
      authService.removeJwtToken();
      $rootScope.authenticated = false;
      $scope.error = true;
    });
  };
}]);
