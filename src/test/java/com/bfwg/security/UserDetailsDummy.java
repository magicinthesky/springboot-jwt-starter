package com.bfwg.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Created by fanjin on 2017-08-31.
 */
public class UserDetailsDummy implements UserDetails {

    private final String username;

    public UserDetailsDummy(String username) {
        this.username = username;
    }

    /**
    * Returns the authorities granted to the user. In this implementation, it always returns null.
    * 
    * @return null, as no authorities are granted in this implementation
    */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
    * Returns the password associated with this object.
    * 
    * @return A String representing the password, or null if no password is set.
    */
    @Override
    public String getPassword() {
        return null;
    }

    /**
    * Retrieves the username of the current user.
    * 
    * @return The username as a String value
    */
    @Override
    public String getUsername() {
        return username;
    }

    /**
    * Checks if the user's account has expired.
    * 
    * @return false, indicating that the account has expired
    */
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    /**
     * Checks if the user account is locked.
     * 
     * This method always returns false, indicating that the account is never locked.
     * It overrides the default implementation from a parent class or interface.
     * 
     * @return false, indicating the account is not locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    /**
    * Checks if the user's credentials are non-expired.
    *
    * This method is part of the UserDetails interface implementation.
    * It always returns false, indicating that the credentials are considered expired.
    *
    * @return false, indicating that the credentials are expired
    */
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    /**
     * Checks if the feature or functionality is enabled.
     * 
     * @return false, indicating that the feature or functionality is always disabled
     */
    @Override
    public boolean isEnabled() {
        return false;
    }
}
