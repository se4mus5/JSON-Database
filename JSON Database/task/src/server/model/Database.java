package server.model;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private final Map<String, String> keyValuePairs;

    public Database() {
        keyValuePairs = new HashMap<>(1000);
    }

    public synchronized String get(String key) {
        return keyValuePairs.getOrDefault(key, "");
    }

    public synchronized boolean set(String key, String value) {
        keyValuePairs.put(key, value);
        return true;
    }

    public synchronized boolean delete(String key) {
        if (keyValuePairs.getOrDefault(key, "").equals("")) return false;
        keyValuePairs.remove(key);
        return true;
    }
}
