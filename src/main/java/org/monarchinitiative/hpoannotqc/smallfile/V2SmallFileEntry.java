package org.monarchinitiative.hpoannotqc.smallfile;


import org.apache.logging.log4j.LogManager;
import org.monarchinitiative.hpoannotqc.exception.HPOException;
import org.monarchinitiative.phenol.ontology.data.TermId;

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
    private final String assignedBy;
    /** Field #15 */
    private final String dateCreated;

    private static final String EMPTY_STRING="";

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
        public Builder(String diseaseId,
                       String diseasename,
                       TermId phenoId,
                       String phenoName,
                       String evidence,
                       String pub,
                       String ab,
                       String date) {
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
                     assignedBy,
                     dateCreated);
        }
    }

    private V2SmallFileEntry(String disID,
            String diseaseName,
            TermId phenotypeId,
            String phenotypeName,
            TermId ageOfOnsetId,
            String ageOfOnsetName,
            String evidenceCode,
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
        this.frequencyModifier =frequencyString;
        this.sex=sex;
        this.negation=negation;
        this.modifier=modifier;
        this.description=description;
        this.publication=publication;
        this.assignedBy=assignedBy;
        this.dateCreated=dateCreated;

    }

    /** @return the row that will be written to the V2 file for this entry. */
    @Override public String toString() { return getRow();}

    /**
     * Return the row that will be used to write the V2 small files entries to a file. Note that
     * we replace null strings (which are a signal for no data available) with the empty string
     * to avoid the string "null" being written.
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
                frequencyModifier !=null? frequencyModifier:EMPTY_STRING,
                sex!=null?sex:"",
                negation!=null?negation:EMPTY_STRING,
                modifier!=null?modifier:EMPTY_STRING,
                description!=null?description:EMPTY_STRING,
                publication,
                evidenceCode!=null?evidenceCode:"",
                assignedBy!=null?assignedBy:EMPTY_STRING,
                dateCreated);
    }



}
