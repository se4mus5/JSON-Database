package common;

import java.io.Serializable;

public class Response implements Serializable {
    ResponseStatus responseStatus;
    String payload;

    public Response(ResponseStatus responseStatus, String payload) {
        this.responseStatus = responseStatus;
        this.payload = payload;
    }

    public Response(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Response(String payload) { this.payload = payload; }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public String getPayload() {
        return payload == null ? "" : payload;
    }

    @Override
    public String toString() {
        return responseStatus == null ? payload : responseStatus.name();
    }
}
