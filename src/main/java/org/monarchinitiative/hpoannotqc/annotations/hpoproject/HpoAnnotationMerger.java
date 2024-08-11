package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.monarchinitiative.hpoannotqc.TermValidator;
import org.monarchinitiative.hpoannotqc.annotations.AnnotationEntryI;
import org.monarchinitiative.hpoannotqc.annotations.FrequencyModifier;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoFrequency;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HpoAnnotationMerger {
    private static final String EMPTY_STRING = "";

    /**
     * To be used for matching n/m frequencies.
     */
    private final static Pattern n_of_m_pattern = Pattern.compile("^(\\d+)/(\\d+?)");

    private final static Pattern percentage_pattern = Pattern.compile("^(\\d*\\.?\\d+)%");

    private final static Pattern hpoTerm_pattern = Pattern.compile("^HP:\\d{7}$");

    private final Ontology ontology;

    private final TermValidator validator;

    public HpoAnnotationMerger(Ontology ontology, TermValidator validator) {
        this.ontology = ontology;
        this.validator = validator;
    }

    public AnnotationEntryI mergeEntries(List<AnnotationEntryI> entrylist) {
        AnnotationEntryI first = entrylist.get(0);
        String diseaseId=first.getDiseaseID();
        String diseaseName=first.getDiseaseName();
        String phenoId=first.getPhenotypeId();
        String phenoName=first.getPhenotypeLabel();
        String onsetId=first.getAgeOfOnsetId();
        String onsetName=first.getAgeOfOnsetLabel();
        FrequencyModifier mergedFrequency = mergeFrequencies(entrylist);
        String sex=first.getSex();
        String negation=first.getNegation();
        String mergedModifiers=mergeModifiers(entrylist);
        String mergedDescriptions=mergeDescriptions(entrylist);
        String mergedPublications=mergePublications(entrylist);
        String evidence=getHighestEvidenceCode(entrylist);
        String mergedBiocuration=mergeBiocuration(entrylist);
        List<String> entries = List.of(diseaseId,
                diseaseName,
                phenoId,
                phenoName,
                onsetId,
                onsetName,
                mergedFrequency.frequencyString(),
                sex,
                negation,
                mergedModifiers,
                mergedDescriptions,
                mergedPublications,
                evidence,
                mergedBiocuration);
        String mergedAnnotationLine = String.join("\t", entries);
        return HpoProjectAnnotationLine.fromLine(mergedAnnotationLine, validator,ontology);
    }

    /**
     * We want to merge entries with different n-of-m frequencies. For instance, if
     * we have 2/3 and 5/7 then we would merge this to 7/10. If one of the entries
     * is not n-of-m, then we will transform it as if the percentage or ontology term
     * represents 10 observations. If the field is empty, then we will assume it is
     * 100%, i.e., 10/10
     *
     * @param entrylist List of frequency strings
     * @return merged frequency string
     */
    public FrequencyModifier mergeFrequencies(final List<AnnotationEntryI> entrylist) {
        int sum_of_numerators = 0;
        int sum_of_denominators = 0;
        List<Integer> numerators = new ArrayList<>();
        List<Integer> denominators = new ArrayList<>();
        final int DEFAULT_NUMBER_OF_OBSERVATIONS = 10;

        for (AnnotationEntryI e : entrylist) {
            String q = e.getFrequencyModifier();
            Matcher matcher = n_of_m_pattern.matcher(q);
            Matcher percentageMatcher = percentage_pattern.matcher(q);
            Matcher termMatcher = hpoTerm_pattern.matcher(q);
            if ( q.isEmpty()) {
                // 1) No frequency entry available. Assume 100%, i.e., 10/10
                ; // no action
            } else if (matcher.matches()){
                // 2) The frequency string is of the form n/m
                String n_str=matcher.group(1);
                String m_str=matcher.group(2);
                // if we match the regex, the following "must" work.
                int n=Integer.parseInt(n_str);
                int m=Integer.parseInt(m_str);
                sum_of_numerators += n;
                sum_of_denominators += m;
            } else if (percentageMatcher.matches()) {
                String p_str=percentageMatcher.group(1);
                // If we match the regex, the following "must" work
                // We assume that the percentage applies to 10 individuals (heuristic)
                double d = Double.parseDouble(p_str);
                int n = (int)Math.round(d/10.0);
                sum_of_numerators += n;
                sum_of_denominators += DEFAULT_NUMBER_OF_OBSERVATIONS;
            } else if (termMatcher.matches()){
                TermId freqid = TermId.of(q);
                HpoFrequency hpofreq= HpoFrequency.fromTermId(freqid);
                double proportion = hpofreq.mean();
                int n=(int)Math.round(proportion*10.0);
                sum_of_numerators += n;
                sum_of_denominators += DEFAULT_NUMBER_OF_OBSERVATIONS;
            } else {
                // should never happen but if it does we want to know right away
                throw new PhenolRuntimeException("Could not parse frequency entry: \"" + q+"\"");
            }
        }
        if (sum_of_numerators == 0 && sum_of_denominators == 0) {
            return HpoProjectFrequency.empty();
        }
        String mergedFreqString = String.format("%d/%d",sum_of_numerators,sum_of_denominators);
        return HpoProjectFrequency.fromHpoaLine(mergedFreqString, ontology);
    }

    public static  String mergeModifiers(final List<AnnotationEntryI> entrylist) {
        List<String> modifiers=new ArrayList<>();
        for (AnnotationEntryI entry : entrylist) {
            String mod = entry.getModifier();
            if (mod!=null && !mod.isEmpty()) {
                modifiers.add(mod);
            }
        }
        if (modifiers.isEmpty()) {
            return ""; // no modifiers, return empty string
        } else {
            return String.join(";",modifiers);
        }
    }

    public static  String mergeDescriptions(final List<AnnotationEntryI> entrylist) {
        List<String> descriptions=new ArrayList<>();
        for (AnnotationEntryI entry : entrylist) {
            String mod = entry.getDescription();
            if (mod != null && ! mod.isEmpty()) {
                descriptions.add(mod);
            }
        }
        if (descriptions.isEmpty()) {
            return ""; // no modifiers, return empty string
        } else {
            return String.join(";",descriptions);
        }
    }

    public static  String mergePublications(final List<AnnotationEntryI> entrylist) {
        Set<String> pubs=new HashSet<>();
        for (AnnotationEntryI entry : entrylist) {
            pubs.add(entry.getPublication());
        }
        return String.join(";",pubs);
    }

    public static String getHighestEvidenceCode(final List<AnnotationEntryI> entrylist) {
        String evi="IEA";//default
        for (AnnotationEntryI entry : entrylist) {
            if (entry.getEvidenceCode().equals("PCS")) {
                return "PCS";
            } else if (entry.getEvidenceCode().equals("TAS")) {
                evi="TAS"; // better than IEA
            }
        }
        return evi;
    }

    public static String mergeBiocuration(final List<AnnotationEntryI> entrylist) {
        Set<String> biocuration=new HashSet<>();
        for (AnnotationEntryI entry : entrylist) {
            biocuration.add(entry.getBiocuration());
        }
        return String.join(";",biocuration);
    }

    public boolean divergentNegation(List<AnnotationEntryI> entrylist) {
        String firstItemNegated = entrylist.get(0).getNegation();
        for (int i = 1; i < entrylist.size(); ++i) {
            if (!firstItemNegated.equals(entrylist.get(i).getNegation())) {
                return true;
            }
        }
        return false; // if we get here we can still merge. Items are not divergent
    }



    public boolean divergentSex(List<AnnotationEntryI> entrylist) {
        String firstItemSex = entrylist.get(0).getSex();
        for (int i = 1; i < entrylist.size(); ++i) {
            if (!firstItemSex.equals(entrylist.get(i).getSex())) {
                return true;
            }
        }
        return false; // if we get here we can still merge. Items are not divergent
    }



    public boolean divergentOnset(List<AnnotationEntryI> entrylist) {
        String firstItemOnsetId = entrylist.get(0).getAgeOfOnsetId();
        for (int i = 1; i < entrylist.size(); ++i) {
            if (!firstItemOnsetId.equals(entrylist.get(i).getAgeOfOnsetId())) {
                return true;
            }
        }
        return false; // if we get here we can still merge. Items are not divergent
    }


}
