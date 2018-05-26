package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.existsPath;

public class Orphanet2BigFile {
    private static final Logger logger = LogManager.getLogger();
    private final static String ORPHANET_DB ="ORPHA";
    private final static String EMPTY_STRING="";
    private final static String ORPHA_EVIDENCE_CODE="TAS";
    private final static String NO_ONSET_CODE_AVAILABLE=EMPTY_STRING;
    private final static String ASSIGNED_BY="ORPHA:orphadata";
    private static final TermId phenotypeRoot= TermId.constructWithPrefix("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =TermId.constructWithPrefix("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =TermId.constructWithPrefix("HP:0031797");
    private static final TermId CLINICAL_MODIFIER_ID =TermId.constructWithPrefix("HP:0012823");
    private final HpoOntology ontology;


    private final List<OrphanetDisorder> orphanetDisorders;
    private final  BufferedWriter writer;

    public Orphanet2BigFile(List<OrphanetDisorder> orphDisorders, BufferedWriter bw, HpoOntology ont){
        this.orphanetDisorders=orphDisorders;
        this.writer=bw;
        this.ontology=ont;
    }


    void writeOrphanetV2() {
        int n=0;
        System.out.println("V2 orph about to write this many od"+ orphanetDisorders.size());
        try {
            for (OrphanetDisorder disorder : orphanetDisorders) {
                List<TermId> hpoIds = disorder.getHpoIds();
                for (TermId tid: hpoIds) {
                    try {
                        String line = transformOrphanetEntry2BigFileLineV2(disorder, tid);
                        writer.write(line + "\n");
                        n++;
                    } catch (HPOException hpoe) {
                        logger.error(String.format("Could not make annotation for term %s of disorder %s ",tid.getIdWithPrefix(),disorder.getName()));
                        logger.error("Will skip this line: "+hpoe.getMessage());
                        // just go to next one.
                    }
                }
            }
            System.out.println(String.format("We output a total of %d orphanet annotations from %d diseases",n,orphanetDisorders.size()));
        } catch (IOException e) {
            logger.fatal(e);
            System.err.println("Exception trying to write orphnaet");
            e.printStackTrace();
            logger.fatal("Could not write orphanet disorder ", e);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }

    }

    /**
     * This is identical to the analogous function in {@link V2BigFile} except that it does not use
     * the Q/C function of that function.
     * @param tid An HPO term id for which we want to get the aspect
     * @return A one-letter String representing the aspect (P,I,C,M).
     */
    private String getAspectV2(TermId tid) throws HPOException {
        Term term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getIdWithPrefix());
            return "?";
        }
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            return "P"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            return "I";
        } else if (existsPath(ontology, tid, CLINICAL_COURSE_ID)) {
            return "C";
        } else if (existsPath(ontology,tid,CLINICAL_MODIFIER_ID)) {
            return "M";
        } else {
            String label=term.getName();
            String msg=String.format("Could not get aspect for term %s [%s]",label,tid.getIdWithPrefix());
            throw new HPOException(msg);
        }
    }



    /** Output a line of an Orphanet entry to the V2 big file, phenotype.hpoa. */
    private String transformOrphanetEntry2BigFileLineV2(OrphanetDisorder entry, TermId hpoId) throws HPOException {
        String diseaseID=String.format("%s:%d", ORPHANET_DB,entry.getOrphaNumber());
        String [] elems = {
                ORPHANET_DB, // DB
                String.valueOf(entry.getOrphaNumber()), // DB_Object_ID
                entry.getName(), // DB_Name
                EMPTY_STRING, // Qualifier
                hpoId.getIdWithPrefix(), // HPO ID
                diseaseID, // DB_Reference
                ORPHA_EVIDENCE_CODE, // Evidence_Code
                NO_ONSET_CODE_AVAILABLE, // Onset
                entry.getFrequency().getIdWithPrefix(),// Frequency (An HPO TermId, always)
                EMPTY_STRING, // Sex (not used)
                EMPTY_STRING, // Modifier (not used)
                getAspectV2(hpoId),
                getTodaysDate(), // Date
                ASSIGNED_BY // Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }

    /** We are using this to supply a date created value for the Orphanet annotations.
     * After some research, no better way of getting the current date was found.
     * @return A String such as 2018-02-22
     */
    private String getTodaysDate() {
        Date date = new Date();
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }


}
