package org.monarchinitiative.hpoannotqc.analysis;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.util.Objects;

/**
 * This class is used to detect duplicates by storing the fields that are important.
 * This is intended to fix a bug in our text mining pipeline
 */
public class UniqueAnnotation {
    private final String hpoid; // e.g., HP:0000510
    private final String reference; // eg OMIM:400004	TAS
    private final String evidence; //e.g., TAS, IEA
    private final String onset;
    private final String frequency;
    private final String biocuration;


    public UniqueAnnotation(String line) {
        String [] fields= line.split("\t");
        if (fields.length != 14) {
            // should never happen
            throw new PhenolRuntimeException("Bad number of fields:" + fields.length);
        }
        // #DatabaseID	DiseaseName	Qualifier	HPO_ID	Reference	Evidence	Onset	Frequency	Sex	Modifier	Aspect	Biocuration
        hpoid = fields[2]; // e.g., HP:0000510
        reference = fields[11]; // eg OMIM:400004	TAS
        evidence = fields[12]; //e.g., TAS, IEA
        onset = fields[4];
        frequency = fields[6];
        biocuration = fields[13];

        if (! hpoid.startsWith("HP:")) {
            throw new PhenolRuntimeException("Bad HP term " + hpoid);
        }
    }


    public boolean isDuplicateRemovalCandidate() {
        return (biocuration.contains("iea") || biocuration.contains("koehler") )
                && evidence.equals("IEA")
                && frequency.equals("")
                && reference.contains("OMIM");
    }

    @Override
    public int hashCode() {
        return Objects.hash( hpoid, reference, evidence,onset,frequency);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof UniqueAnnotation)) return false;
        UniqueAnnotation that = (UniqueAnnotation) obj;
        return this.hpoid.equals(that.hpoid) && this.reference.equals(that.reference)
                && this.evidence.equals(that.evidence)
                && this.onset.equals(that.onset)
                && this.frequency.equals(that.frequency);
    }
}
