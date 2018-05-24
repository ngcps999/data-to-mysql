package com.mycompany.tahiti.analysis.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Bilu {
    private String id;
    private String name;
    private String content;
    private List<String> tags = new ArrayList<>();
    private List<Person> persons = new ArrayList<>();
}
