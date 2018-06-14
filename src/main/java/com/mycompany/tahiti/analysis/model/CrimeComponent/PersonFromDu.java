package com.mycompany.tahiti.analysis.model.CrimeComponent;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class PersonFromDu {
    private IdCode id;

    private String name;
    private String alias;
    private Integer age;
    private Date birthDate;
    private Gender gender;

    private List<Contact> contact;
    private String education;
    private Job job;
    private String nation; //民族
    private MarriageStatus marriageStatus;

    private String xianzhudi; //现住地
    private String birthAddress; //出生地
    private String jiguanAddress; //籍贯所在地
    private String huJiAddress; //户籍地

    @Data
    public class IdCode {
        private IdType type;
        private String number;
    }

    @Data
    public class Contact {
        private ContactType type;
        private List<String> value = new ArrayList<>();
    }


    public enum Gender {
        Male,
        Female;

        public static Gender fromChiString(String name) {

            try {
                if (name.equals("男")) {
                    return Male;
                } else {
                    return Female;
                }
            } catch (Exception var6) {
                return null;
            }
        }
    }

    public enum ContactType {
        Phone;
    }

    public enum IdType {
        IdentityCard;
    }


    public enum MarriageStatus {
        Single,
        Married,
        Devorced,
    }

    @Data
    public class Job {
        private boolean hasJob;
        private String job;
    }

    public String toString(){
        return "id: "+id+" name: "+name+" alias: "+alias+" age: "+age+" birthDate: "+birthDate+
                " gender: "+gender+" contact: "+contact+" education: "+education+" job: "+job +
                " nation: "+nation+" marriageStatus: "+marriageStatus+" xianzhudi: "+xianzhudi+
                " birthAddress: "+birthAddress+" jiguanAddress: "+jiguanAddress+" huJiAddress: "+huJiAddress;
    }
}
