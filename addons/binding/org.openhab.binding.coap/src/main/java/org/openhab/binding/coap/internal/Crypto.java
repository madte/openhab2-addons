package org.openhab.binding.coap.internal;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Crypto {

    public Crypto() {
        // TODO Auto-generated constructor stub
    }

    public static KeyPairGenerator generateAsyncKeypair(String _keyAgreement, String _ellipticCurve, String _provider)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpgen = KeyPairGenerator.getInstance(_keyAgreement, _provider);
        // X9ECParameters ecP = CustomNamedCurves.getByName("Curve25519");
        // ECParameterSpec ecSpec = new ECParameterSpec(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(),
        // ecP.getSeed());

        // kpgen.initialize(ecSpec, new SecureRandom());

        kpgen.initialize(new ECGenParameterSpec(_ellipticCurve), new SecureRandom());
        return kpgen;
    }

    public static KeyPair generateEcdsaKeypair(String _curve) {

        KeyPair kp = null;

        try {
            kp = generateAsyncKeypair("ECDSA", _curve, "BC").generateKeyPair();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return kp;

    }

    public static KeyPair generateKeypair() {
        return null;

    }

}
