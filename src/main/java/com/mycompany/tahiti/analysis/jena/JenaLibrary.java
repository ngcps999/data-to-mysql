package com.mycompany.tahiti.analysis.jena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.Iterator;
import java.util.List;

public interface JenaLibrary {
    Model getModel(String modelName);
    Model getRuntimeModel();
    Model getDefaultModel();
    void removeModel(String modelName);
    void closeDB();

    // read
    List<Statement> getStatements(Model model);
    Iterator<Statement> getStatementsByEntityType(Model model, String type);
    Iterator<Statement> getStatementsById(Model model, String id);
    Iterator<Statement> getStatementsBySP(Model model, Resource resource, String property);
    Iterator<Statement> getStatementsByBatchSP(Model model, List<String> subjects, String property_str);
    Iterator<Statement> getStatementsByPOValue(Model model, String property, String value);
    Iterator<Statement> getStatementsByPO(Model model, String property, Resource object);
    Iterator<Statement> getStatementsByBatchPO(Model model, String property, List<String> objects);

    Iterator<Statement> getStatementsBySourceAndType(Model model, String source, String type);

    // deprecated
    Iterator<Statement> getStatementsBySubjectSubStr(Model model, String substr);

    List<String> getStringValueBySP(Model model, Resource resource, String property);
    List<String> getStringValuesByBatchSP(Model model, List<String> subjects, String property);
    List<String> getObjectNamesBySP(Model model, Resource resource, String property);

    // write
    void persist(List<Statement> statement, String modelName);
    void openReadTransaction();
    void closeTransaction();

    void saveModel(Model newModel, String newModelName);
    void updateRuntimeModelName(String newModelName);

}
