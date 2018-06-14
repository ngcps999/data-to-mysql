package com.mycompany.tahiti.analysis.model.CrimeComponent;

import lombok.Data;

import java.util.List;

@Data
public class CapacityResponsibility {
    List<Responsibility> responsibility;
    public enum Responsibility{
        未成年人,
        精神病,
        残疾人,
        聋哑人
    }
}
