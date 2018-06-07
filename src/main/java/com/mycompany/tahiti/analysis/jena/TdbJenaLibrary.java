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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TdbJenaLibrary extends BaseJenaLibrary {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    //Lock writeLock = readWriteLock.writeLock();

    private static final Logger LOG = Logger.getLogger(TdbJenaLibrary.class);
    public Dataset dataset = null;

    /**
     * 建立TDB数据文件夹；
     */
    public TdbJenaLibrary(String tdbName, boolean jenaDropExistModel, String modelName) {
        super(jenaDropExistModel, modelName);
        dataset = createDataset(tdbName);
    }

    public Dataset createDataset(String tdbName) {
        return TDBFactory.createDataset(tdbName);
    }

    @Override
    public void openReadTransaction(){
        try {
            //writeLock.lock();
            if(dataset.isInTransaction()) {
                dataset.end();
            }
            dataset.begin(ReadWrite.READ);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

    }
    @Override
    public void openWriteTransaction() {
        try {
            if(dataset.isInTransaction()) {
                dataset.end();
            }
            dataset.begin(ReadWrite.WRITE);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

    }

    @Override
    public void closeTransaction(){
        try {
            dataset.end();
            //writeLock.unlock();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

    }


    /**
     * 删除Dataset中的某个model；
     */
    @Override
    public void removeModel(String modelName) {
        try {
            openWriteTransaction();
            dataset.removeNamedModel(modelName);
            dataset.commit();
            closeTransaction();
            LOG.info(modelName + "：已被移除!");
        } finally {
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
     * 获得Dataset中某个model；
     */
    @Override
    public Model getModel(String modelName) {
        Model model;
        try {
            model = dataset.getNamedModel(modelName);
        } finally {
        }
        return model;
    }

    /**
     * 获取默认模型；
     */
    @Override
    public Model getDefaultModel() {
        Model model;
        try {
            model = dataset.getDefaultModel();
        } finally {
        }
        return model;
    }

    @Override
    public void persist(List<Statement> statements, String modelName)
    {
        if(jenaDropExistModel) {
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

    @Override
    public void saveModel(Model newModel, String newModelName) {
        openWriteTransaction();
        dataset.addNamedModel(newModelName, newModel);
        dataset.commit();
        closeTransaction();
    }
}