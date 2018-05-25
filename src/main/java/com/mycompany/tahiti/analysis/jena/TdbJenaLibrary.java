package com.mycompany.tahiti.analysis.jena;

import lombok.val;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TdbJenaLibrary extends BaseJenaLibrary {

   // private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    //Lock writeLock = readWriteLock.writeLock();

    private static final Logger LOG = Logger.getLogger(TdbJenaLibrary.class);
    public Dataset dataset = null;

    /**
     * 建立TDB数据文件夹；
     */
    public TdbJenaLibrary(String tdbName) {
        dataset = TDBFactory.createDataset(tdbName);
    }

    @Override
    public void clearDB() {
        for(val modelName: listModels()) {
            dataset.begin(ReadWrite.WRITE);
            dataset.removeNamedModel(modelName);
            dataset.commit();
            dataset.end();
        }
    }

    public void openReadTransaction(){
        dataset.begin(ReadWrite.READ);
    }

    public void opneWriteTransaction() {
        dataset.begin(ReadWrite.WRITE);
    }

    public void closeTransaction(){
        dataset.end();
    }


    /**
     * 删除Dataset中的某个model；
     */
    @Override
    public void removeModel(String modelName) {
        if (!dataset.isInTransaction())
            dataset.begin(ReadWrite.WRITE);
        try {
            //writeLock.lock();
            dataset.removeNamedModel(modelName);
            dataset.commit();
            LOG.info(modelName + "：已被移除!");
        } finally {
            dataset.end();
            //writeLock.unlock();
        }
    }

    /**
     * 关闭TDB连接；
     */
    @Override
    public void closeDB() {
        dataset.close();
    }

    /**
     * 判断Dataset中是否存在model；
     */
    public boolean findModel(String modelName) {
        boolean result;
        //val writeLock = readWriteLock.writeLock();
        dataset.begin(ReadWrite.READ);
        try {
            //writeLock.lock();
            if (dataset.containsNamedModel(modelName))
                result = true;
            else
                result = false;
        } finally {
            dataset.end();
            //writeLock.unlock();
        }
        return result;
    }

    /**
     * 列出Dataset中所有model；
     */
    public List<String> listModels() {
        dataset.begin(ReadWrite.READ);
        //val writeLock = readWriteLock.writeLock();
        List<String> uriList = new ArrayList<>();
        try {
            //writeLock.lock();
            Iterator<String> names = dataset.listNames();
            String name;
            while (names.hasNext()) {
                name = names.next();
                uriList.add(name);
            }
        } finally {
            dataset.end();
            //writeLock.unlock();
        }
        return uriList;
    }

    /**
     * 获得Dataset中某个model；
     */
    @Override
    public Model getModel(String modelName) {
        Model model;
        //val writeLock = readWriteLock.writeLock();
        //dataset.begin(ReadWrite.READ);
        try {
            //writeLock.lock();
            model = dataset.getNamedModel(modelName);
        } finally {
            //dataset.end();
            //writeLock.unlock();
        }
        return model;
    }

    /**
     * 获取默认模型；
     */
    @Override
    public Model getDefaultModel() {
        //val writeLock = readWriteLock.writeLock();
        dataset.begin(ReadWrite.READ);
        Model model;
        try {
            //writeLock.lock();
            model = dataset.getDefaultModel();
        } finally {
            dataset.end();
            //writeLock.unlock();
        }
        return model;
    }

    /**
     * 获取模型中所有Statement
     * @param model
     * @return
     */
    @Override
    public List<Statement> getStatements(Model model) {
        //val writeLock = readWriteLock.writeLock();
        List<Statement> stmts;
        try {
            //writeLock.lock();
            dataset.begin(ReadWrite.READ);
            StmtIterator sIter = model.listStatements() ;
            stmts = new LinkedList<>();
            for ( ; sIter.hasNext() ; )
            {
                stmts.add(sIter.nextStatement());
            }
            sIter.close();
        } finally {
            dataset.end();
            //writeLock.lock();
        }
        return stmts;
    }

    @Override
    public void persist(List<Statement> statement, String modelName) {

    }
}