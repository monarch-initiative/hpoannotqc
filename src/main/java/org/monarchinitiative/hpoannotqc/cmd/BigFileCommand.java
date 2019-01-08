package org.monarchinitiative.hpoannotqc.cmd;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.io.PhenotypeDotHpoaFileWriter;
import org.monarchinitiative.hpoannotqc.io.HpoAnnotationFileIngestor;
import org.monarchinitiative.hpoannotqc.io.OrphanetXML2HpoDiseaseModelParser;
import org.monarchinitiative.hpoannotqc.smallfile.HpoAnnotationModel;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;

import java.io.*;
import java.util.List;


/**
 * This class coordinates the output of the {@code phenotype_annotation.tab} file or variations thereof with the
 * new an old format. It combines the V2 small files with the Orphanet data (note this has to be downloaded first
 * with the {@link DownloadCommand}).
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class BigFileCommand implements Command {
    private static final Logger logger = LogManager.getLogger();
    /** Path to directory with the ca. 7000 HPO Annotation files ("small files"). */
    private final String hpoAnnotationFileDirectory;
    /** Path to the {@code hp.obo} file. */
    private final String hpOboPath;
    /** Path to the downloaded Orphanet XML file */
    private final String orphanetXMLpath;
    /** Should usually be phenotype.hpoa, may also include path */
    private final String outputFilePath;
    /** path to the omit-list.txt file, which is located with the small files in the same directory */
    private final String omitPath;

    /**
     * Command to create the V2 bigfile from the various small files
     * @param hpopath path to hp.obo
     * @param dir directory with the small files
     * @param orphaXML path to the Orphanet XML file
     * @param outpath path to the new output file (phenotype.hpoa)
     */
    public BigFileCommand(String hpopath, String dir, String orphaXML, String outpath) {
        hpOboPath=hpopath;
        hpoAnnotationFileDirectory =dir;
        orphanetXMLpath=orphaXML;
        outputFilePath=outpath;
        omitPath=String.format("%s%s%s", hpoAnnotationFileDirectory,File.separator,"omit-list.txt");
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
        try {
            // 1. Get the HPO project annotation files
            HpoAnnotationFileIngestor annotationFileIngestor = new HpoAnnotationFileIngestor(hpoAnnotationFileDirectory, omitPath, ontology);
            List<HpoAnnotationModel> hpoFileEntryList = annotationFileIngestor.getV2SmallFileEntries();
            // 2. Get the Orphanet annotation file
            OrphanetXML2HpoDiseaseModelParser orphaParser = new OrphanetXML2HpoDiseaseModelParser(this.orphanetXMLpath, ontology);
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
