package org.monarchinitiative.hpoannotqc.smallfile;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.frequency.FractionalFrequency;
import org.monarchinitiative.phenol.ontology.data.TermId;


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
    /** List of {@link V2SmallFileEntry} objects representing the original lines of the small file */
    private final List<V2SmallFileEntry> originalEntryList;
    /** List of {@link V2SmallFileEntry} objects whereby multiple entries representing the same term are merged. */
    private List<V2SmallFileEntry> mergedEntryList;

    public String getBasename() {
        return basename;
    }

    /** The constructor creates an immutable copy of the original list of {@link V2SmallFileEntry} objects
     * privided by tghe parser
     * @param name Name of the "small file"
     * @param entries List of {@link V2SmallFileEntry} objects -- one per line of the small file.
     */
    public V2SmallFile(String name, List<V2SmallFileEntry> entries) {
        basename=name;
        originalEntryList = ImmutableList.copyOf(entries);
    }

    /** @return original {@link V2SmallFileEntry} objects -- one per line of the small file.*/
    public List<V2SmallFileEntry> getOriginalEntryList() {
        return originalEntryList;
    }

    public int getNumberOfAnnotations() { return originalEntryList.size(); }

    public void mergeRedundantEntries() {
        ImmutableList.Builder<V2SmallFileEntry> builder = new ImmutableList.Builder<>();
        Map<TermId, Long> numberOfAnnotationsByTermId =
                originalEntryList.stream()
                        .collect(Collectors.groupingBy(V2SmallFileEntry::getPhenotypeId, counting()));
        // first add the unique entries
        for (V2SmallFileEntry entry : originalEntryList) {
            if (numberOfAnnotationsByTermId.get(entry.getPhenotypeId()) == 1) {
                builder.add(entry);
            }
        }
        // Now get entries with two or more per HP id.
        List<TermId> redundantTids = numberOfAnnotationsByTermId.entrySet().
                stream().
                filter(e -> e.getValue()>1). // only get terms with more than one occurence
                map(Map.Entry::getKey). // get the TermId from the map
                collect(Collectors.toList());
        for (TermId tid : redundantTids) {
            List <V2SmallFileEntry> annotationList = originalEntryList.
                    stream().
                    filter( v2e->v2e.getPhenotypeId().equals(tid)).
                    collect(Collectors.toList()); // get all entries with the current TermId
            V2SmallFileEntry merged = mergeByFrequency(annotationList);
            builder.add(merged);
        }
        this.mergedEntryList=builder.build();
    }


    /**
     * Merge redundant entries (i.e., those that share the same TermId) according to frequency. Our rule is
     * that (1) the N/M entries have priority. If we find at least one of them, then we add up all N/M entries,
     * for instance 2/3 + 4/5 = 6/8. (2) The percentage entries have the next highest priority. If we have at least
     * one percentage entry, then we take the average percentage. (3) If we have only frequency terms, then take
     * the entry with the highest frequency and report an error of the terms disagree.
     * @param annotationList
     */
    private V2SmallFileEntry mergeByFrequency(List <V2SmallFileEntry> annotationList) {
        List<V2SmallFileEntry> mOfNlist = annotationList.
                stream().filter(V2SmallFileEntry::isNofM).
                collect(Collectors.toList());
        List<V2SmallFileEntry> percentagelist = annotationList.
                stream().
                filter(V2SmallFileEntry::isPercentage).
                collect(Collectors.toList());
        List<V2SmallFileEntry> termlist = annotationList.stream().
                filter(V2SmallFileEntry::isFrequencyTerm).
                collect(Collectors.toList());
        String mergedFrequency;
        if (mOfNlist.size()>0) {
            mergedFrequency =  getMergedFractionalFrequency(mOfNlist);
        }  else if (percentagelist.size()>0) {
            mergedFrequency="todo";
        } else if (termlist.size()>0) {
            mergedFrequency="todo";
        } else {
            mergedFrequency="todo";
            // todo throw expception
        }
        List<String> descriptionlist;
        List<String> modifierlist;
        List<String> sexlist;
        List<String> onsetList;
        List<String> publicationList;
        String evidence;

//        for (V2SmallFileEntry e : annotationList) {
//            if ()
//        }


    return null;
    }


    private String getMergedFractionalFrequency(List<V2SmallFileEntry> entries) {
        List<FractionalFrequency> freqlist = new ArrayList<>();
        for (V2SmallFileEntry entry : entries) {
            String freqstring = entry.getFrequencyModifier();
            Optional<FractionalFrequency> opt = FractionalFrequency.create(freqstring);
            if (opt.isPresent()) {
                freqlist.add(opt.get());
            } else {
                // todo throw exception
            }
        }
        if (freqlist.isEmpty()) {
            // todo throw exception
        }
        FractionalFrequency ff = FractionalFrequency.mergeFrequencies(freqlist);
        return ff.getMofN();

    }









}
