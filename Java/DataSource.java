package org.openhab.binding.ubidots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Data Source.
 *
 * @author Ubidots
 */
public class DataSource extends ApiObject {

    private final static Logger LOG = LoggerFactory.getLogger(UbidotsHandlerFactory.class);

    DataSource(Map<String, Object> raw, ApiClient api) {
        super(raw, api);
    }

    public String getName() {
        return getAttributeString("name");
    }

    public void remove() {
        bridge.delete("datasources/" + getAttributeString("id"));
    }

    static double cam3Control_old = 0, cam3Control = 0;
    static double cam2Control_old = 0, cam2Control = 0;
    static double sprinKler_old = 0, sprinKler = 0;

    // This method retrieves all 'control'-tagged variables at once and process
	// So whichever variable you use to control, you need to tag as "control"

    public static Variable[] getOpenhabVariables() {
        LOG.info("Executing DataSource.getOpenhabVariables and process");
        String json = bridge.get("datasources/your_24digit_device_ID/variables/?tag=control");
        // System.err.println("Returned json: " + json); // for debug
        Gson gson = new Gson();
        List<Map<String, Object>> rawVariables = gson.fromJson(json, List.class);

        Variable[] variables = new Variable[rawVariables.size()];
        Map<String, Object> obj;
        for (int i = 0; i < rawVariables.size(); i++) {
            variables[i] = new Variable(rawVariables.get(i), api);
            // System.err.print("The name of Variable[");
            // System.err.print(i);
            // System.err.println("] = " + variables[i].getName());
            switch (variables[i].getName()) {
                case "Cam2Control":
                    obj = variables[i].getObject("last_value");
                    // System.err.print("Current Cam2Control value is ");
                    cam2Control = (Double) obj.get("value");
                    // System.err.println(cam2Control);
                    if (cam2Control == cam2Control_old) {
                        break;
                    }
                    System.err.println("Cam2Control value changed to " + cam2Control);
                    if (cam2Control == 1.0) {
                        UbidotsHandlerFactory.sendCommand("Cam2OnOff", "ON");
                        System.err.println("sendCommand Cam2OnOff ON");
                    } else {
                        UbidotsHandlerFactory.sendCommand("Cam2OnOff", "OFF");
                        System.err.println("sendCommand Cam2OnOff OFF");
                    }
                    cam2Control_old = cam2Control;
                    break;
                case "Cam3Control":
                    obj = variables[i].getObject("last_value");
                    // System.err.print("Current Cam3Control value is ");
                    cam3Control = (Double) obj.get("value");
                    // System.err.println(cam3Control);
                    if (cam3Control == cam3Control_old) {
                        break;
                    }
                    System.err.println("Cam3Control value changed to " + cam3Control);
                    if (cam3Control == 1.0) {
                        UbidotsHandlerFactory.sendCommand("Cam3OnOff", "ON");
                        System.err.println("sendCommand Cam3OnOff ON");
                    } else {
                        UbidotsHandlerFactory.sendCommand("Cam3OnOff", "OFF");
                        System.err.println("sendCommand Cam3OnOff OFF");
                    }
                    cam3Control_old = cam3Control;
                    break;
                case "Sprinkler":
                    obj = variables[i].getObject("last_value");
                    // System.err.print("Current Sprinkler value is ");
                    sprinKler = (Double) obj.get("value");
                    // System.err.println(sprinKler);
                    if (sprinKler == sprinKler_old) {
                        break;
                    }
                    System.err.println("sprinKler value changed to " + sprinKler);
                    if (sprinKler == 1.0) {
                        UbidotsHandlerFactory.sendCommand("IrriCntl", "ON");
                        System.err.println("sendCommand IrriCntl ON");
                    } else {
                        UbidotsHandlerFactory.sendCommand("IrriCntl", "OFF");
                        System.err.println("sendCommand IrriCntl OFF");
                    }
                    sprinKler_old = sprinKler;
                    break;
                default:
                    break;
            }
        }

        return variables;
    }

    public Variable[] getVariables() {
        String json = bridge.get("datasources/" + getAttributeString("id") + "/variables");

        Gson gson = new Gson();
        List<Map<String, Object>> rawVariables = gson.fromJson(json, List.class);

        Variable[] variables = new Variable[rawVariables.size()];

        for (int i = 0; i < rawVariables.size(); i++) {
            variables[i] = new Variable(rawVariables.get(i), api);
        }

        return variables;
    }

    static ApiClient api;
    static DataSource dataSource;
    static Variable[] variables;
    static boolean run_once = false;

    // This method can send multiple (variable, value) pairs passed in the Map form

    public static boolean postOpenhabVariables(Map<String, Object> map) {
        if (run_once == false) {
            api = new ApiClient("your_Ubidots_API_Key_under_My_Profile");
            dataSource = api.getDataSource("your_24digit_device_ID"); // get my device name of Ubidots
            if (dataSource == null) {
                System.err.println("OpenHAB Ubidots device obtain FAILED!");
                return false;
            }
            System.err.println("OpenHAB Ubidots device obtained!");
            run_once = true;
        }

        Gson gson = new Gson();
        String json = gson.toJson(map);

        String responseStr = new String(bridge.post("devices/YourDeviceName/", json)); // sending
        System.err.println("Ubidots sending response: " + responseStr);
        if (responseStr.matches(".+20[0126].+")) { // if response is ok such as 200,201,202,206
            return true;
        } else {
            run_once = false; // not sent
            return false;
        }
    }

    public Variable createVariable(String name) {
        return createVariable(name, null, null, null, null);
    }

    public Variable createVariable(String name, String unit) {
        return createVariable(name, unit, null, null, null);
    }

    public Variable createVariable(String name, String unit, String description, Map<String, String> properties,
            String[] tags) {
        if (name == null) {
            throw new NullPointerException();
        }

        // Build data map
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", name);

        if (unit != null) {
            data.put("unit", unit);
        }

        if (description != null) {
            data.put("description", description);
        }

        if (properties != null) {
            data.put("properties", properties);
        }

        if (tags != null) {
            data.put("tags", tags);
        }

        Gson gson = new Gson();
        String json = bridge.post("datasources/" + getAttributeString("id") + "/variables", gson.toJson(data));

        Variable var = new Variable(gson.fromJson(json, Map.class), api);

        return var;
    }

}
