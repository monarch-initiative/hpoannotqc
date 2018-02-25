package org.monarchinitiative.hpoannotqc.bigfile;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.orphanet.OrphanetDisorder;
import org.monarchinitiative.hpoannotqc.smallfile.V2LineQualityController;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.phenomics.ontolib.ontology.algo.OntologyAlgorithm.existsPath;

public class BigFileWriter {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private final V2LineQualityController v2qualityController;
    private String bigFileOutputName="phenotype_annotation2.tab";
    private List<String> v2smallFiles;
    private final String v2smallFileDirectory;
    private List<V2SmallFile> v2filelist=new ArrayList<>();
    /** This is the file handle for writing the small files and Orphanet data to file. */
    private BufferedWriter writer;

    private final static String ORPHANET_DB ="ORPHA";
    private final static String EMPTY_STRING="";
    private final static String ORPHA_EVIDENCE_CODE="TAS";
    private final static String NO_ONSET_CODE_AVAILABLE=EMPTY_STRING;
    private final static String ASSIGNED_BY="ORPHA:orphadata";
    private static final TermId phenotypeRoot= ImmutableTermId.constructWithPrefix("HP:0000118");
    private static final TermId FREQUENCY_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0040279");
    private static final TermId ONSET_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0003674");;
    private static final TermId MODIFIER_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0012823");
    private static final TermId INHERITANCE_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0000005");
    private static final TermId MORTALITY_TERM_ID =ImmutableTermId.constructWithPrefix("HP:0040006");;


    public BigFileWriter(HpoOntology ont, String directoryPath) {
        ontology=ont;
        v2smallFileDirectory=directoryPath;
        V2SmallFileParser.setOntology(ontology);
        v2qualityController = new V2LineQualityController(this.ontology);
        v2smallFiles = getListOfV2SmallFiles();
        inputV2files();
        try {
            this.writer = new BufferedWriter(new FileWriter(bigFileOutputName));
        } catch (IOException e) {
            logger.fatal(e);
            logger.fatal("Could not open file for writing at " + bigFileOutputName);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }
    }


    /**
     * Output the big file in the V1 format used from 2009-2018. Note -- given the transformation of the old files and
     * the new structure of the ontology, it is not possible or desirable to recreate the original phenotype_annotation.tab
     * exactly -- this is just for Q/C purposes, and in general the V2 version should be used.
     */
    public void outputBigFileV1() {
        int n=0;
        V2LineQualityController v2qc = new V2LineQualityController(this.ontology);
        try {
            writer.write(BigFileHeader.getHeaderV1() +"\n");
            for (V2SmallFile v2 : v2filelist ) {
                List<V2SmallFileEntry> entryList = v2.getEntryList();
                for (V2SmallFileEntry entry : entryList) {
                    v2qc.checkV2entry(entry);
                    String bigfileLine = transformEntry2BigFileLineV1(entry);
                    writer.write(bigfileLine + "\n");
                    n++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.trace("We output a total of " + n + " big file lines");
        v2qc.dumpQCtoShell();
        v2qc.dumpAssignedByMap();
    }

    /** Return frequency modifier if available, otherwise null. */
    private String getFrequencyString(V2SmallFileEntry entry) {
        if (entry.getFrequencyModifier()!=null) return entry.getFrequencyModifier();
        else return "";
    }

    private String getAspectV1(String hpoid) {
        TermId tid = ImmutableTermId.constructWithPrefix(hpoid);
        if (tid==null) return "?"; // should never happen but whatever
        return getAspectV1(tid);

    }


    private String getAspectV1(TermId tid) {
        HpoTerm term = ontology.getTermMap().get(tid);
        tid = term.getId(); // update in case term is an alt_id
        if (existsPath(ontology, tid, phenotypeRoot)) {
            v2qualityController.incrementGoodAspect();//
            return "O"; // organ/phenotype abnormality
        } else if (existsPath(ontology, tid, INHERITANCE_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "I";
        } else if (existsPath(ontology, tid, ONSET_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "C";
        } else if (existsPath(ontology, tid, MORTALITY_TERM_ID)) {
            v2qualityController.incrementGoodAspect();
            return "M";
        } else {
//            logger.error("Could not identify aspect for entry with term id " + entry.getPhenotypeId().getIdWithPrefix() + "(+" +
//                    entry.getPhenotypeName()+")");
            this.v2qualityController.incrementBadAspect();
            return "?";
        }
    }

    public void writeOrphanet(List<OrphanetDisorder> disorderList) {
        try {
            for (OrphanetDisorder disorder : disorderList) {
                String line = transformOrphanetEntry2BigFileLine(disorder);
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            logger.fatal(e);
            logger.fatal("Could not write orphanet disorder to " + bigFileOutputName);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }
    }

    public void tidyUp() {
        try {
            writer.close();
        } catch (IOException e) {
            logger.fatal(e);
            logger.fatal("Could not close handle to " + bigFileOutputName);
            logger.fatal("No choice but to terminate program, sorry....");
            System.exit(1);
        }
    }



    private String transformOrphanetEntry2BigFileLine(OrphanetDisorder entry) throws IOException {
        String diseaseID=String.format("%s:%d", ORPHANET_DB,entry.getOrphaNumber());
        String [] elems = {
                ORPHANET_DB, // DB
                String.valueOf(entry.getOrphaNumber()), // DB_Object_ID
                entry.getName(), // DB_Name
                EMPTY_STRING, // Qualifier
                entry.getHpoId().getIdWithPrefix(), // HPO ID
                diseaseID, // DB:Reference
                ORPHA_EVIDENCE_CODE, // Evidence code
                NO_ONSET_CODE_AVAILABLE, // Onset modifier
                entry.getFrequency().getIdWithPrefix(),// Frequency modifier (An HPO TermId, always)
                EMPTY_STRING, // With (not used)
                getAspectV1(entry.getHpoId()),
                EMPTY_STRING, // Synonym (not used)
                getTodaysDate(), // Date
                ASSIGNED_BY // Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }


    /** Construct one line for the V1 big file that was in use from 2009-2018. */
    private String transformEntry2BigFileLineV1(V2SmallFileEntry entry) {
        String [] elems = {
                entry.getDB(),
                entry.getDB_Object_ID(),
                entry.getDiseaseName(), // DB_Name
                entry.getNegation(), // Qualifier
                entry.getPhenotypeId().getIdWithPrefix(), // HPO ID
                entry.getPublication(), // DB:Reference
                entry.getEvidenceCode(), // Evidence code
                entry.getAgeOfOnsetId()==null?"":entry.getAgeOfOnsetId().getIdWithPrefix(), // Onset modifier
                getFrequencyString(entry), // Frequency modifier
                EMPTY_STRING, // With (not used)
                getAspectV1(entry.getPhenotypeId()), // Aspect
                EMPTY_STRING, // Synonym (not used)
                entry.getDateCreated(), // Date
                entry.getAssignedBy() // Assigned by
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }


    private void inputV2files() {
        logger.trace("We found " + v2smallFiles.size() + " small files at " + v2smallFileDirectory);
        int i=0;
        for (String path : v2smallFiles) {
            if (++i%1000==0) {
                logger.trace(String.format("Inputting %d-th file at ",i,path));
            }
            V2SmallFileParser parser=new V2SmallFileParser(path);
            v2filelist.add(parser.getV2eEntry());
        }
        logger.trace(String.format("Done with input of %d files",i));
    }



    private List<String> getListOfV2SmallFiles() {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(v2smallFileDirectory))) {
            for (Path path : directoryStream) {
                if (path.toString().endsWith(".tab")) {
                    fileNames.add(path.toString());
                }
                // fileNames.add(path.toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("Could not get list of small v2smallFiles. Terminating...");
            System.exit(1);
        }
        return fileNames;
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
