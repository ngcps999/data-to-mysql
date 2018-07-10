package com.mycompany.tahiti.analysis.repository;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BasicInfo {
    private String id;
    private String name;
    private String formerName;
    private String gender;
    private Integer age;
    private String maritalStatus;
    private String nativePlace;
    private String ethnicGroup;
    private LocalDate dateOfBirth;
    private String identityCard;
    private String bloodType;
    private String occupation;
    private String phone;
    private String address;
    private String height;
}
