package common;

import com.google.gson.Gson;

public class Request {
    final RequestType type;
    String key;
    String value;

    public Request(RequestType type) {
        this.type = type;
    }

    public Request(RequestType type, String key) {
        this.type = type;
        this.key = key;
    }

    public Request(RequestType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public RequestType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value == null ? "" : value;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "Request{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
