package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.*;
import com.mycompany.tahiti.analysis.repository.*;
import io.swagger.annotations.Api;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cases")
@Api(description = "case controller")
public class CaseController {
    @Autowired
    DataFactory dataFactory;

    @GetMapping("/reset")
    public boolean reset() {
        return dataFactory.clear();
    }

    @GetMapping
    public List<CaseBaseInfo> getCases() {
        return dataFactory.getAllCaseBaseInfo();
    }

    @GetMapping("/keyword/{keyword}")
    public List<CaseBaseInfo> searchCases(@PathVariable("keyword") String keyword) {
        List<CaseBaseInfo> allCases = dataFactory.getAllCaseBaseInfo();

        keyword = keyword.trim();

        if (keyword.isEmpty())
            return allCases;

        List<String> caseSIds = new LinkedList<>();
        List<CaseBaseInfo> cases = new LinkedList<>();
        for (CaseBaseInfo cs : allCases) {
            if (cs.getCaseName().contains(keyword) || cs.getCaseId().contains(keyword)) {
                caseSIds.add(cs.getSubjectId());
            }
        }

        Map<String, Person> persons = dataFactory.getPersonRelation();
        for(String personSubject : persons.keySet()){
            Person person = persons.get(personSubject);
            if(keyword.equals(person.getName()) || keyword.equals(person.getIdentity())){
                caseSIds.addAll(person.getCaseList());
            }
        }

        Map<String, List<String>> phonesCaseMap = dataFactory.getPhoneCaseRelationCache();
        if(phonesCaseMap.containsKey(keyword)){
            caseSIds.addAll(phonesCaseMap.get(keyword));
        }

        val uniqueCases = caseSIds.stream().distinct().collect(Collectors.toList());

        for(String caseSubject : uniqueCases) {
            cases.add(dataFactory.getCaseBaseInfoById(caseSubject));
        }

        return cases;
    }

    @ResponseBody
    @GetMapping("/{caseId}")
    public CaseRichInfo getCaseById(@PathVariable("caseId") String caseId) {

        String subjectId = dataFactory.getSubjectIdById(caseId);

        Case aCase = dataFactory.getCaseById(subjectId);

        CaseRichInfo richInfo = new CaseRichInfo();

        if(aCase != null) {
            richInfo.setSubjectId(aCase.getSubjectId());
            richInfo.setCaseId(aCase.getCaseId());
            richInfo.setCaseName(aCase.getCaseName());
            richInfo.setCaseType(aCase.getCaseType());
            richInfo.setBiluNumber(aCase.getBilus().size());

            List<String> processedPerson = new ArrayList<>();

            // caused by no-conflation
            List<String> names = new ArrayList<>();
            List<String> identities = new ArrayList<>();

            for (Bilu bilu : aCase.getBilus()) {
                // set phones
                for(String sId : bilu.getPhones().keySet()) {
                    richInfo.getPhones().put(sId, new ValueObject(bilu.getPhones().get(sId)));
                }

                // set bankcards
                for(String sId : bilu.getBankCards().keySet()) {
                    richInfo.getBankCards().put(sId, new ValueObject(bilu.getBankCards().get(sId)));
                }

                // set bilu
                BiluBaseInfo biluBaseInfo = new BiluBaseInfo();
                biluBaseInfo.setId(bilu.getBiluId());
                biluBaseInfo.setName(bilu.getName());
                richInfo.getBilus().add(biluBaseInfo);

                // set names, identities, detailedPersons, graph;
                for (Person personData : bilu.getPersons()) {
                    if(processedPerson.contains(personData.getSubjectId()))
                        continue;

                    processedPerson.add(personData.getSubjectId());

                    String name = "";
                    if (personData.getName() != null && !personData.getName().isEmpty()) {
                        name = personData.getName();
                        if(!names.contains(name)) {
                            richInfo.getNames().put(personData.getSubjectId(), new ValueObject(name));
                            names.add(name);
                        }
                    }

                    String identity = "";
                    if (personData.getIdentity() != null && !personData.getIdentity().isEmpty()) {
                        identity = personData.getIdentity();
                        if(!identities.contains(identity)) {
                            richInfo.getIdentities().put(personData.getSubjectId(), new ValueObject(identity));
                            identities.add(identity);
                        }
                    }

                    String contact = "";
                    if (personData.getPhone() != null && !personData.getPhone().isEmpty()) {
                        contact = personData.getPhone();
                    }

                    if (!name.isEmpty() && (!identity.isEmpty() || !contact.isEmpty())) {
                        PersonModel personModel = new PersonModel();
                        personModel.setName(personData.getName());
                        personModel.setSubjectId(personData.getSubjectId());
                        personModel.setGender(personData.getGender());
                        personModel.setBirthDay(personData.getBirthDay());
                        personModel.setIdentity(personData.getIdentity());
                        personModel.setPhone(personData.getPhone());

                        if(aCase.getConnections().containsKey(personData.getSubjectId()))
                            personModel.setRole(aCase.getConnections().get(personData.getSubjectId()));
                        else
                            personModel.setRole("");

                        richInfo.getDetailedPersons().add(personModel);
                    }

                    if(!name.isEmpty() || !identity.isEmpty()) {
                        // set graph
                        Node pNode = new Node(personData.getSubjectId());
                        Map<String, Object> props = new HashMap<>();
                        if (!name.isEmpty()) {
                            props.put("name", name);
                            props.put("type", NodeType.Person.toString());
                            if (!identity.isEmpty())
                                props.put("identity", identity);
                        } else {
                            props.put("identity", identity);
                            props.put("type", NodeType.Identity.toString());
                        }

                        if (!contact.isEmpty()) {
                            props.put("phone", contact);
                        }

                        pNode.setProperties(props);
                        richInfo.getGraph().getEntities().add(pNode);

                        Edge edge = new Edge(new Random().nextInt(), aCase.getSubjectId(), personData.getSubjectId());
                        edge.setChiType(EdgeType.GuanlianRen.toString());

                        richInfo.getGraph().getRelationships().add(edge);

                        // find other cased related to this person
                        for (String otherCaseSubjectId : personData.getCaseList()) {
                            if (otherCaseSubjectId.equals(aCase.getSubjectId()))
                                continue;

                            CaseBaseInfo otherCase = dataFactory.getCaseBaseInfoById(otherCaseSubjectId);

                            Node caseNode = new Node(otherCase.getSubjectId());
                            caseNode.setProperties(new HashMap<>());

                            caseNode.getProperties().put("name", otherCase.getCaseName());
                            caseNode.getProperties().put("type", NodeType.Case.toString());
                            richInfo.getGraph().getEntities().add(caseNode);

                            Edge csEdge = new Edge(new Random().nextInt(), personData.getSubjectId(), otherCase.getSubjectId());
                            csEdge.setChiType(EdgeType.GuanlianAnjian.toString());
                            richInfo.getGraph().getRelationships().add(csEdge);
                        }
                    }
                }
            }

            // node - current case
            Node node = new Node(aCase.getSubjectId());
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", aCase.getCaseName());
            properties.put("type", NodeType.Case.toString());
            node.setProperties(properties);

            richInfo.getGraph().getEntities().add(node);
        }
        return richInfo;
    }

    @ResponseBody
    @GetMapping("/{caseId}/person")
    public List<RelevantGraph> getRelevantBiluParagraphsByPersonId(@PathVariable("caseId") String caseId, @RequestParam("keywordList") List<String> keywordList) {

        String subjectId = dataFactory.getSubjectIdById(caseId);

        keywordList.remove("");

        int before_paragraph_length = 30;
        int after_paragraph_length = 40;
        val result = new ArrayList<RelevantGraph>();

        Case aCase = dataFactory.getCaseById(subjectId);
        List<Bilu> bilus = aCase.getBilus();

        for (Bilu bilu : bilus) {
            String content = bilu.getContent();
            for (String keyword : keywordList) {
                if (content != null && content.contains(keyword)) {
                    String BiluId = bilu.getBiluId();
                    String BiluName = bilu.getName();

                    for (int i = -1; (i = content.indexOf(keyword, i + 1)) != -1; i++) {
                        int start_index = i - before_paragraph_length > 0 ? i - before_paragraph_length : 0;
                        int end_index = start_index + before_paragraph_length + after_paragraph_length + keyword.length() < content.length() ? start_index + before_paragraph_length + after_paragraph_length + keyword.length() : content.length() - 1;
                        String paragraph = content.substring(start_index, end_index);
                        val p1 = new RelevantGraph();
                        p1.setBiluName(BiluName);
                        p1.setBiluId(BiluId);
                        p1.setKeyword(keyword);
                        p1.setParagraph(paragraph);
                        result.add(p1);
                    }
                }
            }
        }
        return result;
    }

    @ResponseBody
    @GetMapping("/{caseId}/keyword/{keyword}")
    public List<RelevantGraph> getRelevantBiluParagraphsByKeyword(@PathVariable("caseId") String caseId, @PathVariable("keyword") String keyword) {
        List<String> keywordList = new ArrayList();
        keywordList.add(keyword);
        return getRelevantBiluParagraphsByPersonId(caseId, keywordList);
    }
}
