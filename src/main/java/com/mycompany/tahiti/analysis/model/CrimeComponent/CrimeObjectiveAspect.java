package com.mycompany.tahiti.analysis.model.CrimeComponent;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CrimeObjectiveAspect {
    List<String> date;
    CommitCrime commitCrime;
    List<String> performanceAfterCrime;//犯罪后的表现情况

    @Data
    public class CommitCrime{
        List<String> method;
        CrimeFeature crimeFeature;
        List<String> timing;//时机
        List<String> location;//处所
        List<String> bodypart;//部位
        List<String> tool;//作案工具

        public String toString(){
            return " method: "+String.join(",",method)+" crimeFeature: "+crimeFeature.toString()+
                    " timing: "+String.join(",",timing)+" location: "+String.join(",",location)+
                    " bodypart： "+bodypart+" tool： "+tool;
        }
    }

    @Data
    public class CrimeFeature{
        List<String> organizationForm;//组织形式
        List<String> collusionForm;//勾结形式

        public String toString(){
            return " organizationForm: "+String.join(",",organizationForm)+" collusionForm: "+collusionForm.toString();
        }
    }


}
