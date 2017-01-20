package org.openhab.binding.coap.internal;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import com.google.gson.Gson;

public class DeviceInfoReceiver {

    class DeviceInfo {

        public String id;
        public String type;

    }

    private static final int COAP_INFO_PORT = 5682;

    private DeviceInfo deviceInfo = new DeviceInfo();

    private CoapClient coapClient;

    private Gson gson = new Gson();

    private void createInfoCoapClient(String _hostname) {

        this.coapClient = new CoapClient("coap", _hostname, COAP_INFO_PORT, "info");

    }

    private String read() {

        CoapResponse response = this.coapClient.get();

        if (response != null) {
            deviceInfo = gson.fromJson(response.getResponseText(), DeviceInfo.class);
            return response.getResponseText();

        } else {

            return "Error"; // TODO think of better error handling
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return deviceInfo.id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return deviceInfo.type;
    }

    public DeviceInfoReceiver(String _hostname) {

        this.createInfoCoapClient(_hostname);
        read();

    }

}
