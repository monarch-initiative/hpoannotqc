package org.monarchinitiative.hpoannotqc;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This application will perform Q/C on the HPO rare disease "small file" annotation files, will transform the
 * small files into an integrate "large" file (phenotype_annotation.tab), and print a report to file.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.3 (2018-01-02)
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
