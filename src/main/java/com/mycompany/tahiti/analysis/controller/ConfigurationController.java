package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
@Api(description = "configuration controller")
public class ConfigurationController {

    @Autowired
    TdbJenaLibrary tdbJenaLibrary;
    @GetMapping("/updateJenaModelName/{jenaModelName}")
    public void updateJenaModelName(@PathVariable("jenaModelName") String jenaModelName) {
        Configs.addConfig("jenaModelName", jenaModelName);
        tdbJenaLibrary = new TdbJenaLibrary(Configs.getConfig("tdbName"));
    }
}
