package org.monarchinitiative.hpoannotqc.bigfile;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;
import org.monarchinitiative.phenol.ontology.data.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * This class uses the <a href="https://github.com/phenomics/ontolib">ontolb</a> library to
 * parse both the {@code hp.obo} file and the phenotype annotation file
 * {@code phenotype_annotation.tab}
 * TODO REMVOE THIS CLASS IT IS SUPERFLUOUS
 * (see <a href="http://human-phenotype-ontology.github.io/">HPO Homepage</a>).
 * @author Peter Robinson
 * @author Vida Ravanmehr
 * @version 0.1.1 (2017-11-15)
 */
public class HpoOntologyParser {
    private static final Logger logger = LogManager.getLogger();
    /** Path to the {@code hp.obo} file. */
    private String hpoOntologyPath=null;

    private HpoOntology ontology;

//    Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
//    Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;
    /** Map of all of the Phenotypic abnormality terms (i.e., not the inheritance terms). */
    private Map<TermId,HpoTerm> termmap=null;

    public HpoOntologyParser(String path){
        hpoOntologyPath=path;
    }

    /**
     * Parse the HP ontology file
     * @throws IOException
     */
    public void parseOntology() throws IOException {
        HpoOboParser hpoOboParser = new HpoOboParser(new File(hpoOntologyPath));
        this.ontology = hpoOboParser.parse();
    }


    public HpoOntology getOntology() {
        return ontology;
    }
}
