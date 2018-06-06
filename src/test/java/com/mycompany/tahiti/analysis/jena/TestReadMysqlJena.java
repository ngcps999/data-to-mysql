package com.mycompany.tahiti.analysis.jena;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class TestReadMysqlJena {
    JenaLibrary jenaLibrary;
    public TestReadMysqlJena() {
        //jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jenaConfigFilePath"));
        //jenaLibrary = new MysqlJenaLibrary(Configs.getConfig("jdbcUrl"), Configs.getConfig("mysqlUser"), Configs.getConfig("mysqlPassword"));

    }
    //@Test
    public void readMysqlJenaTest() {
        Model model = jenaLibrary.getRuntimeModel();

        val iter = jenaLibrary.getStatementsBySP(model, null, "common:type.object.name");

//        val iter = model.listStatements();
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
}
