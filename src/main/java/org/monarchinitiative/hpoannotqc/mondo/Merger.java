package org.monarchinitiative.hpoannotqc.mondo;




import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseWithMetadata;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.TermIdWithMetadata;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class takes two HPO disease definitions and merges them TODO more documentation
 */
public class Merger {

    private final HpoDiseaseWithMetadata disease_1;
    private final HpoDiseaseWithMetadata disease_2;
    private final HpoOntology ontology;

    public Merger(HpoDiseaseWithMetadata disease1, HpoDiseaseWithMetadata disease2, HpoOntology ontology) {
        this.disease_1=disease1;
        this.disease_2=disease2;
        this.ontology=ontology;
    }

    public Set<TermId> getSetOfPhenotypicAbnormalities(List<TermIdWithMetadata> timdlist) {
        Set<TermId> set = new HashSet<>();
        for (TermIdWithMetadata tmd : timdlist) {
            set.add(tmd.getTermId());
        }
        return set;
    }


    public HpoDiseaseWithMetadata merge() {
        System.err.println("##########  D1 to D2  ###########");
        mergeDirection1();
        System.err.println("##########  D2 to D1  ###########");
        mergeDirection2();



        return null;
    }



    private void mergeDirection1() {
        ClosestMatch closestMatch1 = new ClosestMatch( ontology, getSetOfPhenotypicAbnormalities(disease_1.getPhenotypicAbnormalities()));


        String name=String.format("%s/%s(merged)",disease_1.getName(),disease_2.getName() );
        String dbase="MONDO";
        String dbId="000042"; // need to get real MONDO id!
        List<TermIdWithMetadata> phenotypicAbnormalities = new ArrayList<>();
        List<TermId> modesOfInheritance  = new ArrayList<>();
        List<TermId> notTerms  = new ArrayList<>();
        List<TermIdWithMetadata> termIn1not2 = new ArrayList<>();
        List<TermIdWithMetadata> termIn2not1 = new ArrayList<>();
        for (TermIdWithMetadata tiwm : disease_2.getPhenotypicAbnormalities()) {
            String label = ontology.getTermMap().get(tiwm.getTermId()).getName();
            if (disease_1.isDirectlyAnnotatedTo(tiwm)) {
                phenotypicAbnormalities.add(tiwm);
                System.err.println(String.format("D1 & D2: %s[%s]" ,label, tiwm.getIdWithPrefix()));
            } else {
                Map<TermId,Integer> bestMatch = closestMatch1.closestTerms(tiwm.getTermId());
                System.err.print(String.format("D1: %s[%s]\tD2: " ,label, tiwm.getIdWithPrefix()));
                String labels=bestMatch.keySet().stream().map(tid -> String.format("%s[%s](d=%d)",
                        ontology.getTermMap().get(tid).getName(),
                        tid.getIdWithPrefix(),
                        bestMatch.get(tid))).collect(Collectors.joining(";"));
                System.err.println(labels);
            }
        }
        return;
    }
    private void mergeDirection2() {
        ClosestMatch closestMatch1 = new ClosestMatch( ontology, getSetOfPhenotypicAbnormalities(disease_2.getPhenotypicAbnormalities()));


        String name=String.format("%s/%s(merged)",disease_2.getName(),disease_1.getName() );
        String dbase="MONDO";
        String dbId="000042"; // need to get real MONDO id!
        List<TermIdWithMetadata> phenotypicAbnormalities = new ArrayList<>();
        List<TermId> modesOfInheritance  = new ArrayList<>();
        List<TermId> notTerms  = new ArrayList<>();
        List<TermIdWithMetadata> termIn1not2 = new ArrayList<>();
        List<TermIdWithMetadata> termIn2not1 = new ArrayList<>();
        for (TermIdWithMetadata tiwm : disease_1.getPhenotypicAbnormalities()) {
            String label = ontology.getTermMap().get(tiwm.getTermId()).getName();
            if (disease_2.isDirectlyAnnotatedTo(tiwm)) {
                phenotypicAbnormalities.add(tiwm);
                System.err.println(String.format("D1 & D2: %s[%s]" ,label, tiwm.getIdWithPrefix()));
            } else {
                Map<TermId,Integer> bestMatch = closestMatch1.closestTerms(tiwm.getTermId());
                System.err.print(String.format("D1: %s[%s]\tD2: " ,label, tiwm.getIdWithPrefix()));
                String labels=bestMatch.keySet().stream().map(tid -> String.format("%s[%s](d=%d)",
                        ontology.getTermMap().get(tid).getName(),
                        tid.getIdWithPrefix(),
                        bestMatch.get(tid))).collect(Collectors.joining(";"));
                System.err.println(labels);
            }
        }
        return;
    }




}
