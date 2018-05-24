package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.model.EntityType;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bi")
@Api(description = "Bi controller")
public class BIController {
    @GetMapping("/overall")
    @ResponseBody
    public Map<EntityType, Integer> analysis(){
        //TODO, this number is just used for frontend
        Map map = new HashMap();
        map.put(EntityType.Bilu, 10086);
        map.put(EntityType.Case, 200);
        map.put(EntityType.Person, 23980);
        map.put(EntityType.Car, 1000);
        map.put(EntityType.Location, 300);
        return map;
    }
}
