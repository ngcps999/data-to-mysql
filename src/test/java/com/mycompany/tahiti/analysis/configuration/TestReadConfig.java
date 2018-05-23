package com.mycompany.tahiti.analysis.configuration;

import org.junit.Test;

public class TestReadConfig {
    @Test
    public void testRedConfig() {
        Configs.loadConfigFile("application.properties");
        Configs.printConfigs();
    }
}
