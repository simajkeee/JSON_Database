package common;

import com.google.gson.JsonElement;

public class RequestParser extends JSONParser {
    public RequestParser(String jsonString) {
        super(jsonString);
    }

    public Request getRequest() {
        String type = getStringByKey("type");
        String key = getStringByKey("key");
        String value = getStringByKey("value");

        Request r = new Request(type);
        if (!key.equals("")) {
            r.setKey(key);
        }

        if (!value.equals("")) {
            r.setValue(value);
        }
        return r;
    }
}
