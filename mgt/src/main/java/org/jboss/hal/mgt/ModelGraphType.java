package org.jboss.hal.mgt;

public enum ModelGraphType {

    RESOURCE("resource", "Resource", "Resources"),
    ATTRIBUTE("attribute", "Attribute", "Attributes"),
    OPERATION("operation", "Operation", "Operations"),
    CAPABILITY("capability", "Capability", "Capabilities");

    public final String type;
    public final String singular;
    public final String plural;

    ModelGraphType(String type, String singular, String plural) {
        this.type = type;
        this.singular = singular;
        this.plural = plural;
    }

    public static ModelGraphType of(String type) {
        for (ModelGraphType resultType : values()) {
            if (resultType.type.equalsIgnoreCase(type)) {
                return resultType;
            }
        }
        return null;
    }
}
