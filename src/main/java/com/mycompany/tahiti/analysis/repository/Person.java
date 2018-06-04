package com.mycompany.tahiti.analysis.repository;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Person {
    private String subjectId;
    private String name;
    private String gender;
    private String phone;
    private String birthDay;
    private String identity;

    // caseId
    private Set<String> caseList = new HashSet<>();

    // biluId
    private Set<String> biluList = new HashSet<>();
}
