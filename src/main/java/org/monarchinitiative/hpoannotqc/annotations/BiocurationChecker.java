package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoaEntryError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiocurationChecker {

    private static final String biocurationRegex = "(\\w+:\\w+|ORCID:\\d{4}-\\d{4}-\\d{4}-\\d{4})\\[\\d{4}-\\d{2}-\\d{2}]";
    /**
     * The pattern that corresponds to {@link #biocurationRegex}.
     */
    private static final Pattern biocurationPattern = Pattern.compile(biocurationRegex);

    public static void checkEntry(HpoAnnotationEntry entry) throws HpoaEntryError {
        String entryList = entry.getBiocuration();
        if (entryList == null || entryList.isEmpty()) {
            throw new HpoaEntryError(entry, "Empty biocuration entry");
        }
        String[] fields = entryList.split(";");
        for (String f : fields) {
            Matcher matcher = biocurationPattern.matcher(f);
            if (!matcher.find()) {
                String msg = String.format("Malformed biocuration entry: \"%s\".", f);
                throw new HpoaEntryError(entry, msg);
            }
        }
    }

    public static boolean check(String biocurationString) {
        Matcher matcher = biocurationPattern.matcher(biocurationString);
        return matcher.find();
    }

}
