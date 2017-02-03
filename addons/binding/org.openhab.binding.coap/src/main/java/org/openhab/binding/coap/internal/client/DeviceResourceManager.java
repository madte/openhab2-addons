package org.openhab.binding.coap.internal.client;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceResourceManager {

    private Logger logger = LoggerFactory.getLogger(DeviceResource.class);

    private DTLSConnector dtlsConnector;
    private List<DeviceResource> deviceResourceList;

    /**
     * @return the deviceResourceList
     */
    public List<DeviceResource> getDeviceResourceList() {
        return deviceResourceList;
    }

    private String deviceUri;

    public DeviceResourceManager(String _deviceUri) {
        // TODO Auto-generated constructor stub
        deviceUri = _deviceUri;
        deviceResourceList = new ArrayList<DeviceResource>();
    }

    public DeviceResourceManager(String _deviceUri, String _dtlsPskIdentity, String _dtlsPsk) {
        // TODO Auto-generated constructor stub
        this(_deviceUri);
        dtlsConnector = createDtlsConnector(_dtlsPskIdentity, _dtlsPsk);
    }

    public DeviceResourceManager(String _deviceUri, KeyPair _kp) {
        // TODO Auto-generated constructor stub
        this(_deviceUri);
        dtlsConnector = createDtlsConnector(_kp);
    }

    // add resource
    public void addDeviceResource(String _id, String _type) {
        DeviceResource tempDeviceResource = new DeviceResource(deviceUri, _id, _type, dtlsConnector);
        tempDeviceResource.observeResource();
        deviceResourceList.add(tempDeviceResource);
    }

    // remove resource
    public void removeDeviceResource(String _id) {
        for (DeviceResource deviceResource : deviceResourceList) {
            if (deviceResource.getChannelId().equals(_id)) {
                deviceResourceList.remove(deviceResource);
            }
        }
    }

    public DeviceResource getDeviceResourceById(String _id) {
        DeviceResource tempDeviceResource = null; // TODO check how to handle if resource is not in the list,
                                                  // nullpointerexception is not cool
        for (DeviceResource deviceResource : deviceResourceList) {
            if (deviceResource.getChannelId().equals(_id)) {
                tempDeviceResource = deviceResource;
            }
        }
        return tempDeviceResource;
    }

    public void clearDeviceResources() {
        deviceResourceList.clear();
    }

    @Override
    public void finalize() {
        clearDeviceResources();
    }

    private DTLSConnector createDtlsConnector(String _dtlsPskIdentity, String _dtlsPsk) {

        StaticPskStore keyStore;

        keyStore = new StaticPskStore(_dtlsPskIdentity, _dtlsPsk.getBytes());
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setPskStore(keyStore);

        return new DTLSConnector(builder.build());
    }

    private DTLSConnector createDtlsConnector(KeyPair _kp) {

        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setIdentity(_kp.getPrivate(), _kp.getPublic());

        return new DTLSConnector(builder.build());
    }
}
