package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.MalformedBiocurationEntryException;
import org.monarchinitiative.hpoannotqc.exception.MalformedCitationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiocurationChecker {

    private static final String biocurationRegex = "(\\w+:\\w+|ORCID:\\d{4}-\\d{4}-\\d{4}-\\d{4})\\[\\d{4}-\\d{2}-\\d{2}]";
    /**
     * The pattern that corresponds to {@link #biocurationRegex}.
     */
    private static final Pattern biocurationPattern = Pattern.compile(biocurationRegex);


    public static void check(String entrylist) {
        if (entrylist == null || entrylist.isEmpty()) {
            throw new MalformedCitationException("empty biocuration entry");
        }
        String[] fields = entrylist.split(";");
        for (String f : fields) {
            Matcher matcher = biocurationPattern.matcher(f);
            if (!matcher.find()) {
                throw new MalformedBiocurationEntryException(f);
            }
        }
    }


}
