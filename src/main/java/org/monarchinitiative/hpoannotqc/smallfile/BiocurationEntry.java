package org.monarchinitiative.hpoannotqc.smallfile;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Each object represents on biocuration entry of the type {@code HPO:skoehler[2018-09-21]}.
 */
public class BiocurationEntry {

    private String biocurator;
    private String date;
    /** regex for patterns such as HPO:skoehler[2018-09-22] */
    private static final String regex = "(\\w+:\\w+)\\[(\\d{4}-\\d{2}-\\d{2})\\]";

    private BiocurationEntry(String bioc, String dt){
        this.biocurator=bioc;
        this.date=dt;
    }


    public String getBiocurator() {
        return biocurator;
    }

    public String getDate() {
        return date;
    }

    public static List<BiocurationEntry> getBiocurationList(String entrylist) {
        ImmutableList.Builder<BiocurationEntry> builder = new ImmutableList.Builder();
        String fields[] = entrylist.split(";");
        for (String f : fields) {
            BiocurationEntry entry = fromString(f);
            if (entry != null) {
                builder.add(entry);
            }
        }
        return builder.build();

    }



    private static BiocurationEntry fromString(String entry) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(entry);

        if (matcher.find()) {
            String biocurator = matcher.group(1);
            String date = matcher.group(2);
            return new BiocurationEntry(biocurator,date);
        } else {
            return null;
        }
    }






}
