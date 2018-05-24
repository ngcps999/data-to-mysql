package com.mycompany.tahiti.analysis.controller;

import com.google.common.collect.Iterators;
import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.model.Case;
import com.mycompany.tahiti.analysis.model.Person;
import io.swagger.annotations.Api;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/api/cases")
@Api(description = "case controller")
public class CaseController {
    @Autowired
    JenaLibrary jenaLibrary;
    Model model;

    @PostConstruct
    public void init() {
         model = jenaLibrary.getModel(Configs.getConfig("jenaMappingModel"));
    }

    @GetMapping
    public List<Case> getCases(){
        val list = new ArrayList<Case>();

        val iterator = jenaLibrary.getStatementsByEntityType(model, "gongan:gongan.case");

        while(iterator.hasNext())
        {
            Statement statement = iterator.next();
            Resource resource = statement.getSubject();

            Case aCase = new Case();

            List<String> ids = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.id");
            if(ids.size() > 0)
                aCase.setCaseId(ids.get(0));

            List<String> names = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.name");
            if(names.size() > 0)
                aCase.setCaseName(names.get(0));

            List<String> types = jenaLibrary.getStringValueBySP(model, resource, "gongan:gongan.case.category");
            if(types.size() > 0)
                aCase.setCaseType(String.join(",", types));

            // count of bilu
            val biluIter1 = jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu");
            if(biluIter1.hasNext())
                aCase.setBiluNumber(Iterators.size(biluIter1));

            // set suspect
            aCase.setSuspects(new LinkedList<>());
            val biluIter2 = jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu");
            while(biluIter2.hasNext()) {
                Statement biluStatement = biluIter2.next();
                val connectionIter = jenaLibrary.getStatementsBySP(model, biluStatement.getSubject(), "gongan:gongan.bilu.connection");
                if (connectionIter.hasNext()) {
                    val connection = connectionIter.next();
                    List<String> connectTypes = jenaLibrary.getStringValueBySP(model, connection.getResource(), "common:common.connection.type");
                    if(connectTypes.contains("common:common.connection.BiluEntityXianyiren"))
                    {
                        val toStatementIter = jenaLibrary.getStatementsBySP(model, connection.getResource(), "common:common.connection.to");

                        // get the person name
                        while(toStatementIter.hasNext()) {
                            List<String> toPersonNames = jenaLibrary.getStringValueBySP(model, toStatementIter.next().getResource(), "common:type.object.name");

                            if(toPersonNames.size() > 0)
                                aCase.getSuspects().add(toPersonNames.get(0));
                        }
                    }
                }
            }
            list.add(aCase);
        }

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
        aCase.setCaseType("殴打类");
        aCase.setNames(Arrays.asList(new String[]{"gexin", "王大锤"}));
        aCase.setPhones(Arrays.asList(new String[]{"我是电话号码", "13911xxxxxx"}));
        aCase.setIdenties(Arrays.asList(new String[]{"我是身份证号", "340821000000000"}));

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
