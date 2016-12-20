package org.openhab.binding.coap.internal.client;

import java.net.InetSocketAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoAPClient extends CoapClient {

    private Logger logger = LoggerFactory.getLogger(CoAPClient.class);
    // private CoapClient coapclient;
    private DTLSConnector dtlsConnector;

    public CoAPClient(String serverURI) {
        // TODO Auto-generated constructor stub
        super(serverURI);
    }

    public CoAPClient(String serverURI, String clientIdentity, String secretPsk) {
        super(serverURI);
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
        builder.setPskStore(new StaticPskStore(clientIdentity, secretPsk.getBytes()));
        dtlsConnector = new DTLSConnector(builder.build());
        /*
         * try {
         * URI uri = new URI(serverURI);
         * super.coapclient = new CoapClient(uri);
         * 
         * this.coapclient.setEndpoint(new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard()));
         * // response = this.coapclient.get();
         * 
         * } catch (URISyntaxException e) {
         * logger.debug("Invalid URI: " + e.getMessage());
         * System.exit(-1);
         * }
         */
    }

}
