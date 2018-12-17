package org.monarchinitiative.hpoannotqc.smallfile;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by peter on 1/20/2018.
 * This class represents the contents of a single annotation line.
 */
public class  V2SmallFileEntry {
    private static final Logger logger = LogManager.getLogger();

    private final static String EMPTY_STRING="";

    /** Field #1 */
    private final String diseaseID;
    /** Field #2 */
    private final String diseaseName;
    /** Field #3 */
    private final TermId phenotypeId;
    /** Field #4 */
    private final String phenotypeName;
    /** Field #5 */
    private final String ageOfOnsetId;
    /** Field #6 */
    private final String ageOfOnsetName;
    /** Field #7 */
    private final String evidenceCode;
    /** Field #8 can be one of N/M, X% or a valid frequency term Id */
    private final String frequencyModifier;
    /** Field #9 */
    private final String sex;
    /** Field #10 */
    private final String negation;
    /** Field #11 */
    private final String modifier;
    /** Field #12 */
    private final String description;
    /** Field #13 */
    private final String publication;
    /** Field #14 */
    private final String biocuration;

    private final static String [] expectedFields ={"#diseaseID",
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

    /**
     /** Number of tab-separated expectedFields in a valid small file. */
    private static final int NUMBER_OF_FIELDS=expectedFields.length;

    public String getDiseaseID() {
        return diseaseID;
    }

    /** The disease ID has two parts, the database (before the :) and the id (after the :).
     * @return the database part of the diseaseID
     */
    public String getDB() {
        String[]A=diseaseID.split(":");
        return A[0];
    }
    /** The disease ID has two parts, the database (before the :) and the id (after the :).
     * @return the object_ID part of the diseaseID
     */
    public String getDB_Object_ID() {
        String[]A=diseaseID.split(":");
        if (A.length>1) return A[1];
        else return diseaseID;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public TermId getPhenotypeId() {
        return phenotypeId;
    }

    public String getPhenotypeName() {
        return phenotypeName;
    }

    public String getAgeOfOnsetId() {
        return ageOfOnsetId!=null?ageOfOnsetId:EMPTY_STRING;
    }

    public String getAgeOfOnsetName() {
        return ageOfOnsetName!=null?ageOfOnsetName:EMPTY_STRING;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public String getFrequencyModifier() {
        return frequencyModifier!=null?frequencyModifier:EMPTY_STRING;
    }

    public String getSex() {
        return sex!=null?sex:EMPTY_STRING;
    }

    public String getNegation() {
        return negation!=null?negation:EMPTY_STRING;
    }
    public String getModifier() {
        return modifier!=null?modifier:EMPTY_STRING;
    }

    public String getDescription() {
        return description!=null?modifier:EMPTY_STRING;
    }

    public String getPublication() {
        return publication;
    }

    public String getBiocuration() {
        return biocuration;
    }


    /** @return true iff this entry has an N/M style frequency entry */
    boolean isNofM() {
        int i = this.frequencyModifier.indexOf('/');
        int len=this.frequencyModifier.length();
        // 1. The string must contain the character '/'
        // 2. The / cannot be on the first (0th) position of the string
        // 3. The / cannot be the last character of the string
        return (i>0 && i<len-1);
    }

    boolean isPercentage() {
        int i=this.frequencyModifier.indexOf('%');
        // percentage symbol must be present and cannot be at the first (0th) position
        if (i<1) return false;
        String regex="\\d{1,3}\\.?\\d?%";
        return this.frequencyModifier.matches(regex);
    }

    boolean isFrequencyTerm() {
        String regex="HP:\\d{7}";
        return this.frequencyModifier.matches(regex);
    }



    public void merge(String freq, List<V2SmallFileEntry> annotlist ) {

    }



    /**
     * This is the header of the V2 small files.
     * @return V2 small file header.
     */
    public static String getHeaderV2() {
        return Arrays.stream(expectedFields).collect(Collectors.joining("\t"));
    }




    private V2SmallFileEntry(String disID,
            String diseaseName,
            TermId phenotypeId,
            String phenotypeName,
            String ageOfOnsetId,
            String ageOfOnsetName,
            String evidenceCode,
            String frequencyString,
            String sex,
            String negation,
            String modifier,
            String description,
            String publication,
            String biocuration) {
        this.diseaseID=disID;
        this.diseaseName=diseaseName;
        this.phenotypeId=phenotypeId;
        this.phenotypeName=phenotypeName;
        this.ageOfOnsetId=ageOfOnsetId;
        this.ageOfOnsetName=ageOfOnsetName;
        this.evidenceCode=evidenceCode;
        this.frequencyModifier =frequencyString;
        this.sex=sex;
        this.negation=negation;
        this.modifier=modifier;
        this.description=description;
        this.publication=publication;
        this.biocuration=biocuration;
    }

    public V2SmallFileEntry makeDuplicate() {
        return new V2SmallFileEntry(this.diseaseID,
                this.diseaseName,
                this.phenotypeId,
                this.phenotypeName,
                this.ageOfOnsetId,
                this.ageOfOnsetName,
                this.evidenceCode,
                this.frequencyModifier,
                this.sex,
                this.negation,
                this.modifier,
                this.description,
                this.publication,
                this.biocuration);
    }

    /** @return the row that will be written to the V2 file for this entry. */
    @Override public String toString() { return getRow();}

    /**
     * Return the row that will be used to write the V2 small files entries to a file. Note that
     * we replace null strings (which are a signal for no data available) with the empty string
     * to avoid the string "null" being written.
     * @return One row of the "big" file corresponding to this entry
     */
    public String getRow() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                diseaseID,
                diseaseName,
                phenotypeId.getValue(),
                phenotypeName,
                ageOfOnsetId!=null?ageOfOnsetId:EMPTY_STRING,
                ageOfOnsetName!=null?ageOfOnsetName:EMPTY_STRING,
                frequencyModifier !=null? frequencyModifier:EMPTY_STRING,
                sex!=null?sex:EMPTY_STRING,
                negation!=null?negation:EMPTY_STRING,
                modifier!=null?modifier:EMPTY_STRING,
                description!=null?description:EMPTY_STRING,
                publication!=null?publication:EMPTY_STRING,
                evidenceCode!=null?evidenceCode:"",
                biocuration!=null?biocuration:EMPTY_STRING);
    }



    public static V2SmallFileEntry fromLine(String line, HpoOntology ontology) throws PhenolException {
        String A[] = line.split("\t");
        if (A.length!= NUMBER_OF_FIELDS) {
            logger.error(String.format("We were expecting %d expectedFields but got %d for line %s",NUMBER_OF_FIELDS,A.length,line ));
            System.exit(1);
        }
        String diseaseID=A[0];
        String diseaseName=A[1];
        TermId phenotypeId = TermId.of(A[2]);
        if (! ontology.getTermMap().containsKey(phenotypeId)) {
            throw new PhenolException("WARNING skipping annotation because we could not find term for (version mismatch?)" + A[2]);
        }
        String phenotypeName=A[3];
        String ageOfOnsetId=A[4];
        if (ageOfOnsetId!=null &&
                ageOfOnsetId.length()>0 &&
                (!ageOfOnsetId.startsWith("HP:"))) {
            throw new PhenolException(String.format("Malformed age of onset termid: \"%s\"",ageOfOnsetId ));
        }
        String ageOfOnsetName=A[5];
        String frequencyString=A[6];
        String sex=A[7];
        String negation=A[8];
        String modifier=A[9];
        String description=A[10];
        String publication=A[11];
        String evidenceCode=A[12];
        String biocuration=A[13];

        V2SmallFileEntry.Builder builder=new V2SmallFileEntry.Builder(diseaseID,diseaseName,phenotypeId,phenotypeName,evidenceCode,publication,biocuration);
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
        return builder.build();
    }




    public static class Builder {
        /** Field #1 */
        private  final String diseaseID;
        /** Field #2 */
        private  final String diseaseName;
        /** Field #3 */
        private  final TermId phenotypeId;
        /** Field #4 */
        private  final String phenotypeName;
        /** Field #5 */
        private  String ageOfOnsetId=EMPTY_STRING;
        /** Field #6 */
        private  String ageOfOnsetName=EMPTY_STRING;
        /** Field #7 */
        private  final String evidenceCode;
        /** Field #8 -- the HPO id for frequency (if available) */
        private  TermId frequencyId=null;
        /** Field #9 -- string representing n/m or x% frequency data*/
        private  String frequencyString=EMPTY_STRING;
        /** Field #10 */
        private  String sex=EMPTY_STRING;
        /** Field #11 */
        private  String negation=EMPTY_STRING;
        /** Field #12 */
        private  String modifier=EMPTY_STRING;
        /** Field #13 */
        private  String description=EMPTY_STRING;
        /** Field #14 */
        private  final String publication;
        /** Field #15 */
        private  final String biocuration;
        public Builder(String diseaseId,
                       String diseasename,
                       TermId phenoId,
                       String phenoName,
                       String evidence,
                       String pub,
                       String biocuration) {
            this.diseaseID=diseaseId;
            this.diseaseName=diseasename;
            this.phenotypeId=phenoId;
            this.phenotypeName=phenoName;
            this.evidenceCode=evidence;
            this.publication=pub;
            this.biocuration=biocuration;
        }

        public Builder frequencyId(TermId f) {
            this.frequencyId=f;
            return this;
        }

        public Builder frequencyString(String f) {
            this.frequencyString = f;
            return this;
        }

        public Builder ageOfOnsetId(String t) {
            this.ageOfOnsetId=t;
            return this;
        }

        public Builder ageOfOnsetName(String n) {
            this.ageOfOnsetName=n;
            return this;
        }

        public Builder sex(String s) { sex=s; return this; }

        public Builder negation(String n) { this.negation=n; return this; }

        public Builder modifier(String n) { this.modifier=n; return this; }

        public Builder description(String d) { this.description=d; return this;}

        public V2SmallFileEntry build() {
            return new V2SmallFileEntry(diseaseID,
                    diseaseName,
                    phenotypeId,
                    phenotypeName,
                    ageOfOnsetId,
                    ageOfOnsetName,
                    evidenceCode,
                    frequencyString,
                    sex,
                    negation,
                    modifier,
                    description,
                    publication,
                    biocuration);
        }
    }


}
