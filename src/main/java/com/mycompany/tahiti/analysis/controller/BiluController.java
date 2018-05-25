package com.mycompany.tahiti.analysis.controller;

import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import com.mycompany.tahiti.analysis.model.BiluBaseInfo;
import com.mycompany.tahiti.analysis.model.BiluRichInfo;
import com.mycompany.tahiti.analysis.model.Person;
import io.swagger.annotations.Api;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
            List<String> bilu_content_list = tdbJenaLibrary.getStringValueBySP(model,bilu_subject,"common:common.document.contentStreame");
            String bilu_content = bilu_content_list.size()>0?bilu_content_list.get(0):"";
            //bilu tags
            List<String> tags = tdbJenaLibrary.getStringValueBySP(model,bilu_subject,"common:type.object.tag");

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
