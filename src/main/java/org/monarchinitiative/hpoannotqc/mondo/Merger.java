package org.monarchinitiative.hpoannotqc.mondo;


import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseWithMetadata;
import com.github.phenomics.ontolib.formats.hpo.TermIdWithMetadata;
import com.github.phenomics.ontolib.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;

/**
 * This class takes two HPO disease definitions and merges them TODO more documentation
 */
public class Merger {

    private final HpoDiseaseWithMetadata disease_1;
    private final HpoDiseaseWithMetadata disease_2;


    public Merger(HpoDiseaseWithMetadata disease1, HpoDiseaseWithMetadata disease2) {
        this.disease_1=disease1;
        this.disease_2=disease2;
    }


    public HpoDiseaseWithMetadata merge() {
        String name=String.format("%s/%s(merged)",disease_1.getName(),disease_2.getName() );
        String dbase="MONDO";
        String dbId="000042"; // need to get real MONDO id!
        List<TermIdWithMetadata> phenotypicAbnormalities = new ArrayList<>();
        List<TermId> modesOfInheritance  = new ArrayList<>();
        List<TermId> notTerms  = new ArrayList<>();
        List<TermIdWithMetadata> termIn1not2 = new ArrayList<>();
        List<TermIdWithMetadata> termIn2not1 = new ArrayList<>();
        for (TermIdWithMetadata tiwm : disease_1.getPhenotypicAbnormalities()) {
            if (disease_2.isDirectlyAnnotatedTo(tiwm)) {
                phenotypicAbnormalities.add(tiwm);
                System.err.println("MATCH " + tiwm.getIdWithPrefix());
            } else {
                System.err.println("NO direct MATCH " + tiwm.getIdWithPrefix());
            }
        }
        return null;
    }




}
