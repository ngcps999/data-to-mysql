package com.mycompany.tahiti.analysis.repository;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CaseBaseInfo {
    private String subjectId;
    private String caseId;
    private String caseName;
    private String caseType;
    private List<String> suspects = new ArrayList<>();
    private Integer biluNumber;
}
