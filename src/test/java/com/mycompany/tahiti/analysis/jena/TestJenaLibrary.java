package com.mycompany.tahiti.analysis.jena;

import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.utils.FileUtils;
import com.mycompany.tahiti.analysis.utils.Utility;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestJenaLibrary {
    @Test
    public void JenaPersistenceTest()
    {
        Configs.loadConfigFile("application.properties");
        JenaLibrary jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jenaConfigFilePath"));;
        String modelName = Configs.getConfig("jenaModelName");

        Model model;
        if (modelName == null) {
            model = jenaLibrary.getDefaultModel();
        } else {
            model = jenaLibrary.getModel(modelName);
        }

        List<String> lines = FileUtils.getFileLines(Utility.getResourcePath("jena/StatementSample.txt"));

        List<Statement> statements = new ArrayList<>();
        for(String line: lines) {
            String[] segments = line.split("    ");

            if(segments.length != 3)
                continue;

            Resource resource = model.createResource(segments[0]);

            Property property = model.createProperty(segments[1]);
            if(!segments[2].startsWith("http"))
            {
                resource.addProperty(property, segments[2]);
            }
            else
            {
                Resource object = model.getResource(segments[2]);
                if(object == null) {
                    Resource obj = model.createResource(segments[2]);
                    resource.addProperty(property, obj);
                }
            }
        }

        jenaLibrary.persist(statements, modelName);
    }
}
