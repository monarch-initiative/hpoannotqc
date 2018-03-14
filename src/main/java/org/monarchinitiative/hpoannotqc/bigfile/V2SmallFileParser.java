package org.monarchinitiative.hpoannotqc.bigfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse of V2 small file into a {@link org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile} object
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 2/05/2018.
 */
public class V2SmallFileParser {
    private static final Logger logger = LogManager.getLogger();

    private static HpoOntology ontology = null;

    private static Map<TermId,HpoTerm> termMap;
    /** key -- all lower-case label of a modifer term. Value: corresponding TermId .*/
    private static Map<String, TermId> modifier2TermId = new HashMap<>();

    private final String pathToV2File;

    private V2SmallFile v2smallfile=null;

    private static final int NUMBER_OF_FIELDS=15;

    public V2SmallFileParser(String path) {
        pathToV2File=path;
        parse();
    }


    public V2SmallFile getV2eEntry() { return v2smallfile;}


    /** This is called once by client code before we start parsing. Not pretty design but it woirks fine for thuis one-off app. */
    public static void setOntology(HpoOntology ont) {
        ontology = ont;
        termMap=ontology.getTermMap();
    }
    private void parse() {
        String basename=(new File(pathToV2File).getName());
        List<V2SmallFileEntry> entryList=new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(pathToV2File));
            String line=br.readLine();
            qcHeaderLine(line);
            while ((line=br.readLine())!=null) {
                //System.out.println(line);
                if (line.startsWith("#")) continue;
                String A[] = line.split("\t");
                if (A.length!= NUMBER_OF_FIELDS) {
                    logger.error(String.format("We were expecting %d fields but got %d for line %s",NUMBER_OF_FIELDS,A.length,line ));
                    System.exit(1);
                }
                String diseaseID=A[0];
                String diseaseName=A[1];
                TermId phenotypeId = ImmutableTermId.constructWithPrefix(A[2]);
                if (! termMap.containsKey(phenotypeId)) {
                    logger.error("FATAL could not find term for " + A[2]);
                    System.exit(1);
                }
                String phenotypeName=A[3];
                TermId ageOfOnsetId=null;
                if (A[4]!=null && A[4].startsWith("HP")) {
                    ageOfOnsetId=ImmutableTermId.constructWithPrefix(A[4]);
                }
                String ageOfOnsetName=A[5];
                String frequencyString=A[6];
                String sex=A[7];
                String negation=A[8];
                String modifier=A[9];
                // modifer is discarded here since it was not in the big file -- FOR NOW TODO
                String description=A[10];
                String publication=A[11];
                String evidenceCode=A[12];
                String assignedBy=A[13];
                String dateCreated=A[14];

                V2SmallFileEntry.Builder builder=new V2SmallFileEntry.Builder(diseaseID,diseaseName,phenotypeId,phenotypeName,evidenceCode,publication,assignedBy,dateCreated);
                if (frequencyString!=null && ! frequencyString.isEmpty()) {
                    builder=builder.frequencyString(frequencyString);
                }
                if (sex!=null && !sex.isEmpty()) {
                    builder=builder.sex(sex);
                }
                if (negation!=null && !negation.isEmpty()) {
                    builder=builder.negation(negation);
                }
                if (modifier!=null && !modifier.isEmpty()) {
                    builder=builder.modifier(modifier);
                }
                if (description!=null && ! description.isEmpty()) {
                    builder=builder.description(description);
                }
                if (ageOfOnsetId!=null) {
                    builder=builder.ageOfOnsetId(ageOfOnsetId);
                }
                if (ageOfOnsetName!=null) {
                    builder=builder.ageOfOnsetName(ageOfOnsetName);
                }
                entryList.add(builder.build());
            }
            br.close();
            this.v2smallfile = new V2SmallFile(basename,entryList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            "assignedBy",
            "dateCreated"};
    /**
     * This method checks that the nead has the expected number and order of lines.
     * If it doesn't, then a serious error has occured somewhere and it is better to
     * die and figure out what is wrong than to attempt error correction
     * @param line a header line of a V2 small file
     */
    private void qcHeaderLine(String line) {
        String fields[] = line.split("\t");
        if (fields.length != expectedFields.length) {
            logger.fatal(String.format("Malformed header line\n"+line+
            "\nExpecting %d fields but got %d",
                    expectedFields.length,
                    fields.length));
            System.exit(1);
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
