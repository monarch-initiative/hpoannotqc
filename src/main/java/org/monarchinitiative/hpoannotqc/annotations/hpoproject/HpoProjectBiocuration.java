package org.monarchinitiative.hpoannotqc.annotations.hpoproject;

import org.monarchinitiative.hpoannotqc.Biocuration;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaError;
import org.monarchinitiative.hpoannotqc.annotations.hpoaerror.MalformedBiocurationEntryError;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HpoProjectBiocuration implements Biocuration {

    private static final String biocurationRegex = "(\\w+:\\w+|ORCID:\\d{4}-\\d{4}-\\d{4}-\\d{4})\\[\\d{4}-\\d{2}-\\d{2}]";
    /**
     * The pattern that corresponds to {@link #biocurationRegex}.
     */
    private static final Pattern biocurationPattern = Pattern.compile(biocurationRegex);

    private static final String EMPTY_STRING = "";

    private String biocuration;

    private final List<HpoaError> errorList;

    public HpoProjectBiocuration(String curationString) {
        errorList = new ArrayList<>();
        if (curationString == null ||curationString.isEmpty()) {
            biocuration = EMPTY_STRING;
            errorList.add(MalformedBiocurationEntryError.empty());
            return;
        }
        String[] fields = curationString.split(";");
        for (String f : fields) {
            Matcher matcher = biocurationPattern.matcher(f);
            if (!matcher.find()) {
                errorList.add(MalformedBiocurationEntryError.malformed(curationString));
                biocuration = EMPTY_STRING;
                return;
            }
        }
        // if we get here, the biocuration string is valid
        this.biocuration = curationString;
    }

    public String curation() {
        return biocuration;
    }

    public List<HpoaError> errors() {
        return errorList;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(biocuration);
    }
}
