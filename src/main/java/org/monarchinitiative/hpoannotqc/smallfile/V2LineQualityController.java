package org.monarchinitiative.hpoannotqc.smallfile;




import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;

import org.monarchinitiative.phenol.ontology.data.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

/**
 * The purpose of this class is to check each V2 small file line from the version 2 (V2) small files that represent
 * the standard HPO annotation format from 2018 onwards. The class will tally up the Q/C results and store any V2
 * lines that appear "dodgy", providing a Q/C report. The class is intended to be used while the files are being converted
 * and to look at each V2 line in turn.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class V2LineQualityController {

    private final HpoOntology ontology;


    private List<String> errors=new ArrayList<>();

    private Map<String,Integer> assignedByMap = new HashMap();

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
    private String qcAgeOfOnsetID() { return String.format("%d good and %d age of onset ID entries",n_good_ageOfOnset_ID,n_bad_ageOfOnset_ID);}
    private int n_good_ageOfOnsetLabel=0;
    private int n_bad_ageOfOnsetLabel=0;
    private String qcAgeOfOnsetLabel() { return String.format("%d good and %d age of onset label entries",
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
    private final TermId ONSET_ROOT = ImmutableTermId.constructWithPrefix("HP:0003674");
    private static final TermId FREQUENCY_ROOT = ImmutableTermId.constructWithPrefix("HP:0040279");
    public V2LineQualityController(HpoOntology onto) {
        ontology=onto;
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
    private boolean checkAgeOfOnsetId(TermId id) {
        if (id==null ) {
            n_good_ageOfOnset_ID++;
            return true;
        }
        if (existsPath(ontology,id,ONSET_ROOT)) {
            n_good_ageOfOnset_ID++;
            return true;
        } else {
            n_bad_ageOfOnset_ID++;
            errors.add("Malformed age of onset ID: \""+id.toString()+"\"");
            return false;
        }
    }


    /** Check that the label is the current label that matches the term id. */
    private boolean checkAgeOfOnsetLabel(TermId id, String label) {
        if (id==null && (label==null||label.isEmpty())){
            n_good_ageOfOnsetLabel++;
            return true;
        }
        if (label==null || label.isEmpty()) {
            n_bad_ageOfOnsetLabel++;
            return false;
        }
        String currentLabel = ontology.getTermMap().get(id).getName();
        if (! currentLabel.equals(label)) {
            String errmsg = String.format("Found usage of wrong age of onset label %s instead of %s for %s: see following line",
                    label,
                    currentLabel,
                    ontology.getTermMap().get(id).getId().getIdWithPrefix());
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



    private boolean checkDateCreated(String date) {
        boolean OK = date.matches("\\d{4,4}-\\d{2,2}-\\d{2,2}");
        if (OK) {
            n_good_dateCreated++;
            return true;
        } else {
            errors.add(String.format("Could not parse date-created \"%s\"",date));
            n_bad_dateCreated++;
            return false;
        }
    }



    private boolean checkAssignBy(String assignedBy) {
        int index = assignedBy.indexOf(":");
        if (index<=0) {
            n_bad_assignedBy++;
            errors.add("Bad assigned-by string \""+assignedBy +"\"");
            return false;
        } else {
            if (! assignedByMap.containsKey(assignedBy) ) {
                assignedByMap.put(assignedBy,0);
            }
            assignedByMap.put(assignedBy,1 + assignedByMap.get(assignedBy)); // increment
            n_good_assignedBy++;
            return true;
        }
    }

    /** There are 3 correct formats for frequency */
    private boolean checkFrequency(String freq) {
        // it is ok not to have frequency data
        if (freq==null || freq.isEmpty()) {
            n_good_frequency++;
            return true;
        }
        if (freq.matches("\\d+/\\d+")) {
            n_good_frequency++;
            return true;
        } else if (freq.matches("\\d{1,3}\\%")) {
            n_good_frequency++;
            return true;
        } else if (! freq.matches("HP:\\d{7,7}")) {
            // cannot be a valid frequency term
            errors.add("Invalid frequency term (see next line): " + freq);
            n_bad_frequency++;
            return false;
        }
        // if we get here and we can validate that the frequency term comes from the right subontology,
        // then the item is valid
        TermId id = ImmutableTermId.constructWithPrefix(freq);
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



    public void checkV2entry(V2SmallFileEntry entry) {
        if (! checkDB(entry.getDB())) {
            errors.add(String.format("Bad DB: %s",entry.toString()));
        }
        if (! checkDiseaseName(entry.getDiseaseName())) {
            errors.add(String.format("Bad disease name: %s",entry.toString()));
        }
        if (! checkNegation(entry.getNegation())) {
            errors.add(String.format("Bad negation: %s",entry.toString()));
        }
        if (! checkPhenotypeId(entry.getPhenotypeId())) {
            errors.add(String.format("Bad phenotypeId: %s",entry.toString()));
        }
        if (! checkPhenotypeLabel(entry.getPhenotypeId(),entry.getPhenotypeName())) {
            errors.add(String.format("Bad phenotype label: %s",entry.toString()));
        }
        if (! checkPublication(entry.getPublication())) {
            errors.add(String.format("Bad publication: %s",entry.toString()));
        }
        if (! checkAgeOfOnsetId(entry.getAgeOfOnsetId())) {
            errors.add(String.format("Bad age of onset id: %s",entry.toString()));
        }
        if (! checkAgeOfOnsetLabel(entry.getAgeOfOnsetId(),entry.getAgeOfOnsetName())) {
            errors.add(String.format("Bad age of onset label: %s",entry.toString()));
        }
        if (! checkEvidence(entry.getEvidenceCode())) {
            errors.add(String.format("Bad evidence code: %s",entry.toString()));
        }
        if (! checkDateCreated(entry.getDateCreated())) {
            errors.add(String.format("Bad data created: %s",entry.toString()));
        }
        if (! checkAssignBy(entry.getAssignedBy())) {
            errors.add(String.format("Bad assigned-by: %s",entry.toString()));
        }
        if (! checkFrequency(entry.getFrequencyModifier())) {
            errors.add(String.format("Bad frequency: %s",entry.toString()));
        }
    }




    public void dumpQCtoShell() {
        System.out.println("#####   V2 Conversion Quality Control   #####");
        System.out.println("#####   Lines with errors   #####");
        for (String err : errors) {
            System.out.println(err);
        }
        System.out.println("#####   Q/C Summary   #####");
        System.out.println(qcDB());
        System.out.println(qcDiseasename());
        System.out.println(qcNegation());
        System.out.println(qcPhenotypeID());
        System.out.println(qcPhenotypeLabel());
        System.out.println(qcPublication());
        System.out.println(qcAgeOfOnsetID());
        System.out.println(qcAgeOfOnsetLabel());
        System.out.println(qcEvidence());
        System.out.println(qcDateCreated());
        System.out.println(qcAssignedBy());
        System.out.println(qcFrequency());
        System.out.println(qcAspect());


    }

}
