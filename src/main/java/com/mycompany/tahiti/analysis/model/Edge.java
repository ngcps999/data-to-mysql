package com.mycompany.tahiti.analysis.model;

import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Data
public class Edge {
    @NonNull
    private long id;

    @NonNull
    private String sourceId;

    @NonNull
    private String targetId;

    private EdgeType type;

    private String chiType;

    private Boolean hasDirection;

    private List<String> sourceTags;

    private Map<String, Object> properties;

}