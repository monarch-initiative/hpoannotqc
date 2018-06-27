package org.monarchinitiative.hpoannotqc.smallfile.frequency;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conveniece class for working with frequencies with the format M/N
 */
public class FractionalFrequency {

    private static final String REGEX = "(\\d+)/(\\d+)";

    private final int numerator;

    private final int denominator;

    private FractionalFrequency(int m, int n) {
        this.numerator=m;
        this.denominator=n;
    }

    public static FractionalFrequency mergeFrequencies(List<FractionalFrequency> fflist){
        int M=0;
        int N=0;
        for (FractionalFrequency ff : fflist) {
            M += ff.numerator;
            N += ff.denominator;
        }
        return new FractionalFrequency(M,N);
    }



    public static Optional<FractionalFrequency> create(String freq) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(freq);
        if (matcher.find()) {
            String M = matcher.group(1);
            String N = matcher.group(2);
            try {
                Integer m = Integer.parseInt(M);
                Integer n = Integer.parseInt(N);
                FractionalFrequency ff = new FractionalFrequency(m,n);
                return Optional.of(ff);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public String getMofN() { return String.format("%d/%d",this.numerator,this.denominator); }



}
