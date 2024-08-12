package org.monarchinitiative.hpoannotqc.annotations.orpha;

import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.legacy.HpoAnnotationEntry;
import org.monarchinitiative.hpoannotqc.annotations.legacy.HpoAnnotationModel;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class OrphanetIngestor {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrphanetIngestor.class);

    private final Ontology ontology;
    private final File orphaInheritanceXMLfile;
    private final File orphaPhenotypeXMLfile;

    /**
     * List of all of the {@link HpoAnnotationModel} objects derived from the Orphanet XML file.
     */
    private final List<AnnotationEntryI> orphanetSmallFileList;

    private final Map<TermId, Collection<AnnotationEntryI>> inheritanceMultiMap;

    private List<HpoaError> errorList;

    public OrphanetIngestor(File orphaPhenotypeXMLfile,
                            File orphaInheritanceXMLfile,
                            Ontology ontology) {
        this.ontology = ontology;
        if (!orphaPhenotypeXMLfile.exists()) {
            throw new PhenolRuntimeException("Could not find " + orphaPhenotypeXMLfile.getAbsolutePath()
                    + " (We were expecting the path to en_product4_HPO.xml");
        }
        if (!orphaInheritanceXMLfile.exists()) {
            throw new PhenolRuntimeException("Could not find " + orphaPhenotypeXMLfile.getAbsolutePath()
                    + " (We were expecting the path to en_product9_ages.xml");
        }
        this.orphaPhenotypeXMLfile = orphaPhenotypeXMLfile;
        this.orphaInheritanceXMLfile = orphaInheritanceXMLfile;
        this.inheritanceMultiMap = new HashMap<>();
        this.errorList = new ArrayList<>();
        OrphanetInheritanceXMLParser inheritanceXMLParser =
                new OrphanetInheritanceXMLParser(orphaInheritanceXMLfile.getAbsolutePath(), ontology);
        Map<TermId, Collection<AnnotationEntryI>> inheritanceMultiMap = inheritanceXMLParser.getDisease2inheritanceMultimap();
        if (inheritanceXMLParser.hasError()) {
            this.errorList.addAll(inheritanceXMLParser.getErrorlist());
        }
        LOGGER.info("We parsed {} Orphanet inheritance entries", inheritanceMultiMap.size());
        // 3. Get Orphanet disease models
        OrphanetXML2HpoDiseaseModelParser orphaParser =
                new OrphanetXML2HpoDiseaseModelParser(this.orphaPhenotypeXMLfile.getAbsolutePath(), ontology);
        Map<TermId, HpoAnnotationModel> prelimOrphaDiseaseMap = orphaParser.getOrphanetDiseaseMap();
        LOGGER.info("We parsed {} Orphanet disease entries", prelimOrphaDiseaseMap.size());
        int c = 0;
        for (TermId diseaseId : prelimOrphaDiseaseMap.keySet()) {
            HpoAnnotationModel model = prelimOrphaDiseaseMap.get(diseaseId);
            if (this.inheritanceMultiMap.containsKey(diseaseId)) {
                Collection<AnnotationEntryI> inheritanceEntryList = this.inheritanceMultiMap.get(diseaseId);
                HpoAnnotationModel mergedModel = model.mergeWithInheritanceAnnotations(inheritanceEntryList);
                prelimOrphaDiseaseMap.put(diseaseId,mergedModel); // replace with model that has inheritance
                c++;
            }
        }
        LOGGER.info("[INFO] We added inheritance information to {} Orphanet disease entries", c);
        this.orphanetSmallFileList = new ArrayList<>(prelimOrphaDiseaseMap.values());
    }


    public List<AnnotationEntryI> parse() {
        // 2. Get the Orphanet Inheritance Annotations

    }


}
