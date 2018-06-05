package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.PersonRichInfo;
import com.mycompany.tahiti.analysis.repository.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/person")
@Api(description = "Person controller")
public class PersonController {
    @Autowired
    DataFactory dataFactory;

    @ResponseBody
    @GetMapping("/{subjectId}")
    public PersonRichInfo getPersonDetail(@RequestParam("subjectId") String subjectId,@RequestParam("minSameCaseNum") Integer minSameCaseNum) {
        PersonRichInfo personRichInfo = new PersonRichInfo();
        Person person = dataFactory.getPersonById(subjectId);
        personRichInfo.setSubjectId(subjectId);
        personRichInfo.setName(person.getName());
        personRichInfo.setBirthDay(person.getBirthDay());
        personRichInfo.setGender(person.getGender());
        personRichInfo.setIdentity(person.getIdentity());
        personRichInfo.setPhone(person.getPhone());
        List<PersonRichInfo.InvolvedCaseWithRole> involvedCases = new ArrayList<>();
        List<PersonRichInfo.SameCasePerson> sameCasePersonListFinal = new ArrayList<>();
        List<PersonRichInfo.SameCasePerson> sameCasePersonList = new ArrayList<>();
        personRichInfo.setInvolvedCases(involvedCases);
        personRichInfo.setSameCasePersonList(sameCasePersonListFinal);

        for(String caseId:person.getCaseList()){
            Case aCase = dataFactory.getCaseById(caseId);
            PersonRichInfo.InvolvedCaseWithRole involvedCaseWithRole = personRichInfo.new InvolvedCaseWithRole();
            involvedCaseWithRole.setCaseId(aCase.getCaseId());
            involvedCaseWithRole.setCaseName(aCase.getCaseName());
            involvedCaseWithRole.setCaseType(aCase.getCaseType());
            involvedCaseWithRole.setBiluNumber(aCase.getBilus().size());
            involvedCaseWithRole.setRole(aCase.getConnections().get(subjectId));
            involvedCases.add(involvedCaseWithRole);

            for(Bilu bilu:aCase.getBilus()){
                for(Person personInBilu:bilu.getPersons()){
                    if((personInBilu.getIdentity()!=null && !personInBilu.getIdentity().isEmpty() && !personInBilu.getIdentity().equals(personRichInfo.getIdentity()))||(personInBilu.getName()!=null&&!personInBilu.getName().isEmpty()&& !personInBilu.getName().equals(personRichInfo.getName()))){
                        PersonRichInfo.SameCasePerson sameCasePerson = personRichInfo.new SameCasePerson();
                        sameCasePerson.setName(personInBilu.getName());
                        sameCasePerson.setIdentity(personInBilu.getIdentity());
                        if(!sameCasePersonList.contains(sameCasePerson)){
                            sameCasePerson.setBirthDay(personInBilu.getBirthDay());
                            sameCasePerson.setGender(personInBilu.getGender());
                            sameCasePerson.setPhone(personInBilu.getPhone());
                            List<CaseBaseInfo> sameCases = new ArrayList<>();
                            CaseBaseInfo caseBaseInfo = new CaseBaseInfo();
                            caseBaseInfo.setCaseName(aCase.getCaseName());
                            caseBaseInfo.setCaseId(aCase.getCaseId());
                            sameCases.add(caseBaseInfo);
                            sameCasePerson.setSameCases(sameCases);
                            sameCasePersonList.add(sameCasePerson);
                        }else {
                            for(PersonRichInfo.SameCasePerson item:sameCasePersonList){
                                if(item.getIdentity()!=null&&sameCasePerson.getIdentity()!=null&&item.getIdentity().equals(sameCasePerson.getIdentity())||item.getName()!=null&&sameCasePerson.getName()!=null&&item.getName().equals(sameCasePerson.getName())){
                                    CaseBaseInfo caseBaseInfo = new CaseBaseInfo();
                                    caseBaseInfo.setCaseName(aCase.getCaseName());
                                    caseBaseInfo.setCaseId(aCase.getCaseId());
                                    if(!item.getSameCases().contains(caseBaseInfo)){
                                        item.getSameCases().add(caseBaseInfo);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if(minSameCaseNum==null)minSameCaseNum = 2;
        for(PersonRichInfo.SameCasePerson sameCasePerson: sameCasePersonList){
            if(sameCasePerson.getSameCases().size()>=minSameCaseNum)sameCasePersonListFinal.add(sameCasePerson);
        }
        return personRichInfo;
    }
}
