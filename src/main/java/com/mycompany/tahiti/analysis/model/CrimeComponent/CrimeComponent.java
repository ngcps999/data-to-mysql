package com.mycompany.tahiti.analysis.model.CrimeComponent;

import lombok.Data;

@Data
public class CrimeComponent {
    CrimeSubject crimeSubject;
    CrimeObject crimeObject;
    CrimeSubjectiveAspect crimeSubjectiveAspect;
    CrimeObjectiveAspect crimeObjectiveAspect;
    CapacityResponsibility capacityResponsibility;
    JointCrime jointCrime;

    public String toString(){
        return "\ncrimeSubject: "+crimeSubject+" \ncrimeObject: "+crimeObject+" \ncrimeSubjectiveAspect: "+crimeSubjectiveAspect+
                "\ncrimeObjectiveAspect: "+crimeObjectiveAspect+" \ncapacityResponsibility: "+capacityResponsibility +
                "\njointCrime: "+jointCrime;
    }
}
