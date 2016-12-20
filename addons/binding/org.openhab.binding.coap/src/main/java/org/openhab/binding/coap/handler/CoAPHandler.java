/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coap.handler;

import static org.openhab.binding.coap.CoAPBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CoAPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Kessel - Initial contribution
 */
public class CoAPHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(CoAPHandler.class);

    private String thingUri = new String();
    private Boolean dtlsEnabled = false;

    public CoAPHandler(Thing thing) {
        super(thing);
    }

    public CoapHandler observeHandler() {

        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String response = new String();

        if (command.toFullString().contentEquals("REFRESH")) {
            return;
        }
        logger.info("handleCommand: On channel: " + channelUID.getId() + " command: " + command.toFullString());
        putStringResource(thingUri + channelUID.getId(), channelUID.getId(), command.toFullString());

        /*
         * if (channelUID.getId().equals(CHANNEL_LED1)) {
         * // TODO: handle command
         *
         * // Note: if communication with thing fails for some reason,
         * // indicate that by setting the status with detail information
         * // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
         * // "Could not control device at IP address x.x.x.x");
         *
         * putStringResource(thingUri + "led1", channelUID.getId(), command.toFullString());
         */
        /*
         * if (command.toFullString().equals("ON")) {
         * response = "1";
         * } else {
         * response = "0";
         * }
         * putStringResource("coap://[2001:db8::225:19ff:fe64:c216]:5683/lights/led3", channelUID.getId(),
         * response);
         */
        /*
         * } else if (channelUID.getId().equals(CHANNEL_LED2)) {
         * putStringResource(thingUri + "led2", channelUID.getId(), command.toFullString());
         *
         * } else if (channelUID.getId().equals(CHANNEL_STRING1)) {
         *
         * }
         */
    }

    private String buildCoapThingUri(String _hostname) {

        String uriTemp = new String();

        if (dtlsEnabled.booleanValue()) {
            uriTemp = "coaps://" + _hostname + ":5684/";
        } else {
            uriTemp = "coap://" + _hostname + ":5683/";
        }
        return uriTemp;
    }

    private void readThingConfiguration() {
        // TODO validate configuration
        boolean validConfig = true;
        Configuration conf = this.getConfig();

        if (conf.containsKey(CONFIG_DTLSENABLED_KEY)) {
            dtlsEnabled = Boolean.parseBoolean(conf.get(CONFIG_DTLSENABLED_KEY).toString());
        } else {
            validConfig = false;
        }

        if (conf.containsKey(CONFIG_IP_KEY)) {
            thingUri = buildCoapThingUri(conf.get(CONFIG_IP_KEY).toString());
        } else {
            validConfig = false;
        }

        if (validConfig) {
            logger.info("Thing Configuration read successful");
        } else {
            logger.info("Thing Configuration read failed");
        }
    }

    @Override
    public void initialize() {

        // observeStringResource("coap://localhost:5683/string1", CHANNEL_STRING1);

        readThingConfiguration();
        String temp = new String();

        // update channel states
        for (Channel channel : getThing().getChannels()) {
            // updateChannelState(channel.getUID());
            // temp = channel.getLabel(); // Led1
            // temp = channel.getUID().getId();// led1
            // temp = channel.getAcceptedItemType();// Switch
            // channel.get
            observeResource(thingUri + channel.getUID().getId(), channel.getUID().getId(),
                    channel.getAcceptedItemType());
        }

        // observeStringResource("coap://[2001:db8::225:19ff:fe64:c216]:5683/lights/led3", CHANNEL_STRING1);

        // CoAPClient
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private CoapObserveRelation observeResource(String uriString, String channel, String itemType) {
        URI uri = null; // URI parameter of the request

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            logger.debug("Invalid URI: " + e.getMessage());
        }

        CoapClient client = new CoapClient(uri);
        logger.debug("Observing resource:" + client.getURI());
        CoapObserveRelation relation = client.observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                logger.debug("Notification from: " + client.getURI());
                logger.debug(Utils.prettyPrint(response));
                String content = response.getResponseText();
                if (itemType.contentEquals("String")) {
                    logger.info("String resource: " + client.getURI() + " changed to: " + content);
                    updateState(channel, StringType.valueOf(content));
                } else if (itemType.contentEquals("Switch")) {
                    logger.info("Switch resource: " + client.getURI() + " changed to: " + content);
                    updateState(channel, OnOffType.valueOf(content));
                }

            }

            @Override
            public void onError() {
                logger.info("OBSERVING FAILED");
            }
        });
        return relation;
    }

    private void putStringResource(String uriString, String channel, String value) {
        URI uri = null; // URI parameter of the request

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            logger.debug("Invalid URI: " + e.getMessage());
        }

        CoapClient client = new CoapClient(uri);
        logger.debug("PUT " + "'" + value + "' FROM Channel " + "'" + channel + "'" + " To URI: " + "'"
                + client.getURI() + "'");
        CoapResponse response = client.put(value, MediaTypeRegistry.TEXT_PLAIN);

        if (response != null) {

            logger.debug(response.getResponseText());

            logger.debug("\nADVANCED\n");
            // access advanced API with access to more details through .advanced()
            logger.debug(Utils.prettyPrint(response));

        } else {
            logger.debug("No response received.");
        }

    }
}
