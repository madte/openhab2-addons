package org.openhab.binding.coap.internal.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.openhab.binding.coap.handler.DeviceResourceObserver;
import org.openhab.binding.coap.handler.DeviceResourceObserverInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceResource {

    private Logger logger = LoggerFactory.getLogger(DeviceResource.class);

    // private DTLSConnector dtlsConnector;
    private CoapClient coapClient;
    private CoapHandler coapNotificationHandler;
    private DeviceResourceObserverInstance deviceResourceObserver = new DeviceResourceObserverInstance();
    private CoapObserveRelation observeRelation;

    private String channelId;
    private String channelType;

    private URI uri;

    public DeviceResource(String _uri, String _element) {

        this.createCoapClient(_uri + _element);
    }

    public DeviceResource(String _uri, String _channel, String _type, DeviceResourceObserver _deviceResourceObserver) {

        this.channelId = _channel;
        this.channelType = _type;
        // this.deviceResourceObserver = _deviceResourceObserver;

        this.createCoapClient(_uri + _channel);
    }

    public DeviceResource(String _deviceUri, String _id, String _type, DTLSConnector _dtlsConnector,
            DeviceResourceObserver _deviceResourceObserver) {
        // TODO Auto-generated constructor stub
        this(_deviceUri, _id, _type, _deviceResourceObserver);
        this.coapClient.setEndpoint(new CoapEndpoint(_dtlsConnector, NetworkConfig.getStandard()));
    }

    @Override
    public void finalize() {
        cancelObserve();
    }

    /*
     * private DTLSConnector createDtlsConnector(String _clientIdentity, String _secretPsk) {
     *
     * DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
     * builder.setPskStore(new StaticPskStore(_clientIdentity, _secretPsk.getBytes()));
     * return new DTLSConnector(builder.build());
     * }
     */

    private void createCoapClient(String _uri) {

        try {
            this.uri = new URI(_uri);
            this.coapClient = new CoapClient(uri);
            logger.debug("Created coap client for URI: " + _uri);
        } catch (URISyntaxException e) {
            logger.debug("Invalid URI: " + e.getMessage());
            System.exit(-1); // TODO change to something more appropriate
        }
    }

    public String readResource() {

        logger.debug("GET to: " + coapClient.getURI());
        CoapResponse response = this.coapClient.get();

        if (response != null) {

            logger.debug("Response of GET from: " + coapClient.getURI() + " with value: " + response.getResponseText());
            logger.debug("\nADVANCED\n");
            // access advanced API with access to more details through .advanced()
            logger.debug(Utils.prettyPrint(response));
            return response.getResponseText();

        } else {
            logger.debug("No response received.");
            return "Error"; // TODO think of better error handling
        }
    }

    public Boolean writeResource(String _value) {

        logger.debug("PUT to: " + coapClient.getURI() + " with value: " + _value);
        CoapResponse response = this.coapClient.put(_value, MediaTypeRegistry.TEXT_PLAIN);

        if (response != null) {
            logger.debug("Response of PUT from: " + coapClient.getURI() + " with value: " + response.getResponseText());
            logger.debug("\nADVANCED\n");
            // access advanced API with access to more details through .advanced()
            logger.debug(Utils.prettyPrint(response));

        } else {
            logger.debug("No response received.");
            return false;
        }

        return true;
    }

    public String observeResource() {

        this.coapNotificationHandler = new CoapHandler() {

            @Override
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                logger.debug("Notification from: " + coapClient.getURI() + " with value: " + content);
                logger.debug("\nADVANCED\n");
                logger.debug(Utils.prettyPrint(response));
                DeviceResourceObserverInstance.getDeviceResourceObserverInstance()
                        .handleDeviceResourceNotification(channelId, channelType, content);
            }

            @Override
            public void onError() {
                logger.info("OBSERVING FAILED");
                // TODO forward error to thinghandler to set status to offline
            }
        };

        this.observeRelation = this.coapClient.observe(this.coapNotificationHandler, 50);
        return null;
    }

    private void cancelObserve() {
        this.observeRelation.proactiveCancel();
    }

    public String getChannelId() {
        return channelId;
    }

}
