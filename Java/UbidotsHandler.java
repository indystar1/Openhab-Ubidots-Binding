/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ubidots;

import static org.openhab.binding.ubidots.UbidotsBindingConstants.CHANNEL_1;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UbidotsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Indy Star - Initial contribution
 */
public class UbidotsHandler extends BaseThingHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UbidotsHandler.class);

    public UbidotsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_1)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
        String channelID = channelUID.getId().toString();
        String thingID = channelUID.getThingUID().toString();
        LOG.info("In Ubidots handleCommand! Channel Id= " + channelID + ", Thing Id= " + thingID + ", Command= "
                + command.toString());

        if (channelID.equals("gauge1")) {
            LOG.info("Received Command for gauge1 to turn " + command);
        }
    }

    public void receiveCommand(String itemName, Command command) {
        LOG.info("receiveCommand: UbiOnOff gauge1: Received a command!");
    }

    public void sendCommand(String itemName, Command command) {
        LOG.info("I am now in Ubidots sendCommand!");
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        LOG.debug("Initializing Ubidots handler!");
        super.initialize();

        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
