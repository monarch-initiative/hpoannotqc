package org.monarchinitiative.hpoannotqc.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseWithMetadata;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.io.obo.hpo.HpoAnnotation2DiseaseParser;
import com.github.phenomics.ontolib.io.obo.hpo.HpoOboParser;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.bigfile.BigFileWriter;
import org.monarchinitiative.hpoannotqc.bigfile.HpoOntologyParser;

import java.io.File;
import java.util.Map;
import java.util.Objects;


/** This tool is intended to compare the annotations of two diseases. It will help us to develop
 * the merge code for OMIM/ORDO to MONDO annotations. For now we will use the "old" phenotype_annotation.tab,
 * but soon we will update to the new V2 version.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class Compare2Diseases  implements Command {
    private static final Logger logger = LogManager.getLogger();
    private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation> phenotypeSubOntology=null;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
    private static Map<String,HpoDiseaseWithMetadata> diseaseMap;
    private final String phenotype_annotation_path;
    private final String hpOboPath;



    public Compare2Diseases(String hpopath, String annotationPath, String disease1, String disease2) {
        hpOboPath=hpopath;
        phenotype_annotation_path = annotationPath;
        logger.trace(String.format("Compare %s and %s using HPO file %s and annotation file %s",disease1,disease2,hpopath,annotationPath ));
        HpoOntologyParser parser = new HpoOntologyParser(hpopath);
        try {
            parser.parseOntology();
            phenotypeSubOntology = parser.getPhenotypeSubontology();
            inheritanceSubontology = parser.getInheritanceSubontology();
            Objects.requireNonNull(phenotypeSubOntology);
            Objects.requireNonNull(inheritanceSubontology);
            HpoAnnotation2DiseaseParser annotationParser = new HpoAnnotation2DiseaseParser(phenotype_annotation_path, phenotypeSubOntology, inheritanceSubontology);
            diseaseMap = annotationParser.getDiseaseMap();
            logger.error("Done parsing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Perform the downloading.
     */
    @Override
    public void execute()  {
        try {
            HpoOboParser hpoOboParser = new HpoOboParser(new File(hpOboPath));
            this.ontology = hpoOboParser.parse();

        } catch (Exception e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }
    }
}
