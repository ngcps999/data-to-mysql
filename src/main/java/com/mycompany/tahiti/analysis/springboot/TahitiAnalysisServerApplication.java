package com.mycompany.tahiti.analysis.springboot;

import com.mycompany.tahiti.analysis.repository.DataFactory;
import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.TdbJenaLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableAutoConfiguration
@Configuration
@ComponentScan("com.mycompany.tahiti")
@EnableSwagger2
public class TahitiAnalysisServerApplication {
    @Autowired
    DataFactory dataFactory;

    @Bean
    public TdbJenaLibrary createTdbJenaLibrary() {
        //return new MysqlJenaLibrary(Configs.getConfig("jdbcUrl"), Configs.getConfig("mysqlUser"), Configs.getConfig("mysqlPassword"));
        return new TdbJenaLibrary(Configs.getConfig("tdbName"));
    }

    public TahitiAnalysisServerApplication(@Value("${tdbName}") String tdbName, @Value("${jenaModelName}") String jenaModelName){
        Configs.loadConfigFile("application.properties");
        Configs.addConfig("tdbName", tdbName);
        Configs.addConfig("jenaModelName", jenaModelName);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("*");
            }
        };
    }

    @Bean
    public DataFactory createDataFactory() {
        dataFactory.getAllCaseBaseInfo();
        return dataFactory;
    }

    public static void main(String[] args){
        SpringApplication.run(TahitiAnalysisServerApplication.class,args);
    }
}
