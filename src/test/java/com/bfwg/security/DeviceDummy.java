package com.bfwg.security;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.stereotype.Component;

/**
 * Created by fanjin on 2017-10-07.
 */
@Component
public class DeviceDummy implements Device {
    private boolean normal;
    private boolean mobile;
    private boolean tablet;

    /**
     * Checks if the current state is normal.
     * 
     * @return true if the state is normal, false otherwise
     */
    @Override
    public boolean isNormal() {
        return normal;
    }

    /**
    * Checks if the current device is a mobile device.
    *
    * @return true if the device is mobile, false otherwise
    */
    @Override
    public boolean isMobile() {
        return mobile;
    }

    /**
    * Determines if the device is a tablet.
    * 
    * @return true if the device is a tablet, false otherwise
    */
    @Override
    public boolean isTablet() {
        return tablet;
    }

    /**
    * Returns the device platform.
    * 
    * @return The DevicePlatform of the device, or null if not available.
    */
    @Override
    public DevicePlatform getDevicePlatform() {
        return null;
    }

    /**
    * Sets the normal state of the object.
    * 
    * @param normal The boolean value indicating whether the state should be set to normal (true) or not normal (false)
    */
    public void setNormal(boolean normal) {
        this.normal = normal;
    }

    /**
    * Sets the mobile status of the object.
    * 
    * @param mobile the boolean value indicating whether the object is mobile or not
    */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    /**
    * Sets the tablet status of the device.
    * 
    * @param tablet boolean value indicating whether the device is a tablet (true) or not (false)
    */
    public void setTablet(boolean tablet) {
        this.tablet = tablet;
    }
}
