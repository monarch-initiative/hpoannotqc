package org.monarchinitiative.hpoannotqc.smallfile;

import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;


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
    /** Field #8 */
    private final TermId frequencyId;
    /** Field #9 */
    private final String frequencyString;
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
        private  String ageOfOnsetName=null;
        /** Field #7 */
        private  final String evidenceCode;
        /** Field #8 */
        private  TermId frequencyId=null;
        /** Field #9 */
        private  String frequencyString=null;
        /** Field #10 */
        private  String sex=null;
        /** Field #11 */
        private  String negation=null;
        /** Field #12 */
        private  String modifier=null;
        /** Field #13 */
        private  String description=null;
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
                     frequencyId,
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
            TermId frequencyId,
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
        this.frequencyId=frequencyId;
        this.frequencyString=frequencyString;
        this.sex=sex;
        this.negation=negation;
        this.modifier=modifier;
        this.description=description;
        this.publication=publication;
        this.assignedBy=assignedBy;
        this.dateCreated=dateCreated;

    }

    V2SmallFileEntry(OldSmallFileEntry oldEntry) {
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
           evidenceCode="UNKKNOWN";
        } else
            evidenceCode=evi;
        frequencyId=oldEntry.getFrequencyId();
        frequencyString=oldEntry.getFrequencyString();
        sex=oldEntry.getSex();
        negation=oldEntry.getNegation();
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
                "frequencyId",
                "frequencyString",
                "sex",
                "negation",
                "modifier",
                "description",
                "publication",
                "assignedBy",
                "dateCreated"};
        return Arrays.stream(fields).collect(Collectors.joining("\t"));
    }


    public String getRow() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                diseaseID,
                diseaseName,
                phenotypeId.getIdWithPrefix(),
                phenotypeName,
                ageOfOnsetId!=null?ageOfOnsetId.getIdWithPrefix() : "",
                ageOfOnsetName!=null?ageOfOnsetName:"",
                evidenceCode,
                frequencyId!=null?frequencyId.getIdWithPrefix():"",
                frequencyString!=null?frequencyString:"",
                sex,
                negation,
                modifier,
                description,
                publication,
                assignedBy,
                dateCreated);
    }


}
