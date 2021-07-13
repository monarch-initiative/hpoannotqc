package org.monarchinitiative.hpoannotqc.analysis;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pair of OMIM names and biocuration data
 */
public class NameDate implements Comparable<NameDate> {

    private final String label;

    private final Date date;
    /** either #600123 or 600123 at beginning of string. */
    private final static Pattern omimPattern = Pattern.compile("^#?\\d{6,6}\\s+");



    public NameDate(String label, String biocurationString) {
        this.label = label;
        final Matcher matcher = omimPattern.matcher(biocurationString);
        String [] fields = biocurationString.split(";");
        String curation1 = fields[0]; // take the first (earliest) entry
        int i = curation1.indexOf("[");
        int j = curation1.indexOf("]");
        if (i < 0 || j < 0 || j < i) {
            // should never happen!
            throw new PhenolRuntimeException("Malformed curation string " + curation1);
        }
        String dateString = curation1.substring(i+1, j);
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.date = df1.parse(dateString);
        } catch (ParseException e) {
            throw new PhenolRuntimeException("Could not parse date (" + dateString +") because " + e.getMessage());
        }
    }

    /**
     * Return true (SHOUT) if a label has multiple words and any second character is capitalized
     * @param label the original label assigned to the HPO entry
     * @return true if the label is in all caps
     */
    private boolean isShouting(String label) {
        String [] fields = label.split("\\s+");
        if (fields.length == 1) {
            return false;
        }
        // if all words are shouting, return true.
        // if only some words are shouting, assume that they have acronyms and return false
        int n_shouting_words = 0;
        for (String word : fields) {
            if (isNumeral(word) || (word.length() > 1 && Character.isUpperCase(word.charAt(1)))) {
                n_shouting_words++;
            }
        }
        return n_shouting_words == fields.length;
    }

    /** Check if a word is like 1 or 12A (type number)*/
    private boolean isNumeral(String word) {
        for (char c : word.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }

    private boolean isTypeNumber(String label) {
        if (label.length() == 1) {
            return true;
        } else {
            int n_letters = 0;
            int n_digits = 0;
            for (char c : label.toCharArray()) {
                if (Character.isDigit(c)) {
                    n_digits++;
                } else if (Character.isLetter(c)) {
                    n_letters++;
                }
            }
            if (n_letters > 1) {
                return false;
            } else if (n_digits+n_letters < label.length()) {
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean isSpecialWord(String w) {
        if (w.equals("DNA")) {
            return true;
        } else {
            return false;
        }
    }

    private String unshout(String label) {
        String [] fields = label.split("\\s+");
        String firstWord = Character.toTitleCase(fields[0].charAt(0)) + fields[0].substring(1).toLowerCase(Locale.ROOT);
        List<String> newfields = new ArrayList<>();
        newfields.add(firstWord);
        for (int i = 1; i < fields.length; i++) {
            String word = fields[i];
            if (isSpecialWord(word)) {
                newfields.add(word);
            } else if (isRomanNumber(word)) {
                newfields.add(word);
            } else if (isTypeNumber(word) ){
                newfields.add(word);
            } else{
                newfields.add((word.toLowerCase(Locale.ROOT)));
            }
        }
        return String.join(" ", newfields);
    }



    private boolean isRomanNumber(String word) {
        final Set<Character> validRomanNumerals = new HashSet<>();
        validRomanNumerals.add('M');
        validRomanNumerals.add('D');
        validRomanNumerals.add('C');
        validRomanNumerals.add('L');
        validRomanNumerals.add('X');
        validRomanNumerals.add('V');
        validRomanNumerals.add('I');

        for (char letter : word.toCharArray()) {
            if (! validRomanNumerals.contains(letter)) {
                return false;
            }
        }
        return true;
    }


    public String getPrettyVersion() {
        String [] fields = this.label.split(";");
        String firstLabel = fields[0];
        final Matcher matcher = omimPattern.matcher(firstLabel);
        if (matcher.find()) {
            // remove the #600123 from the begining of the String
            firstLabel = firstLabel.substring(matcher.end()).trim();
        }
        fields = firstLabel.split("\\s+");
        if (fields.length == 1) {
            return firstLabel;
        }
        if (isShouting(firstLabel)) {
            firstLabel = unshout(firstLabel);
        }
        return firstLabel;
    }

    @Override
    public int compareTo(NameDate that) {
        return that.date.compareTo(this.date);
    }
}
