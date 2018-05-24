package com.mycompany.tahiti.analysis.jena;

import com.mycompany.tahiti.analysis.configuration.Configs;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class TestReadMysqlJena {
    JenaLibrary jenaLibrary;
    public TestReadMysqlJena() {
        Configs.loadConfigFile("application.properties");
        //jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jenaConfigFilePath"));
        jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jdbcUrl"), Configs.getConfig("mysqlUser"), Configs.getConfig("mysqlPassword"));

    }
    @Test
    public void readMysqlJenaTest() {
        Model model = jenaLibrary.getModel(Configs.getConfig("jenaMappingModel"));
        val iter = model.listStatements();
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
}
