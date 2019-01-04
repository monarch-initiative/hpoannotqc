package org.monarchinitiative.hpoannotqc.smallfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;


import org.monarchinitiative.phenol.ontology.data.*;

import java.util.*;

import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.*;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * The purpose of this class is to check each V2 small file line from the version 2 (V2) small files that represent
 * the standard HPO annotation format from 2018 onwards. The class will tally up the Q/C results and store any V2
 * lines that appear "dodgy", providing a Q/C report. The class is intended to be used while the files are being converted
 * and to look at each V2 line in turn.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class SmallFileEntryQC {
    private static final Logger logger = LogManager.getLogger();
    private final HpoOntology ontology;


    private final List<String> errors=new ArrayList<>();
    private final List<SmallFileQCCode> errorcodes=new ArrayList<>();

    private final Set<TermId> onsetTerms;

    private final Map<String,Integer> assignedByMap = new HashMap<>();

   /** True if the current SmallFileEntry we just analyzed had no QC errors. */
    private boolean clean;

    private final TermId ONSET_ROOT = TermId.of("HP:0003674");
    private static final TermId FREQUENCY_ROOT = TermId.of("HP:0040279");
    /** The current entry we are processing (used for error Q/C and updated for each entry). */
    private SmallFileEntry currentSmallFileEntry;

    public SmallFileEntryQC(HpoOntology onto) {
        this.ontology=onto;
        this.onsetTerms = getDescendents(ontology,ONSET_ROOT);
    }


    private void checkDB(String db) {
        if (db.equals("OMIM") ||
                db.equals("ORPHA") ||
                db.equals("DECIPHER")) {
            return;
        } else {
            errors.add(String.format("Bad DB: %s",db));
            errorcodes.add(BAD_DATABASE_NAME);
            clean = false;
        }
    }

    /** Check not null. */
    private void checkDiseaseName(String name) {
        if (name==null || name.isEmpty()) {
            errors.add(String.format("Bad disease name: %s",name));
            errorcodes.add(MISSING_DISEASE_NAME);
            clean = false;
        }
    }

    /**
     * The negation string can be null or empty but if it is present it must be "NOT"
     * @param negation Must be either the empty/null String or "NOT"
     * @return true if the input is valid
     */
    private void checkNegation(String negation) {
        if ( negation!=null &&  ! negation.isEmpty() && ! negation.equals("NOT")) {
            errors.add(String.format("Bad negation: \"%s\"",negation));
            errorcodes.add(MALFORMED_NEGATION);
            clean = false;
        }
    }


    /**
     * Check that the id is not an alt_id (i.e., out of date!)
     * @param id the {@link TermId} for a phenotype HPO term
     */
    private void checkPhenotypeId(TermId id) {
        if (id==null || ! ontology.getTermMap().containsKey(id)) {
            errors.add("Could not find HPO term id for \""+id.getValue()+"\"");
            errorcodes.add(PHENOTYPE_ID_NOT_FOUND);
            clean = false;
        } else {
            TermId current = ontology.getTermMap().get(id).getId();
            if (! current.equals(id)) {
                String errmsg = String.format("Found usage of alt_id %s for %s (%s): see following line",
                        id,
                        current,
                        ontology.getTermMap().get(id).getName());
                errors.add(errmsg);
                errorcodes.add(USAGE_OF_OBSOLETE_PHENOTYPE_ID);
                clean = false;
            }
        }
    }

    /** Check that the label is the current label that matches the term id. */
    private void checkPhenotypeLabel(TermId id, String label) {
        if (label==null || label.isEmpty()) {
            errors.add("Empty HPO term label");
            errorcodes.add(USAGE_OF_INVALID_PHENOTYPE_LABEL);
            clean= false;
        }
        String currentLabel = ontology.getTermMap().get(id).getName();
        if (! currentLabel.equals(label)) {
            String errmsg = String.format("Found usage of wrong term label %s instead of %s for %s: see following line",
                    label,
                    currentLabel,
                    ontology.getTermMap().get(id).getName());
            errors.add(errmsg);
            errorcodes.add(USAGE_OF_INVALID_PHENOTYPE_LABEL);
            clean= false;
        }
    }


    private void checkPublication(String pub) {
        if (pub == null || pub.isEmpty()) {
            errors.add("Empty citation string");
            errorcodes.add(MISSING_CITATION);
            clean = false;
        }
        int index = pub.indexOf(":");
        if (index <= 0) { // there needs to be a colon in the middle of the string
            errors.add(String.format("Malformed citation id (not a CURIE): \"%s\"", pub));
            errorcodes.add(MALFORMED_CITATION);
            clean = false;
            return;
        }
        if (pub.contains("::")) { // should only be one colon separating prefix and id
            errors.add(String.format("Malformed citation id (double colon): \"%s\"", pub));
            errorcodes.add(MALFORMED_CITATION);
            clean = false;
            return;
        }
        if (pub.startsWith("HPO")) {
            errors.add(String.format("Malformed citation id: \"%s\"", pub));
            errorcodes.add(MALFORMED_CITATION);
            clean = false;
            return;
        }
        if (pub.contains(" ")) {
            errors.add(String.format("Malformed citation id (contains space): \"%s\"", pub));
            errorcodes.add(MALFORMED_CITATION);
            clean = false;
            return;
        }
        if (!pub.startsWith("PMID") &&
                !pub.startsWith("OMIM")&&
                !pub.startsWith("http") &&
                !pub.startsWith("DECIPHER") &&
                !pub.startsWith("ISBN")) {
            errors.add(String.format("Did not recognize publication prefix: \"%s\" for %s ", pub.substring(0, index), pub));
            errorcodes.add(MALFORMED_CITATION);
            clean = false;
        }
    }

    /**
     * check the age of onset id. It is allowed to be null, but then the age of onset label also has to be null.
     * If it is not null, it has to be a valid term in the Onset subhierarchy of the hpo.
     * @param id A term id that should be from the Onset subhierarchy
     */
    private void checkAgeOfOnsetId(String id) {
        if (id==null || id.isEmpty() ) {
          // valid, onset is not required
            return;
        }
        TermId tid = TermId.of(id);
        if (! ontology.getTermMap().containsKey(tid)) {
            errors.add("Attempt to add onset ID that was not in graph: "+id);
            errorcodes.add(INVALID_ONSET_ALT_ID);
            clean = false;
        }
        if (! checkIsNotAltId(tid)) {
            errors.add("Usage of (obsolete) alt_id for onset term: " + id);
            errorcodes.add(INVALID_ONSET_ALT_ID);
            clean = false;
        }
        if (! this.onsetTerms.contains(tid)) {
            errors.add("Malformed age of onset ID: \""+tid.toString()+"\"");
            errorcodes.add(INVALID_ONSET_ALT_ID);
            clean = false;
        }
    }

    /**
     *
     * @param tid An HPO Term Id
     * @return true if the tid is up to date, i.e., NOT an alt_id
     */
    private boolean checkIsNotAltId(TermId tid) {
        if (! ontology.getTermMap().containsKey(tid)) {
            errors.add("TermId not found at all in ontology: " + tid.getValue());
            return false;
        }
        TermId upToDate = ontology.getPrimaryTermId(tid);
        return tid.equals(upToDate);
    }


    /** Check that the label is the current label that matches the term id. */
    private void checkAgeOfOnsetLabel(String id, String label) {
        if ((id==null || id.isEmpty()) && (label==null||label.isEmpty())){
            // ok, not required
            return;
        }
        if (label==null || label.isEmpty()) {
            errors.add(String.format("Bad age of onset label: %s(%s)",label,id));
            errorcodes.add(INVALID_ONSET_LABEL);
            clean = false;
        }
        Objects.requireNonNull(id);
        TermId tid = TermId.of(id);
        String currentLabel = ontology.getTermMap().get(tid).getName();
        if (! currentLabel.equals(label)) {
            String errmsg = String.format("Found usage of wrong age of onset label %s instead of %s for %s: see following line",
                    label,
                    currentLabel,
                    ontology.getTermMap().get(tid).getId().getValue());
            errors.add(errmsg);
            errorcodes.add(INVALID_ONSET_LABEL);
            clean = false;
        }
    }

    private void checkEvidence(String evi) {
        if (! evi.equals("IEA") &&
                ! evi.equals("PCS") &&
                ! evi.equals("TAS")) {
            errors.add(String.format("Did not recognize evidence code \"%s\".",evi));
            errorcodes.add(INVALID_EVIDENCE_CODE);
            clean=false;
        }
    }



    private void checkBiocuration(String entrylist) {
        if (entrylist==null || entrylist.isEmpty()) {
            errors.add(String.format("empty biocuration entry: %s",entrylist));
            errorcodes.add(MISSING_BIOCURATION_ENTRY);
            clean = false;
            return;
        }
        List<BiocurationEntry> entries = BiocurationEntry.getBiocurationList(entrylist);
        if (entries.isEmpty()) {
            errors.add(String.format("malformed biocuration entry: %s",entrylist));
            errorcodes.add(MALFORMED_BIOCURATION_ENTRY);
            clean = false;
        }
    }


    /** There are 3 correct formats for frequency. For example, 4/7, 32% (or 32.6%), or
     * an HPO term from the frequency subontology. */
    private void checkFrequency(String freq) {
        // it is ok not to have frequency data
        if (freq==null || freq.isEmpty()) {
            return;
        }
        if (freq.matches("\\d+/\\d+") ||
                freq.matches("\\d{1,3}%") ||
                freq.matches("\\d{1,3}\\.\\d+%")) {
            // valid numerical frequencies!
            return;
        } else if (! freq.matches("HP:\\d{7}")) {
            // cannot be a valid frequency term
            errors.add("Invalid frequency term: " + freq);
            errorcodes.add(INVALID_FREQUENCY_TERM);
            clean = false;
            return;
        }
        // if we get here and we can validate that the frequency term comes from the right subontology,
        // then the item is valid
        TermId id=null;
        try {
            id = TermId.of(freq);
        } catch (PhenolRuntimeException pre) {
            System.out.println("[RunTimeError] Could not parse frequency term id " + freq);
            System.err.println("for entry="+currentSmallFileEntry.toString());
            System.exit(1);
        }
        if (! existsPath(ontology,id,FREQUENCY_ROOT)) {
            errors.add(String.format("Could not find term %s [%s] in frequency subontology",
                    ontology.getTermMap().get(id).getName(),
                    ontology.getTermMap().get(id).getId().getValue()));
           errorcodes.add(INVALID_FREQUENCY_TERM);
            clean = false;
        }
    }



    public void dumpAssignedByMap() {
        System.out.println("### Biocurated annotations ###");
        for (String ab : assignedByMap.keySet()) {
            System.out.println(ab +": n="+assignedByMap.get(ab));
        }
    }


    public List<String> getErrors() {
        return this.errors;
    }
    public List<SmallFileQCCode> getErrorCodes() { return this.errorcodes; }
    public boolean hasErrors() { return (! clean); }

    public boolean checkSmallFileEntry(SmallFileEntry entry) {
        // note the function set errors, errorcodes, and clean
        this.clean=true;
        errors.clear(); // reset list of error messages for the current entry.
        errorcodes.clear(); // reset list of error codes for the entry
        this.currentSmallFileEntry=entry;
        checkDB(entry.getDB());
        checkDiseaseName(entry.getDiseaseName());
        checkNegation(entry.getNegation());
        checkPhenotypeId(entry.getPhenotypeId());
        checkPhenotypeLabel(entry.getPhenotypeId(),entry.getPhenotypeLabel());
        checkPublication(entry.getPublication());
        checkAgeOfOnsetId(entry.getAgeOfOnsetId());
        checkAgeOfOnsetLabel(entry.getAgeOfOnsetId(),entry.getAgeOfOnsetLabel());
        checkEvidence(entry.getEvidenceCode());
        checkBiocuration(entry.getBiocuration());
        checkFrequency(entry.getFrequencyModifier());
        return clean; // returns true if there were zero errors with this line
    }




    public void dumpQCtoLog() {
        logger.info("#####   V2 Conversion Quality Control   #####");
        logger.info("#####   Lines with errors   #####");
        if (errors.size()>0) {
        for (String err : errors) {
            logger.error(err);
        }
        } else {
            logger.info("No errors detected");
        }

        logger.info("#####   Q/C Summary   #####");
    }

}
