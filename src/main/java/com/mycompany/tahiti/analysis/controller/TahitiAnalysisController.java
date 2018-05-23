package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import io.swagger.annotations.Api;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/server")
@Api(description = "server")
public class TahitiAnalysisController {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Autowired
    JenaLibrary jenaLibrary;

    @ResponseBody
    @PostMapping("/getBIResult")
    public List<String> getSimilarCases(@RequestBody String caseInfo) {
        return null;
    }


    @GetMapping("/getCaseInfo/{caseId}")
    @ResponseBody
    public String analysis(@PathVariable("caseId") String caseId){
        return "";
    }

}
