package com.mycompany.tahiti.analysis.jena;

import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.utils.FileUtils;
import lombok.val;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

import java.util.List;

public class TestJenaLibrary {

    Model model;

    public TestJenaLibrary()
    {
        Configs.loadConfigFile("application.properties");
        MysqlJenaLibrary jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jenaConfigFilePath"));
        jenaLibrary.store.getConnection().getTransactionHandler();

        String modelName = Configs.getConfig("jenaTestModelName");
        jenaLibrary.removeModel(modelName);

        if (modelName == null) {
            model = jenaLibrary.getDefaultModel();
        } else {
            model = jenaLibrary.getModel(modelName);
        }
    }

    @Test
    public void JenaPersistenceTest()
    {
        model.begin();

        List<String> lines = FileUtils.getFileLines("jena/StatementSample.txt");

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

        model.commit();

        val iter = model.listStatements();
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
}
