package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

public class HpoaSkippableError implements HpoaError {

    private final String disease;
    private final String message;




    public HpoaSkippableError(String disease, String msg) {
        this.disease = disease;
        this.message = msg;
    }




    @Override
    public String getDisease() {
        return disease;
    }

    @Override
    public String getMessage() {
        return message;
    }


    /**
     * This class of Error is serious enough that we will skip the resulting
     * error for ORPHA data
     * @return true if this error should lead us to skip this item
     */
    @Override
    public boolean skippable(){
        return true;
    }

}
