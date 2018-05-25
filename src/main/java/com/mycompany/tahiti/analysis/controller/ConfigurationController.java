package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.configuration.Configs;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuration")
@Api(description = "configuration controller")
public class ConfigurationController {

    @GetMapping("/updateJenaModelName/{jenaModelName}")
    public void updateJenaModelName(@PathVariable("jenaModelName") String jenaModelName) {
        Configs.addConfig("jenaModelName", jenaModelName);
    }
}
