package com.mycompany.tahiti.analysis.model;

public enum EdgeType {

    //drugs
    GRAPH_DRUG_SELL,
    GRAPH_DRUG_INTRODUCE,
    GRAPH_DRUG_COADDICT;

    EdgeType(){

    }

    public static EdgeType fromString(String name) {
        try {
            return valueOf(name);
        } catch (Exception var6) {
            return null;
        }
    }
}