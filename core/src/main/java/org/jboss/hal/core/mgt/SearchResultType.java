package org.jboss.hal.core.mgt;

public enum SearchResultType {

    RESOURCE("Resource"),
    ATTRIBUTE("Attribute"),
    OPERATION("Operation"),
    CAPABILITY("Capability");

    public final String type;

    SearchResultType(String type) {
        this.type = type;
    }

    public static SearchResultType of(String type) {
        for (SearchResultType resultType : values()) {
            if (resultType.type.equals(type)) {
                return resultType;
            }
        }
        throw new IllegalArgumentException("Unknown search result type: " + type);
    }
}
