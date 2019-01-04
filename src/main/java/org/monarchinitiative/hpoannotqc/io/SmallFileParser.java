package org.monarchinitiative.hpoannotqc.io;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileEntry;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileEntryQC;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse of V2 small file into a {@link SmallFile} object
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 2/05/2018.
 */
public class SmallFileParser {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    /** key -- all lower-case label of a modifer term. Value: corresponding TermId .*/
    private static Map<String, TermId> modifier2TermId = new HashMap<>();
    /** Path to a file such as "OMIM-600123.tab" containing data about the phenotypes of a disease. */
    private final String pathToV2File;
    /** The column names of the small file. */
    private static final String[] expectedFields = {
            "#diseaseID",
            "diseaseName",
            "phenotypeID",
            "phenotypeName",
            "onsetID",
            "onsetName",
            "frequency",
            "sex",
            "negation",
            "modifier",
            "description",
            "publication",
            "evidence",
            "biocuration"};
    /** A list of all erroneous Small File lines encountered during parsing */
    private List<String> parseErrors;

    /** Number of tab-separated fields in a valid small file. */
    private static final int NUMBER_OF_FIELDS=expectedFields.length;
    /** An object for quality control of the Small File lines. */
    private final SmallFileEntryQC smallFileEntryQC;

    public SmallFileParser(String path, HpoOntology ontology) {
        pathToV2File=path;
        this.ontology=ontology;
        smallFileEntryQC =new SmallFileEntryQC(this.ontology);
    }


    public Optional<SmallFile> parse() {
        String basename=(new File(pathToV2File).getName());
        List<SmallFileEntry> entryList=new ArrayList<>();
        this.parseErrors=new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathToV2File));
            String line=br.readLine();
            qcHeaderLine(line);
            while ((line=br.readLine())!=null) {
                try {
                    SmallFileEntry entry = SmallFileEntry.fromLine(line, ontology);
                    entryList.add(entry);
                } catch (PhenolException e) {
                    parseErrors.add(String.format("%s:%s",pathToV2File,e.getMessage()));
                }
            }
            br.close();
            return  Optional.of(new SmallFile(basename,entryList));
        } catch (IOException | HPOException e) {
            logger.error(String.format("Error parsing %s",pathToV2File));
            e.printStackTrace();
        }
        for (String s : parseErrors) {
            System.err.println(s);
        }
        return Optional.empty();
    }




     /* This method checks that the nead has the expected number and order of lines.
     * If it doesn't, then a serious error has occured somewhere and it is better to
     * die and figure out what is wrong than to attempt error correction
     * @param line a header line of a V2 small file
     */
    private void qcHeaderLine(String line) throws HPOException  {
        String fields[] = line.split("\t");
        if (fields.length != expectedFields.length) {
            String msg = String.format("Malformed header line\n"+line+
            "\nExpecting %d fields but got %d",
                    expectedFields.length,
                    fields.length);
            logger.error(msg);
            throw new HPOException(msg);
        }
        for (int i=0;i<fields.length;i++) {
            if (! fields[i].equals(expectedFields[i])) {
                logger.fatal("Malformed header in file: "+pathToV2File);
                logger.fatal(String.format("Malformed field %d. Expected %s but got %s",
                        i,expectedFields[i],fields[i]));
                System.exit(1);
            }
        }
        // if we get here, all is good
    }


}
