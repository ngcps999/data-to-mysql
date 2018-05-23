package com.mycompany.tahiti.analysis.jena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Iterator;
import java.util.List;

public interface JenaLibrary {
    Model getModel(String modelName);
    Model getDefaultModel();
    void removeModel(String modelName);
    void clearDB();
    void closeDB();

    // read
    List<Statement> getStatements(Model model);
    Iterator<Statement> getStatementsByEntityType(Model model, String type);
    Iterator<Statement> getStatementsBySP(Model model, Resource resource, String property);
    Iterator<Statement> getStatementsBySourceAndType(Model model, String source, String type);
    Iterator<Statement> getStatementsBySubjectSubStr(Model model, String substr);

    // write
    void persist(List<Statement> statement, String modelName);
}
