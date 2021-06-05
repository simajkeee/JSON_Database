package common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;

public class JSONParser {
    private JsonElement jelement;
    protected JsonObject jobject;
    protected JsonPrimitive jprimitive;


    public JSONParser(String jsonString) {
        jelement = JsonParser.parseString(jsonString);
        init();
    }

    public JSONParser(JsonElement jsonEl) {
        jelement = jsonEl;
        init();
    }

    public void init() {
        if (jelement.isJsonObject()) {
            jobject = jelement.getAsJsonObject();
        } else if (jelement.isJsonPrimitive()) {
            jprimitive = jelement.getAsJsonPrimitive();
        }
    }

    public JsonObject getJobject() {
        return jobject;
    }

    public String toString() {
        return jobject.toString();
    }

    public String getStringByKey(String key) {
        JsonElement el = jobject.get(key);
        if (el == null) {
            return "";
        }
        if (el.isJsonPrimitive()) {
            return el.getAsString();
        } else if (el.isJsonObject() || el.isJsonArray()) {
            return el.toString();
        } else {
            return "";
        }
    }

    public static JsonElement getMultiLevelJson(String[] keys) {
        JsonObject current = new JsonObject();
        if (keys.length == 1) {
            current.add(keys[0], null);
            return current;
        }
        current.add(keys[0], getMultiLevelJson(Arrays.copyOfRange(keys, 1, keys.length)));
        return current;
    }

    public void setInDepthValue(String[] keys, String val) {
        JsonElement el = getInDepthValue(keys);
        el = el.isJsonPrimitive() ? getInDepthValue(Arrays.copyOfRange(keys, 0, keys.length - 1)) : el;
        if (null == el) {
            JsonElement newEl = null;
            JsonElement nextEl = jobject;
            for (int i = 0; i < keys.length; i++) {
                if (nextEl.getAsJsonObject().get(keys[i]) == null) {
                    newEl = getMultiLevelJson(Arrays.copyOfRange(keys, i+1, keys.length));
                    jobject.add(keys[i], newEl);
                    el = getInDepthValue(keys);
                    break;
                }
                nextEl = nextEl.getAsJsonObject().get(keys[i]);
            }
        }
        JsonObject inDepthEl = el.getAsJsonObject();
        String keyToSetVal = keys[keys.length - 1];
        if (inDepthEl.has(keyToSetVal)) {
            inDepthEl.add(keyToSetVal, new JsonPrimitive(val));
        }
    }



    public JsonElement getInDepthValue(String[] keys) {
        JsonObject next = this.jobject;
        for (String k : keys) {
            JsonElement el = next.get(k);
            if (null == el) {
                return null;
            }

            if (el.isJsonNull()) {
                break;
            }

            if (el.isJsonPrimitive()) {
                return el;
            }

            next = el.getAsJsonObject();
        }
        return next;
    }

    public boolean deleteInDepthValue(String[] keys) {
        if (keys.length == 0) {
            return false;
        }
        String keyToDelete = keys[keys.length - 1];
        if (keys.length == 1) {
            return removeJsonObjectKey(jobject, keyToDelete);
        }

        JsonElement elToChek = getInDepthValue(Arrays.copyOfRange(keys, 0, keys.length - 1));
        return (elToChek == null) ? false : removeJsonObjectKey(elToChek, keyToDelete);
    }

    public boolean removeJsonObjectKey(JsonElement elToChek, String keyToDelete) {
        if (elToChek.isJsonPrimitive()) {
            return false;
        }
        JsonObject el = elToChek.getAsJsonObject();
        if (!el.has(keyToDelete)) {
            return false;
        }
        el.remove(keyToDelete);
        return true;
    }
}
