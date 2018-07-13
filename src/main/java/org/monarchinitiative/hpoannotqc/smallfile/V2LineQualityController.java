package org.monarchinitiative.hpoannotqc.smallfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;


import org.monarchinitiative.phenol.ontology.data.*;

import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * The purpose of this class is to check each V2 small file line from the version 2 (V2) small files that represent
 * the standard HPO annotation format from 2018 onwards. The class will tally up the Q/C results and store any V2
 * lines that appear "dodgy", providing a Q/C report. The class is intended to be used while the files are being converted
 * and to look at each V2 line in turn.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class V2LineQualityController {
    private static final Logger logger = LogManager.getLogger();
    private final HpoOntology ontology;


    private final List<String> errors=new ArrayList<>();

    private final Set<TermId> onsetTerms;

    private final Map<String,Integer> assignedByMap = new HashMap<>();

    private int n_good_DB=0;
    private int n_bad_DB=0;
    private String qcDB() { return String.format("%d good and %d bad database entries",n_good_DB,n_bad_DB); }
    private int n_good_diseasename=0;
    private int n_bad_diseasename=0;
    private String qcDiseasename() {  return String.format("%d good and %d bad disease name entries",n_good_diseasename,n_bad_diseasename);}
    private int n_good_negation=0;
    private int n_bad_negation=0;
    private String qcNegation() {  return String.format("%d good and %d bad negation entries",n_good_negation,n_bad_negation);}
    private int n_good_phenotypeID=0;
    private int n_bad_phenotypeID=0;
    private String qcPhenotypeID() { return String.format("%d good and %d bad phenotypeID entries",n_good_phenotypeID,n_bad_phenotypeID);}
    private int n_good_phenotypeLabel=0;
    private int n_bad_phenotypeLabel=0;
    private String qcPhenotypeLabel() { return String.format("%d good and %d bad phenotype label entries",n_good_phenotypeLabel,n_bad_phenotypeLabel);}
    private int n_good_publication=0;
    private int n_bad_publication=0;
    private String qcPublication() { return String.format("%d good and %d bad publication entries",n_good_publication,n_bad_publication);}
    private int n_good_ageOfOnset_ID=0;
    private int n_bad_ageOfOnset_ID=0;
    private String qcAgeOfOnsetID() { return String.format("%d good and %d bad age of onset ID entries",n_good_ageOfOnset_ID,n_bad_ageOfOnset_ID);}
    private int n_good_ageOfOnsetLabel=0;
    private int n_bad_ageOfOnsetLabel=0;
    private String qcAgeOfOnsetLabel() { return String.format("%d good and %d bad age of onset label entries",
            n_good_ageOfOnsetLabel,n_bad_ageOfOnsetLabel);}
    private int n_good_evidence=0;
    private int n_bad_evidence=0;
    private String qcEvidence() { return String.format("%d good and %d bad evidence entries",n_good_evidence,n_bad_evidence);}
    private int n_good_aspect=0;
    private int n_bad_aspect=0;
    private String qcAspect() { return String.format("%d good and %d bad aspect entries",n_good_aspect,n_bad_aspect);}
    private int n_good_dateCreated=0;
    private int n_bad_dateCreated=0;
    private String qcDateCreated() { return String.format("%d good and %d bad data-created entries",n_good_dateCreated,n_bad_dateCreated);}
    private int n_good_assignedBy=0;
    private int n_bad_assignedBy=0;
    private String qcAssignedBy() { return String.format("%d good and %d bad assigned-by entries",n_good_assignedBy,n_bad_assignedBy);}
    private int n_good_frequency=0;
    private int n_bad_frequency=0;
    private String qcFrequency() { return String.format("%d good and %d bad frequency entries",n_good_frequency,n_bad_frequency);}

    /** Todo get from phenol */
    private final TermId ONSET_ROOT = TermId.constructWithPrefix("HP:0003674");
    private static final TermId FREQUENCY_ROOT = TermId.constructWithPrefix("HP:0040279");

    public V2LineQualityController(HpoOntology onto) {

        this.ontology=onto;
        this.onsetTerms = getDescendents(ontology,ONSET_ROOT);
    }


    private boolean checkDB(String db) {
        if (db.equals("OMIM") ||
                db.equals("ORPHA") ||
                db.equals("DECIPHER")) {
            n_good_DB++;
            return true;
        } else {
            n_bad_DB++;
            return false;
        }
    }

    /** Check not null. */
    private boolean checkDiseaseName(String name) {
        if (name!=null && ! name.isEmpty()) {
            n_good_diseasename++;
            return true;
        } else {
            n_bad_diseasename++;
            return false;
        }
    }

    /**
     * The negation string can be null or empty but if it is present it must be "NOT"
     * @param negation Must be either the empty/null String or "NOT"
     * @return true if the input is valid
     */
    private boolean checkNegation(String negation) {
        if (negation==null ||  negation.isEmpty() || negation.equals("NOT")) {
            n_good_negation++;
            return true;
        } else {
            n_bad_negation++;
            return false;
        }
    }

    public void incrementGoodAspect() { n_good_aspect++;}
    public void incrementBadAspect() { n_bad_aspect++; }

    /**
     * Check that the id is not an alt_id
     * @param id the {@link TermId} for a phenotype HPO term
     * @return true iff phenotype id is valid
     */
    private boolean checkPhenotypeId(TermId id) {
        if (id==null) {
            return false;
        } else {
            TermId current = ontology.getTermMap().get(id).getId();
            if (current.equals(id)) {
                n_good_phenotypeID++;
                return true;
            } else {
                String errmsg = String.format("Found usage of alt_id %s for %s (%s): see following line",
                        id,
                        current,
                        ontology.getTermMap().get(id).getName());
                errors.add(errmsg);
                n_bad_phenotypeID++;
                return false;
            }
        }
    }

    /** Check that the label is the current label that matches the term id. */
    private boolean checkPhenotypeLabel(TermId id, String label) {
        if (label==null || label.isEmpty()) {
            n_bad_phenotypeLabel++;
            return false;
        }
        String currentLabel = ontology.getTermMap().get(id).getName();
        if (! currentLabel.equals(label)) {
            String errmsg = String.format("Found usage of wrong term label %s instead of %s for %s: see following line",
                    label,
                    currentLabel,
                    ontology.getTermMap().get(id).getName());
            errors.add(errmsg);
            n_bad_phenotypeLabel++;
            return false;
        } else {
            n_good_phenotypeLabel++;
            return true;
        }
    }


    private boolean checkPublication(String pub) {
        if (pub==null || pub.isEmpty()) {
            n_bad_publication++;
            return false;
        }
        int index = pub.indexOf(":");
        if (index <=0) { // there needs to be a colon in the middle of the string
            n_bad_publication++;
            return false;
        }
        if (pub.contains("::")) { // should only be one colon separating prefix and id
            n_bad_publication++;
            return false;
        }
        if (pub.startsWith("HPO")) {
            n_bad_publication++;
            return false;
        }
        if (pub.contains(" ")) {
            n_bad_publication++;
            return false;
        }
        if (pub.startsWith("PMID") ||
                pub.startsWith("OMIM") ||
                pub.startsWith("http") ||
                pub.startsWith("DECIPHER") ||
                pub.startsWith("ISBN")) {
            n_good_publication++;
            return true;
        } else {
            errors.add(String.format("Did not recognize publication prefix: \"%s\" for %s (see next line)",pub.substring(0,index),pub));
        }
        n_bad_publication++;
        return false;
    }

    /**
     * check the age of onset id. It is allowed to be null, but then the age of onset label also has to be null.
     * If it is not null, it has to be a valid term in the Onset subhierarchy of the hpo.
     * @param id A term id that should be from the Onset subhierarchy
     * @return true if the HPO id is a valid onset term id.
     */
    private boolean checkAgeOfOnsetId(String id) {
        if (id==null || id.isEmpty() ) {
            n_good_ageOfOnset_ID++;
            return true;
        }
        TermId tid = TermId.constructWithPrefix(id);
        if (! ontology.getTermMap().containsKey(tid)) {
            errors.add("Attempt to add onset ID that was not in graph: "+id);
            System.err.println("Attempt to add onset ID that was not in graph: "+id);
            return false;
        }
        if (! checkIsNotAltId(tid)) {
            errors.add("Attempt to use alt_id for onset term: " + id);
            return false;
        }
        if (this.onsetTerms.contains(tid)) {
            n_good_ageOfOnset_ID++;
            return true;
        } else {
            n_bad_ageOfOnset_ID++;
            errors.add("Malformed age of onset ID: \""+tid.toString()+"\"");
            System.err.println("Malformed age of onset ID: \""+tid.toString()+"\"");
            return false;
        }
    }

    /**
     *
     * @param tid An HPO Term Id
     * @return true if the tid is up to date, i.e., NOT an alt_id
     */
    private boolean checkIsNotAltId(TermId tid) {
        if (! ontology.getTermMap().containsKey(tid)) {
            errors.add("TermId not found at all in ontology: " + tid.getIdWithPrefix());
            return false;
        }
        TermId upToDate = ontology.getPrimaryTermId(tid);
        return tid.equals(upToDate);
    }


    /** Check that the label is the current label that matches the term id. */
    private boolean checkAgeOfOnsetLabel(String id, String label) {
        if ((id==null || id.isEmpty()) && (label==null||label.isEmpty())){
            n_good_ageOfOnsetLabel++;
            return true;
        }
        if (label==null || label.isEmpty()) {
            n_bad_ageOfOnsetLabel++;
            return false;
        }
        TermId tid = TermId.constructWithPrefix(id);
        String currentLabel = ontology.getTermMap().get(tid).getName();
        if (! currentLabel.equals(label)) {
            String errmsg = String.format("Found usage of wrong age of onset label %s instead of %s for %s: see following line",
                    label,
                    currentLabel,
                    ontology.getTermMap().get(tid).getId().getIdWithPrefix());
            errors.add(errmsg);
            n_bad_ageOfOnsetLabel++;
            return false;
        } else {
            n_good_ageOfOnsetLabel++;
            return true;
        }
    }

    private boolean checkEvidence(String evi) {
        if (evi==null || evi.isEmpty() || evi.equals("null")) {
            n_bad_publication++;
            return false;
        } else if (evi.equals("IEA") ||
                evi.equals("PCS") ||
                evi.equals("ICE") ||
                evi.equals("TAS")) {
            n_good_evidence++;
            return true;
        } else {
            n_bad_evidence++;
            return false;
        }
    }



    private boolean checkBiocuration(String entrylist) {
        if (entrylist==null || entrylist.isEmpty())
            return false;
        List<BiocurationEntry> entries = BiocurationEntry.getBiocurationList(entrylist);
        if (entries.size()<1)
            return false;
        else
            return true;
    }


    /** There are 3 correct formats for frequency. For example, 4/7, 32% (or 32.6%), or
     * an HPO term from the frequency subontology. */
    private boolean checkFrequency(String freq) {
        // it is ok not to have frequency data
        if (freq==null || freq.isEmpty()) {
            n_good_frequency++;
            return true;
        }
        if (freq.matches("\\d+/\\d+")) {
            n_good_frequency++;
            return true;
        } else if (freq.matches("\\d{1,3}%")) {
            n_good_frequency++;
            return true;
        }else if (freq.matches("\\d{1,3}\\.\\d+%")) {
            n_good_frequency++;
            return true;
        } else if (! freq.matches("HP:\\d{7}")) {
            // cannot be a valid frequency term
            errors.add("Invalid frequency term (see next line): " + freq);
            n_bad_frequency++;
            return false;
        }
        // if we get here and we can validate that the frequency term comes from the right subontology,
        // then the item is valid
        TermId id = TermId.constructWithPrefix(freq);
        //boolean OK = frequencySubontology.getTermMap().containsKey(id);
        if (existsPath(ontology,id,FREQUENCY_ROOT)) {
            n_good_frequency++;
            return true;
        }else {
            errors.add(String.format("Could not find term %s [%s] in frequency subontology (see next line)",
                    ontology.getTermMap().get(id).getName(),
                    ontology.getTermMap().get(id).getId().getIdWithPrefix()));
            n_bad_frequency++;
            return false;
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



    public boolean checkV2entry(V2SmallFileEntry entry) {
        boolean clean=true;
        if (! checkDB(entry.getDB())) {
            errors.add(String.format("Bad DB: %s",entry.toString()));
            clean = false;
        }
        if (! checkDiseaseName(entry.getDiseaseName())) {
            errors.add(String.format("Bad disease name: %s",entry.toString()));
            clean = false;
        }
        if (! checkNegation(entry.getNegation())) {
            errors.add(String.format("Bad negation: %s",entry.toString()));
        clean = false;
        }
        if (! checkPhenotypeId(entry.getPhenotypeId())) {
            errors.add(String.format("Bad phenotypeId: %s",entry.toString()));
            clean = false;
        }
        if (! checkPhenotypeLabel(entry.getPhenotypeId(),entry.getPhenotypeName())) {
            errors.add(String.format("Bad phenotype label: %s",entry.toString()));
            clean = false;
        }
        if (! checkPublication(entry.getPublication())) {
            errors.add(String.format("Bad publication [%s]: %s",
                    entry.getPublication(),
                    entry.toString()));
            clean = false;
        }
        if (! checkAgeOfOnsetId(entry.getAgeOfOnsetId())) {
            errors.add(String.format("Bad age of onset id: %s",entry.toString()));
            clean = false;
        }
        if (! checkAgeOfOnsetLabel(entry.getAgeOfOnsetId(),entry.getAgeOfOnsetName())) {
            errors.add(String.format("Bad age of onset label: %s",entry.toString()));
            clean = false;
        }
        if (! checkEvidence(entry.getEvidenceCode())) {
            errors.add(String.format("Bad evidence code: \"%s\" for entry %s",
                    entry.getEvidenceCode(),entry.toString()));
            logger.error(String.format("Bad evidence code: \"%s\" for entry %s",
                    entry.getEvidenceCode(),entry.toString()));
            clean = false;
        }
        if (! checkBiocuration(entry.getBiocuration())) {
            errors.add(String.format("Bad data created: %s",entry.toString()));
            clean = false;
        }
        if (! checkFrequency(entry.getFrequencyModifier())) {
            errors.add(String.format("Bad frequency [%s]: %s",
                    entry.getFrequencyModifier(),
                    entry.toString()));
            clean = false;
        }
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
        logger.info(qcDB());
        logger.info(qcDiseasename());
        logger.info(qcNegation());
        logger.info(qcPhenotypeID());
        logger.info(qcPhenotypeLabel());
        logger.info(qcPublication());
        logger.info(qcAgeOfOnsetID());
        logger.info(qcAgeOfOnsetLabel());
        logger.info(qcEvidence());
        logger.info(qcDateCreated());
        logger.info(qcAssignedBy());
        logger.info(qcFrequency());
        logger.info(qcAspect());
    }

}
