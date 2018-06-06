package com.mycompany.tahiti.analysis.jena;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Test;

public class TestReadTdbJena {
    @Test
    public void testReadTdbJena() {
        TdbJenaLibrary tdbJenaLibrary = new TdbJenaLibrary("src/main/resources/TDB", false, "biluDev");
        tdbJenaLibrary.openReadTransaction();
        Model model = tdbJenaLibrary.getModel("biluProd");
        val iter = model.listStatements(null, model.getProperty("gongan:gongan.case.bilu"), (RDFNode) null);
        while(iter.hasNext()) {
            System.out.println(iter.next().getObject().getClass());
        }
        tdbJenaLibrary.closeTransaction();
    }
}
