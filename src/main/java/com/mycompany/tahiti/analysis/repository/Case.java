package com.mycompany.tahiti.analysis.repository;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Case {

    public void SetBy(CaseBaseInfo simpleCase)
    {
        setSubjectId(simpleCase.getSubjectId());
        setCaseId(simpleCase.getCaseId());
        setCaseName(simpleCase.getCaseName());
        setCaseType(simpleCase.getCaseType());
    }

    private String subjectId;
    private String caseId;
    private String caseName;
    private String caseType;
    private List<Bilu> bilus = new ArrayList<>();

    //key is subjectid, value is connection type;
    private Map<String, String> connections = new HashMap<>();
}
