package com.mycompany.tahiti.analysis.model;


import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Data
public class Node {
    @NonNull
    private String id;

    private List<String> labels;

    private List<String> sourceTags;

    private Map<String, Object> properties;

}