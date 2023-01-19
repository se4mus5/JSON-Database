package common;

import com.google.gson.Gson;

public class Response {
    ResponseType response;
    String value;
    String reason;

    public Response(ResponseType response, String reason) {
        this.response = response;
        this.reason = reason;
    }

    /**
     * Constructor to set the value instance parameter upon creation. This is totally a hack: only needed in one use case
     * of the app. Very likely a redesign candidate as future stage requirements evolve.
     * @param response Response type enum
     * @param value value returned to client
     * @param valueIndicator assign any int to indicate that the String value parameter will be assigned to the value instance variable.
     */
    public Response(ResponseType response, String value, int valueIndicator) {
        this.response = response;
        this.value = value;
    }

    public Response(ResponseType response) {
        this.response = response;
    }

    public Response(String value) { this.value = value; }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
