package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.annalysis.model.Case;
import com.mycompany.tahiti.annalysis.model.Person;
import io.swagger.annotations.Api;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/cases")
@Api(description = "case controller")
public class CaseController {
    @GetMapping
    public List<Case> getCases(){
        val list = new ArrayList<Case>();
        Case aCase = new Case();
        aCase.setCaseId("1111111122233");
        aCase.setCaseName("王大锤殴打别人案件");
        aCase.setBiluNumber(10);
        aCase.setSuspects(Arrays.asList(new String[]{"王大锤"}));
        list.add(aCase);
        return list;
    }

    @ResponseBody
    @GetMapping("/{caseId}")
    public Case getCaseById(@PathVariable("caseId") String caseId) {
        Case aCase = new Case();
        aCase.setCaseId("1111111122233");
        aCase.setCaseName("王大锤殴打别人案件");
        aCase.setBiluNumber(10);
        aCase.setSuspects(Arrays.asList(new String[]{"王大锤"}));
        Person person = new Person();
        person.setName("王大锤");
        person.setIdentity("32212324324235331X");
        person.setId("http://mycompany.ai.com/person/王大锤");
        person.setBirthDay("1988年6月14日");
        person.setGender("男");
        person.setPhone("18888888881");
        person.setRole("嫌疑人");
        aCase.getDetailedPersons().add(person);
        return aCase;
    }

    @ResponseBody
    @GetMapping("/{caseId}/persons/{personId}")
    public Person getPersonById(@PathVariable("caseId") String caseId, @PathVariable("personId") String personId) {
        Person person = new Person();
        person.setName("王大锤");
        person.setIdentity("32212324324235331X");
        person.setId("http://mycompany.ai.com/person/王大锤");
        person.setBirthDay("1988年6月14日");
        person.setGender("男");
        person.setPhone("18888888881");
        person.setRole("嫌疑人");
        return person;
    }
}
