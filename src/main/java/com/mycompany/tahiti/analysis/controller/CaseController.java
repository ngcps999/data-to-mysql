package com.mycompany.tahiti.analysis.controller;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.*;
import io.swagger.annotations.Api;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cases")
@Api(description = "case controller")
public class CaseController {
    @Autowired
    TdbJenaLibrary jenaLibrary;

    List<CaseBaseInfo> caseBaseInfos = new LinkedList<>();

    public List<CaseBaseInfo> getAllCaseBaseInfo()
    {
        LocalTime time = LocalTime.now();
        if(caseBaseInfos.size() == 0 || time.getMinute() % 30 == 0) {
            try {
                jenaLibrary.openReadTransaction();
                Model model = jenaLibrary.getModel(Configs.getConfig("jenaModelName"));
                val list = new ArrayList<CaseBaseInfo>();

                val iterator = jenaLibrary.getStatementsByEntityType(model, "gongan:gongan.case");

                while (iterator.hasNext()) {
                    Statement statement = iterator.next();
                    Resource resource = statement.getSubject();

                    CaseBaseInfo aCase = new CaseBaseInfo();
                    getCaseBaseInfo(model, resource, aCase);
                    list.add(aCase);
                }
                caseBaseInfos = list;
                return list;
            } finally {
                jenaLibrary.closeTransaction();
            }
        }
        else
            return caseBaseInfos;
    }

    @GetMapping
    public List<CaseBaseInfo> getCases(){
        return getAllCaseBaseInfo();
    }

    @GetMapping("/keyword/{keyword}")
    public List<CaseBaseInfo> searchCases(@PathVariable("keyword") String keyword){
        List<CaseBaseInfo> allCases = getAllCaseBaseInfo();

        List<CaseBaseInfo> cases = new LinkedList<>();
        for(CaseBaseInfo cs : allCases)
        {
            if(cs.getCaseName().contains(keyword))
                cases.add(cs);
        }

        return cases;
    }

    @ResponseBody
    @GetMapping("/{caseId}")
    public CaseRichInfo getCaseById(@PathVariable("caseId") String caseId) {
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getModel(Configs.getConfig("jenaModelName"));

            CaseRichInfo aCase = new CaseRichInfo();

            val iterator = jenaLibrary.getStatementsById(model, caseId);

            while (iterator.hasNext()) {
                Statement statement = iterator.next();
                Resource resource = statement.getSubject();

                getCaseBaseInfo(model, resource, aCase);

                val bilus = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());

                val entities = Lists.newArrayList(jenaLibrary.getStatementsByBatchSP(model, bilus, "gongan:gongan.bilu.entity")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());
                val persons = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:person.person")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
                // join entities.o and person.s to get all persons
                persons.retainAll(entities);

                // get names
                aCase.setNames(jenaLibrary.getStringValuesByBatchSP(model, persons, "common:type.object.name").stream().distinct().collect(Collectors.toList()));
                // get 身份证号
                val identities = Lists.newArrayList(jenaLibrary.getStatementsByBatchSP(model, persons, "common:person.person.identification")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());
                aCase.setIdentities(jenaLibrary.getStringValuesByBatchSP(model, identities, "common:person.identification.number"));

                // get all things
                val things = Lists.newArrayList(jenaLibrary.getStatementsByBatchSP(model, bilus, "gongan:gongan.bilu.thing")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());

                // get phone
                val phones = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:thing.phone")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
                phones.retainAll(things);
                aCase.setPhones(jenaLibrary.getStringValuesByBatchSP(model, phones, "common:thing.phone.phoneNumber"));

                // get bank cards
                val bankCards = Lists.newArrayList(jenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:thing.bankcard")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
                bankCards.retainAll(things);
                aCase.setBankCards(jenaLibrary.getStringValuesByBatchSP(model, bankCards, "common:thing.bankcard.bankCardId"));

                // get bilu
                for (String bilu : bilus) {
                    BiluBaseInfo biluBaseInfo = new BiluBaseInfo();
                    val ids = jenaLibrary.getStringValueBySP(model, model.getResource(bilu), "common:type.object.id");
                    if(ids.size() > 0)
                        biluBaseInfo.setId(ids.get(0));

                    val names = jenaLibrary.getStringValueBySP(model, model.getResource(bilu), "common:type.object.name");
                    if(names.size() > 0)
                        biluBaseInfo.setName(names.get(0));

                    aCase.getBilus().add(biluBaseInfo);
                }

                // get
                for(String person : persons)
                {
                    Person personModel = new Person();

                    val names = jenaLibrary.getStringValueBySP(model, model.getResource(person), "common:type.object.name");
                    if(names.size() > 0)
                        personModel.setName(names.get(0));

                    if(personModel.getName() == null || personModel.getName().isEmpty())
                        continue;

                    val personIdentities = jenaLibrary.getStatementsBySP(model, model.getResource(person), "common:person.person.identification");
                    if(personIdentities.hasNext()) {
                        val personIds = jenaLibrary.getStringValueBySP(model, personIdentities.next().getResource(), "common:person.identification.number");
                        if(personIds.size() > 0)
                            personModel.setIdentity(personIds.get(0));
                    }

                    val contactIters = jenaLibrary.getStatementsBySP(model, model.getResource(person), "common:person.person.contact");
                    if(contactIters.hasNext()) {
                        val contacts = jenaLibrary.getStringValueBySP(model, contactIters.next().getResource(), "common:person.contact.number");
                        if(contacts.size() > 0)
                            personModel.setPhone(contacts.get(0));
                    }

                    if((personModel.getIdentity() == null || personModel.getIdentity().isEmpty()) && (personModel.getPhone() == null || personModel.getPhone().isEmpty()))
                        continue;

                    val ids = jenaLibrary.getStringValueBySP(model, model.getResource(person), "common:type.object.id");
                    if(ids.size() > 0)
                        personModel.setId(ids.get(0));

                    val birthdays = jenaLibrary.getStringValueBySP(model, model.getResource(person), "common:person.person.birthDate");
                    if(birthdays.size() > 0)
                        personModel.setBirthDay(birthdays.get(0));

                    val genders = jenaLibrary.getStringValueBySP(model, model.getResource(person), "common:person.person.gender");
                    if(genders.size() > 0) {
                        if(genders.get(0).toLowerCase().equals("female"))
                            personModel.setGender("女");
                        else if(genders.get(0).toLowerCase().equals("male"))
                            personModel.setGender("男");
                    }

                    //        person.setRole("嫌疑人");
                    aCase.getDetailedPersons().add(personModel);
                }
                break;
            }

            return aCase;

        } finally {
            jenaLibrary.closeTransaction();
        }
    }


    @GetMapping("/{caseId}/person")
    public List<RelevantGraph> getRelevantBiluParagraphsByPersonId(@PathVariable("caseId") String caseId, @RequestParam("personId") String personId){
        val res = new ArrayList<RelevantGraph>();
        val p1 = new RelevantGraph();
        p1.setBiluId("123");
        p1.setBiluName("王大锤笔录");
        p1.setKeyword("王大锤");
        p1.setParagraph("我叫王大锤，我一开始只想着不用多久 我就会升职加薪");
        res.add(p1);
        return res;
    }

    @ResponseBody
    @GetMapping("/{caseId}/keyword/{keyword}")
    public List<RelevantGraph> getRelevantBiluParagraphsByKeyword(@PathVariable("caseId") String caseId, @PathVariable("keyword") String keyword){
        val res = new ArrayList<RelevantGraph>();
        val p1 = new RelevantGraph();
        p1.setBiluId("456");
        p1.setBiluName("孔连顺笔录");
        p1.setKeyword(keyword);
        p1.setParagraph("我叫" + keyword  +"，我一开始只想着不用多久 我就会升职加薪");
        res.add(p1);
        return res;
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
