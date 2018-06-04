package com.mycompany.tahiti.analysis.Repository;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Case {
    private String subjectId;
    private String caseId;
    private String caseName;
    private String caseType;
    private List<Bilu> bilus = new ArrayList<>();
}
