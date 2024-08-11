package org.monarchinitiative.hpoannotqc.annotations.hpoproject;


import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Items of this class represent one line of an annotation file for one disease as curated
 * by the HPO project
 * @author Peter Robinson
 */
public class HpoProjectAnnotationLine {

    /**
     * These are the fields of the per-disease annotation files ("small files")
     */
    private final static String[] expectedFields = {
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
    /**
     * Number of tab-separated expectedFields in a valid small file.
     */
    private static final int NUMBER_OF_FIELDS = expectedFields.length;

    public HpoProjectAnnotationLine(String diseaseID,
                                    String diseaseName,
                                    TermId phenotypeId,
                                    String phenotypeName,
                                    String ageOfOnsetId,
                                    String ageOfOnsetName,
                                    String frequencyString,
                                    String sex,
                                    String negation,
                                    String modifier,
                                    String description,
                                    String publication,
                                    String evidenceCode,
                                    String biocuration) {
    }


    public static HpoProjectAnnotationLine fromLine(String line, Ontology ontology) {
        String[] A = line.split("\t");

        if (A.length != NUMBER_OF_FIELDS) {
            throw new HpoAnnotQcException(String.format("We were expecting %d expectedFields but got %d for line %s", NUMBER_OF_FIELDS, A.length, line));
        }
        String diseaseID = A[0];
        String diseaseName = A[1];
        TermId phenotypeId = TermId.of(A[2]);
        String phenotypeName = A[3];
        String ageOfOnsetId = A[4];
        String ageOfOnsetName = A[5];
        String frequencyString = A[6];
        String sex = A[7];
        String negation = A[8];
        String modifier = A[9];
        String description = A[10];
        String publication = A[11];
        String evidenceCode = A[12];
        String biocuration = A[13];

        HpoProjectAnnotationLine entry = new HpoProjectAnnotationLine(diseaseID,
                diseaseName,
                phenotypeId,
                phenotypeName,
                ageOfOnsetId,
                ageOfOnsetName,
                frequencyString,
                sex,
                negation,
                modifier,
                description,
                publication,
                evidenceCode,
                biocuration);
        // if the following method does not throw an Exception, we are good to go!
        //performQualityControl(entry, ontology, diseaseName);
        return entry;

    }
}
