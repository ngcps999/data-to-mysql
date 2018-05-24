package com.mycompany.tahiti.analysis.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Case {
    private String caseId;
    private String caseName;
    private String caseType;
    private List<String> suspects;
    private List<String> names;
    private List<String> phones;
    private List<String> identies;
    private Integer biluNumber;
    private List<Person> detailedPersons = new ArrayList<>();
    private List<Bilu> bilus = new ArrayList<>();
}
