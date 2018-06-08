package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.*;
import com.mycompany.tahiti.analysis.repository.CaseBaseInfo;
import com.mycompany.tahiti.analysis.repository.DataFactory;
import com.mycompany.tahiti.analysis.repository.Person;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/bi")
@Api(description = "Bi controller")
public class BIController {
    int Bandan_lenght = 10;
    @Autowired
    DataFactory dataFactory;

    @GetMapping("/overall")
    @ResponseBody
    public Map<EntityType, Integer> analysis() {
        Map<EntityType, Integer> map = new HashMap<>();
        map.put(EntityType.Person, dataFactory.getPersonCount());
        map.put(EntityType.Bilu, dataFactory.getBiluCount());
        map.put(EntityType.Case, dataFactory.getCaseCount());

        double carRate = 0.0089;
        double addressRate = 3.18;
        //have no data right now
        if (map.get(EntityType.Bilu) != null) {
            map.put(EntityType.Car, (int) Math.ceil(map.get(EntityType.Bilu) * carRate));
            map.put(EntityType.Location, (int) Math.ceil(map.get(EntityType.Bilu) * addressRate));
        } else {
            map.put(EntityType.Car, null);
            map.put(EntityType.Location, null);
        }
        return map;
    }

    @GetMapping("/personCount")
    @ResponseBody
    public List<Map.Entry<String, Integer>> personCount() {
        Map<String, Integer> map = dataFactory.getPersonBiluCount();
        List<Map.Entry<String, Integer>> entries = entriesSortedByValues(map);
        return entries.subList(0, Bandan_lenght);
    }

    static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    @GetMapping("/tagCount")
    @ResponseBody
    public Map<String, Integer> tagCount() {
        Map<String, Integer> map = dataFactory.getTagBiluCount();
        Map<String, Integer> result = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

        return returnTopN(result, Bandan_lenght);
    }

    @GetMapping("/caseCategory")
    @ResponseBody
    public Map<String, Integer> caseCategory() {
        Map<String, Integer> map = new HashMap();

        List<CaseBaseInfo> cases = dataFactory.getAllCaseBaseInfo();
        for (CaseBaseInfo aCase : cases) {
            if (aCase.getCaseType() != null && !aCase.getCaseType().isEmpty()) {
                String[] caseCate = aCase.getCaseType().split(",");
                for (String cate : caseCate) {
                    if (map.keySet().contains(cate)) {
                        map.put(cate, map.get(cate) + 1);
                    } else {
                        map.put(cate, 1);
                    }
                }
            }
        }
        return map;
    }

    @GetMapping("/peopleGraph")
    @ResponseBody
    public Graph getPeopleRelation(@RequestParam("entityNum") String entityNum) {
        Graph graph = new Graph();
        Map<String, Person> personRelationCache = dataFactory.getPersonRelaticn();
        Map<String, Integer> personCaseCount = new HashMap<>();
        for (String subjectId : personRelationCache.keySet()) {
            personCaseCount.put(subjectId, personRelationCache.get(subjectId).getCaseList().size());
        }
        List<Map.Entry<String, Integer>> entries = entriesSortedByValues(personCaseCount);
        List<Map.Entry<String, Integer>> topEntries = entries.subList(0, Integer.parseInt(entityNum));
        //set node
        for (Map.Entry<String, Integer> entry : topEntries) {
            Node node = new Node(entry.getKey());
            Map<String, Object> properties = new HashMap<>();
            if (personRelationCache.get(entry.getKey()).getName() != null && !personRelationCache.get(entry.getKey()).getName().isEmpty()){
                properties.put("name", personRelationCache.get(entry.getKey()).getName());
                properties.put("type", NodeType.Person.toString());
                properties.put("crimeCount",entry.getValue());
                node.setProperties(properties);
                graph.getEntities().add(node);
            }else if(personRelationCache.get(entry.getKey()).getIdentity() != null && !personRelationCache.get(entry.getKey()).getIdentity().isEmpty()){
                properties.put("name", personRelationCache.get(entry.getKey()).getIdentity());
                properties.put("type",NodeType.Identity.toString());
                properties.put("crimeCount",entry.getValue());
                node.setProperties(properties);
                graph.getEntities().add(node);
            }
        }
        //generate edge
        for(Node node1 : graph.getEntities()){
            for(Node node2:graph.getEntities()){
                if(!node1.getId().equals(node2.getId())){
                    int sameCaseCount = 0;
                    for(String case1:personRelationCache.get(node1.getId()).getCaseList()){
                        if(personRelationCache.get(node2.getId()).getCaseList().contains(case1))sameCaseCount++;
                    }
                    if(sameCaseCount>0){
                        Edge edge = new Edge(new Random().nextInt(),node1.getId(),node2.getId());
                        edge.setChiType(EdgeType.Gongan.toString());
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("accompliceCount",sameCaseCount);
                        edge.setProperties(properties);
                        graph.getRelationships().add(edge);
                    }
                }
            }
        }
        return graph;
    }

    public Map<String, Integer> returnTopN(Map<String, Integer> raw_result, int n) {
        Map<String, Integer> result = new LinkedHashMap<>();
        int i = 0;
        for (String key : raw_result.keySet()) {
            if (i < n) result.put(key, raw_result.get(key));
            i++;
        }
        return result;
    }
}
