package org.monarchinitiative.hpoannotqc.smallfile;



import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.formats.hpo.HpoTermRelation;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;
import org.monarchinitiative.phenol.ontology.data.*;


import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * This class uses the <a href="https://github.com/phenomics/ontolib">ontolb</a> library to
 * parse both the {@code hp.obo} file and the phenotype annotation file
 * {@code phenotype_annotation.tab}
 * (see <a href="http://human-phenotype-ontology.github.io/">HPO Homepage</a>).
 * @author Peter Robinson
 * @author Vida Ravanmehr
 * @version 0.1.5 (2018-02-02)
 */
public class HpoOntologyParser {
    /** Path to the {@code hp.obo} file. */
    private String hpoOntologyPath=null;

    private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
    private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;
    /** Map of all of the Phenotypic abnormality terms (i.e., not the inheritance terms). */
    private Map<TermId,HpoTerm> termmap=null;

    public HpoOntologyParser(String path) throws HPOException {
        hpoOntologyPath=path;
        parseOntology();
    }

    /**
     * Parse the HP ontology file and place the data in {@link #abnormalPhenoSubOntology} and
     * {@link #inheritanceSubontology}.
     * @throws IOException if we cannot parse the hp.obo file
     */
    private void parseOntology() throws HPOException {
        TermPrefix pref = new ImmutableTermPrefix("HP");
        TermId inheritId = new ImmutableTermId(pref,"0000005");
        try {
            HpoOboParser hpoOboParser = new HpoOboParser(new File(hpoOntologyPath));
            this.ontology = hpoOboParser.parse();
            this.abnormalPhenoSubOntology = ontology.getPhenotypicAbnormalitySubOntology();
            this.inheritanceSubontology = ontology.subOntology(inheritId);
        } catch (Exception e) {
            throw new HPOException(String.format("error trying to parse hp.obo file at %s: %s",hpoOntologyPath,e.getMessage()));
        }
    }

    public Ontology<HpoTerm, HpoTermRelation> getPhenotypeSubontology() { return this.abnormalPhenoSubOntology; }
    public Ontology<HpoTerm, HpoTermRelation> getInheritanceSubontology() { return  this.inheritanceSubontology; }

    public HpoOntology getOntology() {
        return ontology;
    }
}
