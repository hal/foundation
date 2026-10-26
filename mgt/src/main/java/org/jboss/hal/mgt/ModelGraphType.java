package org.jboss.hal.mgt;

public enum ModelGraphType {

    RESOURCE("resource"),
    ATTRIBUTE("attribute"),
    OPERATION("operation"),
    CAPABILITY("capability");

    public final String type;

    ModelGraphType(String type) {
        this.type = type;
    }

    public static ModelGraphType of(String type) {
        for (ModelGraphType resultType : values()) {
            if (resultType.type.equals(type)) {
                return resultType;
            }
        }
        throw new IllegalArgumentException("Unknown search result type: " + type);
    }
}
