package org.monarchinitiative.hpoannotqc.cmd;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriter;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetXML2HpoDiseaseModelParser;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;

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
    /** Path to directory where we will write the new "small files". */
    private final String v2smallFileDirectory;
    /** Path to the {@code hp.obo} file. */
    private final String hpOboPath;
    /** Path to the downloaded Orphanet XML file */
    private final String orphanetXMLpath;
    private HpoOntology ontology;
    /** True if we should output the V1 big file (2009-2018). False if we should output v2. */
    private boolean doV1bigfileOutput;
    /** Number of annotations for which we could not figure out the aspect. */
   //private int n_bad_aspect=0;


    public BigFileCommand(String hpopath, String dir, String orphaXML, String bfVersion) {
        hpOboPath=hpopath;
        v2smallFileDirectory =dir;
        orphanetXMLpath=orphaXML;
        if (bfVersion.equalsIgnoreCase("v1")) {
            doV1bigfileOutput=true;
        } else if (bfVersion.equalsIgnoreCase("v2")){
            doV1bigfileOutput=false;
        } else {
            logger.fatal("Did not recognize big file version: " + bfVersion);
            logger.fatal("Terminating program. Choose -v v1 or -v v2");
        }
    }



    @Override
    public void execute() {
        try {
            logger.trace("Parsing hp.obo ...");
            HpoOboParser hpoOboParser = new HpoOboParser(new File(hpOboPath));
            this.ontology = hpoOboParser.parse();
        } catch (IOException e) {
            logger.fatal("Unable to parse hp.obo file at " + hpOboPath);
            logger.fatal("Unable to recover, stopping execution");
            return;
        }
        BigFileWriter writer = new BigFileWriter(ontology, v2smallFileDirectory);
        try {
           // writer.outputBigFileV1();
            OrphanetXML2HpoDiseaseModelParser parser = new OrphanetXML2HpoDiseaseModelParser(this.orphanetXMLpath, this.ontology);
            List<OrphanetDisorder> orphanetDisorders = parser.getDisorders();
            debugPrintOrphanetDisorders(orphanetDisorders);
            if (doV1bigfileOutput) {
                writer.initializeV1filehandle();
                writer.outputBigFileV1();
                writer.appendOrphanetV1(orphanetDisorders);
                writer.closeFileHandle();
            } else {
                /// now output the V2 version of the file
                writer.initializeV2filehandle();
                writer.outputBigFileV2();
               writer.appendOrphanetV2(orphanetDisorders);
                writer.closeFileHandle();
            }

        } catch (IOException e) {
            logger.fatal("[ERROR] Could not output V2 big file",e);
        }

    }



   private void debugPrintOrphanetDisorders(List<OrphanetDisorder> orphanetDisorders){
        int n_annot=0;
        for (OrphanetDisorder od : orphanetDisorders) {
            n_annot += od.getHpoIds().size();
        }
        System.out.println(String.format("We extracted %d orphanet disorders with %d annotations ",orphanetDisorders.size(),n_annot));
   }

}
