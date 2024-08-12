package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.hpoannotqc.annotations.*;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorReport;
import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoProjectAnnotationFileIngestor;
import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoProjectAnnotationModel;
import org.monarchinitiative.hpoannotqc.annotations.util.AspectIdentifier;
import org.monarchinitiative.hpoannotqc.annotations.util.HpoAnnotQcUtil;
import org.monarchinitiative.hpoannotqc.annotations.util.HpoBigfileUtil;
import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * To replace the BigFileCommand!
 */
@CommandLine.Command(name = "bigfile",
        aliases = {"G"},
        mixinStandardHelpOptions = true,
        description = "Generated phenotype.hpoa file")
public class BigFileGenerateCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(BigFileGenerateCommand.class);
    /**
     * Path to the {@code hp.json} file (optional; will be derived from the data download by default).
     */
    @CommandLine.Option(names = {"-j", "--hpo"},
            description = "custom path to hp.json (default: get it from data directory)")
    private String hpJsonPath = null;
    /**
     * Directory with hp.json and en_product>HPO.xml files.
     */
    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    @CommandLine.Option(names = {"-a", "--annot"},
            description = "Path to directory with the ca. 7900 HPO Annotation files",
            required = true)
    private String hpoAnnotationFileDirectory;
    /**
     * Should usually be phenotype.hpoa, may also include path
     */
    @CommandLine.Option(names = {"-o", "--output"},
            description = "name of output file (default: ${DEFAULT-VALUE})")
    private String outputFilePath = "phenotype.hpoa";
    @CommandLine.Option(names = {"-m", "--merge"},
            description = "merge frequency data (default: ${DEFAULT-VALUE})")
    private boolean merge_frequency = true;
    @CommandLine.Option(names = "--tolerant",
            description = "tolerant mode (update obsolete term ids if possible; default: ${DEFAULT-VALUE})")
    private boolean tolerant = true;

    /**
     * Command to create the{@code phenotype.hpoa} file from the various small HPO Annotation files.
     */
    public BigFileGenerateCommand() {

    }

    @Override
    public Integer call() throws Exception {
        Ontology ontology = HpoAnnotQcUtil.getHpo(hpJsonPath);
        String orphanetXMLpath = String.format("%s%s%s", downloadDirectory, File.separator, "en_product4.xml");
        String orphanetInheritanceXmlPath = String.format("%s%s%s", downloadDirectory, File.separator, "en_product9_ages.xml");
        LOGGER.info("annotation directory = {}", hpoAnnotationFileDirectory);
        // 1. Get list of small files
        File hpoaSmallFileDir = new File(hpoAnnotationFileDirectory);
        HpoProjectAnnotationFileIngestor annotationFileIngestor =
                new HpoProjectAnnotationFileIngestor(hpoaSmallFileDir.getAbsolutePath(), ontology, this.merge_frequency);
        List<HpoProjectAnnotationModel> models = annotationFileIngestor.getHpoaFileEntries();
        List<HpoaErrorReport> errorList = annotationFileIngestor.getErrors();
        if (errorList.isEmpty()) {
            String msg = String.format("No errors found while parsing %d small files.",
                    models.size());
            LOGGER.info(msg);
        } else {
            // We want to be error free before generating the big file
            for (HpoaErrorReport report : errorList) {
                System.out.printf("%s: %s\n", report.title(), report.error().getCategoryAndError());
            }
            throw new PhenolRuntimeException("Found errors in HPO project small file ingest");
        }
        // List<HpoaError> errors = models.;

        // Use this to determine the aspect of HPO terms



        return 0;
    }



    private int outputHpoProjectAnnotations(Writer writer,
                                            List<AnnotationModel> hpoModels,
                                            AspectIdentifier aspectIdentifier) throws  IOException {
        int n = 0;

        for (AnnotationModel smallFile : hpoModels) {
            List<AnnotationEntry> entryList = smallFile.getEntryList();
            for (AnnotationEntry entry : entryList) {
                if (! entry.hasError()) {
                    String bigfileLine = entry.toBigFileLine(aspectIdentifier);
                    writer.write(bigfileLine + "\n");
                } else {
                    String err = String.format("[ERROR] with entry (%s) skipping line: %s",
                            entry.getDiseaseName(),
                            entry.getErrors().get(0).getMessage());
                    System.err.printf("[ERROR-HPO] %s\n", err);
                    LOGGER.error(err);
                }
                n++;
            }
        }
        System.out.printf("Output %d HPO project small small files.%n", n);
        return n;
    }

    private int outputOrphaAnnotations(Writer writer,
                                       AspectIdentifier aspectIdentifier,
                                       List<AnnotationModel> orphaModels) throws IOException {
        int m = 0;
        for (AnnotationModel smallFile : orphaModels) {
            List<AnnotationEntry> entryList = smallFile.getEntryList();
            for (AnnotationEntry entry : entryList) {
                if (entry.hasError()) {
                    String err = String.format("[ERROR] with entry (%s): %s",
                            entry.getDiseaseName(),
                            entry.getErrors().get(0).getMessage());
                    System.err.printf("TODO: HOW TO PROCESS: %sn",err);
                    // TODO

                }
                try {
                    String bigfileLine = entry.toBigFileLine(aspectIdentifier);
                    writer.write(bigfileLine + "\n");
                } catch (HpoAnnotQcException e) {
                    LOGGER.error(e.getMessage());
                    System.err.println(e.getMessage());
                }
                m++;
            }
        }
        LOGGER.info("We output a total of {} big file lines from the Orphanet Annotation files", m);
        return m;
    }


    /*
    /**
   * In the header of the {@code phenotype.hpoa} file, we write the
   * number of OMIM, Orphanet, and DECIPHER entries. This is calculated
   * here (except for Orphanet).
    private void setNumberOfDiseasesForHeader() {
        this.n_decipher = 0;
        this.n_omim = 0;
        this.n_unknown = 0;
        for (HpoAnnotationModel diseaseModel : internalAnnotationModelList) {
            if (diseaseModel.isOMIM()) n_omim++;
            else if (diseaseModel.isDECIPHER()) n_decipher++;
            else n_unknown++;
        }
        this.n_orphanet = orphanetSmallFileList.size();
    }

     */

    public void outputBigFile(File outputFile,
                              Ontology ontology,
                              List<AnnotationModel> hpoModels,
                              List<AnnotationModel> orphaModels) throws IOException {
        int n_omim = 42;
        int n_decipher = 42;
        int n_orpha = 42;
        int n_unknown = 0;
        String description = String.format("#description: \"HPO annotations for rare diseases [%d: OMIM; %d: DECIPHER; %d ORPHANET]\"", n_omim, n_decipher, n_orpha);
        List<String> errorList = new ArrayList<>();
        if (n_unknown > 0) {
            description = String.format("%s -- warning: %d entries could not be assigned to a database", description, n_unknown);
            errorList.add(description);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(description + "\n");
        HpoBigfileUtil bfUtil = new HpoBigfileUtil(ontology);
        String todayDate = bfUtil.getTodaysDate();
        String hpoVersion = bfUtil.getHpoVersion();
        writer.write(String.format("#version: %s\n", todayDate));
        writer.write("#tracker: https://github.com/obophenotype/human-phenotype-ontology/issues\n");
        writer.write(String.format("#hpo-version: %s\n", hpoVersion));

        final AspectIdentifier aspectIdentifier = new AspectIdentifier(ontology);
        writer.write(bfUtil.getHeaderLine() + "\n");
        int n = outputHpoProjectAnnotations(writer, hpoModels, aspectIdentifier);
        int m = outputOrphaAnnotations(writer, aspectIdentifier, orphaModels);
        LOGGER.info("We output a total of {} big file lines from the Orphanet Annotation files", m);
        LOGGER.info("Total output lines was {}", (n + m));

        writer.close();
        if (!errorList.isEmpty()) {
            System.out.println("**********************");
            System.out.println("**********************");
            System.out.println("**********************");
            System.out.println("ERRORS");
            for (String line : errorList) {
                System.out.println(line);
            }
        }
    }
}
