package com.mycompany.tahiti.analysis.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CaseRichInfo extends CaseBaseInfo {
    private List<String> names;
    private List<String> phones;
    private List<String> identities;
    private List<String> bankCards;

    private List<Person> detailedPersons = new ArrayList<>();
    private List<BiluBaseInfo> bilus = new ArrayList<>();

    private Graph graph;
}
