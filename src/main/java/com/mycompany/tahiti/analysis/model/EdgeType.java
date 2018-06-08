package com.mycompany.tahiti.analysis.model;

public enum EdgeType {

    GuanlianRen("关联人"),
    GuanlianAnjian("关联案件"),
    Gongan("共案");

    private final String type;
    EdgeType(String type) {
        if(type != null)
            this.type = type.toLowerCase();
        else this.type = "valid";
    }

    public String toString() {
        return this.type;
    }

    public static EdgeType fromString(String type) {
        try {
            return valueOf(type);
        } catch (Exception var6) {
            return null;
        }
    }
}