package org.monarchinitiative.hpoannotqc.smallfile;

/**
 * A set of constants that represent quality control actions performed on the "old" small files. We use this to
 * tally up how many times each action was performed whilst converting all of our old small files to {@link V2SmallFile}
 * small files. This class can be removed after we have transitioned to the V2 files.
 */
public enum SmallFileQCCode {
    DID_NOT_FIND_EVIDENCE_CODE("Didnt find valid evidence code"),
    GOT_GENE_DATA("Found and discarded gene data"),
    UPDATING_ALT_ID("Updated alt_id to current primary id"),
    UPDATING_HPO_LABEL("Updated label to current label"),
    CREATED_MODIFER("Created modifier term"),
    UPDATED_DATE_FORMAT("Updated created-by date to canonical date format"),
    NO_DATE_CREATED("No data created found"),
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
    ASSIGNED_BY_EMPTY("assigned by code is empty"),
    UNINITIALIZED_DISEASE_NAME("disease name not initialized"),
    GOT_EQ_ITEM("Found and discarded EQ items");

    private final String name;

    SmallFileQCCode(String n) { name=n;}

    public String getName() { return name;}
}
