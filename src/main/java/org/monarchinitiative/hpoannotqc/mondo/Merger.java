package org.monarchinitiative.hpoannotqc.mondo;




import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class takes two HPO disease definitions and merges them TODO more documentation
 */
public class Merger {

    private final HpoDisease disease_1;
    private final HpoDisease disease_2;
    private final HpoOntology ontology;

    public Merger(HpoDisease disease1, HpoDisease disease2, HpoOntology ontology) {
        this.disease_1=disease1;
        this.disease_2=disease2;
        this.ontology=ontology;
    }

    public Set<TermId> getSetOfPhenotypicAbnormalities(List<HpoAnnotation> timdlist) {
        Set<TermId> set = new HashSet<>();
        for (HpoAnnotation tmd : timdlist) {
            set.add(tmd.getTermId());
        }
        return set;
    }


    public HpoDisease merge() {
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
        List<HpoAnnotation> phenotypicAbnormalities = new ArrayList<>();
        List<TermId> modesOfInheritance  = new ArrayList<>();
        List<TermId> notTerms  = new ArrayList<>();
        List<HpoAnnotation> termIn1not2 = new ArrayList<>();
        List<HpoAnnotation> termIn2not1 = new ArrayList<>();
        for (HpoAnnotation tiwm : disease_2.getPhenotypicAbnormalities()) {
            String label = ontology.getTermMap().get(tiwm.getTermId()).getName();
            if (disease_1.isDirectlyAnnotatedTo(tiwm.getTermId())) {
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
    }
    private void mergeDirection2() {
        ClosestMatch closestMatch1 = new ClosestMatch( ontology, getSetOfPhenotypicAbnormalities(disease_2.getPhenotypicAbnormalities()));


        String name=String.format("%s/%s(merged)",disease_2.getName(),disease_1.getName() );
        String dbase="MONDO";
        String dbId="000042"; // need to get real MONDO id!
        List<HpoAnnotation> phenotypicAbnormalities = new ArrayList<>();
        List<TermId> modesOfInheritance  = new ArrayList<>();
        List<TermId> notTerms  = new ArrayList<>();
        List<TermId> termIn1not2 = new ArrayList<>();
        List<TermId> termIn2not1 = new ArrayList<>();
        for (HpoAnnotation tiwm : disease_1.getPhenotypicAbnormalities()) {
            String label = ontology.getTermMap().get(tiwm.getTermId()).getName();
            if (disease_2.isDirectlyAnnotatedTo(tiwm.getTermId())) {
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
    }




}
