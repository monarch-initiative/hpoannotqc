package org.monarchinitiative.hpoannotqc.orphanet;

public class OrphanetDisorder {

    private int id;
    private int orphaNumber;
    private String name;
    private String hpoId;
    private String hpoLabel;
    private String frequency;
    private boolean isDiagnosticCriterion=false;




    public OrphanetDisorder() {

    }


    public void setId(int id) {
     this.id=id;
    }


    public void setOrphaNumber(int n) {
        this.orphaNumber=n;
    }

    public void setName(String n) {
        name=n;
    }

    public void setHPO(String id, String label) {
        this.hpoId=id;
        this.hpoLabel=label;
    }

    public void setFrequency(String f) {
        this.frequency=f;
    }

    public void setDiagnosticCriterion() { isDiagnosticCriterion=true; }


    @Override
    public String toString() {
        String dc=isDiagnosticCriterion?" [diagnostic criterion]":"";
        return String.format("ORPHA:%d %s (%d) %s [%s]: %s%s",orphaNumber,name,id,hpoLabel,hpoId,frequency,dc);
    }


}
