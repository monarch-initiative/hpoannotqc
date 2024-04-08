package org.monarchinitiative.hpoannotqc.cmd;


import org.monarchinitiative.hpoannotqc.annotations.HpoAnnotationEntry;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotationModelException;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteTermIdException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Performs sanity-checking on the phenotype.hpoa "big file"
 */

@CommandLine.Command(name = "big-file-qc", aliases = {"Q"}, mixinStandardHelpOptions = true, description = "Q/C phenotype.hpoa file")
public class BigFileQcCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BigFileQcCommand.class);

    @CommandLine.Option(names={"-a","--annot"},
            description = "Path to directory with the ca. 7900 HPO Annotation files",
            required = true)
    private String hpoAnnotationFileDirectory;
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

    private int validLine = 0;
    private int invalidLine = 0;


    @Override
    public Integer call() {
        File phenotypeHpoaFile = new File(phenotypeHpoa);
        File hpoJsonFile = new File(hpoJson);
        if (! phenotypeHpoaFile.isFile()) {
            throw new PhenolRuntimeException("Could not find phenotype.hpoa -- run bigfile command");
        }
        if (! hpoJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hpo.json -- run download command");
        }
        Ontology hpo = OntologyLoader.loadOntology(hpoJsonFile);
        File smallFileDirectory = new File(hpoAnnotationFileDirectory);
        if (!smallFileDirectory.exists()) {
            throw new PhenolRuntimeException("Could not find " + smallFileDirectory + " (We were expecting the directory with the HPO disease annotation files");
        } else if (!smallFileDirectory.isDirectory()) {
            throw new PhenolRuntimeException(smallFileDirectory.getAbsolutePath() + " is not a directory (We were expecting the directory with the HPO disease annotation files");
        } else {
            for (File f: getListOfV2SmallFiles(String.valueOf(smallFileDirectory))) {
                try {
                    qcOneSmallFile(f, hpo);
                } catch (PhenolRuntimeException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        System.out.printf("Valid lines: %d; invalid lines: %d\n", validLine, invalidLine);

        return null;
    }

    private void qcOneSmallFile(File smallFile, Ontology hpo) throws PhenolRuntimeException{
        try (BufferedReader br = new BufferedReader(new FileReader(smallFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue; // header
                try {
                    HpoAnnotationEntry entry = HpoAnnotationEntry.fromLine(line, hpo);
                    validLine++;
                } catch (HpoAnnotationModelException | ObsoleteTermIdException ham) {
                    System.err.printf("%s: %s\n", smallFile.getName(), ham.getMessage());
                    invalidLine++;
                }
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }


    private String baseName(Path path) {
        String bname = path.getFileName().toString();
        bname = bname.replace('-', ':').replace(".tab", "");
        return bname;
    }

    private List<File> getListOfV2SmallFiles(String smallFileDirectory) {
        List<File> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(smallFileDirectory))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    String basename = baseName(path);
                    fileNames.add(new File(path.toString()));
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return fileNames;
    }

}
