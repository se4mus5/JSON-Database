package server.model;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private final Map<String, String> keyValuePairs;

    public Database() {
        keyValuePairs = new HashMap<>(1000);
    }

    public String get(String key) {
        return keyValuePairs.getOrDefault(key, "");
    }

    public boolean set(String key, String value) {
        keyValuePairs.put(key, value);
        return true;
    }

    public boolean delete(String key) {
        if (keyValuePairs.getOrDefault(key, "").equals("")) return false;
        keyValuePairs.remove(key);
        return true;
    }
}
