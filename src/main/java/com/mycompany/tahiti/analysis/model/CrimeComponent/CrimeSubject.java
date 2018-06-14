package com.mycompany.tahiti.analysis.model.CrimeComponent;



import java.util.List;

public class CrimeSubject extends PersonFromDu {
    List<String> cym;//曾用名
    Boolean isDeaf;//是否是聋哑人
    String resume;//简历
    String familyInformation;//家庭情况
    List<Family> family;//家庭成员
    String criminalRecord;//前科
    Boolean isNpcOrCPPCC;//是否是人大代表或政协委员
    Boolean isSick;//是否有疾病

    String hujiCertificate;//户籍证明
    String archives;//学籍档案
    String birthCertificate;//出生证明
    String writtenJudgement;//判决书
    String ruling;//裁定书
    String releaseCertificate;//释放证明
    String psychiatricAppraisal;//精神病鉴定书
    String clinicalHistory;//病历
    String disabledCertificate;//残疾证


    class Family {
        String name;
        PersonFromDu.IdCode Id;
        FamilyRelation relationShip;

        @Override
        public String toString() {
            return " family name: "+name+"family Id: "+Id+" relationShip: "+relationShip;
        }
    }

    enum FamilyRelation {
        丈夫,
        妻子,
        父亲,
        母亲,
        儿子,
        女儿,
        爷爷,
        奶奶,
        外公,
        外婆,
        兄弟,
        姐妹,
        普通亲属
    }

    public String toString(){
        return super.toString()+" cym: "+cym+" isDeaf: "+isDeaf+" resume: "+resume+" familyInformation: "+
                familyInformation + "family: "+family+" criminalRecord: "+criminalRecord+" isNpcOrCPPCC: "+isNpcOrCPPCC+
                "isSick: "+isSick+" hujiCertificate: "+hujiCertificate+" archives: "+archives+" birthCertificate: "+
                birthCertificate+" writtenJudgement:"+writtenJudgement+" ruling: "+ruling+" releaseCertificate: "+releaseCertificate+
                " psychiatricAppraisal: "+psychiatricAppraisal+ " clinicalHistory: "+clinicalHistory+" disabledCertificate: "+disabledCertificate;
    }
}
