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
import org.eclipse.smarthome.core.library.types.StringType;
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

    public CoAPHandler(Thing thing) {
        super(thing);
    }

    public CoapHandler observeHandler() {

        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getId().equals(CHANNEL_LED1)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");

            putStringResource("coap://vs0.inf.ethz.ch:5683/test", channelUID.getId(), command.toFullString());
        } else if (channelUID.getId().equals(CHANNEL_LED2)) {

        } else if (channelUID.getId().equals(CHANNEL_STRING1)) {

        }
    }

    @Override
    public void initialize() {

        observeStringResource("coap://vs0.inf.ethz.ch:5683/obs", CHANNEL_STRING1);

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

    private void observeStringResource(String uriString, String channel) {
        URI uri = null; // URI parameter of the request

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            logger.debug("Invalid URI: " + e.getMessage());
        }

        CoapClient client = new CoapClient(uri);
        logger.debug("Get URI:" + client.getURI());
        CoapObserveRelation relation = client.observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                logger.info("NOTIFICATION: " + content);
                updateState(channel, StringType.valueOf(content));
            }

            @Override
            public void onError() {
                logger.info("OBSERVING FAILED");
            }
        });
    }

    private void putStringResource(String uriString, String channel, String value) {
        URI uri = null; // URI parameter of the request

        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            logger.debug("Invalid URI: " + e.getMessage());
        }

        CoapClient client = new CoapClient(uri);
        logger.debug("PUT " + "'" + value + "' FROM Channel " + "'" + channel + "'" + " To URI: " + "'" + client.getURI() + "'");
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
