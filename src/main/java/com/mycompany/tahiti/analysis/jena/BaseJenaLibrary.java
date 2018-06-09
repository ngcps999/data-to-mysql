package com.mycompany.tahiti.analysis.jena;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.val;
import org.apache.jena.rdf.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class BaseJenaLibrary implements JenaLibrary{
    protected boolean jenaDropExistModel = false;
    protected String modelName;
    protected Model cacheModel = null;

    public BaseJenaLibrary(boolean jenaDropExistModel, String modelName) {
        this.jenaDropExistModel = jenaDropExistModel;
        this.modelName = modelName;
    }

    @Override
    public String getModelName(){
        return modelName;
    }

    @Override
    public Model getModel(String modelName) {
        return null;
    }

    @Override
    public Model getLatestModel() {
        cacheModel = getModel(modelName);
        return cacheModel;
    }

    @Override
    public Model getRuntimeModel() {
        if(cacheModel == null) {
            cacheModel = getModel(modelName);
        }
        return cacheModel;
    }

    @Override
    public void updateCacheModel() {
        cacheModel = getModel(modelName);
    }

    @Override
    public Model getDefaultModel() {
        return null;
    }

    @Override
    public void removeModel(String modelName) {

    }

    @Override
    public void saveModel(Model newModel, String newModelName) {
    }

    @Override
    public void updateRuntimeModelName(String newModelName){
        modelName = newModelName;
    }

    @Override
    public Model deepCopyModel(Model model){
        return ModelFactory.createDefaultModel().add(model);
    }

    @Override
    public List<Statement> getResultByPOContains(String p, String o) {
        openReadTransaction();
        val model = getRuntimeModel();
        SimpleSelector simpleSelector = new SimpleSelector() {
            @Override
            public boolean selects(Statement s) {
                return s.getPredicate().toString().contains(p) && s.getObject().toString().contains(o);
            }
        };
        val statements = Lists.newArrayList(model.listStatements(simpleSelector));
        closeTransaction();
        return statements;
    }

    @Override
    public void closeDB() {

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

    @Override
    public Iterator<Statement> getStatementsByEntityType(Model model, String type) {
        Property property = model.getProperty("common:type.object.type");
        SimpleSelector simpleSelector = new SimpleSelector(null, property, type);
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsById(Model model, String id) {
        Property property = model.getProperty("common:type.object.id");
        SimpleSelector simpleSelector = new SimpleSelector(null, property, id);
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsBySP(Model model, Resource resource, String property) {
        Property p = model.getProperty(property);
        SimpleSelector simpleSelector = new SimpleSelector(resource, p, (RDFNode) null);
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsByBatchSP(Model model, HashSet<String> subjects, String property_str) {
        SimpleSelector selector = new SimpleSelector(null, null, (RDFNode) null) {
            Property property = model.getProperty(property_str);

            @Override
            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject().toString()) && s.getPredicate().equals(property);
            }
        };
        return model.listStatements(selector);
    }

    @Override
    public Iterator<Statement> getStatementsByPOValue(Model model, String property, String value)
    {
        SimpleSelector simpleSelector = new SimpleSelector(null, model.getProperty(property), value);
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsByPO(Model model, String property, Resource object)
    {
        SimpleSelector simpleSelector = new SimpleSelector(null, model.getProperty(property), object);
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsByBatchPO(Model model, String property, HashSet<String> objects)
    {
        SimpleSelector simpleSelector = new SimpleSelector(null, model.getProperty(property), (RDFNode)null){
            public boolean selects(Statement st) {
                return objects.contains(st.getObject().toString());
            }
        };
        return model.listStatements(simpleSelector);
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
    public List<String> getStringValuesByBatchSP(Model model, HashSet<String> subjects, String property)
    {
        Property p = model.getProperty(property);
        SimpleSelector simpleSelector = new SimpleSelector(null, p, (RDFNode) null) {
            @Override
            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject().toString());
            }
        };

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
    public List<String> getObjectNamesBySP(Model model, Resource resource, String property)
    {
        Property p = model.getProperty(property);
        SimpleSelector simpleSelector = new SimpleSelector(resource, p, (RDFNode) null);
        val iterator = model.listStatements(simpleSelector);

        val subjects = iterator.toList().stream().map(iter->iter.getResource().toString()).collect(Collectors.toList());

        SimpleSelector selector = new SimpleSelector(null, model.getProperty("common:type.object.name"),(RDFNode) null) {
            @Override
            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject().toString());
            }
        };

        val nameIters = model.listStatements(selector);

        return nameIters.toList().stream().map(iter->iter.getString()).collect(Collectors.toList());
    }

    @Override
    public void persist(List<Statement> statement, String modelName) {

    }

    @Override
    public void openReadTransaction() {

    }

    @Override
    public void openWriteTransaction() {

    }

    @Override
    public void closeTransaction() {

    }
}
