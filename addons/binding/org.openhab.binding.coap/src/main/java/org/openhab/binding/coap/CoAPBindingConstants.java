/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link CoAPBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Kessel - Initial contribution
 */
public class CoAPBindingConstants {

    public static final String BINDING_ID = "coap";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "coapdevice");

    // List of all Channel ids
    public final static String CHANNEL_LED1 = "led1";
    public final static String CHANNEL_LED2 = "led2";
    public final static String CHANNEL_STRING1 = "string1";
}
