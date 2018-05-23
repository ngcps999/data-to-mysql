package com.mycompany.tahiti.analysis.configuration;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 配置信息读取
 */
public class Configs {
    private static Map<String, String> configMap = new HashMap<>();
    public static boolean loaded = false;
    private static final Logger logger = Logger.getLogger(Configs.class.getName());

    /**
     * Get config value from loaded config file.
     * @param key	Config key
     * @return	Corresponding config value
     */
    public static String getConfig(String key){
        if(!loaded){
            throw new RuntimeException("未加载任何配置文件，请先调用loadConfigFile来加载配置文件");
        }
        return configMap.get(key);
    }

    /**
     * Get integer config value from loaded config file.
     * @param key	Config key
     * @param defaultValue	Default value when parse failed
     * @return	Corresponding integer config value.
     */
    public static int getConfigInt(String key, int defaultValue){
        int res = defaultValue;
        if(!loaded){
            throw new RuntimeException("未加载任何配置文件，请先调用loadConfigFile来加载配置文件");
        }
        try {
            res = Integer.parseInt(configMap.get(key));
        }catch (Exception e){}
        return res;
    }

    /**
     * Get boolean config value from loaded config file.
     * @param key	Config key
     * @param defaultValue	Default value when parse failed
     * @return	Corresponding boolean config value.
     */
    public static boolean getConfigBoolean(String key, boolean defaultValue){
        boolean res = defaultValue;
        if(!loaded){
            throw new RuntimeException("未加载任何配置文件，请先调用loadConfigFile来加载配置文件");
        }
        try {
            res = Boolean.parseBoolean(configMap.get(key));
        }catch (Exception e){}
        return res;
    }

    /**
     * Load all configurations from external config file
     */
    public static void loadConfigFile(String configName){
        synchronized(Configs.class){
            loaded = true;
            configMap.clear();
            //Load all configs from config file to configMap.
            Properties properties = new Properties();
            InputStream in = null;
            try {
                String configFile = "";
                if(OverallSettings.debug)
                    configFile = "/" + OverallSettings.configTestDir + "/" + configName;
                else
                    configFile = "/" + OverallSettings.configDir + "/" + configName;
                in = Configs.class.getResourceAsStream(configFile);
                properties.load(in);
                Iterator it = properties.keySet().iterator();	//load all configurations to configMap
                while(it.hasNext()){
                    String k = it.next().toString();
                    configMap.put(k, properties.get(k).toString().trim());
                }
            } catch (Exception e) {
                System.out.println("Exception when load customer config: " + e.getMessage());
            }finally{
                try { in.close(); } catch (IOException e) { }
            }
        }
    }

    /**
     * 打印当前config
     */
    public static void printConfigs(){
        logger.info("=========================== CONFIGURATIONS ===========================");
        configMap.forEach((k,v)-> logger.info(k + ": " + v));
    }
}