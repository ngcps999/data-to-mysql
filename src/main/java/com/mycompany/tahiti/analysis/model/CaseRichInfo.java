package com.mycompany.tahiti.analysis.model;

import com.mycompany.tahiti.analysis.repository.CaseBaseInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CaseRichInfo extends CaseBaseInfo {
    // subjectId, value
    private Map<String, ValueObject> names = new HashMap<>();
    private Map<String, ValueObject> phones = new HashMap<>();
    private Map<String, ValueObject> identities = new HashMap<>();
    private Map<String, ValueObject> bankCards = new HashMap<>();

    private List<PersonModel> detailedPersons = new ArrayList<>();
    private List<BiluBaseInfo> bilus = new ArrayList<>();

    private Graph graph = new Graph();
}
