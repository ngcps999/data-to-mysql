package com.mycompany.tahiti.analysis.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Graph {
    private List<Edge> relationships = new ArrayList<>();
    private List<Node> entities = new ArrayList<>();
}