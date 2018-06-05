package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.CaseBaseInfo;
import com.mycompany.tahiti.analysis.model.PersonRichInfo;
import com.mycompany.tahiti.analysis.repository.Bilu;
import com.mycompany.tahiti.analysis.repository.Case;
import com.mycompany.tahiti.analysis.repository.DataFactory;
import com.mycompany.tahiti.analysis.repository.Person;
import io.swagger.annotations.Api;
import lombok.val;
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
    public PersonRichInfo getBiluById(@RequestParam("subjectId") String subjectId) {
        PersonRichInfo personRichInfo = new PersonRichInfo();
        Person person = dataFactory.getPersons().get(subjectId);
        personRichInfo.setName(person.getName());
        personRichInfo.setBirthDay(person.getBirthDay());
        personRichInfo.setGender(person.getGender());
        personRichInfo.setIdentity(person.getIdentity());
        personRichInfo.setPhone(person.getPhone());
        List<PersonRichInfo.InvolvedCaseWithRole> involvedCases = new ArrayList<>();
        List<PersonRichInfo.SameCasePerson> sameCasePersonList = new ArrayList<>();
        personRichInfo.setInvolvedCases(involvedCases);
        personRichInfo.setSameCasePersonList(sameCasePersonList);

        for(Case aCase:person.getCaseList()){
            PersonRichInfo.InvolvedCaseWithRole involvedCaseWithRole = personRichInfo.new InvolvedCaseWithRole();
            involvedCaseWithRole.setCaseId(aCase.getCaseId());
            involvedCaseWithRole.setCaseName(aCase.getCaseName());
            involvedCaseWithRole.setCaseType(aCase.getCaseType());
            involvedCaseWithRole.setBiluNumber(aCase.getBilus().size());

            for(Bilu bilu:person.getBiluList()) {
                for (val connection : bilu.getConnections().keySet()) {
                    if (bilu.getConnections().get(connection).contains("嫌疑人")) {
                        involvedCaseWithRole.setRole("嫌疑人");
                    } else if (bilu.getConnections().get(connection).contains("证人")) {
                        involvedCaseWithRole.setRole("证人");
                    } else if (bilu.getConnections().get(connection).contains("受害人")) {
                        involvedCaseWithRole.setRole("受害人");
                    }
                }
            }
            involvedCases.add(involvedCaseWithRole);

            //Contains用法 caseId不同
            for(Bilu bilu:aCase.getBilus()){
                for(Person personInBilu:bilu.getPersons()){
                    if((personInBilu.getIdentity()!=null && !personInBilu.getIdentity().isEmpty())||(personInBilu.getName()!=null&&!personInBilu.getName().isEmpty())){
                        PersonRichInfo.SameCasePerson sameCasePerson = personRichInfo.new SameCasePerson();
                        sameCasePerson.setName(personInBilu.getName());
                        sameCasePerson.setIdentity(personInBilu.getIdentity());

                        if(!sameCasePersonList.contains(sameCasePerson)){
                            sameCasePerson.setBirthDay(personInBilu.getBirthDay());
                            sameCasePerson.setGender(personInBilu.getGender());
                            sameCasePerson.setPhone(personInBilu.getPhone());
                            sameCasePerson.setSameCasesCount(1);
                            List<CaseBaseInfo> sameCases = new ArrayList<>();
                            CaseBaseInfo caseBaseInfo = new CaseBaseInfo();
                            caseBaseInfo.setCaseName(aCase.getCaseName());
                            caseBaseInfo.setCaseId(aCase.getCaseId());
                            sameCases.add(caseBaseInfo);
                            sameCasePerson.setSameCases(sameCases);
                            sameCasePersonList.add(sameCasePerson);
                        }else {
                            for(PersonRichInfo.SameCasePerson item:sameCasePersonList){
                                if(item.getIdentity().equals(sameCasePerson.getIdentity())||item.getName().equals(sameCasePerson.getName())){
                                    CaseBaseInfo caseBaseInfo = new CaseBaseInfo();
                                    caseBaseInfo.setCaseName(aCase.getCaseName());
                                    caseBaseInfo.setCaseId(aCase.getCaseId());
                                    if(!item.getSameCases().contains(caseBaseInfo)){
                                        item.setSameCasesCount(item.getSameCasesCount()+1);
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
        return personRichInfo;
    }
}
