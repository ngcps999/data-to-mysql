package com.mycompany.tahiti.analysis.model;

public enum EntityType {
    Bilu("bilu"),
    Case("case"),
    Person("person"),
    Car("car"),
    Location("location");

    private final String type;
    EntityType(String type) {
        if(type != null)
            this.type = type.toLowerCase();
        else this.type = "valid";
    }

    public boolean equalsName(String otherType) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return type.equals(otherType);
    }

    public String toString() {
        return this.type;
    }

    public static EntityType fromString(String type) {
        for (EntityType s : EntityType.values()) {
            if (s.type.equalsIgnoreCase(type)) {
                return s;
            }
        }
        return null;
    }
    public String getValue() { return type; }
}
