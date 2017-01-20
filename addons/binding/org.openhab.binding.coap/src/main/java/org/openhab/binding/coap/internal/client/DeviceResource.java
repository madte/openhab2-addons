package org.openhab.binding.coap.internal.client;

import java.net.InetSocketAddress;
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
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.openhab.binding.coap.handler.CoAPHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceResource {

    private Logger logger = LoggerFactory.getLogger(DeviceResource.class);

    private DTLSConnector dtlsConnector;
    private CoapClient coapClient;
    private CoapHandler resourceHandler;
    private CoAPHandler coapHandler;
    private CoapObserveRelation observeRelation;

    private String channelId;
    private String channelType;

    private URI uri;

    public DeviceResource(String _uri, String _element) { // TODO maybe create extra class for simple coap get

        this.createCoapClient(_uri + _element);
    }

    public DeviceResource(String _uri, String _channel, String _type, CoAPHandler _coapHandler) {

        this.channelId = _channel;
        this.channelType = _type;
        this.coapHandler = _coapHandler;

        this.createCoapClient(_uri + _channel);
    }

    public DeviceResource(String _uri, String _channel, String _type, CoAPHandler _coapHandler, String _clientIdentity,
            String _secretPsk) {

        this(_uri, _channel, _type, _coapHandler);
        // load key store
        StaticPskStore keyStore;

        keyStore = new StaticPskStore(_clientIdentity, _secretPsk.getBytes());
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setPskStore(keyStore);

        dtlsConnector = new DTLSConnector(builder.build()); // TODO think about creating
        // dtlsConnector only once per thing
        this.coapClient.setEndpoint(new CoapEndpoint(this.dtlsConnector, NetworkConfig.getStandard()));
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
        } catch (URISyntaxException e) {
            logger.debug("Invalid URI: " + e.getMessage());
            System.exit(-1); // TODO change to something more appropriate
        }
    }

    public String readResource() {

        CoapResponse response = this.coapClient.get();

        if (response != null) {

            logger.debug(response.getResponseText());
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

        CoapResponse response = this.coapClient.put(_value, MediaTypeRegistry.TEXT_PLAIN);

        if (response != null) {

            logger.debug(response.getResponseText());
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

        this.resourceHandler = new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                logger.debug("Notification from: " + coapClient.getURI());
                logger.debug(Utils.prettyPrint(response));
                String content = response.getResponseText();
                coapHandler.handleCoapNotification(channelId, channelType, content);
            }

            @Override
            public void onError() {
                logger.info("OBSERVING FAILED");
                // TODO forward error to thinghandler to set status to offline
            }
        };

        this.observeRelation = this.coapClient.observe(this.resourceHandler);
        return null;
    }

    private void cancelObserve() {
        this.observeRelation.proactiveCancel();
    }

    public String getChannelId() {
        return channelId;
    }

}
