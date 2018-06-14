package com.mycompany.tahiti.analysis.model;

import com.mycompany.tahiti.analysis.model.CrimeComponent.CrimeComponent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BiluRichInfo extends BiluBaseInfo {
    private String content;
    private CrimeComponent crimeComponent;
    private List<String> tags = new ArrayList<>();
//    private List<Person> persons = new ArrayList<>();
}