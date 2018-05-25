package com.mycompany.tahiti.analysis.jena;

import com.mycompany.tahiti.analysis.configuration.Configs;
import lombok.val;
import org.apache.jena.rdf.model.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BaseJenaLibrary implements JenaLibrary{
    @Override
    public Model getModel(String modelName) {
        return null;
    }

    @Override
    public Model getDefaultModel() {
        return null;
    }

    @Override
    public void removeModel(String modelName) {

    }

    @Override
    public void clearDB() {

    }

    @Override
    public void closeDB() {

    }

    @Override
    public Iterator<Statement> getStatementsByEntityType(Model model, String type) {
        Property property = model.getProperty("common:type.object.type");
        SimpleSelector simpleSelector = new SimpleSelector(null, property, type);
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsBySP(Model model, Resource resource, String property) {
        Property p = model.getProperty(property);
        SimpleSelector simpleSelector = new SimpleSelector(resource, p, (RDFNode) null);
        return model.listStatements(simpleSelector);
    }

    @Override
    public List<String> getStringValueBySP(Model model, Resource resource, String property)
    {
        Property p = model.getProperty(property);
        SimpleSelector simpleSelector = new SimpleSelector(resource, p, (RDFNode) null);
        val iterator = model.listStatements(simpleSelector);

        List<String> values = new LinkedList<>();
        while(iterator.hasNext())
        {
            Statement statement = iterator.next();
            values.add(statement.getString());
        }
        return values;
    }

    @Override
    public Iterator<Statement> getStatementsBySourceAndType(Model model, String source, String type) {
        SimpleSelector selector = new SimpleSelector(null, null, (RDFNode)null) {
            Property property = model.getProperty("common:type.object.type");
            public boolean selects(Statement st) {
                return st.getSubject().toString().startsWith(source) && Objects.equals(property, st.getPredicate()) && st.getObject().toString().equals(type);
            }
        };
        return model.listStatements(selector);
    }

    @Override
    public Iterator<Statement> getStatementsBySubjectSubStr(Model model, String substr) {
        SimpleSelector selector = new SimpleSelector(null, null, (RDFNode)null) {
            Property property = model.getProperty("common:type.object.type");
            public boolean selects(Statement st) {
                return st.getSubject().toString().contains(substr);
            }
        };
        return model.listStatements(selector);
    }

    @Override
    public Iterator<Statement> getStatementsBySubjectInListAndProperty(Model model,List<String> subjects,String property_str){
        SimpleSelector selector = new SimpleSelector(null,null,(RDFNode) null){
            Property property = model.getProperty(property_str);
            @Override
            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject().toString()) && s.getPredicate().equals(property);
            }
        };
        return model.listStatements(selector);
    }

    @Override
    public void persist(List<Statement> statements, String modelName)
    {
        if(Configs.getConfigBoolean("jenaDropExistModel", false)) {
            removeModel(modelName);
        }
        //val writeLock = readWriteLock.writeLock();
        try {
            //writeLock.lock();
            Model model;
            if (modelName == null) {
                model = getDefaultModel();
            } else {
                model = getModel(modelName);
            }
            model.begin();
            for(val statement: statements) {
                model.add(statement);
            }
            model.commit();
        } finally {
            //writeLock.unlock();
        }
    }

    /**
     * 获取模型中所有Statement
     * @param model
     * @return
     */
    @Override
    public List<Statement> getStatements(Model model) {
        List<Statement> stmts;
        try {
            StmtIterator sIter = model.listStatements() ;
            stmts = new LinkedList<>();
            for ( ; sIter.hasNext() ; )
            {
                stmts.add(sIter.nextStatement());
            }
            sIter.close();
        } finally {
        }
        return stmts;
    }
}
