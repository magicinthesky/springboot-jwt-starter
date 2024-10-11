angular.module('myApp.services', [])
/**
 * Angular factory service for authentication-related operations.
 * Manages JWT tokens and provides methods for user authentication and authorization.
 * @returns {Object} An object containing methods for authentication operations:
 *   - getUser: Retrieves the current user's information
 *   - getJwtToken: Retrieves the JWT token from local storage
 *   - setJwtToken: Stores the JWT token in local storage
 *   - removeJwtToken: Removes the JWT token from local storage
 *   - createAuthorizationTokenHeader: Creates an authorization header with the JWT token
 */
.factory('AuthService', function($http) {
	var user = null;
	var TOKEN_KEY = 'jwtToken';

  var getJwtToken = function() {
    return localStorage.getItem(TOKEN_KEY);
  };

  var setJwtToken = function(token) {
      localStorage.setItem(TOKEN_KEY, token);
  };

  var removeJwtToken = function() {
      localStorage.removeItem(TOKEN_KEY);
  };

  var createAuthorizationTokenHeader = function() {
      var token = getJwtToken();
      if (token) {
          return {
            "Authorization": "Bearer " + token,
            'Content-Type': 'application/json'
          };
      } else {
          return {
            'Content-Type': 'application/json'
          };
      }
  }

  var getUser = function() {
    return $http({
      headers: createAuthorizationTokenHeader(),
      method: 'GET',
      url: 'api/whoami'
    });
  };

  return {
    getUser: getUser,
    getJwtToken: getJwtToken,
    setJwtToken: setJwtToken,
    removeJwtToken: removeJwtToken,
    createAuthorizationTokenHeader: createAuthorizationTokenHeader
  };
});