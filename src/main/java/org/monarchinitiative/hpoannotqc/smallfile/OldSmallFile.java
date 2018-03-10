package org.monarchinitiative.hpoannotqc.smallfile;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.*;


/**
 * The HPO annotations are currently distribued across roughly 7000 "small files", which were created between 2009 and 2017.
 * We want to unify and extend the format for these files. This class represents a single "old" small file. The app will
 * transform these objects into {@link V2SmallFile} objects. Note that the "logic" for transforming small files has been
 * coded in the {@link OldSmallFileEntry} class, and {@link OldSmallFile} basically just identifies the column indices and splits up the
 * lines into corresponding fields. There is some variability in the naming of columns (e.g., Sex and SexID), and this
 * class tries to figure that out.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 1/20/2018.
 */
public class OldSmallFile {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int UNINITIALIZED=-42;
    /** The number of fields is usually 21, but we parse according to the header and there is some variability. The
     * number of fields is not a requirement for our old-file format
     */
    private int n_fields;
    private int DISEASE_ID_INDEX=UNINITIALIZED;
    private int DISEASE_NAME_INDEX=UNINITIALIZED;
    private int GENE_ID_INDEX=UNINITIALIZED;
    private int GENE_NAME_INDEX=UNINITIALIZED;
    private int GENOTYPE_INDEX=UNINITIALIZED;
    private int GENE_SYMBOL_INDEX=UNINITIALIZED;
    private int PHENOTYPE_ID_INDEX=UNINITIALIZED;
    private int PHENOTYPE_NAME_INDEX=UNINITIALIZED;
    private int AGE_OF_ONSET_ID_INDEX=UNINITIALIZED;
    private int AGE_OF_ONSET_NAME_INDEX=UNINITIALIZED;
    private int EVIDENCE_ID_INDEX=UNINITIALIZED;
    private int EVIDENCE_NAME_INDEX=UNINITIALIZED;
    private int FREQUENCY_INDEX=UNINITIALIZED;
    private int SEX_ID_INDEX=UNINITIALIZED;
    private int SEX_NAME_INDEX=UNINITIALIZED;
    /** Some entries have just "Sex" with no ID/Name */
    private int SEX_INDEX=UNINITIALIZED;
    private int NEGATION_ID_INDEX=UNINITIALIZED;
    private int NEGATION_NAME_INDEX=UNINITIALIZED;
    private int DESCRIPTION_INDEX=UNINITIALIZED;
    private int PUB_INDEX=UNINITIALIZED;
    private int ASSIGNED_BY_INDEX=UNINITIALIZED;
    private int DATE_CREATED_INDEX=UNINITIALIZED;
    private int ENTITY_ID_INDEX=UNINITIALIZED;
    private int ENTITY_NAME_IDX=UNINITIALIZED;
    /** SOme entries just have "Evidence"??? */
    private int EVIDENCE_INDEX=UNINITIALIZED;
    private int QUALITY_ID_INDEX=UNINITIALIZED;
    private int QUALITY_NAME_INDEX=UNINITIALIZED;
    private int ADDL_ENTITY_ID_INDEX=UNINITIALIZED;
    private int ADDL_ENTITY_NAME_INDEX=UNINITIALIZED;
    private int ABNORMAL_ID_INDEX=UNINITIALIZED;
    private int ABNORMAL_NAME_INDEX=UNINITIALIZED;
    private int ORTHOLOGS_INDEX=UNINITIALIZED;

    private BiMap<FieldType,Integer> fields2index;
    /** A list of {@link OldSmallFileEntry} objects, each of which corresponds
     * to a line in the old small file (except for the header). */
    private List<OldSmallFileEntry> entrylist=new ArrayList<>();
    /** Path to an old small file, e.g., OMIM-600321.tab */
    private final String pathToOldSmallFile;

    private boolean hasQCissue=false;

    private int n_corrected_date=0;
    private int n_no_evidence=0;
    private int n_gene_data=0;
    private int n_alt_id=0;
    private int n_update_label=0;
    private int n_created_modifier=0;
    private int n_EQ_item=0;
    private int n_publication_prefix_in_lower_case=0;
    private int n_replaced_empty_publication_string=0;
    private int n_corrected_publication_with_database_but_no_id=0;
    private int n_no_date_created=0;
    private int n_changed_MIM_to_OMIM=0;
    private int n_changed_PUBMED_to_PMID=0;
    private int n_added_forgotten_colon=0;
    private int n_frequency_with_dash=0;
    private int n_frequency_removed_whitespace;
    private int n_assigned_by_only_HPO=0;
    private int n_assigned_by_empty =0;
    private int n_converted_n_of_m=0;
    private int n_lineHasQcIssue=0;

    /** This is called for lines that have less than the expected number of fields given the number of fields in the header.
     * In practice this seems to be related to entries that are missing a "Date created" field.
     */
    private int n_less_than_expected_number_of_lines=0;

    public OldSmallFile(String path) {
        pathToOldSmallFile=path;
        parse();
    }

    String getBasename() {
        return new File(pathToOldSmallFile).getName();
    }


    /** Ingest one "old" small file. */
    private void parse() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathToOldSmallFile));
            String line;
            line=br.readLine();// the header
            processHeader(line); // identify the indices
            while ((line=br.readLine())!=null ){
                if (line.trim().isEmpty()) continue; // skip empty lines
                try {
                    processContentLine(line);
                } catch (HPOException e) {
                    LOGGER.error(e.getMessage() + "\nOffending line:\n"+line);
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<OldSmallFileEntry> getEntrylist() {
        return entrylist;
    }

    public int getN_corrected_date() {
        return n_corrected_date;
    }

    public int getN_no_evidence() {
        return n_no_evidence;
    }

    public int getN_gene_data() {
        return n_gene_data;
    }

    public int getN_alt_id() {
        return n_alt_id;
    }

    public int getN_update_label() {
        return n_update_label;
    }

    public int getN_created_modifier() {
        return n_created_modifier;
    }

    public int getN_EQ_item() {
        return n_EQ_item;
    }

    public int getN_less_than_expected_number_of_lines() { return n_less_than_expected_number_of_lines; }

    public int getN_lineHasQcIssue() {
        return n_lineHasQcIssue;
    }

    private void processContentLine(String line) throws HPOException {
        String F[]=line.split("\t");
        if (F.length != n_fields) {
            LOGGER.trace("We were expecting " + n_fields + " fields but got only " + F.length + "for line:\n"+line);
            n_less_than_expected_number_of_lines++;
        }
        OldSmallFileEntry entry = new OldSmallFileEntry();
        for (int i=0;i<F.length;i++) {
            FieldType typ = this.fields2index.inverse().get(i);
            if (typ==null) {
                LOGGER.error(String.format("Could not retrieve typ for i=%d",i ));
                LOGGER.error("Offending line: \""+line+"\"");
                System.exit(1);
            }
            switch (typ) {
                case DISEASE_ID:
                    entry.addDiseaseId(F[i]);
                    break;
                case DISEASE_NAME:
                    entry.addDiseaseName(F[i]);
                    break;
                case GENE_ID:
                    entry.addGeneId(F[i]);
                    break;
                case GENE_NAME:
                    entry.setGeneName(F[i]);
                    break;
                case GENOTYPE:
                    entry.setGenotype(F[i]);
                    break;
                case GENE_SYMBOL:
                    entry.setGenesymbol(F[i]);
                    break;
                case PHENOTYPE_ID:
                    entry.setPhenotypeId(F[i]);
                    break;
                case PHENOTYPE_NAME:
                    entry.setPhenotypeName(F[i]);
                    break;
                case AGE_OF_ONSET_ID:
                    entry.setAgeOfOnsetId(F[i]);
                    break;
                case AGE_OF_ONSET_NAME:
                    entry.setAgeOfOnsetName(F[i]);
                    break;
                case EVIDENCE_ID:
                    entry.setEvidenceId(F[i]);
                    break;
                case EVIDENCE_NAME:
                    entry.setEvidenceName(F[i]);
                    break;
                case FREQUENCY:
                    entry.setFrequencyString(F[i]);
                    break;
                case SEX_ID:
                    entry.setSexID(F[i]);
                    break;
                case SEX_NAME:
                    entry.setSexName(F[i]);
                    break;
                case NEGATION_ID:
                    entry.setNegationID(F[i]);
                    break;
                case NEGATION_NAME:
                    entry.setNegationName(F[i]);
                    break;
                case DESCRIPTION:
                    entry.setDescription(F[i]);
                    break;
                case PUB:
                    entry.setPub(F[i]);
                    break;
                case ASSIGNED_BY:
                    entry.setAssignedBy(F[i]);
                    break;
                case DATE_CREATED:
                    entry.setDateCreated(F[i]);
                    break;
                case ADDL_ENTITY_ID:
                    entry.setAddlEntityId(F[i]);
                    break;
                case ADDL_ENTITY_NAME:
                    entry.setAddlEntityName(F[i]);
                    break;
                case ENTITY_ID:
                    entry.setEntityId(F[i]);
                    break;
                case ENTITY_NAME:
                    entry.setEntityName(F[i]);
                    break;
                case QUALITY_ID:
                    entry.setQualityId(F[i]);
                    break;
                case QUALITY_NAME:
                    entry.setQualityName(F[i]);
                    break;
                case EVIDENCE:
                    entry.setEvidence(F[i]);
                    break;
                case ABNORMAL_ID:
                    entry.setAbnormalId(F[i]);
                    break;
                case ABNORMAL_NAME:
                    entry.setAbnormalName(F[i]);
                    break;
                case SEX:
                    entry.setSex(F[i]);
                    break;
                case ORTHOLOGS:
                    entry.setOrthologs(F[i]);
                    break;
                default:
                    LOGGER.error("Need to add switch-case for id="+typ);
                    System.exit(1);
            }
        }
        // When we get here, we have added all of the fields of the OLD file. We will do a Q/C check and
        // record any "repair" jobs that needed to be performed.
        Set<SmallFileQCCode> qcItemList = entry.doQCcheck();
        tallyQCitems(qcItemList,line);
        if (entry.hasQCissues()) {
            // if there was a QC issue, then the old line will have been output to the LOG together
            // with an indication of the issue. Therefore, we output the corresponding new line to LOG
            // so we can perform checking.
            // Note that the actual output of the new lines is done by the V2SmallFile class and not here.
            V2SmallFileEntry v2entry = new V2SmallFileEntry(entry);
            LOGGER.trace("V2 entry: " + v2entry.getRow());
            n_lineHasQcIssue++;

        }
        entrylist.add(entry);
    }





    public boolean hasQCissue() {
        return hasQCissue;
    }

    public int getN_publication_prefix_in_lower_case() {
        return n_publication_prefix_in_lower_case;
    }

    public int getN_replaced_empty_publication_string() {
        return n_replaced_empty_publication_string;
    }

    public int getN_corrected_publication_with_database_but_no_id() {
        return n_corrected_publication_with_database_but_no_id;
    }

    public int getN_no_date_created() {
        return n_no_date_created;
    }

    public int getN_changed_MIM_to_OMIM() {
        return n_changed_MIM_to_OMIM;
    }

    public int getN_changed_PUBMED_to_PMID() {
        return n_changed_PUBMED_to_PMID;
    }

    public int getN_added_forgotten_colon() {
        return n_added_forgotten_colon;
    }

    public int getN_frequency_with_dash() {
        return n_frequency_with_dash;
    }

    public int getN_frequency_removed_whitespace() {
        return n_frequency_removed_whitespace;
    }

    public int getN_assigned_by_only_HPO() {
        return n_assigned_by_only_HPO;
    }

    public int getN_assigned_by_empty() {
        return n_assigned_by_empty;
    }

    public int getN_converted_n_of_m() {
        return n_converted_n_of_m;
    }

    /**
     * This function gets called for all entries (old small file lines) that have one or
     * more Q/C issues. Basically we just tally them up.
     * @param qcitems list of QC issues found for this line
     * @param line A line of the small file
     */
    private void tallyQCitems(Set<SmallFileQCCode> qcitems, String line) {
        if (qcitems.size()==0)return;
        for (SmallFileQCCode qcode : qcitems) {
            if (! qcode.equals(UPDATED_DATE_FORMAT)) this.hasQCissue=true;

            switch (qcode) {
                case UPDATED_DATE_FORMAT:
                    n_corrected_date++;
                    break;// do not output log entry about date format
                case DID_NOT_FIND_EVIDENCE_CODE:
                   n_no_evidence++;
                    LOGGER.trace(String.format("%s:%s",DID_NOT_FIND_EVIDENCE_CODE.name(),line));
                    break;
                case GOT_GENE_DATA:
                    n_gene_data++;
                    LOGGER.trace(String.format("%s:%s",GOT_GENE_DATA.name(),line));
                    break;
                case PUBLICATION_PREFIX_IN_LOWER_CASE:
                    n_publication_prefix_in_lower_case++;
                    break;
                case REPLACED_EMPTY_PUBLICATION_STRING:
                    n_replaced_empty_publication_string++;
                    break;
                case CORRECTED_PUBLICATION_WITH_DATABASE_BUT_NO_ID:
                    n_corrected_publication_with_database_but_no_id++;
                    break;
                case ADDED_FORGOTTEN_COLON:
                    n_added_forgotten_colon++;
                    break;
                case NO_DATE_CREATED:
                    n_no_date_created++;
                    break;
                case UPDATING_ALT_ID:
                    n_alt_id++;
                    LOGGER.trace(String.format("%s:%s",UPDATING_ALT_ID.name(),line));
                    break;
                case UPDATING_HPO_LABEL:
                    n_update_label++;
                    LOGGER.trace(String.format("%s:%s",UPDATING_HPO_LABEL.name(),line));
                    break;
                case CHANGED_MIM_TO_OMIM:
                    n_changed_MIM_to_OMIM++;
                    break;
                case CHANGED_PUBMED_TO_PMID:
                    n_changed_PUBMED_to_PMID++;
                    break;
                case CREATED_MODIFER:
                    n_created_modifier++;
                    LOGGER.trace(String.format("%s:%s",CREATED_MODIFER.name(),line));
                    break;
                case GOT_EQ_ITEM:
                    n_EQ_item++;
                    LOGGER.trace(String.format("%s:%s",GOT_EQ_ITEM.name(),line));
                    break;
                case FREQUENCY_WITH_DASH:
                    n_frequency_with_dash++;
                    LOGGER.trace(String.format("%s:%s",FREQUENCY_WITH_DASH.name(),line));
                    break;
                case REMOVED_FREQUENCY_WHITESPACE:
                    n_frequency_removed_whitespace++;
                    LOGGER.trace(String.format("%s:%s", REMOVED_FREQUENCY_WHITESPACE.name(),line));
                    break;
                case ASSIGNED_BY_EMPTY:
                    n_assigned_by_empty++;
                    LOGGER.trace(String.format("%s:%s",ASSIGNED_BY_EMPTY.name(),line));
                    break;
                case ASSIGNED_BY_ONLY_HPO:
                    n_assigned_by_only_HPO++;
                    LOGGER.trace(String.format("%s:%s",ASSIGNED_BY_ONLY_HPO.name(),line));
                    break;
                case CONVERTED_N_OF_M:
                    n_converted_n_of_m++;
                    LOGGER.trace(String.format("%s:%s",CONVERTED_N_OF_M.name(),line));
                    break;
            }
        }
    }


    private void processHeader(String header) {
        String A[] = header.split("\t");
        n_fields=A.length;
        ImmutableBiMap.Builder<FieldType,Integer> builder = new ImmutableBiMap.Builder();
        for (int i=0;i<n_fields;i++) {
            FieldType fieldtype= FieldType.string2fields(A[i]);
            builder.put(fieldtype,i);
            switch (A[i]) {
                case "Disease ID":
                    DISEASE_ID_INDEX=i;
                    break;
                case "Disease Name":
                    DISEASE_NAME_INDEX=i;
                    break;
                case "Gene ID":
                    GENE_ID_INDEX=i;
                    break;
                case "Gene Name":
                    GENE_NAME_INDEX=i;
                    break;
                case "Genotype":
                    GENOTYPE_INDEX=i;
                    break;
                case "Gene Symbol(s)":
                    GENE_SYMBOL_INDEX=i;
                    break;
                case "Phenotype ID":
                    PHENOTYPE_ID_INDEX=i;
                    break;
                case "Phenotype Name":
                    PHENOTYPE_NAME_INDEX=i;
                    break;
                case "Age of Onset ID":
                    AGE_OF_ONSET_ID_INDEX=i;
                    break;
                case "Age of Onset Name":
                    AGE_OF_ONSET_NAME_INDEX=i;
                    break;
                case "Evidence ID":
                    EVIDENCE_ID_INDEX=i;
                    break;
                case "Evidence Name":
                    EVIDENCE_NAME_INDEX=i;
                    break;
                case "Frequency":
                    FREQUENCY_INDEX=i;
                case "Sex ID":
                    SEX_ID_INDEX=i;
                    break;
                case "Sex Name":
                    SEX_NAME_INDEX=i;
                    break;
                case "Negation ID":
                    NEGATION_ID_INDEX=i;
                    break;
                case "Negation Name":
                    NEGATION_NAME_INDEX=i;
                    break;
                case "Description":
                    DESCRIPTION_INDEX=i;
                    break;
                case "Pub":
                    PUB_INDEX=i;
                    break;
                case "Assigned by":
                    ASSIGNED_BY_INDEX=i;
                    break;
                case "Date Created":
                    DATE_CREATED_INDEX=i;
                    break;
                case "Entity ID":
                    ENTITY_ID_INDEX=i;
                    break;
                case "Entity Name":
                    ENTITY_NAME_IDX=i;
                    break;
                case "Add'l Entity ID":
                    ADDL_ENTITY_ID_INDEX=i;
                    break;
                case "Add'l Entity Name":
                    ADDL_ENTITY_NAME_INDEX=i;
                    break;
                case "Quality ID":
                    QUALITY_ID_INDEX=i;
                    break;
                case "Quality Name":
                    QUALITY_NAME_INDEX=i;
                    break;
                case "Evidence":
                    EVIDENCE_INDEX=i;
                    break;
                case "Abnormal ID":
                    ABNORMAL_ID_INDEX=i;
                    break;
                case "Abnormal Name":
                    ABNORMAL_NAME_INDEX=i;
                    break;
                case "Sex":
                    SEX_INDEX=i;
                    break;
                case "Orthologs":
                    ORTHOLOGS_INDEX=i;
                    break;
                default:
                    LOGGER.error("Did not recognize header field \""+A[i]+"\"");
                    LOGGER.error("Terminating, please check OldSmallFile");
                    System.exit(1);
            }
            this.fields2index=builder.build();
        }
    }

}
