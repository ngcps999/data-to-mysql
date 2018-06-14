package com.mycompany.tahiti.analysis.model.CrimeComponent;

import lombok.Data;

import java.util.List;

@Data
public class CrimeObject {
    List<String> crimeObject;

    public String toString(){
        return " "+String.join(",",crimeObject);
    }
}
