package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.EntityType;
import com.mycompany.tahiti.analysis.repository.CaseBaseInfo;
import com.mycompany.tahiti.analysis.repository.DataFactory;
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
    public Map<EntityType, Integer> analysis(){
        Map<EntityType, Integer> map = new HashMap<>();
        map.put(EntityType.Person,dataFactory.getPersonCount());
        map.put(EntityType.Bilu,dataFactory.getBiluCount());
        map.put(EntityType.Case,dataFactory.getCaseCount());

        double carRate = 0.0089;
        double addressRate = 3.18;
        //have no data right now
        if (map.get(EntityType.Bilu)!=null){
            map.put(EntityType.Car, (int)Math.ceil(map.get(EntityType.Bilu)*carRate));
            map.put(EntityType.Location, (int)Math.ceil(map.get(EntityType.Bilu)*addressRate));
        }else{
            map.put(EntityType.Car, null);
            map.put(EntityType.Location, null);
        }
        return map;
    }

    @GetMapping("/personCount")
    @ResponseBody
    public List<Map.Entry<String,Integer>> personCount(){
        Map<String,Integer> map = dataFactory.getPersonBiluCount();
        List<Map.Entry<String,Integer>> entries = entriesSortedByValues(map);
        return entries.subList(0,Bandan_lenght);
    }

    static <K,V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    @GetMapping("/tagCount")
    @ResponseBody
    public Map<String,Integer> tagCount(){
        Map<String,Integer> map = dataFactory.getTagBiluCount();
        Map<String, Integer> result = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

        return returnTopN(result,Bandan_lenght);
    }

    @GetMapping("/caseCategory")
    @ResponseBody
    public Map<String,Integer> caseCategory(){
        Map<String,Integer> map = new HashMap();

        List<CaseBaseInfo> cases = dataFactory.getAllCaseBaseInfo();
        for(CaseBaseInfo aCase:cases){
            if(aCase.getCaseType()!=null &&!aCase.getCaseType().isEmpty()){
                String[] caseCate = aCase.getCaseType().split(",");
                for(String cate:caseCate){
                    if(map.keySet().contains(cate)){
                        map.put(cate,map.get(cate)+1);
                    }else{
                        map.put(cate,1);
                    }
                }
            }
        }
        return map;
    }

    public Map<String, Integer> returnTopN(Map<String, Integer> raw_result,int n){
        Map<String, Integer> result = new LinkedHashMap<>();
        int i=0;
        for(String key:raw_result.keySet()){
            if(i<n) result.put(key,raw_result.get(key));
            i++;
        }
        return result;
    }
}
