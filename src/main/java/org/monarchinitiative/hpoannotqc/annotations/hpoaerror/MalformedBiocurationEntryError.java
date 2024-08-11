package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

import static org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorCategory.MALFORMED_BIOCURATION_ENTRY;

public class MalformedBiocurationEntryError implements HpoaError {


    private final String message;

    public MalformedBiocurationEntryError(String msg) {
        this.message = msg;
    }

    @Override
    public HpoaErrorCategory category() {
        return MALFORMED_BIOCURATION_ENTRY;
    }


    @Override
    public String getMessage() {
        return message;
    }


    public static MalformedBiocurationEntryError empty() {
        return new MalformedBiocurationEntryError("Empty biocuration entry");
    }

    public static MalformedBiocurationEntryError malformed(String entry) {
        return new MalformedBiocurationEntryError(String.format("Malformed biocuration entry: \"%s\"", entry));
    }


}
