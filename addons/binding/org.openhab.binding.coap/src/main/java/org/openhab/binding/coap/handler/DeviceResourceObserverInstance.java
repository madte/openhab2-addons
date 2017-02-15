package org.openhab.binding.coap.handler;

public class DeviceResourceObserverInstance {

    private static DeviceResourceObserver deviceResourceObserverInstance;

    /**
     * @return the deviceResourceObserverInstance
     */
    public static DeviceResourceObserver getDeviceResourceObserverInstance() {
        return deviceResourceObserverInstance;
    }

    public DeviceResourceObserverInstance() {
        // TODO Auto-generated constructor stub

    }

    public DeviceResourceObserverInstance(DeviceResourceObserver _deviceResourceObserverInstance) {
        // TODO Auto-generated constructor stub

        deviceResourceObserverInstance = _deviceResourceObserverInstance;
    }

}
