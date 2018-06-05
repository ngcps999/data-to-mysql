package com.mycompany.tahiti.analysis.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class ValueObject {
    @NonNull
    private String value;
}
