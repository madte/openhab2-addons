package org.openhab.binding.coap.handler;

/**
 * @author Madte
 *
 *         Interface to implement Observer pattern for device resources
 *
 */
public interface DeviceResourceObserver {
    public void handleDeviceResourceNotification(String _channelId, String _channelType, String _value);
}
