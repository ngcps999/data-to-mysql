package com.mycompany.tahiti.analysis.configuration;

/**
 * 一些全局设置。和具体运行环境、运行的pipeline无关的。
 */
public interface OverallSettings {
    boolean debug = false; //是否是debug模式
    String configDir = "config";    //配置文件所在的目录（都在resources下）
    String configTestDir = "config_test";    //测试配置文件所在的目录（都在resources下）
}