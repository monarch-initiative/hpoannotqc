package org.monarchinitiative.hpoannotqc.smallfile;

import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;
import org.monarchinitiative.hpoannotqc.exception.HPOException;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by peter on 1/20/2018.
 */
public class V2SmallFileEntry {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();
    /** Field #1 */
    private final String diseaseID;
    /** Field #2 */
    private final String diseaseName;
    /** Field #3 */
    private final TermId phenotypeId;
    /** Field #4 */
    private final String phenotypeName;
    /** Field #5 */
    private final TermId ageOfOnsetId;
    /** Field #6 */
    private final String ageOfOnsetName;
    /** Field #7 */
    private final String evidenceCode;
//    /** Field #8 */
//    private final TermId frequencyId;
    /** Field #9 */
    private final String frequencyModifier;
    /** Field #10 */
    private final String sex;
    /** Field #11 */
    private final String negation;
    /** Field #12 */
    private final String modifier;
    /** Field #13 */
    private final String description;
    /** Field #14 */
    private final String publication;
    /** Field #15 */
    private final String assignedBy;
    /** Field #16 */
    private final String dateCreated;

    private static final String EMPTY_STRING="";

    public String getDiseaseID() {
        return diseaseID;
    }

    public String getDB() {
        String[]A=diseaseID.split(":");
        return A[0];
    }

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

    public TermId getAgeOfOnsetId() {
        return ageOfOnsetId;
    }

    public String getAgeOfOnsetName() {
        return ageOfOnsetName;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public String getFrequencyModifier() {
        return frequencyModifier;
    }

    public String getSex() {
        return sex;
    }

    public String getNegation() {
        return negation;
    }

    public String getModifier() {
        return modifier;
    }

    public String getDescription() {
        return description;
    }

    public String getPublication() {
        return publication;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public String getDateCreated() {
        return dateCreated;
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
        private  TermId ageOfOnsetId=null;
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
        private  final String assignedBy;
        /** Field #16 */
        private  final String dateCreated;
        public Builder(String diseaseId, String diseasename, TermId phenoId, String phenoName,String evidence,String pub, String ab,String date) {
            this.diseaseID=diseaseId;
            this.diseaseName=diseasename;
            this.phenotypeId=phenoId;
            this.phenotypeName=phenoName;
            this.evidenceCode=evidence;
            this.publication=pub;
            this.assignedBy=ab;
            this.dateCreated=date;
        }

        public Builder frequencyId(TermId f) {
            this.frequencyId=f;
            return this;
        }

        public Builder frequencyString(String f) {
            this.frequencyString = f;
            return this;
        }

        public Builder ageOfOnsetId(TermId t) {
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
            V2SmallFileEntry entry=new V2SmallFileEntry(diseaseID,
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
                     assignedBy,
                     dateCreated);
            return entry;
        }
    }

    private V2SmallFileEntry(String disID,
            String diseaseName,
            TermId phenotypeId,
            String phenotypeName,
            TermId ageOfOnsetId,
            String ageOfOnsetName,
            String evidenceCode,
           // TermId frequencyId,
            String frequencyString,
            String sex,
            String negation,
            String modifier,
            String description,
            String publication,
            String assignedBy,
            String dateCreated) {
        this.diseaseID=disID;
        this.diseaseName=diseaseName;
        this.phenotypeId=phenotypeId;
        this.phenotypeName=phenotypeName;
        this.ageOfOnsetId=ageOfOnsetId;
        this.ageOfOnsetName=ageOfOnsetName;
        this.evidenceCode=evidenceCode;
        //this.frequencyId=frequencyId;
        this.frequencyModifier =frequencyString;
        this.sex=sex;
        this.negation=negation;
        this.modifier=modifier;
        this.description=description;
        this.publication=publication;
        this.assignedBy=assignedBy;
        this.dateCreated=dateCreated;

    }

    V2SmallFileEntry(OldSmallFileEntry oldEntry) throws HPOException {
        diseaseID=oldEntry.getDiseaseID();
        diseaseName=oldEntry.getDiseaseName();
        phenotypeId=oldEntry.getPhenotypeId();
        phenotypeName=oldEntry.getPhenotypeName();
        ageOfOnsetId=oldEntry.getAgeOfOnsetId();
        ageOfOnsetName=oldEntry.getAgeOfOnsetName();
        String evi=oldEntry.getEvidenceID();
        if (evi==null) {
            evi=oldEntry.getEvidenceName();
        }
        if (evi==null) {
            evi=oldEntry.getEvidence();
        }
        if (evi==null) {
           logger.error("Could not get valid evidence code");
           evidenceCode="UNKNOWN";
        } else
            evidenceCode=evi;
        if (evidenceCode.equals("HPO")) {
            System.exit(1);
        }
       // frequencyId=oldEntry.getFrequencyId();
        frequencyModifier =oldEntry.getThreeWayFrequencyString();
        sex=oldEntry.getSex();
        negation=oldEntry.getNegation().equals("null")?"":oldEntry.getNegation();
        Set<TermId> modifierSet=oldEntry.getModifierSet();
        if (modifierSet==null || modifierSet.isEmpty()) {
            modifier="";
        } else {
            modifier=modifierSet.stream().map(TermId::getIdWithPrefix).collect(Collectors.joining(";")); }
        description=oldEntry.getDescription();
        publication=oldEntry.getPub();
        assignedBy=oldEntry.getAssignedBy();
        dateCreated=oldEntry.getDateCreated();
       // System.out.println(getRow());
    }


    /**
     * @return Header line for the new V2 small files.
     */
    public static String getHeader() {
        String []fields={"#DiseaseID",
                "DiseaseName",
                "HpoId",
                "HpoName",
                "ageOfOnsetId",
                "ageOfOnsetName",
                "evidenceCode",
                "frequencyModifier",
                "sex",
                "negation",
                "modifier",
                "description",
                "publication",
                "assignedBy",
                "dateCreated"};
        return Arrays.stream(fields).collect(Collectors.joining("\t"));
    }

    @Override public String toString() { return getRow();}

    /**
     * Return the row that will be used to write the V2 small files entries to a file. Note that
     * we are checking for null strings TODO -- catch this upstream.
     * @return
     */
    public String getRow() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                diseaseID,
                diseaseName,
                phenotypeId.getIdWithPrefix(),
                phenotypeName,
                ageOfOnsetId!=null?ageOfOnsetId.getIdWithPrefix():EMPTY_STRING,
                ageOfOnsetName!=null?ageOfOnsetName:EMPTY_STRING,
                evidenceCode!=null?evidenceCode:"",
                frequencyModifier !=null? frequencyModifier:EMPTY_STRING,
                sex!=null?sex:"",
                negation!=null?negation:EMPTY_STRING,
                modifier!=null?modifier:EMPTY_STRING,
                description!=null?description:EMPTY_STRING,
                publication,
                assignedBy!=null?assignedBy:EMPTY_STRING,
                dateCreated);
    }


}
