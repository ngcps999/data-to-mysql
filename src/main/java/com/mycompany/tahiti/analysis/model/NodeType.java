package com.mycompany.tahiti.analysis.model;

public enum NodeType {

    //drugs
    Case("案件"),
    Person("人"),
    Identity("身份证");

    private final String type;
    NodeType(String type) {
        if(type != null)
            this.type = type.toLowerCase();
        else this.type = "valid";
    }

    public String toString() {
        return this.type;
    }

    public static NodeType fromString(String type) {
        try {
            return valueOf(type);
        } catch (Exception var6) {
            return null;
        }
    }
}
