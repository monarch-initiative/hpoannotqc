package org.monarchinitiative.hpoannotqc.bigfile;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import com.github.phenomics.ontolib.ontology.data.Term;
import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFile;
import org.monarchinitiative.hpoannotqc.smallfile.V2SmallFileEntry;

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

    private V2SmallFile v2smallfile=null;

    private static final int NUMBER_OF_FIELDS=16;

    public V2SmallFileParser(String path) {
        parse(path);
    }


    public V2SmallFile getV2eEntry() { return v2smallfile;}


    /** This is called once by client code before we start parsing. Not pretty design but it woirks fine for thuis one-off app. */
    public static void setOntology(HpoOntology ont) {
        ontology = ont;
        termMap=ontology.getTermMap();
    }


    private void parse(String path) {
        String basename=(new File(path).getName());
        List<V2SmallFileEntry> entryList=new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
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
                TermId ageOfOnsetId;
                String ageOfOnsetName=A[5];
                String evidenceCode=A[6];
                String publication=A[13];
                String assignedBy=A[14];
                String dateCreated=A[15];

                V2SmallFileEntry.Builder builder=new V2SmallFileEntry.Builder(diseaseID,diseaseName,phenotypeId,phenotypeName,evidenceCode,publication,assignedBy,dateCreated);
                if (A[7]!=null && A[7].startsWith("HP:")) {
                    TermId frequencyId = ImmutableTermId.constructWithPrefix(A[7]);
                    builder = builder.frequencyId(frequencyId);
                }
                String frequencyString=A[8];
                if (frequencyString!=null && ! frequencyString.isEmpty()) {
                    builder=builder.frequencyString(frequencyString);
                }
                String sex=A[9];
                if (sex!=null && !sex.isEmpty()) {
                    builder=builder.sex(sex);
                }
                String negation=A[10];
                if (negation!=null && !negation.isEmpty()) {
                    builder=builder.negation(negation);
                }
                String modifier=A[11];
                if (modifier!=null && !modifier.isEmpty()) {
                    builder=builder.modifier(modifier);
                }
               // modifer is discarded here since it was not in the big file -- FOR NOW TODO
                String description=A[12];
                if (description!=null && ! description.isEmpty()) {
                    builder.description(description);
                }
                entryList.add(builder.build());

            }
            br.close();
            this.v2smallfile = new V2SmallFile(basename,entryList);

        } catch (IOException e) {
            e.printStackTrace();
        }




    }


}
