package org.monarchinitiative.hpoannotqc.smallfile;

/**
 * A set of constants that represent quality control actions performed
 * on the {@link SmallFile} objects.
 * @author Peter Robinson
 */
public enum SmallFileQCCode {
    INVALID_EVIDENCE_CODE("invalid evidence code"),
    MISSING_DISEASE_NAME("bad diseasename"),
    BAD_DATABASE_NAME("bad database name"),
    MALFORMED_NEGATION("malformed negation string"),
    USAGE_OF_OBSOLETE_PHENOTYPE_ID("usage of out-of-date phenotype ID"),
    PHENOTYPE_ID_NOT_FOUND("phenotype ID not found"),
    USAGE_OF_INVALID_PHENOTYPE_LABEL("usage of invalid HPO label"),
    INVALID_FREQUENCY_TERM("invalid frequency term"),
    MISSING_CITATION("missing citation"),
    MALFORMED_CITATION("malformed citation"),
    INVALID_ONSET_ALT_ID("invalid id of onset term "),
    INVALID_ONSET_LABEL("invalid ONSET label"),
    MISSING_BIOCURATION_ENTRY("missing biocuration entry"),
    MALFORMED_BIOCURATION_ENTRY("malformed biocuration entry"),
    UNINITIALIZED_DISEASE_NAME("disease name not initialized");

    private final String name;

    SmallFileQCCode(String n) { name=n;}

    public String getName() { return name;}
}
