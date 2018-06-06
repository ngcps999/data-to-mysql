package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.EntityType;
import io.swagger.annotations.Api;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping("/api/bi")
@Api(description = "Bi controller")
public class BIController {
    int Bandan_lenght = 10;
    @Autowired
    JenaLibrary jenaLibrary;
    Map<EntityType,String> entity_type_dict = new HashMap<>();

    @PostConstruct
    public void init(){
        entity_type_dict.put(EntityType.Bilu,"gongan:gongan.bilu");
        entity_type_dict.put(EntityType.Case,"gongan:gongan.case");
        entity_type_dict.put(EntityType.Person,"common:person.person");
    }

    @GetMapping("/overall")
    @ResponseBody
    public Map<EntityType, Integer> analysis(){
        jenaLibrary.openReadTransaction();
        Model model = jenaLibrary.getRuntimeModel();
        Map<EntityType, Integer> map = new HashMap<>();
        for( EntityType entityType:entity_type_dict.keySet()){
            setCount(map,model,entity_type_dict.get(entityType),entityType);
        }

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

        jenaLibrary.closeTransaction();
        return map;
    }

    @GetMapping("/personCount")
    @ResponseBody
    public Map<String,Integer> personCount(){
        jenaLibrary.openReadTransaction();
        Model model = jenaLibrary.getRuntimeModel();
        Iterator<Statement> iterator = jenaLibrary.getStatementsByEntityType(model,"common:person.person");
        List<String> resourceList = new ArrayList<>();
        while (iterator.hasNext()){
            resourceList.add(iterator.next().getSubject().toString());
        }

        Map<String,Integer> map = new HashMap();
        Iterator<Statement> iterator_names = jenaLibrary.getStatementsByBatchSP(model,resourceList,"common:type.object.name");
        iteratorObjectToMap(iterator_names,map);

        //排序
        Map<String, Integer> result = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        jenaLibrary.closeTransaction();
        return returnTopN(result,Bandan_lenght);
    }

    @GetMapping("/tagCount")
    @ResponseBody
    public Map<String,Integer> tagCount(){
        jenaLibrary.openReadTransaction();
        Model model = jenaLibrary.getRuntimeModel();
        Iterator<Statement> iterator_tag = jenaLibrary.getStatementsBySP(model,null,"common:type.object.tag");
        Map<String,Integer> map = new HashMap();
        iteratorObjectToMap(iterator_tag,map);

        Map<String, Integer> result = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

        jenaLibrary.closeTransaction();
        return returnTopN(result,Bandan_lenght);
    }

    @GetMapping("/caseCategory")
    @ResponseBody
    public Map<String,Integer> caseCategory(){
        jenaLibrary.openReadTransaction();
        Model model = jenaLibrary.getRuntimeModel();
        Iterator<Statement> iterator = jenaLibrary.getStatementsBySP(model,null,"gongan:gongan.case.category");
        Map<String,Integer> map = new HashMap();
        iteratorObjectToMap(iterator,map);
        jenaLibrary.closeTransaction();
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

    public void setCount(Map<EntityType,Integer> map, Model model,String type, EntityType entityType){
        Iterator<Statement> iter = jenaLibrary.getStatementsByEntityType(model,type);
        int count = Iterators.size(iter);
        map.put(entityType,new Integer(count));
    }

    public void iteratorObjectToMap(Iterator<Statement> iterator, Map<String,Integer> map){
        while (iterator.hasNext()){
            Statement statement = iterator.next();
            String object = statement.getObject().toString();
            if(map.keySet().contains(object)){
                map.put(object,map.get(object)+1);
            }else{
                map.put(object,1);
            }
        }
    }
}
