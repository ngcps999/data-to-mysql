package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.jena.BaseJenaLibrary;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import io.swagger.annotations.Api;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/configuration")
@Api(description = "configuration controller")
public class ConfigurationController {

    @Autowired
    JenaLibrary jenaLibrary;
    @GetMapping("/updateJenaModelName/{jenaModelName}")
    public void updateJenaModelName(@PathVariable("jenaModelName") String jenaModelName) {
        if(jenaLibrary instanceof BaseJenaLibrary) {
            ((BaseJenaLibrary) jenaLibrary).setModelName(jenaModelName);
        }
    }
    @GetMapping("/getModelName")
    public String getRuntimeModelName() {
        if(jenaLibrary instanceof BaseJenaLibrary) {
            return ((BaseJenaLibrary) jenaLibrary).getModelName();
        } else {
            return null;
        }
    }

    @GetMapping("/size/{jenaModelName}")
    public long getModelSize(@PathVariable("jenaModelName") String jenaModelName) {
        val library = (BaseJenaLibrary) jenaLibrary;
        library.openReadTransaction();
        long size = library.getModel(jenaModelName).size();
        library.closeTransaction();
        return size;
    }

    @GetMapping("/statements/poContains/{p}/{o}")
    public List<String> subjectSubStr(@PathVariable("p") String p, @PathVariable("o") String o) {
        val library = (BaseJenaLibrary) jenaLibrary;
        library.openReadTransaction();
        val statements = library.getResultByPOContains(p, o);
        List<String> result = new ArrayList<>();
        statements.forEach(r->result.add(r.toString()));
        library.closeTransaction();
        return result;
    }

}
