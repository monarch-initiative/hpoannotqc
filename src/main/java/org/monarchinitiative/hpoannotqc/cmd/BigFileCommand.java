package org.monarchinitiative.hpoannotqc.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModel;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.annotations.hpo.HpoAnnotationFileIngestor;
import org.monarchinitiative.phenol.io.annotations.hpo.OrphanetXML2HpoDiseaseModelParser;
import org.monarchinitiative.phenol.io.annotations.hpo.PhenotypeDotHpoaFileWriter;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.*;
import java.util.List;


/**
 * This class coordinates the output of the {@code phenotype_annotation.tab} file or variations thereof with the
 * new an old format. It combines the V2 small files with the Orphanet data (note this has to be downloaded first
 * with the {@link DownloadCommand}).
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@Parameters(commandDescription = "Create phenotype.hpoa file")
public class BigFileCommand implements Command {
    private static final Logger logger = LogManager.getLogger();
    /** Path to the {@code hp.obo} file. */
    private final String hpOboPath;
    /** Path to the downloaded Orphanet XML file */
    private final String orphanetXMLpath;
    /** Directory with hp.obo and en_product>HPO.xml files. */
    @Parameter(names={"-d","--data"}, description ="directory to download data (default: data)" )
    private String downloadDirectory="data";

    @Parameter(names={"-a","--annot"},description = "Path to directory with the ca. 7000 HPO Annotation files ", required = true)
    private String hpoAnnotationFileDirectory;
    /** Should usually be phenotype.hpoa, may also include path */
    @Parameter(names={"-o","--output"},description="name of output file")
    private String outputFilePath="phenotype.hpoa";

    @Parameter(names="--tolerant",description = "tolerant mode (update obsolte term ids if possible)")
    private boolean tolerant=true;

    /** Command to create the{@code phenotype.hpoa} file from the various small HPO Annotation files. */
    public BigFileCommand() {
        hpOboPath=String.format("%s%s%s",downloadDirectory,File.separator, "hp.obo" );
        orphanetXMLpath=String.format("%s%s%s",downloadDirectory,File.separator, "en_product4_HPO.xml" );

    }

    @Override
    public void execute() {
        HpoOntology ontology;
        try {
            logger.trace("Parsing hp.obo ...");
            HpOboParser hpoOboParser = new HpOboParser(new File(hpOboPath));
            ontology = hpoOboParser.parse();
        } catch (Exception e) {
            logger.fatal("Unable to parse hp.obo file at " + hpOboPath);
            logger.fatal("Unable to recover, stopping execution");
            return;
        }
        // path to the omit-list.txt file, which is located with the small files in the same directory
        String omitPath=String.format("%s%s%s", hpoAnnotationFileDirectory,File.separator,"omit-list.txt");
        System.err.println("[INFO] annotation="+hpoAnnotationFileDirectory);
        try {
            // 1. Get the HPO project annotation files
            HpoAnnotationFileIngestor annotationFileIngestor = new HpoAnnotationFileIngestor(hpoAnnotationFileDirectory, omitPath, ontology);
            List<HpoAnnotationModel> hpoFileEntryList = annotationFileIngestor.getV2SmallFileEntries();
            // 2. Get the Orphanet annotation file
            OrphanetXML2HpoDiseaseModelParser orphaParser = new OrphanetXML2HpoDiseaseModelParser(this.orphanetXMLpath, ontology, tolerant);
            List<HpoAnnotationModel> orphaFileEntryList = orphaParser.getOrphanetDiseaseModels();
            // 3. Combine both and output to phenotype.hpoa ("big file")
            PhenotypeDotHpoaFileWriter writer = new PhenotypeDotHpoaFileWriter(ontology, hpoFileEntryList, orphaFileEntryList, outputFilePath);
            writer.setOntologyMetadata(ontology.getMetaInfo());
            writer.outputBigFile();
        } catch (IOException e) {
            logger.fatal("[ERROR] Could not output phenotype.hpoa (big file). ",e);
        }
    }

}
