package org.monarchinitiative.hpoannotqc.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriter;
import org.monarchinitiative.hpoannotqc.bigfile.V2SmallFileParser;
import org.monarchinitiative.hpoannotqc.smallfile.OldSmallFileEntry;
import org.monarchinitiative.hpoannotqc.smallfile.V2LineQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.phenomics.ontolib.ontology.algo.OntologyAlgorithm.existsPath;

public class BigFileCommand implements Command {
    private static final Logger logger = LogManager.getLogger();
    /** Path to directory where we will write the new "small files". */
    private final String v2smallFileDirectory;
    private final String hpOboPath;
    private HpoOntology ontology;
    /** Number of annotations for which we could not figure out the aspect. */
    private int n_bad_aspect=0;

    private String bigFileOutputName="phenotype_annotation2.tab";

    private static final TermId phenotypeRoot=ImmutableTermId.constructWithPrefix("HP:0000118");
    private static final TermId FREQUENCY_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0040279");
    private static final TermId ONSET_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0003674");
    private static final TermId MODIFIER_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0012823");
    private static final TermId INHERITANCE_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0000005");
    private static final TermId MORTALITY_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0040006");

    public BigFileCommand(String hpopath, String dir) {
        hpOboPath=hpopath;
        v2smallFileDirectory =dir;
    }



    @Override
    public void execute() {
        try {
            HpoOboParser hpoOboParser = new HpoOboParser(new File(hpOboPath));
            this.ontology = hpoOboParser.parse();
            BigFileWriter writer = new BigFileWriter(ontology,v2smallFileDirectory);
            writer.outputBigFile();
        } catch (Exception e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }
    }




}
