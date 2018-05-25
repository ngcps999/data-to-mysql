package com.mycompany.tahiti.analysis.jena;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class TestReadTdbJena {
    @Test
    public void testReadTdbJena() {
        TdbJenaLibrary tdbJenaLibrary = new TdbJenaLibrary("src/main/resources/TDB");
        tdbJenaLibrary.openReadTransaction();
        Model model = tdbJenaLibrary.getModel("biluV4");
        val iter = model.listStatements();
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }
    }
}
