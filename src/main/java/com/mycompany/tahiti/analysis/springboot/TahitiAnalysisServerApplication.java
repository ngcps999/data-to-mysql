package com.mycompany.tahiti.analysis.springboot;

import com.mycompany.tahiti.analysis.fusion.FusionEngine;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.repository.DataFactory;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
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
@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class TahitiAnalysisServerApplication {
    @Autowired
    DataFactory dataFactory;

    @Autowired
    JenaLibrary jenaLibrary;

    @Value("${conflation.enable-fusion}") String enableFusion;
    @Value("${conflation.conflatedModelName}") String newModelName;
    @Value("${conflation.subject-prefix}") String subjectPrefix;

    private static final Logger LOG = Logger.getLogger(TahitiAnalysisServerApplication.class);

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowCredentials(true);
            }
        };
    }

    @Bean
    @Autowired
    public DataFactory createDataFactory(JenaLibrary jenaLibrary) {
        conflate(jenaLibrary);
        dataFactory.getAllCaseBaseInfo();
        dataFactory.getPersonRelation();
        dataFactory.getPhoneCaseRelationCache();
        LOG.info("DataFactory is created!");
        return dataFactory;
    }

    public void conflate(JenaLibrary jenaLibrary) {
        if(enableFusion.trim().toLowerCase().equals("true")){
            FusionEngine fusionEngine = new FusionEngine(jenaLibrary, subjectPrefix);
            Model model = fusionEngine.generateFusionModel();

            jenaLibrary.removeModel(newModelName);
            jenaLibrary.saveModel(model, newModelName);
            jenaLibrary.updateRuntimeModelName(newModelName);
            jenaLibrary.updateCacheModel();
        }
    }

    public static void main(String[] args){
        val context = SpringApplication.run(TahitiAnalysisServerApplication.class,args);
    }
}