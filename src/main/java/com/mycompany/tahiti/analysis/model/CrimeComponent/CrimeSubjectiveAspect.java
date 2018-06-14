package com.mycompany.tahiti.analysis.model.CrimeComponent;

import lombok.Data;

import java.util.List;

@Data
public class CrimeSubjectiveAspect {
    List<String> motivation;
    public String toString(){
        return " motivation: "+motivation;
    }
}
