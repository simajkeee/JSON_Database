package common;

import com.google.gson.JsonElement;

public class DbResponse {
    public final static String errMsg       = "ERROR";
    public final static String okMsg        = "OK";

    private String response;
    private String reason;
    private JsonElement value;

    public void setResponse(String response) {
        this.response = response;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setValue(JsonElement value) {
        this.value = value;
    }
}
