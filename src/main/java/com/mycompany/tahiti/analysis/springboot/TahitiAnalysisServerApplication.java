package com.mycompany.tahiti.analysis.springboot;

import com.mycompany.tahiti.analysis.fusion.FusionEngine;
import com.mycompany.tahiti.analysis.jena.JenaLibrary;
import com.mycompany.tahiti.analysis.repository.DataFactory;
import lombok.val;
import org.apache.jena.rdf.model.Model;
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

    @Autowired
    JenaLibrary jenaLibrary;

    @Value("${engine.enable-fusion}") String enableFusion;
    @Value("${engine.modelName}") String newModelName;

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
        val context = SpringApplication.run(TahitiAnalysisServerApplication.class,args);
        val app = context.getBean(TahitiAnalysisServerApplication.class);
        if(app.enableFusion.trim().toLowerCase().equals("true")){
            FusionEngine fusionEngine = new FusionEngine();
            Model model = fusionEngine.GenerateFusionModel(app.newModelName);

            app.jenaLibrary.saveModel(model, app.newModelName);
            app.jenaLibrary.updateRuntimeModelName(app.newModelName);
        }
    }
}