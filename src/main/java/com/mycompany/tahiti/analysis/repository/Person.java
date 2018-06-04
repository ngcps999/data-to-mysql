package com.mycompany.tahiti.analysis.repository;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Person {
    private String subjectId;
    private String name;
    private String gender;
    private String phone;
    private String birthDay;
    private String identity;

    private List<Case> caseList = new ArrayList<>();
    private List<Bilu> biluList = new ArrayList<>();
}
