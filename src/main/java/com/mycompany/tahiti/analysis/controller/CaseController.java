package com.mycompany.tahiti.analysis.controller;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.BiluBaseInfo;
import com.mycompany.tahiti.analysis.model.CaseBaseInfo;
import com.mycompany.tahiti.analysis.model.CaseRichInfo;
import com.mycompany.tahiti.analysis.model.Person;
import io.swagger.annotations.Api;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cases")
@Api(description = "case controller")
public class CaseController {
    @Autowired
    TdbJenaLibrary jenaLibrary;

    @GetMapping
    public  List<CaseBaseInfo> getCases(){
        jenaLibrary.openReadTransaction();
        Model model = jenaLibrary.getModel(Configs.getConfig("jenaModelName"));
        val list = new ArrayList<CaseBaseInfo>();

        val iterator = jenaLibrary.getStatementsByEntityType(model, "gongan:gongan.case");

        while(iterator.hasNext())
        {
            Statement statement = iterator.next();
            Resource resource = statement.getSubject();

            CaseBaseInfo aCase = new CaseBaseInfo();
            getCaseBaseInfo(model, resource, aCase);
            list.add(aCase);
        }
        jenaLibrary.closeTransaction();
        return list;
    }

    @ResponseBody
    @GetMapping("/{caseId}")
    public CaseRichInfo getCaseById(@PathVariable("caseId") String caseId) {
        try{
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getModel(Configs.getConfig("jenaModelName"));

            CaseRichInfo aCase = new CaseRichInfo();

            val iterator = jenaLibrary.getStatementsById(model, caseId);

            while (iterator.hasNext()) {
                Statement statement = iterator.next();
                Resource resource = statement.getSubject();

                getCaseBaseInfo(model, resource, aCase);

                val bilus = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());

                val entities = Lists.newArrayList(jenaLibrary.getStatementsByBatchSP(model, bilus, "gongan:gongan.bilu.entity")).stream().map(s -> s.getResource().toString()).distinct();
                val persons = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:person.person")).stream().map(s -> s.getSubject().toString()).collect(Collectors.toSet());
                // join entities.o and person.s to get all persons
                List<String> personInCase = entities.filter(e -> persons.contains(e)).collect(Collectors.toList());

                // get names
                aCase.setNames(jenaLibrary.getStringValuesByBatchSP(model, personInCase, "common:type.object.name"));
                // get 身份证号
                val identities = Lists.newArrayList(jenaLibrary.getStatementsByBatchSP(model, personInCase, "common:person.person.identification")).stream().map(s -> s.getResource().toString()).collect(Collectors.toList());
                aCase.setIdentities(jenaLibrary.getStringValuesByBatchSP(model, identities, "common:person.identification.number"));

                // get all things
                val things = Lists.newArrayList(jenaLibrary.getStatementsByBatchSP(model, bilus, "gongan:gongan.bilu.thing")).stream().map(s -> s.getResource().toString()).distinct();

                // get phone
                val phones = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:thing.phone")).stream().map(s -> s.getSubject().toString()).collect(Collectors.toList());
                List<String> phoneInCase = things.filter(e -> phones.contains(e)).collect(Collectors.toList());
                aCase.setPhones(jenaLibrary.getStringValuesByBatchSP(model, phoneInCase, "common:thing.phone.phoneNumber"));

                // get bank cards
                val bankCards = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:thing.bankcard")).stream().map(s -> s.getSubject().toString()).collect(Collectors.toList());
                List<String> bankCardsInCase = things.filter(e -> bankCards.contains(e)).collect(Collectors.toList());
                aCase.setBankCards(jenaLibrary.getStringValuesByBatchSP(model, bankCardsInCase, "common:thing.bankcard.bankCardId"));

                BiluBaseInfo biluBaseInfo = new BiluBaseInfo();

                aCase.getBilus().add(biluBaseInfo);

                Person person = new Person();

                aCase.getDetailedPersons().add(person);

                break;
            }

            return aCase;
//
//        BiluBaseInfo bilu = new BiluBaseInfo();
//        bilu.setName("我是笔录1");
//        bilu.setId("我是笔录id1");
//
//        Person person = new Person();
//        person.setName("王大锤");
//        person.setIdentity("32212324324235331X");
//        person.setId("http://mycompany.ai.com/person/王大锤");
//        person.setBirthDay("1988年6月14日");
//        person.setGender("男");
//        person.setPhone("18888888881");
//        person.setRole("嫌疑人");
//        aCase.getDetailedPersons().add(person);

        }
        finally
        {
            jenaLibrary.closeTransaction();
        }
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

    public void getCaseBaseInfo(Model model, Resource resource, CaseBaseInfo caseBaseInfo)
    {
        List<String> ids = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.id");
        if(ids.size() > 0)
            caseBaseInfo.setCaseId(ids.get(0));

        List<String> names = jenaLibrary.getStringValueBySP(model, resource, "common:type.object.name");
        if(names.size() > 0)
            caseBaseInfo.setCaseName(names.get(0));

        List<String> types = jenaLibrary.getStringValueBySP(model, resource, "gongan:gongan.case.category");
        if(types.size() > 0)
            caseBaseInfo.setCaseType(String.join(",", types));

        // count of bilu
        val biluIter1 = jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu");
        if(biluIter1.hasNext())
            caseBaseInfo.setBiluNumber(Iterators.size(biluIter1));

        // set suspect
        caseBaseInfo.setSuspects(new LinkedList<>());
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
                            caseBaseInfo.getSuspects().add(toPersonNames.get(0));
                    }
                }
            }
        }
    }

}
