package com.mycompany.tahiti.analysis.jena;

import com.mycompany.tahiti.analysis.utils.Utility;
import lombok.val;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sdb.SDBFactory;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.sql.JDBC;
import org.apache.jena.sdb.sql.SDBConnection;
import org.apache.jena.sdb.store.DatabaseType;
import org.apache.jena.sdb.store.LayoutType;
import org.apache.jena.sdb.util.StoreUtils;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MysqlJenaLibrary extends BaseJenaLibrary{
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private static final Logger logger = Logger.getLogger(MysqlJenaLibrary.class.getName());
    private static final String NS = "http://knowledge.richInfo.com/";
    public Store store = null;

    /**
     * 以数据库连接初始化Store
     */
    public MysqlJenaLibrary(String jdbcUrl, String user, String pw, boolean jenaDropExistModel, String modelName) {
        super(jenaDropExistModel, modelName);
        StoreDesc desc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
        // 加载mysql驱动
        JDBC.loadDriverMySQL();
        SDBConnection conn = new SDBConnection(jdbcUrl, user, pw);
        this.store = SDBFactory.connectStore(conn, desc);
        initDB();
        logger.info("initialize store using jdbcUrl");
    }

    /**
     * 以配置文件初始化Store
     */
    public MysqlJenaLibrary(String configFilePath, boolean jenaDropExistModel, String modelName) {
        super(jenaDropExistModel, modelName);
        this.store = SDBFactory.connectStore(Utility.getResourcePath(configFilePath));
        initDB();
        logger.info("initialize store using config file from " + configFilePath);
    }

    /**
     * 初始化mysql数据库
     */
    private void initDB() {
        try {
            if( StoreUtils.isFormatted(this.store) == false)
            {
                logger.info("initialize database");
                logger.info("create table format and truc data");
                store.getTableFormatter().create();
            }
        } catch (SQLException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    /**
     * 清空数据
     */
    public void clearDB() {
        this.store.getTableFormatter().truncate();
        logger.info("clear all the data in the SDB");
    }

    public void closeDB() {
        this.store.close();
        logger.info("close store");
    }


    /**
     * 清除Store中的某个model中的数据;
     * @param modelName
     */
    public void removeModel(String modelName) {
        val writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            Model model = getModel(modelName);
            model.removeAll();
            logger.info("clear all the data in the model " + modelName);
        } finally {
            writeLock.unlock();
        }

    }
    /**
     * 获取store中的某个模型
     * @param modelName
     * @return
     */
    public Model getModel(String modelName) { return SDBFactory.connectNamedModel(this.store, modelName); }
    /**
     * 获取默认模型
     * @return
     */
    public Model getDefaultModel() { return SDBFactory.connectDefaultModel(this.store); }
}

