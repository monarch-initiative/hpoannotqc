package org.monarchinitiative.hpoannotqc.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Performs sanity-checking on the phenotype.hpoa "big file"
 */

@CommandLine.Command(name = "qc", aliases = {"Q"}, mixinStandardHelpOptions = true, description = "Q/C phenotype.hpoa file")
public class BigFileQcCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigFileQcCommand.class);

    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    /** Default is the current CWD */
    @CommandLine.Option(names = {"-p"},
            description = "Path to phenotype.hpoa file")
    private String phenotypeHpoa = "phenotype.hpoa";

    @CommandLine.Option(names = {"--hpo"},
            description = "Path to hpo.json file")
    private String hpoJson = "data/hp.json";


    record HpoaFileLine(
           TermId databaseId,
           String disease_name,
           String qualifier,
           TermId hpoId,
           String reference) {



        public static HpoaFileLine from(String line){
            String [] parts = line.split("\t");
            if (parts.length != 12){
                throw new PhenolRuntimeException("Malformed HPOA file line: \"" + line +"\" with " + parts.length + " fields (required: 12)");
            }
            TermId databaseId = TermId.of(parts[0]);
            String diseaseName = parts[1];
            String qualifier = parts[2];
            TermId hpoId = TermId.of(parts[3]);
            String reference = parts[4];
            return new HpoaFileLine(
                    databaseId,
                    diseaseName,
                    qualifier,
                    hpoId,
                    reference);
        }
    }



    @Override
    public Integer call() {
        File phenotypeHpoaFile = new File(phenotypeHpoa);
        File hpoJsonFile = new File(hpoJson);
        List<String> errors = new ArrayList<>();
        if (! phenotypeHpoaFile.isFile()) {
            throw new PhenolRuntimeException("Could not find phenotype.hpoa -- run bigfile command");
        }
        if (! hpoJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hpo.json -- run download command");
        }
        Ontology hpo = OntologyLoader.loadOntology(hpoJsonFile);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(phenotypeHpoaFile.getAbsolutePath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                } else if (line.startsWith("database_id")) {
                    continue;
                }
                HpoaFileLine hpoaLine = HpoaFileLine.from(line);
            }
        } catch (PhenolRuntimeException e) {
            errors.add(e.getMessage());
        } catch (IOException ioe) {
            throw new PhenolRuntimeException(ioe);
        }
        if (!errors.isEmpty()) {
            System.err.println("[ERROR] " + phenotypeHpoaFile.getAbsolutePath());
            for (String error : errors) {
                System.err.println(error);
            }
            return 1;
        } else {
            System.out.println("[INFO] No errors detected");
            return 0;
        }
    }

}
