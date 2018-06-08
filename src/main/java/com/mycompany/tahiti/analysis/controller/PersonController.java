package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.PersonRichInfo;
import com.mycompany.tahiti.analysis.repository.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/person")
@Api(description = "Person controller")
public class PersonController {
    @Autowired
    DataFactory dataFactory;

    @ResponseBody
    @GetMapping("/details")
    public PersonRichInfo getPersonDetail(@RequestParam("subjectId") String subjectId, @RequestParam("minSameCaseNum") Integer minSameCaseNum) {
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
        List<PersonRichInfo.SameCaseEntity> sameCaseNameList = new ArrayList<>();
        personRichInfo.setInvolvedCases(involvedCases);
        personRichInfo.setSameCasePersonList(sameCasePersonListFinal);
        personRichInfo.setSameCasePersonNameList(sameCaseNameList);
        for (String caseSubjectId : person.getCaseList()) {
            Case aCase = dataFactory.getCaseById(caseSubjectId);
            PersonRichInfo.InvolvedCaseWithRole involvedCaseWithRole = personRichInfo.new InvolvedCaseWithRole();
            involvedCaseWithRole.setSubjectId(aCase.getSubjectId());
            involvedCaseWithRole.setCaseId(aCase.getCaseId());
            involvedCaseWithRole.setCaseName(aCase.getCaseName());
            involvedCaseWithRole.setCaseType(aCase.getCaseType());
            involvedCaseWithRole.setBiluNumber(aCase.getBilus().size());
            involvedCaseWithRole.setRole(aCase.getConnections().get(subjectId));
            involvedCases.add(involvedCaseWithRole);

            for (Bilu bilu : aCase.getBilus()) {
                for (Person personInBilu : bilu.getPersons()) {
                    if (!isNullOrEmpty(personInBilu.getSubjectId())&&!isNullOrEmpty(personRichInfo.getSubjectId())&&!personInBilu.getSubjectId().equals(personRichInfo.getSubjectId())){
                        PersonRichInfo.SameCasePerson sameCasePerson = personRichInfo.new SameCasePerson();
                        sameCasePerson.setSubjectId(personInBilu.getSubjectId());
                        sameCasePerson.setName(personInBilu.getName());
                        sameCasePerson.setIdentity(personInBilu.getIdentity());
                        if (!sameCasePersonList.contains(sameCasePerson)) {
                            sameCasePerson.setBirthDay(personInBilu.getBirthDay());
                            sameCasePerson.setGender(personInBilu.getGender());
                            sameCasePerson.setPhone(personInBilu.getPhone());
                            List<CaseBaseInfo> sameCases = new ArrayList<>();
                            CaseBaseInfo caseBaseInfo = new CaseBaseInfo();
                            caseBaseInfo.setCaseName(aCase.getCaseName());
                            caseBaseInfo.setSubjectId(aCase.getSubjectId());
                            caseBaseInfo.setCaseId(aCase.getCaseId());
                            sameCases.add(caseBaseInfo);
                            sameCasePerson.setSameCases(sameCases);
                            sameCasePersonList.add(sameCasePerson);
                        } else {
                            for (PersonRichInfo.SameCasePerson item : sameCasePersonList) {
                                if ( !isNullOrEmpty(item.getIdentity()) && !isNullOrEmpty(sameCasePerson.getIdentity()) && item.getIdentity().equals(sameCasePerson.getIdentity()) || !isNullOrEmpty(item.getName()) && !isNullOrEmpty(sameCasePerson.getName()) && item.getName().equals(sameCasePerson.getName())) {
                                    CaseBaseInfo caseBaseInfo = new CaseBaseInfo();
                                    caseBaseInfo.setCaseName(aCase.getCaseName());
                                    caseBaseInfo.setSubjectId(aCase.getSubjectId());
                                    caseBaseInfo.setCaseId(aCase.getCaseId());
                                    if (!item.getSameCases().contains(caseBaseInfo)) {
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

        if (minSameCaseNum == null) minSameCaseNum = 2;
        for (PersonRichInfo.SameCasePerson sameCasePerson : sameCasePersonList) {
            if (sameCasePerson.getSameCases().size() >= minSameCaseNum) {
                if (hasNameOnly(sameCasePerson)){
                    PersonRichInfo.SameCaseEntity sameCaseEntity = personRichInfo.new SameCaseEntity(sameCasePerson.getName());
                    sameCaseEntity.setSubjectId(sameCasePerson.getSubjectId());
                    sameCaseNameList.add(sameCaseEntity);
                }else{
                    sameCasePersonListFinal.add(sameCasePerson);
                }
            }
        }
        return personRichInfo;
    }

    public boolean hasNameOnly(PersonRichInfo.SameCasePerson sameCasePerson) {
        if (!isNullOrEmpty(sameCasePerson.getName()) && isNullOrEmpty(sameCasePerson.getBirthDay()) && isNullOrEmpty(sameCasePerson.getGender()) && isNullOrEmpty(sameCasePerson.getIdentity()) && isNullOrEmpty(sameCasePerson.getPhone()) && isNullOrEmpty(sameCasePerson.getRole()))
            return true;
        return false;
    }

    public boolean isNullOrEmpty(String str) {
        if (str == null || str.isEmpty()) return true;
        return false;
    }
}
