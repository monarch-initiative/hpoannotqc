package org.monarchinitiative.hpoannotqc.bigfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
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
    private static final TermId phenotypeRoot= ImmutableTermId.constructWithPrefix("HP:0000118");
    private static final TermId INHERITANCE_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0000005");
    private static final TermId CLINICAL_COURSE_ID =ImmutableTermId.constructWithPrefix("HP:0031797");
    private static final TermId ONSET_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0003674");
    private final HpoOntology ontology;


    private final List<OrphanetDisorder> orphanetDisorders;
    private final  BufferedWriter writer;

    public Orphanet2BigFile(List<OrphanetDisorder> orphDisorders, BufferedWriter bw, HpoOntology ont){
        this.orphanetDisorders=orphDisorders;
        this.writer=bw;
        this.ontology=ont;
    }





    public void writeOrphanetV1() {
        int n=0;
        try {
            for (OrphanetDisorder disorder : orphanetDisorders) {
                List<TermId> hpoIds = disorder.getHpoIds();
                for (TermId tid: hpoIds) {
                    String line = transformOrphanetEntry2BigFileLine(disorder,tid);
                    writer.write(line + "\n");
                    n++;
                }
            }
        } catch (IOException e) {
            logger.fatal(e);
            logger.fatal("Could not write orphanet disorder ", e);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }
        logger.trace("We output a total of " + n + " orphanet annotations");
    }

    public void writeOrphanetV2() {
        int n=0;
        try {
            for (OrphanetDisorder disorder : orphanetDisorders) {
                List<TermId> hpoIds = disorder.getHpoIds();
                for (TermId tid: hpoIds) {
                    String line = transformOrphanetEntry2BigFileLineV2(disorder,tid);
                    writer.write(line + "\n");
                    n++;
                }
            }
        } catch (IOException e) {
            logger.fatal(e);
            logger.fatal("Could not write orphanet disorder ", e);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }
        logger.trace("We output a total of " + n + " orphanet annotations");
    }

    private String getAspectV2(TermId tid) {
        HpoTerm term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getIdWithPrefix());
            return "?";
        }
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            return "P"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            return "I";
        } else if (existsPath(ontology, tid, ONSET_TERM_ID)) {
            return "C";
        } else {
            return "?";
        }
    }

    /**
     * Figure out the Aspect of this line (O,I, or C) based on the location of the term in the ontology.
     * @param tid Term for which we want to calculate the Aspect
     * @return One-letter String representing the Aspect (O, I, or C).
     */
    private String getAspectV1(TermId tid) {
        HpoTerm term = ontology.getTermMap().get(tid);
        if (term==null) {
            logger.error("Invalid HPO tid="+tid.getIdWithPrefix());
            return "?";
        }
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            return "O"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            return "I";
        } else if (existsPath(ontology, tid, CLINICAL_COURSE_ID)) {
            return "C";
        }  else {
//            logger.error("Could not identify aspect for entry with term id " + entry.getPhenotypeId().getIdWithPrefix() + "(+" +
//                    entry.getPhenotypeName()+")");
            return "?";
        }
    }


    /** Output a line of an Orphanet entry to the V2 big file, phenotype.hpoa. */
    private String transformOrphanetEntry2BigFileLineV2(OrphanetDisorder entry, TermId hpoId) throws IOException {
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





    private String transformOrphanetEntry2BigFileLine(OrphanetDisorder entry, TermId hpoId) throws IOException {
        String diseaseID=String.format("%s:%d", ORPHANET_DB,entry.getOrphaNumber());
        String [] elems = {
                ORPHANET_DB, // DB
                String.valueOf(entry.getOrphaNumber()), // DB_Object_ID
                entry.getName(), // DB_Name
                EMPTY_STRING, // Qualifier
                hpoId.getIdWithPrefix(), // HPO ID
                diseaseID, // DB:Reference
                ORPHA_EVIDENCE_CODE, // Evidence code
                NO_ONSET_CODE_AVAILABLE, // Onset modifier
                entry.getFrequency().getIdWithPrefix(),// Frequency modifier (An HPO TermId, always)
                EMPTY_STRING, // With (not used)
                getAspectV1(hpoId),
                EMPTY_STRING, // Synonym (not used)
                getTodaysDate(), // Date
                ASSIGNED_BY // Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }

    /** We are using this to supply a date created value for the Orphanet annotations. TODO is there a better
     * way of doing this
     * @return A String such as 2018-02-22
     */
    public String getTodaysDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        Date date = new Date();
        return dateFormat.format(date); //2016/11/16 12:08:43
    }


}
