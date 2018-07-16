package org.monarchinitiative.hpoannotqc.cmd;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.TermId;


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
    private static Map<TermId,HpoDisease> diseaseMap;
    private final String phenotype_annotation_path;
    private final String hpOboPath;



    public Compare2Diseases(String hpopath, String annotationPath, String disease1, String disease2) {
        hpOboPath=hpopath;
        phenotype_annotation_path = annotationPath;
        logger.trace(String.format("Compare %s and %s using HPO file %s and annotation file %s",disease1,disease2,hpopath,annotationPath ));
        HpOboParser hpoOboParser = new HpOboParser(new File(hpOboPath));
        try {
            this.ontology = hpoOboParser.parse();
            Objects.requireNonNull(ontology);
            HpoDiseaseAnnotationParser annotationParser = new HpoDiseaseAnnotationParser(phenotype_annotation_path, ontology);
            diseaseMap = annotationParser.parse();
            logger.error("Done parsing");
        } catch (PhenolException e) {
            e.printStackTrace();
        }
    }
    /**
     * Perform the downloading.
     */
    @Override
    public void execute()  {
        try {
            HpOboParser hpoOboParser = new HpOboParser(new File(hpOboPath));
            this.ontology = hpoOboParser.parse();

        } catch (PhenolException e) {
            logger.error(String.format("error trying to parse hp.obo file at %s: %s",hpOboPath,e.getMessage()));
            System.exit(1); // we cannot recover from this
        }
    }
}
