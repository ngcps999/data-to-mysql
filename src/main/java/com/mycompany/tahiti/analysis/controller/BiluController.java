package com.mycompany.tahiti.analysis.controller;

import com.google.gson.Gson;
import com.mycompany.tahiti.analysis.model.BiluRichInfo;
import com.mycompany.tahiti.analysis.model.CrimeComponent.CrimeComponent;
import com.mycompany.tahiti.analysis.repository.Bilu;
import com.mycompany.tahiti.analysis.repository.DataFactory;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/bilus")
@Api(description = "Bilu controller")
public class BiluController {
    @Autowired
    DataFactory dataFactory;

    @ResponseBody
    @GetMapping("/{biluId}")
    public BiluRichInfo getBiluById(@PathVariable("biluId") String biluId) {

        String subjectId = dataFactory.getSubjectIdById(biluId);

        Bilu bilu = dataFactory.getBiluById(subjectId);
        BiluRichInfo biluRichInfo = new BiluRichInfo();
        biluRichInfo.setId(biluId);
        biluRichInfo.setName(bilu.getName());
        biluRichInfo.setContent(bilu.getContent());
        biluRichInfo.setTags(bilu.getPersons().stream().map(p->p.getName()).collect(Collectors.toList()));
        biluRichInfo.setCrimeComponent(new Gson().fromJson(dataFactory.getBiluCrimeComponent(subjectId), CrimeComponent.class));

        return biluRichInfo;
    }
}
