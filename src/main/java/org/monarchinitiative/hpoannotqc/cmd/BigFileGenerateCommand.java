package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoProjectAnnotationFileIngestor;
import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoProjectAnnotationModel;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
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
    /** Path to the {@code hp.json} file (optional; will be derived from the data download by default). */
    @CommandLine.Option(names = {"-j", "--hpo"},
            description = "custom path to hp.json (default: get it from data directory)")
    private String hpJsonPath = null;
    /** Directory with hp.json and en_product>HPO.xml files. */
    @CommandLine.Option(names = {"-d", "--data"},
            description = "directory to download data (default: ${DEFAULT-VALUE})")
    private String downloadDirectory = "data";
    @CommandLine.Option(names={"-a","--annot"},
            description = "Path to directory with the ca. 7900 HPO Annotation files",
            required = true)
    private String hpoAnnotationFileDirectory;
    /** Should usually be phenotype.hpoa, may also include path */
    @CommandLine.Option(names={"-o","--output"},
            description="name of output file (default: ${DEFAULT-VALUE})")
    private String outputFilePath = "phenotype.hpoa";
    @CommandLine.Option(names={"-m","--merge"},
            description="merge frequency data (default: ${DEFAULT-VALUE})")
    private boolean merge_frequency=true;
    @CommandLine.Option(names="--tolerant",
            description = "tolerant mode (update obsolete term ids if possible; default: ${DEFAULT-VALUE})")
    private boolean tolerant = true;

    /** Command to create the{@code phenotype.hpoa} file from the various small HPO Annotation files. */
    public BigFileGenerateCommand() {

    }

    @Override
    public Integer call() throws Exception {
        Ontology ontology = HpoAnnotQcUtil.getHpo(hpJsonPath);
        String orphanetXMLpath = String.format("%s%s%s",downloadDirectory, File.separator, "en_product4.xml" );
        String orphanetInheritanceXmlPath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product9_ages.xml" );
        LOGGER.info("annotation directory = {}", hpoAnnotationFileDirectory);
        // 1. Get list of small files
        File hpoaSmallFileDir = new File(hpoAnnotationFileDirectory);
        HpoProjectAnnotationFileIngestor annotationFileIngestor =
                new HpoProjectAnnotationFileIngestor(hpoaSmallFileDir.getAbsolutePath(), ontology, this.merge_frequency);
        List<HpoProjectAnnotationModel> models = annotationFileIngestor.getHpoaFileEntries();
        List<HpoaError> errorList = annotationFileIngestor.getErrors();
        if (errorList.isEmpty()) {
            String msg = String.format("No errors found while parsing %d small files.",
                    models.size());
            LOGGER.info(msg);
        } else {
            // We want to be error free before generating the big file
            for (HpoaError hpoaError : errorList) {
                System.out.println(hpoaError.getCategoryAndError());
            }
            throw new PhenolRuntimeException("Found errors in HPO project small file ingest");
        }
        // List<HpoaError> errors = models.;
        return 0;
    }
}
