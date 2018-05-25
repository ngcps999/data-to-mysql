package com.mycompany.tahiti.analysis.jena;

import com.mycompany.tahiti.analysis.configuration.Configs;
import lombok.val;
import org.apache.jena.rdf.model.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public Iterator<Statement> getStatementsByBatchSP(Model model, List<String> subjects, String property) {
        Property p = model.getProperty(property);
        SimpleSelector simpleSelector = new SimpleSelector(null, p, (RDFNode) null){
            @Override
            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject().toString());
            }
        };
        return model.listStatements(simpleSelector);
    }

    @Override
    public Iterator<Statement> getStatementsByPO(Model model, String property, String value)
    {
        SimpleSelector simpleSelector = new SimpleSelector(null, model.getProperty(property), value);
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
    public List<String> getStringValuesByBatchSP(Model model, List<String> subjects, String property)
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

    public List<String> getExpectedObjectNamesBySP(Model model, Resource resource, String property, String type)
    {
        Property p = model.getProperty(property);

        // r1 = select O from full Where S = resource and P = property;
        SimpleSelector firstSelector = new SimpleSelector(resource, p, (RDFNode) null);
        val resourceProperty = model.listStatements(firstSelector);

        // get candidates
        val objects = resourceProperty.toList().stream().map(iter->iter.getResource().toString()).collect(Collectors.toList());

        // filter candidates
        // r2 = select S from full INNER JOIN r1 where full.S = r1.o and p == "common:type.object.type" and o == type;
        SimpleSelector secondSelector = new SimpleSelector(null, model.getProperty("common:type.object.type"), type) {
            @Override
            public boolean selects(Statement s) {
                return objects.contains(s.getSubject().toString());
            }
        };

        val objectIterator = model.listStatements(secondSelector);

        // get candidate
        val subjects = objectIterator.toList().stream().map(iter->iter.getSubject().toString()).collect(Collectors.toList());

        // get names
        // r3 = select o from full inner join r2 where full.s == r2.s and p = "common:type.object.name";
        SimpleSelector nameSelector = new SimpleSelector(null, model.getProperty("common:type.object.name"), (RDFNode) null) {
            @Override
            public boolean selects(Statement s) {
                return subjects.contains(s.getSubject().toString());
            }
        };

        val nameIters = model.listStatements(nameSelector);
        return nameIters.toList().stream().map(iter->iter.getString()).collect(Collectors.toList());
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
}
