package org.monarchinitiative.hpoannotqc.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Performs sanity-checking on the phenotype.hpoa "big file"
 */

@CommandLine.Command(name = "big-file-qc", aliases = {"Q"}, mixinStandardHelpOptions = true, description = "Q/C phenotype.hpoa file")
public class BigFileQcCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    /** Default is the current CWD */
    @CommandLine.Option(names = {"-b"},
            description = "Path to phenotype.hpoa file")
    private String phenotypeHpoa = "phenotype.hpoa";

    @CommandLine.Option(names = {"--hpo"},
            description = "Path to hpo.json file", required = true)
    private String hpoJson;


    @Override
    public Integer call() throws Exception {
        File phenotypeHpoaFile = new File(phenotypeHpoa);
        File hpoJsonFile = new File(hpoJson);
        if (! phenotypeHpoaFile.isFile()) {
            throw new PhenolRuntimeException("Could not find phenotype.hpoa -- run bigfile command");
        }
        if (! hpoJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hpo.json -- run download command");
        }
        Ontology hpo = OntologyLoader.loadOntology(hpoJsonFile);

        try (BufferedReader br = new BufferedReader(new FileReader(phenotypeHpoaFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
