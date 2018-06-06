package com.mycompany.tahiti.analysis.controller;

import com.google.common.collect.Lists;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.repository.*;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.*;
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
    JenaLibrary jenaLibrary;

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

        List<CaseBaseInfo> cases = new LinkedList<>();
        for (CaseBaseInfo cs : allCases) {
            if (cs.getCaseName().contains(keyword))
                cases.add(cs);
        }

        return cases;
    }

    @ResponseBody
    @GetMapping("/{caseId}")
    public CaseRichInfo getCaseById(@PathVariable("caseId") String caseId) {
        Case aCase = dataFactory.getCaseById(caseId);

        CaseRichInfo richInfo = new CaseRichInfo();

        if(aCase != null) {
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

                    // set graph
                    Node pNode = new Node(personData.getSubjectId());
                    Map<String, Object> props = new HashMap<>();
                    if(!name.isEmpty()) {
                        props.put("name", name);
                        props.put("type", NodeType.Person.toString());
                        if(!identity.isEmpty())
                            props.put("identity", identity);
                    }
                    else {
                        props.put("identity", identity);
                        props.put("type", NodeType.Identity.toString());
                    }

                    if(!contact.isEmpty()) {
                        props.put("phone", contact);
                    }

                    pNode.setProperties(props);
                    richInfo.getGraph().getEntities().add(pNode);

                    Edge edge = new Edge(new Random().nextInt(), aCase.getSubjectId(), personData.getSubjectId());
                    edge.setChiType(EdgeType.GuanlianRen.toString());

                    richInfo.getGraph().getRelationships().add(edge);

                    // find other cased related to this person
                    for(String otherCaseId : personData.getCaseList()) {
                        if (otherCaseId.equals(aCase.getCaseId()))
                            continue;

                        Case otherCase = dataFactory.getCaseById(otherCaseId);

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
        try {
            jenaLibrary.openReadTransaction();
            Model model = jenaLibrary.getRuntimeModel();

            keywordList.remove("");

            List<String> bilus_list = new ArrayList<>();
            val iterator = jenaLibrary.getStatementsById(model, caseId);
            while (iterator.hasNext()) {
                Statement statement = iterator.next();
                Resource resource = statement.getSubject();
                List<String> bilus;
                bilus = Lists.newArrayList(jenaLibrary.getStatementsBySP(model, resource, "gongan:gongan.case.bilu")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());
                bilus_list.addAll(bilus);
            }

            int before_paragraph_length = 30;
            int after_paragraph_length = 40;
            val result = new ArrayList<RelevantGraph>();
            Iterator<Statement> stIter = jenaLibrary.getStatementsByBatchSP(model, bilus_list, "common:common.document.contentStream");
            while (stIter.hasNext()) {
                Statement statement = stIter.next();
                String content = statement.getString();
                for (String keyword : keywordList) {
                    if (content != null && content.contains(keyword)) {
                        String BiluId = "";
                        val ids = jenaLibrary.getStringValueBySP(model, statement.getSubject(), "common:type.object.id");
                        if (ids.size() > 0) BiluId = ids.get(0);

                        String BiluName = "";
                        val names = jenaLibrary.getStringValueBySP(model, statement.getSubject(), "common:type.object.name");
                        if (names.size() > 0) BiluName = names.get(0);

                        for (int i = -1; (i = content.indexOf(keyword, i + 1)) != -1; i++) {
                            int start_index = i-before_paragraph_length>0?i-before_paragraph_length:0;
                            int end_index = start_index+before_paragraph_length+after_paragraph_length+keyword.length()<content.length()?start_index+before_paragraph_length+after_paragraph_length+keyword.length():content.length()-1;
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
        } finally {
            jenaLibrary.closeTransaction();
        }
    }

    @ResponseBody
    @GetMapping("/{caseId}/keyword/{keyword}")
    public List<RelevantGraph> getRelevantBiluParagraphsByKeyword(@PathVariable("caseId") String caseId, @PathVariable("keyword") String keyword) {
        List<String> keywordList = new ArrayList();
        keywordList.add(keyword);
        return getRelevantBiluParagraphsByPersonId(caseId, keywordList);
    }
}
