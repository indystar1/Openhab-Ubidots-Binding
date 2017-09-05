package org.openhab.binding.ubidots;

import java.util.HashMap;
import java.util.Map;

class ApiObject {

    private static Map<String, Object> raw;
    protected ApiClient api;
    protected static ServerBridge bridge;

    ApiObject(Map<String, Object> raw, ApiClient api) {
        ApiObject.raw = new HashMap<String, Object>(raw);
        this.api = api;
        bridge = api.getServerBridge();
    }

    ApiObject(Map<String, Object> raw) {
        ApiObject.raw = new HashMap<String, Object>(raw);
    }

    static String getAttributeString(String name) {
        return (String) raw.get(name);
    }

    Double getAttributeDouble(String name) {
        return (Double) raw.get(name);
    }

    protected Object getAttribute(String name) {
        return raw.get(name);
    }

    public String getId() {
        return (String) raw.get("id");
    }
}
