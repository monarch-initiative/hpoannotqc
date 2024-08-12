package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.annotations.hpoproject.HpoAnnotationMerger;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HpoBigfileUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(HpoBigfileUtil.class);
    private final String todayDate;
    private final String hpoVersion;

    public HpoBigfileUtil(Ontology ontology) {
        todayDate = HpoAnnotQcUtil.getTodaysDate();
        hpoVersion = ontology.version().orElse("n/a");
        if (!hpoVersion.equals(todayDate)) {
            String err = String.format("Mismatching release dates-now %s, ontology-release: %s.",
                    todayDate, hpoVersion);
            LOGGER.warn(err);
        }
    }

    /**
     * Create header line with the fields. Prepend a '#' symbol
     * @return Header line for the big file (indicating column names for the data).
     */
    public String getHeaderLine() {
        String[] fields = {"database_id",
                "disease_name",
                "qualifier",
                "hpo_id",
                "reference",
                "evidence",
                "onset",
                "frequency",
                "sex",
                "modifier",
                "aspect",
                "biocuration"};
        return String.join("\t", fields);
    }


    public String getTodaysDate() {
        return todayDate;
    }

    public String getHpoVersion() {
        return hpoVersion;
    }
}
