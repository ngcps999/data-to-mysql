package com.mycompany.tahiti.analysis.model;

import lombok.Data;

import java.util.List;

@Data
public class CaseBaseInfo {
    private String caseId;
    private String caseName;
    private String caseType;
    private List<String> suspects;
    private Integer biluNumber;
}
