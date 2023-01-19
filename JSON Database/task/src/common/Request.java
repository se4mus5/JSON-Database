package common;

import java.io.Serializable;

public class Request implements Serializable {
    final RequestType requestType;
    int cellId;
    String payload;

    public Request(RequestType requestType) {
        this.requestType = requestType;
    }

    public Request(RequestType requestType, int cellId) {
        this.requestType = requestType;
        this.cellId = cellId;
    }

    public Request(RequestType requestType, int cellId, String payload) {
        this.requestType = requestType;
        this.cellId = cellId;
        this.payload = payload;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public int getCellId() {
        return cellId;
    }

    public String getPayload() {
        return payload == null ? "" : payload;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(requestType.name().toLowerCase());
        if (cellId != 0) // will only be 0 if uninitialized
            sb.append(" ").append(cellId);
        if (payload != null)
            sb.append(" ").append(payload);

        return sb.toString();
    }
}
