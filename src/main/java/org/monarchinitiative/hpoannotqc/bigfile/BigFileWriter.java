package org.monarchinitiative.hpoannotqc.bigfile;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.ArrayList;
import java.util.Arrays;
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
    }



    public void outputBigFile() {
        int n=0;
        V2LineQualityController v2qc = new V2LineQualityController(this.ontology);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(bigFileOutputName));
            writer.write(V2SmallFileEntry.getHeader() +"\n");
            for (V2SmallFile v2 : v2filelist ) {
                List<V2SmallFileEntry> entryList = v2.getEntryList();
                for (V2SmallFileEntry entry : entryList) {
                    v2qc.checkV2entry(entry);
                    String bigfileLine = transformEntry2BigFileLine(entry);
                    writer.write(bigfileLine + "\n");
                    n++;
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.trace("We output a total of " + n + " big file lines");
        v2qc.dumpQCtoShell();
    }

    /** Return frequency modifier if available, otherwise null. */
    private String getFrequencyString(V2SmallFileEntry entry) {
        if (entry.getFrequencyModifier()!=null) return entry.getFrequencyModifier();
        else return "";
    }

    private String getAspect(V2SmallFileEntry entry) {
        TermId tid = entry.getPhenotypeId();
        HpoTerm term = ontology.getTermMap().get(tid);
        if (existsPath(ontology, tid, phenotypeRoot)) {
            v2qualityController.incrementGoodAspect();//
            System.out.println("Aspect = o");
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
            v2qualityController.incrementBadAspect();
            return "?";
        }
    }





    private String transformEntry2BigFileLine(V2SmallFileEntry entry) {
        String [] elems = {
                entry.getDB(),
                entry.getDB_Object_ID(),
                entry.getDiseaseName(),
                entry.getNegation(),
                entry.getPhenotypeId().getIdWithPrefix(),
                entry.getPhenotypeName(),
                entry.getPublication(),
                entry.getEvidenceCode(),
                entry.getAgeOfOnsetId()==null?"":entry.getAgeOfOnsetId().getIdWithPrefix(),
                getFrequencyString(entry),
                "", /* with*/
                getAspect(entry),
                "", /* synonym */
                entry.getDateCreated(),
                entry.getAssignedBy()
        };
        return Arrays.stream(elems).collect(Collectors.joining("\t"));
    }


    private void inputV2files() {
        logger.trace("We found " + v2smallFiles.size() + " small files at " + v2smallFileDirectory);
        for (String path : v2smallFiles) {
            System.out.println(path);
            V2SmallFileParser parser=new V2SmallFileParser(path);
            v2filelist.add(parser.getV2eEntry());
        }
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

}
