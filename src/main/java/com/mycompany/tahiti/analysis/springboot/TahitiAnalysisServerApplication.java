package com.mycompany.tahiti.analysis.springboot;

import com.mycompany.tahiti.analysis.configuration.Configs;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.jena.MysqlJenaLibrary;
import com.mycompany.tahiti.analysis.jena.TdbJenaPersistence;
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

    @Bean
    public JenaLibrary createJenaLibrary() {
        //return new MysqlJenaLibrary(Configs.getConfig("jdbcUrl"), Configs.getConfig("mysqlUser"), Configs.getConfig("mysqlPassword"));
        return new TdbJenaPersistence(Configs.getConfig("tdbName"));
    }

    public TahitiAnalysisServerApplication(@Value("${tdbName}") String tdbName){
        Configs.loadConfigFile("application.properties");
        Configs.addConfig("tdbName", tdbName);
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

    public static void main(String[] args){
        SpringApplication.run(TahitiAnalysisServerApplication.class,args);
    }
}
