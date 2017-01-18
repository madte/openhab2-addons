/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coap.handler;

import static org.openhab.binding.coap.CoAPBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.coap.internal.client.CoapResource;
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

    private String thingUri;
    private String identity;
    private String secret;
    private Boolean dtlsEnabled = false;

    private List<CoapResource> coapResourceList = new ArrayList<CoapResource>();

    public CoAPHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Boolean commandExecuted = false;

        if (command.toFullString().contentEquals("REFRESH")) {
            return;
        }

        for (CoapResource resource : coapResourceList) {

            if (channelUID.getId().contentEquals(resource.getChannelId())) {
                logger.debug(
                        "handleCommand: On channel: " + channelUID.getId() + " command: " + command.toFullString());
                if (resource.writeResource(command.toFullString())) {
                    commandExecuted = true;
                    updateStatus(ThingStatus.ONLINE);
                    return;
                }
            }
        }
        if (!commandExecuted) {
            logger.debug("Channel " + channelUID.getId() + " has no CoapResource assigned!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

        }

    }

    public void handleCoapNotification(String _channelId, String _channelType, String _value) {

        if (_channelType.contentEquals("String")) {
            logger.info("String resource: " + _channelType + " changed to: " + _value);
            updateState(_channelId, StringType.valueOf(_value));
        } else if (_channelType.contentEquals("Switch")) {
            logger.info("Switch resource: " + _channelType + " changed to: " + _value);
            updateState(_channelId, OnOffType.valueOf(_value));
        }
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

    private Boolean readThingConfiguration() {
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

        if (conf.containsKey(CONFIG_IDENTITY_KEY)) {
            identity = conf.get(CONFIG_IDENTITY_KEY).toString();
        } else {
            validConfig = false;
        }

        if (conf.containsKey(CONFIG_SECRET_KEY)) {
            secret = conf.get(CONFIG_SECRET_KEY).toString();
        } else {
            validConfig = false;
        }

        if (validConfig) {
            logger.info("Thing Configuration read successful");
            return true;
        } else {
            logger.info("Thing Configuration read failed");
            return false;
        }
    }

    @Override
    public void initialize() {

        if (readThingConfiguration()) {
            // update channel states
            for (Channel channel : getThing().getChannels()) {
                // temp = channel.getLabel(); // Led1
                // temp = channel.getUID().getId();// led1
                // temp = channel.getAcceptedItemType();// Switch
                CoapResource newResource;

                if (dtlsEnabled) {
                    newResource = new CoapResource(thingUri, channel.getUID().getId(), channel.getAcceptedItemType(),
                            this, identity, secret);
                } else {
                    newResource = new CoapResource(thingUri, channel.getUID().getId(), channel.getAcceptedItemType(),
                            this);
                }

                newResource.observeResource();
                coapResourceList.add(newResource);
            }

            // observeStringResource("coap://[2001:db8::225:19ff:fe64:c216]:5683/lights/led3", CHANNEL_STRING1);

            // Long running initialization should be done asynchronously in background.
            updateStatus(ThingStatus.ONLINE);
        } else {
            // not properly initialized
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {

        // delete all coap resources
        coapResourceList.clear();

    }

}
