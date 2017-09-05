/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ubidots;

import static org.openhab.binding.ubidots.UbidotsBindingConstants.THING_TYPE_UBI_COMM;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UbidotsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Indy Star - Initial contribution
 */
public class UbidotsHandlerFactory extends BaseThingHandlerFactory implements EventSubscriber {

    private final static Logger LOG = LoggerFactory.getLogger(UbidotsHandlerFactory.class);
    protected static ItemRegistry itemRegistry = null;
    protected static EventPublisher eventPublisher = null;
    private boolean update_flag;
    static double homeTemperature = 0;
    static double old_homeTemperature = 0;
    static double homeHumidity = 0;
    static double old_homeHumidity = 0;
    static double baseTemperature = 0;
    static double old_baseTemperature = 0;
    static double baseHumidity = 0;
    static double old_baseHumidity = 0;
    static String old_equipStatus = "";
    static int hvacOnOffInteger = 0;
    static int FIR1_Alert = 0;
    static int old_FIR1_Alert = 0;
    static int FIR2_Alert = 0;
    static int old_FIR2_Alert = 0;

    private static final int POLLING_INTERVAL_SECONDS = 10; // added for periodic invocation of Ubidots GET control
                                                            // variables
    private BigDecimal refresh = new BigDecimal(POLLING_INTERVAL_SECONDS);
    ScheduledFuture<?> refreshJob;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static Boolean serverUp = false;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_UBI_COMM);

    public void UbidotsHandlerFactory() {
        // super(thing);
        LOG.info("Running the constructor of UbidotsHandlerFactory!");
        try {
            serverUp = false;
            periodicGet();
            serverUp = true;
        } catch (InterruptedException e) {
            serverUp = false;
            System.err.println("Ubidots server unavailable or connection error: " + e.getMessage());
            LOG.error("Ubidots server unavailable or connection error: ", e);
        }
    }

    private void periodicGet() throws InterruptedException {
        Runnable runnable = new Runnable() {
            // This is called every refresh interval
            @Override
            public void run() {
                if (serverUp == false) {
                    new UbidotsHandlerFactory();
                    // System.err.println("Ubidots server unavailable or connection error");
                } else {
                    DataSource.getOpenhabVariables();
                }
            }
        };
        // Creating a refreshing job for getting control variables
        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_UBI_COMM)) {
            LOG.info("A New Ubidots handler Created!");
            return new UbidotsHandler(thing);
        }

        return null;
    }

    public void setItemRegistry(ItemRegistry itemRegistry) {
        UbidotsHandlerFactory.itemRegistry = itemRegistry;
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        UbidotsHandlerFactory.itemRegistry = null;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        UbidotsHandlerFactory.eventPublisher = eventPublisher;
    }

    public void unsetEventPublisher(EventPublisher eventPublisher) {
        UbidotsHandlerFactory.eventPublisher = null;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Collections.singleton(ItemStateEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    public static void sendCommand(String itemName, String commandString) {
        try {
            if (itemRegistry != null) {
                Item item = itemRegistry.getItem(itemName);
                Command command = null;
                if (item != null) {
                    if (eventPublisher != null) {
                        if ("toggle".equalsIgnoreCase(commandString)
                                && (item instanceof SwitchItem || item instanceof RollershutterItem)) {
                            if (OnOffType.ON.equals(item.getStateAs(OnOffType.class))) {
                                command = OnOffType.OFF;
                            }
                            if (OnOffType.OFF.equals(item.getStateAs(OnOffType.class))) {
                                command = OnOffType.ON;
                            }
                            if (UpDownType.UP.equals(item.getStateAs(UpDownType.class))) {
                                command = UpDownType.DOWN;
                            }
                            if (UpDownType.DOWN.equals(item.getStateAs(UpDownType.class))) {
                                command = UpDownType.UP;
                            }
                        } else {
                            command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), commandString);
                        }
                        if (command != null) {
                            System.err.println(
                                    "In Ubidots, posting a command " + commandString + " for item " + itemName);
                            LOG.info("In Ubidots, posting a command '{}' for item '{}'", commandString, itemName);
                            eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command));
                        } else {
                            LOG.warn("In Ubidots, Received an invalid command '{}' for item '{}'", commandString,
                                    itemName);
                        }
                    }
                } else {
                    LOG.warn("In Ubidots, Received a command '{}' for non-existent item '{}'", commandString, itemName);
                }
            } else {
                return;
            }
        } catch (ItemNotFoundException e) {
            LOG.warn("In Ubidots, received a command for a non-existent item '{}'", itemName);
        }
    }

    @Override
    public void receive(Event event) {
        ItemStateEvent ise = (ItemStateEvent) event;
        String itemName = ise.getItemName();
        String itemState = ise.getItemState().toString();

        Map<String, Object> map = new HashMap<String, Object>();

        // Although I coded the way multiple variables can be updated,
        // because this method is invoked at each event,
        // always a single variable is updated at a time.

        update_flag = false;
        if (itemName.equals("HomeTemperature")) {
            homeTemperature = Double.parseDouble(itemState);
            if (homeTemperature != old_homeTemperature) {
                update_flag = true;
                old_homeTemperature = homeTemperature;
                map.put("HomeTemperature", homeTemperature);
            }
        } else if (itemName.equals("HomeHumidity")) {
            homeHumidity = Double.parseDouble(itemState);
            if (homeHumidity != old_homeHumidity) {
                update_flag = true;
                old_homeHumidity = homeHumidity;
                map.put("HomeHumidity", homeHumidity);
            }
        }
        if (itemName.equals("BaseTemperature")) {
            baseTemperature = Double.parseDouble(itemState);
            if (baseTemperature != old_baseTemperature) {
                update_flag = true;
                old_baseTemperature = baseTemperature;
                map.put("BaseTemperature", baseTemperature);
            }
        } else if (itemName.equals("BaseHumidity")) {
            baseHumidity = Double.parseDouble(itemState);
            if (baseHumidity != old_baseHumidity) {
                update_flag = true;
                old_baseHumidity = baseHumidity;
                map.put("BaseHumidity", baseHumidity);
            }
        } else if (itemName.equals("YunAlive")) {
            map.put("YunAlive", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("DehumidStatus")) {
            map.put("DehumidStatus", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("IrriStatus")) {
            map.put("IrriStatus", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("FrontMotion")) {
            map.put("FrontMotion", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("Cam1Status")) {
            map.put("Cam1Status", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("Cam2Status")) {
            map.put("Cam2Status", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("Cam3Status")) {
            map.put("Cam3Status", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("equipmentStatus")) {
            if (!old_equipStatus.equals(itemState)) {
                update_flag = true;
                old_equipStatus = new String(itemState);
                if (old_equipStatus.equals("")) {
                    System.err.println("No HVAC equipment is running!");
                    LOG.info("No HVAC equipment is running!");
                    hvacOnOffInteger = 0;
                } else {
                    System.err.println("The following HVAC equipment is currently running: " + old_equipStatus);
                    LOG.info("The following HVAC equipment is currently running: " + old_equipStatus);
                    hvacOnOffInteger = 1;
                }
                map.put("HAVCStatus", hvacOnOffInteger);
            }
        } else if (itemName.equals("WaterSoftenerState")) {
            map.put("WaterSoftenerState", Integer.parseInt(itemState));
            update_flag = true;
        } else if (itemName.equals("FIR1Alert")) {
            FIR1_Alert = Integer.parseInt(itemState);
            if (FIR1_Alert != old_FIR1_Alert) {
                update_flag = true;
                old_FIR1_Alert = FIR1_Alert;
            }
        } else if (itemName.equals("FIR2Alert")) {
            FIR2_Alert = Integer.parseInt(itemState);
            if (FIR2_Alert != old_FIR2_Alert) {
                update_flag = true;
                old_FIR2_Alert = FIR2_Alert;
            }
        }

        if (update_flag == true) {
            try {
                if (DataSource.postOpenhabVariables(map)) {
                    LOG.info("Sending to Ubidots SUCCESSFUL^^");
                } else {
                    System.err.println("Sending to Ubidots FAILED :(");
                }
            } catch (Exception e) {
                System.err.println("Sending to Ubidots failed: " + e.getMessage());
                LOG.error("Sending to Ubidots failed: " + e.getMessage());
            }
            if (serverUp == false) {
                UbidotsHandlerFactory();
            }
        }
    }
}
