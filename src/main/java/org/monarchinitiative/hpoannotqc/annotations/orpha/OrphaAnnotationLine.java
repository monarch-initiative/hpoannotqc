package org.monarchinitiative.hpoannotqc.annotations.orpha;

import org.monarchinitiative.hpoannotqc.annotations.Biocuration;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntry;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaTermError;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.monarchinitiative.phenol.annotations.formats.hpo.HpoFrequency.EXCLUDED;

public class OrphaAnnotationLine implements AnnotationEntry {
    static Logger LOGGER = LoggerFactory.getLogger(OrphaAnnotationLine.class);

    private static final String EMPTY_STRING = "";
    private static final String DEFAULT_ORPHA_EVIDENCE = "TAS";
    /**
     * The CURIE of the disease, e.g., OMIM:600201 (Field #0).
     */
    private final String diseaseID;
    /**
     * Field #2
     */
    private final String diseaseName;
    /**
     * Field #3
     */
    private final String phenotypeId;
    /**
     * Field #4
     */
    private final String phenotypeName;

    /**
     * Field #8 can be one of N/M, X% or a valid frequency term identifier.
     */
    private final String frequencyModifier;
   /**
     * Field #14
     */
    private final Biocuration biocuration;

    /** List of any errors encountered while parsing this entry. */
    private final List<HpoaError> errorList;

    public OrphaAnnotationLine(String disID,
                               String diseaseName,
                               String phenotypeId,
                               String phenotypeName,
                               String frequencyString,
                               String biocuration,
                               List<HpoaError> errors) {
        this.diseaseID = disID;
        this.diseaseName = diseaseName;
        this.phenotypeId = phenotypeId;
        this.phenotypeName = phenotypeName;
        this.frequencyModifier = frequencyString;
        this.biocuration = new OrphaBiocuration();
        this.errorList = errors;
    }

    /**
     * If the frequency of an HPO term is listed in Orphanet as Excluded (0%), then we encode it as
     * a NOT (negated) term.
     *
     * @param diseaseID             Orphanet ID, e.g., ORPHA:99776
     * @param diseaseName           Orphanet disease name, e.g., Moasic trisomy 9
     * @param hpoId                 HPO id (e.g., HP:0001234) as String
     * @param hpoLabel              corresponding HPO term Label
     * @param frequency             Orphanet frequency data as TermId
     * @param ontology              reference to HPO ontology
     * @param biocuration           A String to represent provenance from Orphanet, e.g., ORPHA:orphadata[2019-01-05]
     * @param replaceObsoleteTermId if true, correct obsolete term ids and do not throw an exception.
     * @return corresponding HpoAnnotationEntry object
     */
    public static AnnotationEntry fromOrphaData(String diseaseID,
                                                String diseaseName,
                                                String hpoId,
                                                String hpoLabel,
                                                TermId frequency,
                                                Ontology ontology,
                                                String biocuration,
                                                boolean replaceObsoleteTermId) {

        if (hpoId == null) {
            throw new PhenolRuntimeException("Null String passed as hpoId for disease " + (diseaseID != null ? diseaseID : "n/a"));
        }
        List<HpoaError> errorList = new ArrayList<>();
        TermId phenotypeId = TermId.of(hpoId);
        // replace the frequency TermId with its string equivalent
        // except if it is Excluded, which we treat as a negative annotation
        String frequencyString = frequency.equals(EXCLUDED.id()) ? EMPTY_STRING : frequency.getValue();
        // Todo, discuss with Orphanet, probably retire the NOT
        String negationString = frequency.equals(EXCLUDED.id()) ? "NOT" : EMPTY_STRING;

        if (replaceObsoleteTermId) {
            TermId currentPhenotypeId = ontology.getPrimaryTermId(phenotypeId);
            if (currentPhenotypeId != null && !currentPhenotypeId.equals(phenotypeId)) {
                String newLabel = ontology.getTermLabel(phenotypeId).orElseThrow();
                String message = String.format("Replacing obsolete TermId \"%s\" with current ID \"%s\" (and obsolete label %s with current label %s)",
                        hpoId, currentPhenotypeId.getValue(), hpoLabel, newLabel);
                HpoaError hpoae = new HpoaTermError(currentPhenotypeId, message);
                errorList.add(hpoae);
                phenotypeId = currentPhenotypeId;
                hpoLabel = newLabel;
            }
            // replace label if needed
            if (currentPhenotypeId != null) { // we can only get new name if we got the new id!
                String currentPhenotypeLabel = ontology.getTermLabel(phenotypeId).orElseThrow();
                if (!hpoLabel.equals(currentPhenotypeLabel)) {
                    String message = String.format("Replacing obsolete Term label \"%s\" with current label \"%s\"",
                            hpoLabel, currentPhenotypeLabel);
                    errorList.add(new HpoaTermError(currentPhenotypeId, message));
                    LOGGER.warn("{}: {}", diseaseID, message);
                    hpoLabel = currentPhenotypeLabel;
                }
            }
        }

        AnnotationEntry entry = new OrphaAnnotationLine(diseaseID,
                diseaseName,
                phenotypeId.getValue(),
                hpoLabel,
                frequencyString,
                biocuration,
                errorList);
        // if the following method does not throw an Exception, we are good to go!
        performQualityControl(entry, ontology, diseaseName);

        return entry;
    }

    /**
     * If the frequency of an HPO term is listed in Orphanet as Excluded (0%), then we encode it as
     * a NOT (negated) term.
     *
     * @param diseaseID        Orphanet ID, e.g., ORPHA:99776
     * @param diseaseName      Orphanet disease name, e.g., Moasic trisomy 9
     * @param hpoInheritanceId HPO id (e.g., HP:0001234) for an inheritance term
     * @param hpoLabel         corresponding HPO term Label
     * @param biocuration      A String to represent provenance from Orphanet, e.g., ORPHA:orphadata[2019-01-05]
     * @return corresponding HpoAnnotationEntry object
     */
    public static AnnotationEntry fromOrphaInheritanceData(String diseaseID,
                                                           String diseaseName,
                                                           String hpoInheritanceId,
                                                           String hpoLabel,
                                                           String biocuration,
                                                           List<HpoaError> errorList) {
        return new OrphaAnnotationLine(diseaseID,
                diseaseName,
                hpoInheritanceId,
                hpoLabel,
                EMPTY_STRING, // frequency, always empty for inheritance
                biocuration,
                errorList);
    }
    /**
     * This method checks all the fields of the HpoAnnotationEntry. If there is an error, then
     * it throws an Exception (upon the first error). If no exception is thrown, then the
     * no errors were found.
     *
     * @param entry    The {@link AnnotationEntry} to be tested.
     * @param ontology A reference to an HpoOntology object (needed for Q/C'ing terms).
     */
    private static void performQualityControl(AnnotationEntry entry, Ontology ontology, String diseaseName)  {

        //checkAgeOfOnsetFields(entry, ontology, diseaseName);

    }

    @Override
    public String getDiseaseID() {
        return diseaseID;
    }

    @Override
    public String getDatabasePrefix() {
        return TermId.of(diseaseID).getPrefix();
    }

    @Override
    public String getDatabaseIdentifier() {
        return TermId.of(diseaseID).getId();
    }

    @Override
    public String getDiseaseName() {
        return diseaseName;
    }

    @Override
    public String getPhenotypeId() {
        return phenotypeId;
    }

    @Override
    public TermId getPhenotypeTermId() {
        return TermId.of(phenotypeId);
    }

    @Override
    public String getPhenotypeLabel() {
        return phenotypeName;
    }

    @Override
    public String getAgeOfOnsetId() {
        return EMPTY_STRING;
    }

    @Override
    public String getAgeOfOnsetLabel() {
        return EMPTY_STRING;
    }

    @Override
    public String getEvidenceCode() {
        return DEFAULT_ORPHA_EVIDENCE;
    }

    @Override
    public String getFrequencyModifier() {
        return frequencyModifier;
    }

    @Override
    public String getSex() {
        return EMPTY_STRING;
    }

    @Override
    public String getNegation() {
        return EMPTY_STRING;
    }

    @Override
    public String getModifier() {
        return EMPTY_STRING;
    }

    @Override
    public String getDescription() {
        return EMPTY_STRING;
    }

    @Override
    public String getPublication() {
        return "";
    }

    @Override
    public String getBiocuration() {
        return biocuration.curation();
    }

    @Override
    public List<HpoaError> getErrors() {
        return errorList;
    }

    @Override
    public boolean hasError() {
        return ! errorList.isEmpty();
    }
}
