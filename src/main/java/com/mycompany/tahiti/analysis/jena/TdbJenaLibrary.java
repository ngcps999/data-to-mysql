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
    Lock writeLock = readWriteLock.writeLock();

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
            dataset.removeNamedModel(modelName);
            dataset.commit();
        }
    }

    public void openReadTransaction(){
        try {
            writeLock.lock();
            if(dataset.isInTransaction()) {
                dataset.end();
            }
            dataset.begin(ReadWrite.READ);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

    }

    public void openWriteTransaction() {
        try {
            writeLock.lock();
            if(dataset.isInTransaction()) {
                dataset.end();
            }
            dataset.begin(ReadWrite.WRITE);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

    }

    public void closeTransaction(){
        try {
            if(dataset.isInTransaction()) {
                dataset.end();
            }
            writeLock.unlock();
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
            dataset.removeNamedModel(modelName);
            dataset.commit();
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
     * 判断Dataset中是否存在model；
     */
    public boolean findModel(String modelName) {
        boolean result;
        try {
            if (dataset.containsNamedModel(modelName))
                result = true;
            else
                result = false;
        } finally {
        }
        return result;
    }

    /**
     * 列出Dataset中所有model；
     */
    public List<String> listModels() {
        List<String> uriList = new ArrayList<>();
        try {
            Iterator<String> names = dataset.listNames();
            String name;
            while (names.hasNext()) {
                name = names.next();
                uriList.add(name);
            }
        } finally {
        }
        return uriList;
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
}