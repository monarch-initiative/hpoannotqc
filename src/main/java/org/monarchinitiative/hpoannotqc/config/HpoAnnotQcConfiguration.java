package org.monarchinitiative.hpoannotqc.config;


import org.monarchinitiative.hpoannotqc.HpoAnnotQc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class HpoAnnotQcConfiguration {
    public static final Logger logger = LoggerFactory.getLogger(HpoAnnotQcConfiguration.class);

    private Environment env;


    public HpoAnnotQcConfiguration(Environment environ) { this.env=environ; }

    @Bean
    public Path hpOboPath() {
        return Paths.get(env.getProperty("hpo")).toAbsolutePath();
    }

    @Bean
    public Path annotationsPath() {
        return Paths.get(env.getProperty("annotations")).toAbsolutePath();
    }

    @Bean
    public HpoAnnotQc hpoAnnotQc(Path hpOboPath, Path annotationsPath){
        return new HpoAnnotQc(hpOboPath,annotationsPath);
    }


}
