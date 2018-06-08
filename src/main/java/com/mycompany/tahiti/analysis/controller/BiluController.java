package com.mycompany.tahiti.analysis.controller;

import com.google.common.collect.Lists;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.BiluRichInfo;
import com.mycompany.tahiti.analysis.repository.Bilu;
import com.mycompany.tahiti.analysis.repository.DataFactory;
import io.swagger.annotations.Api;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bilus")
@Api(description = "Bilu controller")
public class BiluController {
    @Autowired
    JenaLibrary jenaLibrary;

    @Autowired
    DataFactory dataFactory;

    @ResponseBody
    @GetMapping("/{biluId}")
    public BiluRichInfo getBiluById(@PathVariable("biluId") String biluId) {

        Bilu bilu = dataFactory.getBiluById(biluId);
        BiluRichInfo biluRichInfo = new BiluRichInfo();
        biluRichInfo.setId(biluId);
        biluRichInfo.setName(bilu.getName());
        biluRichInfo.setContent(bilu.getContent());
        biluRichInfo.setTags(bilu.getPersons().stream().map(p->p.getName()).collect(Collectors.toList()));

        return biluRichInfo;
    }
}
