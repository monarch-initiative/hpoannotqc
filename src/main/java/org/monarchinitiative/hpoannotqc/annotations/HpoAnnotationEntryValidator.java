package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoaEntryError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteAspectError;
import org.monarchinitiative.hpoannotqc.exception.ObsoleteTermError;
import org.monarchinitiative.phenol.annotations.base.Sex;
import org.monarchinitiative.phenol.annotations.formats.EvidenceCode;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
    This class validates the fields and business logic of an HPO Annotation entry. This should be done in small-qc-file,
    which runs on new data of hpo-annotation-data and updates to human-phenotype-ontology.
*/
public class HpoAnnotationEntryValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HpoAnnotationEntryValidator.class);
    private static final Pattern RATIO_PATTERN = Pattern.compile("(?<numerator>\\d+)/(?<denominator>\\d+)");
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(?<value>\\d+\\.?(\\d+)?)%");
    private static final String EMPTY_STRING = "";
    private static final String[] expectedFields = { "#diseaseID", "diseaseName", "phenotypeID", "phenotypeName",
            "onsetID", "onsetName", "frequency", "sex", "negation", "modifier", "description", "publication",
            "evidence", "biocuration" };
    private static final int NUMBER_OF_FIELDS = expectedFields.length;
    private static final EnumSet<EvidenceCode> EVIDENCE_CODES = EnumSet.of(EvidenceCode.IEA, EvidenceCode.TAS,
            EvidenceCode.PCS);

    private static final Set<String> VALID_CITATION_PREFIXES = Set.of("PMID", "OMIM", "http", "https", "DECIPHER",
    "ORPHA", "ISBN", "ISBN-10", "ISBN-13");

    public static void performQualityControl(HpoAnnotationEntry entry, Ontology ontology)
            throws HpoaEntryError, ObsoleteTermError, ObsoleteAspectError, PhenolRuntimeException {
        checkDB(entry);
        checkPhenotypeFields(entry, ontology);
        checkAgeOfOnsetFields(entry, ontology);
        checkFrequency(entry, ontology);
        checkSexEntry(entry);
        checkNegation(entry);
        checkModifier(entry, ontology);
        checkPublication(entry);
        checkEvidence(entry);
        BiocurationChecker.checkEntry(entry);
    }

    private static void checkDB(HpoAnnotationEntry entry) throws HpoaEntryError, ObsoleteTermError, ObsoleteTermError {

        DiseaseDatabase db = DiseaseDatabase.fromString(entry.getDatabasePrefix());
        if (!DiseaseDatabase.validDiseaseDatabases().contains(db)) {
            throw new HpoaEntryError(entry, "Invalid database symbol.");
        }

        String name = entry.getDiseaseName();
        if (name == null || name.isEmpty()) {
            throw new HpoaEntryError(entry, "Missing disease name.");
        }
    }

    private static void checkPhenotypeFields(HpoAnnotationEntry entry, Ontology ontology)
            throws HpoaEntryError, ObsoleteTermError {
        TermId id = entry.getPhenotypeId();
        String termLabel = entry.getPhenotypeLabel();
        if (id == null) {
            throw new HpoaEntryError(entry, "Phenotype id was null.");
        } else if (!ontology.containsTerm(id)) {
            throw new HpoaEntryError(entry, "Could not find HPO term id.");
        }
        TermId primaryId = ontology.getPrimaryTermId(id);
        Optional<String> primaryLabel = ontology.getTermLabel(primaryId);
        if (primaryId == null) {
            throw new HpoaEntryError(entry, "Could not find id in ontology.");
        }

        if (primaryLabel.isEmpty() || primaryLabel.get().trim().isEmpty()) {
            throw new HpoaEntryError(entry, "Empty Label for term id.");
        }

        if (!primaryId.equals(id)) {
            throw new ObsoleteTermError(entry, primaryId, primaryLabel.get());
        }

        if (!primaryLabel.get().equals(termLabel)) {
            throw new ObsoleteTermError(entry, primaryId, primaryLabel.get());
        }
    }

    private static void checkAgeOfOnsetFields(HpoAnnotationEntry entry, Ontology ontology)
            throws HpoaEntryError, ObsoleteAspectError {
        String onsetId = entry.getAgeOfOnsetId();
        String onsetLabel = entry.getAgeOfOnsetLabel();
        if (onsetId == null || onsetId.isEmpty()) {
            if (onsetLabel != null && !onsetLabel.isEmpty()) {
                throw new HpoaEntryError(entry, "Onset ID empty but Onset label present");
            } else {
                return;
            }
        }
        TermId tid = TermId.of(onsetId);
        TermId primaryId = ontology.getPrimaryTermId(tid);
        String primaryLabel = ontology.getTermLabel(primaryId).orElse("");

        if (!primaryId.equals(tid) || !ontology.containsTerm(primaryId)) {
            throw new ObsoleteAspectError(entry, primaryId, primaryLabel);
        }

        if (!isValidInheritanceTerm(tid, ontology)) {
            throw new HpoaEntryError(entry, "Onset Id is not of type inheritance.");
        }

        if (onsetLabel == null || onsetLabel.isEmpty() || primaryLabel.trim().isEmpty()) {
            throw new HpoaEntryError(entry, "Onset label is missing.");
        }

        if (!primaryLabel.equals(onsetLabel)) {
            throw new HpoaEntryError(entry, "Onset label does not match id.");
        }
    }

    private static void checkEvidence(HpoAnnotationEntry entry) throws HpoaEntryError {
        String evi = entry.getEvidenceCode();
        if (!EVIDENCE_CODES.contains(EvidenceCode.valueOf(evi))) {
            throw new HpoaEntryError(entry, String.format("Invalid evidence code: \"%s\"", evi));
        }
    }

    private static void checkFrequency(HpoAnnotationEntry entry, Ontology ontology) throws HpoaEntryError {
        String freq = entry.getFrequencyModifier();
        if (freq == null || freq.isEmpty()) {
            return;
        }
        Matcher matcher = RATIO_PATTERN.matcher(freq);
        if (matcher.matches()) {
            int numerator = Integer.parseInt(matcher.group("numerator"));
            int denominator = Integer.parseInt(matcher.group("denominator"));
            if (numerator > denominator || denominator == 0) {
                throw new HpoaEntryError(entry, String.format("Malformed frequency (n/d): \"%s\"", freq));
            } else {
                return;
            }
        }
        matcher = PERCENTAGE_PATTERN.matcher(freq);
        if (matcher.matches()) {
            float percent = Float.parseFloat(matcher.group("value"));
            if (percent > 100f || percent <= 0f) {
                throw new HpoaEntryError(entry, String.format("Malformed frequency float: \"%s\"", freq));
            } else {
                return;
            }
        }
        if (!freq.matches("HP:\\d{7}")) {
            throw new HpoaEntryError(entry, String.format("Malformed frequency not term id or float or (n/d): \"%s\"", freq));
        }
        TermId id;
        id = TermId.of(freq);
        if (!isValidFrequencyTerm(id, ontology)) {
            throw new HpoaEntryError(entry, String.format("Frequency term id is not in frequency root: %s [%s]", ontology.getTermLabel(id).orElseThrow(), id.getValue()));
        }
    }

    private static void checkSexEntry(HpoAnnotationEntry entry) throws HpoaEntryError {
        Optional<Sex> sex = Sex.parse(entry.getSex());
        if (sex.isEmpty()) return;
        if (!sex.get().equals(Sex.MALE) && !sex.get().equals(Sex.FEMALE)){
            throw new HpoaEntryError(entry, String.format("Malformed sex entry: \"%s\"", sex.get().toString()));
        }
    }

    private static void checkNegation(HpoAnnotationEntry entry) throws HpoaEntryError {
        String negation = entry.getNegation();
        if (negation != null && !negation.isEmpty() && !negation.equals("NOT")) {
            throw new HpoaEntryError(entry, String.format("Malformed negation entry: \"%s\"", negation));
        }
    }

    private static void checkModifier(HpoAnnotationEntry entry, Ontology ontology) throws HpoaEntryError {
        String modifierString = entry.getModifier();
        if (modifierString == null || modifierString.isEmpty()) return;
        String[] A = modifierString.split(";");
        for (String a : A) {
            try {
                TermId tid = TermId.of(a);
                if (!isValidModifier(tid, ontology) && !isValidPaceOfProgressionTerm(tid, ontology) && !isValidTemporalPatternTerm(tid, ontology) && isValidInheritanceModifierTerm(tid, ontology)) {
                    throw new HpoaEntryError(entry, String.format("Modifier id not in pace of progression, temporal, inheritance: %s", tid.getValue()));
                }
            } catch (PhenolRuntimeException e) {
                throw new HpoaEntryError(entry, String.format("Malformed modifier term id: \"%s\"", a));
            }
        }
    }

    private static void checkPublication(HpoAnnotationEntry entry) throws HpoaEntryError {
        String pub = entry.getPublication();
        if (pub == null || pub.isEmpty()) {
            throw new HpoaEntryError(entry, "Empty citation string");
        }
        int index = pub.indexOf(":");
        if (index <= 0) {
            throw new HpoaEntryError(entry, String.format("Malformed citation id (not a CURIE): \"%s\"", pub));
        }
        if (pub.contains("::")) {
            throw new HpoaEntryError(entry, String.format("Malformed citation id (double colon): \"%s\"", pub));
        }
        if (pub.contains(" ")) {
            throw new HpoaEntryError(entry, String.format("Malformed citation id (contains space): \"%s\"", pub));
        }
        String prefix = pub.substring(0, index);
        if (!VALID_CITATION_PREFIXES.contains(prefix)) {
            throw new HpoaEntryError(entry, String.format("Did not recognize publication prefix: \"%s\" ", pub));
        }
        int len = pub.length();
        if (len - index < 2) {
            throw new HpoaEntryError(entry, String.format("Malformed publication string: \"%s\" ", pub));
        }
    }

    private static boolean isValidInheritanceTerm(TermId tid, Ontology hpo) {
        final TermId ONSET_ROOT = TermId.of("HP:0003674");
        return hpo.graph().existsPath(tid, ONSET_ROOT);
    }

    private static boolean isValidClinicalModifierTerm(TermId tid, Ontology hpo) {
        final TermId CLINICAL_MODIFIER_ROOT = TermId.of("HP:0012823");
        return hpo.graph().existsPath(tid, CLINICAL_MODIFIER_ROOT);
    }

    private static boolean isValidTemporalPatternTerm(TermId tid, Ontology hpo) {
        final TermId TEMPORAL_PATTERN_ROOT = TermId.of("HP:0011008");
        return hpo.graph().existsPath(tid, TEMPORAL_PATTERN_ROOT);
    }

    private static boolean isValidPaceOfProgressionTerm(TermId tid, Ontology hpo) {
        final TermId PACE_OF_PROGRESSION_ROOT = TermId.of("HP:0003679");
        return hpo.graph().existsPath(tid, PACE_OF_PROGRESSION_ROOT);
    }

    private static boolean isValidInheritanceModifierTerm(TermId tid, Ontology hpo) {
        final TermId INHERITANCE_MODIFIER_ROOT = TermId.of("HP:0034335");
        return hpo.graph().existsPath(tid, INHERITANCE_MODIFIER_ROOT);
    }

    private static boolean isValidModifier(TermId tid, Ontology ontology) {
        return isValidTemporalPatternTerm(tid, ontology) || isValidPaceOfProgressionTerm(tid, ontology)
                || isValidClinicalModifierTerm(tid, ontology) || isValidInheritanceModifierTerm(tid, ontology);
    }

    private static boolean isValidFrequencyTerm(TermId tid, Ontology hpo) {
        final TermId FREQUENCY_ROOT = TermId.of("HP:0040279");
        return hpo.graph().existsPath(tid, FREQUENCY_ROOT);
    }
}
