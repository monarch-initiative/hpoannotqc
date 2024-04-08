package org.monarchinitiative.hpoannotqc.cmd;

import org.monarchinitiative.hpoannotqc.annotations.PhenotypeDotHpoaFileWriter;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.*;
import java.util.concurrent.Callable;


/**
 * This class coordinates the output of the {@code phenotype_annotation.tab} file or variations thereof with the
 * new an old format. It combines the V2 small files with the Orphanet data (note this has to be downloaded first
 * with the {@link DownloadCommand}).
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@CommandLine.Command(name = "big-file", aliases = {"B"}, mixinStandardHelpOptions = true, description = "Create phenotype.hpoa file")
public class BigFileCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(BigFileCommand.class);
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
    public BigFileCommand() {
        if (hpJsonPath == null) {
            hpJsonPath = String.format("%s%s%s", downloadDirectory, File.separator, "hp.json");
        }
        File f = new File(hpJsonPath);
        if (! f.isFile()) {
            String err = String.format("Could not find hp.jon file at \"%s\".", hpJsonPath);
            System.err.println(err);
            throw new PhenolRuntimeException(err);
        }
    }

    @Override
    public Integer call()  {
        // Path to the downloaded Orphanet XML file
        String orphanetXMLpath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product4.xml" );
        //  Path to the dowloaded Orphanet inheritance file, en_product9_ages.xml.
        String orphanetInheritanceXmlPath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product9_ages.xml" );
        Ontology ontology = OntologyLoader.loadOntology(new File(hpJsonPath));
        // path to the omit-list.txt file, which is located with the small files in the same directory
        LOGGER.info("annotation directory = "+hpoAnnotationFileDirectory);
        try {
            PhenotypeDotHpoaFileWriter pwriter = PhenotypeDotHpoaFileWriter.factory(ontology,
                    hpoAnnotationFileDirectory,
                    orphanetXMLpath,
                    orphanetInheritanceXmlPath,
                    outputFilePath,
                    tolerant,
                    merge_frequency);
            pwriter.outputBigFile();
        } catch (IOException e) {
            LOGGER.error("[ERROR] Could not output phenotype.hpoa (big file). ",e);
        } catch (PhenolRuntimeException pre) {
            LOGGER.error("Caught phenol runtime exception: "+ pre.getMessage());
        }
        return 0;
    }

}
