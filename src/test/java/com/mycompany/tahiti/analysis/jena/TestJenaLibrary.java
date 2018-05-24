package com.mycompany.tahiti.analysis.jena;

import com.google.common.collect.Iterators;
import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.utils.FileUtils;
import lombok.val;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

import java.util.List;

public class TestJenaLibrary {

    MysqlJenaLibrary jenaLibrary;

    public TestJenaLibrary()
    {
        Configs.loadConfigFile("application.properties");
        jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jenaConfigFilePath"));
        jenaLibrary.store.getConnection().getTransactionHandler();
    }

    @Test
    public void JenaPersistenceTest()
    {
        String modelName = Configs.getConfig("jenaTestModelName");
        jenaLibrary.removeModel(modelName);

        Model model;

        if (modelName == null) {
            model = jenaLibrary.getDefaultModel();
        } else {
            model = jenaLibrary.getModel(modelName);
        }

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
        System.out.println(Iterators.size(iter));
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
}
