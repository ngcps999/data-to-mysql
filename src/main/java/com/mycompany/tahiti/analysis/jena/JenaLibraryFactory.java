package com.mycompany.tahiti.analysis.jena;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JenaLibraryFactory {
    @Value("${jena.tdbName}")
    String tdbName;
    @Value("${jena.dropExistModel}")
    String jenaDropExistModel;
    @Value("${jena.modelName}")
    String modelName;
    @Value("${jena.type}")
    String jenaType;
    @Value("${jena.fusekiURI}")
    String fusekiURI;

    @Bean
    public JenaLibrary createTdbJenaLibrary() {
        switch (jenaType) {
            case "tdb":{return new TdbJenaLibrary(tdbName, jenaDropExistModel.toLowerCase().trim().equals("true"), modelName);}
            case "fuseki": {return new FusekiJenaLibrary(fusekiURI, jenaDropExistModel.toLowerCase().trim().equals("true"), modelName);}
            default:throw new RuntimeException("Currently not support ");
        }
    }
}
