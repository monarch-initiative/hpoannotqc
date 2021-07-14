package org.monarchinitiative.hpoannotqc.analysis;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Pair of OMIM names and biocuration data
 */
public class NameDate implements Comparable<NameDate> {

    private final String label;

    private final Date date;
    /** either #600123 or 600123 at beginning of string. */
    private final static Pattern omimPattern = Pattern.compile("^[#%]?\\d{6,6}\\s+");



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
            if (isNumeral(word) || (word.length() > 1 && Character.isUpperCase(word.charAt(1)) || isSpecialWord(word))) {
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
        if (label.endsWith(",")) {
            label = label.substring(0,label.length()-1);
        }
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
        } if (w.equals("X-LINKED")) {
            return true;
        } if (w.equals("X-LINKED,")) {
            return true;
        } if (w.equalsIgnoreCase("Robin")) {
            return true;
        } if (w.equalsIgnoreCase("Fallot")) {
            return true;
        } else if (w.length() == 1) {
            // might be from FANCONI ANEMIA, COMPLEMENTATION GROUP E
            return true;
        } else if (isRomanNumber(w)) {
            return true;
        } else{
            return false;
        }
    }

    private String unshout(String label) {
        String [] fields = label.split("\\s+");
        String firstWord = titleCase(fields[0]);
        List<String> newfields = new ArrayList<>();
        newfields.add(firstWord);
        for (int i = 1; i < fields.length; i++) {
            String word = fields[i];
            if (word.equalsIgnoreCase("X-LINKED")) {
                newfields.add("X-linked");
            } else if (word.equalsIgnoreCase("(Zellweger)")) {
                newfields.add("(Zellweger)");
            } else if (word.equalsIgnoreCase("lange")) {
                newfields.add("Lange"); // Cornelia de Lange
            } else if (word.equalsIgnoreCase("X-LINKED,")) {
                newfields.add("X-linked,");
            }  else if (word.equalsIgnoreCase("ROBIN")) {
                newfields.add("Robin"); // Robin sequence
            } else if (word.equalsIgnoreCase("Fallot")) {
                newfields.add("Fallot"); // Tetralogy of Fallot
            } else if (word.equalsIgnoreCase("Diamond-Blackfan")) {
                newfields.add("Diamond-Blackfan"); // Diamond-Blackfan anemia
            } else if (word.equalsIgnoreCase("ACTH-SECRETING")) {
                newfields.add("ACTH-secreting"); // Tetralogy of Fallot
            } else if (isSpecialWord(word)) {
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
        if (word.equalsIgnoreCase("mild")) return false; // mild is probably not a Roman numeral in OMIM
        final Set<Character> validRomanNumerals = new HashSet<>();
        validRomanNumerals.add('M');
        validRomanNumerals.add('D');
        validRomanNumerals.add('C');
        validRomanNumerals.add('L');
        validRomanNumerals.add('X');
        validRomanNumerals.add('V');
        validRomanNumerals.add('I');
        validRomanNumerals.add(',');

        for (char letter : word.toCharArray()) {
            if (! validRomanNumerals.contains(letter)) {
                return false;
            }
        }
        return true;
    }


    String titleCase(String word) {
        int dashPos = word.indexOf("-");
        if (word.equalsIgnoreCase("Diamond-Blackfan")) { return "Diamond-Blackfan";}
        if (dashPos < 0) {
            return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
        } else {
            String [] elements = word.split("-");
            List<String> elnew = new ArrayList<>();
            for (String e : elements) {
                elnew.add(Character.toUpperCase(e.charAt(0)) + e.substring(1).toLowerCase(Locale.ROOT));
            }
            return String.join("-", elnew);
        }
    }

    private String unshoutWord(String word) {
        if (isRomanNumber(word) || isSpecialWord(word)) {
            return word;
        } else {
            return word.toLowerCase(Locale.ROOT);
        }
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
        if (isXsyndrome(fields)) {
            return titleCase(fields[0]) + " " + fields[1].toLowerCase(Locale.ROOT);
        } else if (isXsyndromeN(fields)) {
            return titleCase(fields[0]) + " " + fields[1].toLowerCase(Locale.ROOT) + " " + fields[2];
        }
        if (fields.length == 1) {
            return firstLabel;
        }
        if (isXtype(fields)) {
            /// we know if we get here that there are at least 3 fields
            int LEN = fields.length;
            String [] starters = Arrays.copyOfRange(fields, 0,LEN - 2);
            String firstpart = String.join(" ", starters).toLowerCase(Locale.ROOT);
            firstpart = firstpart.substring(0,1).toUpperCase() + firstpart.substring(1);
            String name = titleCase(fields[LEN-2]);
            return firstpart + " " + name + " type";
        }
        if (isReversedXtype(fields)) {
            // disease ends with TYPE IH etc
            int LEN = fields.length;
            String [] starters = Arrays.copyOfRange(fields, 0,LEN - 2);
            String firstpart = Arrays.stream(starters).map(this::unshoutWord).collect(Collectors.joining(" "));
            firstpart = firstpart.substring(0,1).toUpperCase() + firstpart.substring(1);
            return firstpart + " type " + fields[LEN-1] ;
        }
        if (isShouting(firstLabel)) {
            firstLabel = unshout(firstLabel);
        }

        return firstLabel;
    }

    private boolean isReversedXtype(String[] fields) {
        if (fields.length < 3) return false;
        int LEN = fields.length;
        if (fields[LEN-2].equalsIgnoreCase("type")) {
            return true;
        } else {
            return false;
        }
    }


    private boolean isXtype(String[] fields) {
        if (fields.length < 3) return false;
        int LEN = fields.length;
        if (fields[LEN-1].equalsIgnoreCase("type")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isXsyndrome(String[] fields) {
        return  fields.length == 2 && fields[1].equalsIgnoreCase("SYNDROME");
    }

    private boolean isXsyndromeN(String[] fields) {
        return   fields.length == 3 && fields[1].equalsIgnoreCase("SYNDROME") && isNumeral(fields[2]);
    }

    @Override
    public int compareTo(NameDate that) {
        return that.date.compareTo(this.date);
    }
}
