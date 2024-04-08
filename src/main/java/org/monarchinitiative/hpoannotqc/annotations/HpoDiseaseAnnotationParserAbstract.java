package org.monarchinitiative.hpoannotqc.annotations;

import java.util.*;

public abstract class HpoDiseaseAnnotationParserAbstract {

    protected final Map<String, Integer> malformedBiocurationIdMap;

    protected final Set<String> obsoleteTermIdSet;

    /** Key - some malformed citation; vlaue-number of times this citation was encountered */
    protected final Map<String, Integer> malformedCitationMap;

    protected final Set<String> problematicHpoTerms;

    protected final List<String> parseErrors;

    public HpoDiseaseAnnotationParserAbstract() {
        malformedBiocurationIdMap = new HashMap<>();
        obsoleteTermIdSet = new HashSet<>();
        malformedCitationMap = new HashMap<>();
        problematicHpoTerms = new HashSet<>();
        parseErrors = new ArrayList<>();
    }








}
