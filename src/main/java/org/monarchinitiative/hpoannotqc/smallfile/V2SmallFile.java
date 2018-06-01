package org.monarchinitiative.hpoannotqc.smallfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.stream.Collectors.*;


import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;

/**
 * This class represents one disease-entity annotation  consisting usually of multiple annotations lines, and using
 * the new format introduced in 2018. The constructor will need to be adapted to input the new V2 file format
 * once the dust has settled. TODO
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 1/20/2018.
 */
public class V2SmallFile {
    private static final Logger logger = LogManager.getLogger();
    /** The base name of the V2 file, which is the same as the v1 small file. */
    private final String basename;
    private List<V2SmallFileEntry> entryList;

    public String getBasename() {
        return basename;
    }


    public V2SmallFile(String name, List<V2SmallFileEntry> entries) {
        basename=name;
        entryList=entries;
    }



    public List<V2SmallFileEntry> getEntryList() {
        return entryList;
    }

    public int getNumberOfAnnotations() { return entryList.size(); }

    public void mergeRedundantEntries() {
        Map<TermId, Long> numberOfAnnotationsByTermId =
                entryList.stream()
                        .collect(Collectors.groupingBy(V2SmallFileEntry::getPhenotypeId, counting()));
        List<TermId> redundantTids = numberOfAnnotationsByTermId.entrySet().
                stream().
                filter(e -> e.getValue()>1). // only get terms with more than one occurence
                map(Map.Entry::getKey). // get the TermId from the map
                collect(Collectors.toList());

        for (TermId tid : redundantTids) {
            List <V2SmallFileEntry> annotationList = entryList.
                    stream().
                    filter( v2e->v2e.getPhenotypeId().equals(tid)).
                    collect(Collectors.toList()); // get all entries with the current TermId
            V2SmallFileEntry mergedEntry = getMergedEntry(annotationList);
        }



    }

    private V2SmallFileEntry getMergedEntry(List <V2SmallFileEntry> annotationList) {
        if (annotationList.size()==1) return annotationList.get(0);

        V2SmallFileEntry merged = annotationList.get(0).clone();
        //merged.getFrequencyModifier();
        return merged;
    }



    /**
     * This is the header of the V2 small files.
     * @return V2 small file header.
     */
    public static String getHeaderV2() {
        String []fields={"#diseaseID",
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
            "assignedBy",
            "dateCreated"};
        return Arrays.stream(fields).collect(Collectors.joining("\t"));
    }


    /**
     * Convenience class for holding N/M frequency data
     */
    private static class NofM {
        int numerator;
        int denominator;
        NofM(String freq) throws Exception{
            int i=freq.indexOf("/");
            if (i<1) throw new Exception("invalid N of M String: \"" + freq + "\"");
            numerator=Integer.parseInt(freq.substring(0,i));
            denominator=Integer.parseInt(freq.substring(i+1));

        }

    }


}
