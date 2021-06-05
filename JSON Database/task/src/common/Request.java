package common;

public class Request {
    private String type;
    private String value;
    private String key;

    public Request(String type) {
        this.type = type;
    }

    public String getType() {
        return type.replaceAll("\"", "");
    }

    public String getValue() {
        return value.replaceAll("\"", "");
    }

    public String getKey() {
        return key.replaceAll("\"", "");
    }

    public String getOrigType() {
        return type;
    }

    public String getOrigValue() {
        return value;
    }

    public String getOrigKey() {
        return key;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isMultiLevel() {
        return this.getKey().trim().charAt(0) == '[';
    }

    public String[] getAllLevels() {
        if (isMultiLevel()) {
            String k = this.getKey().replaceAll("\\[", "").replaceAll("\\]", "");
            return k.split("\\W+");
        }
        return new String[]{this.getKey()};
    }
}
