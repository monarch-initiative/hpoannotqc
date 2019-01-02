package org.monarchinitiative.hpoannotqc.smallfile;

/**
 * A set of constants that represent quality control actions performed
 * on the {@link SmallFile} objects.
 * @author Peter Robinson
 */
public enum SmallFileQCCode {
    INVALID_EVIDENCE_CODE("invalid evidence code"),
    BAD_DISEASE_NAME("bad diseasename"),
    BAD_DATABASE_NAME("bad database name"),
    MALFORMED_NEGATION("malformed negation string"),
    USAGE_OF_OBSOLETE_PHENOTYPE_ID("usage of out-of-date phenotype ID"),
    USAGE_OF_INVALID_PHENOTYPE_LABEL("usage of invalid HPO label"),
    INVALID_FREQUENCY_TERM("invalid frequency term"),
    MISSING_CITATION("missing citation"),
    MALFORMED_CITATION("malformed citation"),
   INVALID_ONSET_ALT_ID("invalid id of onset term "),
    INVALID_ONSET_LABEL("invalid ONSET label"),
    MISSING_BIOCURATION_ENTRY("missing biocuration entry"),
    MALFORMED_BIOCURATION_ENTRY("malformed biocuration entry"),
    PUBLICATION_PREFIX_IN_LOWER_CASE("Publication prefix was in lower case"),
    REPLACED_EMPTY_PUBLICATION_STRING("replaced empty publication string with disease ID"),
    CORRECTED_PUBLICATION_WITH_DATABASE_BUT_NO_ID("corrected publication entry with database name but no id"),
    CHANGED_MIM_TO_OMIM("changed mim prefix to omim"),
    CHANGED_PUBMED_TO_PMID("changed prefix PUBMED to PMID"),
    ADDED_FORGOTTEN_COLON("Adding a \":\" forgotten from a publication string"),
    FREQUENCY_WITH_TOO_MANY_DIGITS("frequency with too many digits of significance"),
    CONVERTED_N_OF_M("converted n of m to n/m"),
    FREQUENCY_WITH_DASH("frequency with dash and range (e.g., 1-2%)"),
    REMOVED_FREQUENCY_WHITESPACE("removed extra whitespace in frequency data"),
    ASSIGNED_BY_ONLY_HPO("assigned by code has only \"HPO\""),
    UNINITIALIZED_DISEASE_NAME("disease name not initialized"),
    HPO_PUBLICATION_CODE("HPO code in publication field"),
    PUBLICATION_HAD_NO_DB_PART("publication had not database (e.g., OMIM)");

    private final String name;

    SmallFileQCCode(String n) { name=n;}

    public String getName() { return name;}
}
