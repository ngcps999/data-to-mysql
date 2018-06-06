package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.jena.BaseJenaLibrary;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
