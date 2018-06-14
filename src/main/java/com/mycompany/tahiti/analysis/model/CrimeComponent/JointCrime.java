package com.mycompany.tahiti.analysis.model.CrimeComponent;

import java.util.List;

public class JointCrime {
    List<String> jointCrime;
    public String toString(){
        return " "+String.join(",",jointCrime);
    }
}
