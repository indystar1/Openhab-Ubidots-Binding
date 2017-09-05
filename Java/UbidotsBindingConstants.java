/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ubidots;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link UbidotsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Indy Star - Initial contribution
 */
public class UbidotsBindingConstants {

    private static final String BINDING_ID = "ubidots";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UBI_COMM = new ThingTypeUID(BINDING_ID, "ubi_comm"); // changed

    // List of all Channel ids
    public static final String CHANNEL_1 = "gauge1";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_UBI_COMM);
}
