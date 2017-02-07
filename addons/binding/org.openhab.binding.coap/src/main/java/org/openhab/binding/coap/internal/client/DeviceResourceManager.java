package org.openhab.binding.coap.internal.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.openhab.binding.coap.handler.DeviceResourceObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceResourceManager {

    private Logger logger = LoggerFactory.getLogger(DeviceResourceManager.class);

    private static final String TRUST_STORE_PASSWORD = "rootPass";
    private static final String KEY_STORE_PASSWORD = "endPass";
    private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
    private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";

    // private DTLSConnector dtlsConnector;
    private List<DeviceResource> deviceResourceList;
    private DeviceResourceObserver deviceResourceObserver;

    private String deviceUri;
    private String dtlsPskIdentity = null;
    private String dtlsPsk = null;

    private KeyPair keyPair = null;

    public DeviceResourceManager(String _deviceUri, DeviceResourceObserver _deviceResourceObserver) {
        // TODO Auto-generated constructor stub
        deviceUri = _deviceUri;
        deviceResourceList = new ArrayList<DeviceResource>();
        deviceResourceObserver = _deviceResourceObserver;
    }

    public DeviceResourceManager(String _deviceUri, DeviceResourceObserver _deviceResourceObserver,
            String _dtlsPskIdentity, String _dtlsPsk) {
        // TODO Auto-generated constructor stub
        this(_deviceUri, _deviceResourceObserver);
        this.dtlsPskIdentity = _dtlsPskIdentity;
        this.dtlsPsk = _dtlsPsk;
    }

    public DeviceResourceManager(String _deviceUri, DeviceResourceObserver _deviceResourceObserver, KeyPair _kp) {
        // TODO Auto-generated constructor stub
        this(_deviceUri, _deviceResourceObserver);
        this.keyPair = _kp;
    }

    // add resource
    public void addDeviceResource(String _id, String _type) {
        DeviceResource tempDeviceResource;
        if ((this.dtlsPsk != null) && (this.dtlsPskIdentity != null)) {
            tempDeviceResource = new DeviceResource(deviceUri, _id, _type,
                    createDtlsConnector(this.dtlsPskIdentity, this.dtlsPsk), this.deviceResourceObserver);
        } else if (this.keyPair != null) { // no DTLS connector so create unsecured client
            tempDeviceResource = new DeviceResource(deviceUri, _id, _type, createDtlsConnector(this.keyPair),
                    this.deviceResourceObserver);
        } else {
            tempDeviceResource = new DeviceResource(deviceUri, _id, _type, this.deviceResourceObserver);
        }
        deviceResourceList.add(tempDeviceResource); // TODO add resource read for instant resource status
        tempDeviceResource.observeResource();
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

    private DTLSConnector createDtlsConnector(KeyPair _kp) { // TODO figure out how to generate appropriate keypairs
                                                             // including signatures/certificates for
                                                             // TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8

        InputStream inTrust = null;
        InputStream in = null;

        try {
            // load key store
            KeyStore keyStore = KeyStore.getInstance("JKS");
            in = getClass().getClassLoader().getResourceAsStream(KEY_STORE_LOCATION);
            keyStore.load(in, KEY_STORE_PASSWORD.toCharArray());
            in.close();

            // load trust store
            KeyStore trustStore = KeyStore.getInstance("JKS");
            inTrust = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_LOCATION);
            trustStore.load(inTrust, TRUST_STORE_PASSWORD.toCharArray());

            // You can load multiple certificates if needed
            Certificate[] trustedCertificates = new Certificate[1];
            trustedCertificates[0] = trustStore.getCertificate("root");

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
            // builder.setPskStore(new StaticPskStore("Client_identity", "secretPSK".getBytes()));

            builder.setIdentity((PrivateKey) keyStore.getKey("client", KEY_STORE_PASSWORD.toCharArray()),
                    keyStore.getCertificateChain("client"), true);
            builder.setTrustStore(trustedCertificates);
            builder.setSupportedCipherSuites(new CipherSuite[] { CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 });
            return new DTLSConnector(builder.build());
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        // ECDHECryptography temp =
        // ECDHECryptography.fromNamedCurveId(ECDHECryptography.SupportedGroup.secp256r1.getId());

        // builder.setIdentity(temp.getPrivateKey(), temp.getPublicKey());

        // builder.setIdentity(_kp.getPrivate(), _kp.getPublic());

    }
}
