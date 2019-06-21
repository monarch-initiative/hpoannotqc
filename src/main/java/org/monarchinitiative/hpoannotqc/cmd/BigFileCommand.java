package org.monarchinitiative.hpoannotqc.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Multimap;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationEntry;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModel;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.io.annotations.hpo.HpoAnnotationFileIngestor;
import org.monarchinitiative.phenol.io.annotations.hpo.OrphanetInheritanceXMLParser;
import org.monarchinitiative.phenol.io.annotations.hpo.OrphanetXML2HpoDiseaseModelParser;
import org.monarchinitiative.phenol.io.annotations.hpo.PhenotypeDotHpoaFileWriter;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(BigFileCommand.class);
    /** Path to the {@code hp.obo} file. */
    private final String hpOboPath;
    /** Path to the downloaded Orphanet XML file */
    private final String orphanetXMLpath;
    /** Path to the dowloaded Orphanet inheritance file, {@code en_product9_ages.xml}.*/
    private final String orphanetInheritanceXmlPath;
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
        hpOboPath = String.format("%s%s%s",downloadDirectory,File.separator, "hp.obo" );
        orphanetXMLpath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product4_HPO.xml" );
        orphanetInheritanceXmlPath = String.format("%s%s%s",downloadDirectory,File.separator, "en_product9_ages.xml" );
    }

    @Override
    public void execute() {
        Ontology ontology = OntologyLoader.loadOntology(new File(hpOboPath));
        // path to the omit-list.txt file, which is located with the small files in the same directory
        System.err.println("[INFO] annotation="+hpoAnnotationFileDirectory);
        try {
            PhenotypeDotHpoaFileWriter pwriter = PhenotypeDotHpoaFileWriter.factory(ontology,
                    hpoAnnotationFileDirectory,
                    orphanetXMLpath,
                    orphanetInheritanceXmlPath,
                    outputFilePath);
            pwriter.outputBigFile();
        } catch (IOException e) {
            logger.error("[ERROR] Could not output phenotype.hpoa (big file). ",e);
        } catch (PhenolRuntimeException pre) {
            logger.error("Caught phenol runtime exception: "+ pre.getMessage());
        }
    }

}
