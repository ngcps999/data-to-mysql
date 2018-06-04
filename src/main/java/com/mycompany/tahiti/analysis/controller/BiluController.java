package com.mycompany.tahiti.analysis.controller;

import com.google.common.collect.Lists;
import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.BiluRichInfo;
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
@RequestMapping("/api/bilus")
@Api(description = "Bilu controller")
public class BiluController {
    @Autowired
    TdbJenaLibrary tdbJenaLibrary;

    @ResponseBody
    @GetMapping("/{biluId}")
    public BiluRichInfo getBiluById(@PathVariable("biluId") String biluId) {
        try{
            tdbJenaLibrary.openReadTransaction();
            Model model = tdbJenaLibrary.getModel(Configs.getConfig("jenaModelName"));
            Iterator<Statement> statements_iterator = tdbJenaLibrary.getStatementsById(model,biluId);
            Resource bilu_subject = null;
            while(statements_iterator.hasNext()){
                bilu_subject = statements_iterator.next().getSubject();
                break;
            }
            if(bilu_subject==null)return null;

            //bilu name
            List<String> bilu_name_list = tdbJenaLibrary.getStringValueBySP(model,bilu_subject,"common:type.object.name");
            String bilu_name = bilu_name_list.size()>0?bilu_name_list.get(0):"";
            //bilu content
            List<String> bilu_content_list = tdbJenaLibrary.getStringValueBySP(model,bilu_subject,"common:common.document.contentStream");
            String bilu_content = bilu_content_list.size()>0?bilu_content_list.get(0):"";

            //bilu tags
            val entities = Lists.newArrayList(tdbJenaLibrary.getStatementsBySP(model, bilu_subject, "gongan:gongan.bilu.entity")).stream().map(s -> s.getResource().toString()).distinct().collect(Collectors.toList());
            val persons = Lists.newArrayList(tdbJenaLibrary.getStatementsByPO(model, "common:type.object.type", "common:person.person")).stream().map(s -> s.getSubject().toString()).distinct().collect(Collectors.toList());
            // join entities.o and person.s to get all persons
            persons.retainAll(entities);

            List<String> tags = tdbJenaLibrary.getStringValuesByBatchSP(model, persons, "common:type.object.name").stream().distinct().collect(Collectors.toList());

            BiluRichInfo bilu = new BiluRichInfo();
            bilu.setId(biluId);
            bilu.setName(bilu_name);
            bilu.setContent(bilu_content);
            bilu.setTags(tags);

            return bilu;

        }finally {
            tdbJenaLibrary.closeTransaction();
        }
    }
}
