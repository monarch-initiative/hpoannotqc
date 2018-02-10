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
    GOT_EQ_ITEM("Found and discarded EQ items");

    private final String name;

    SmallFileQCCode(String n) { name=n;}

    public String getName() { return name;}
}
