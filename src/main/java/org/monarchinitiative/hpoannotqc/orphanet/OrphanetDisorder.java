package org.monarchinitiative.hpoannotqc.orphanet;

import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode;

import java.util.ArrayList;
import java.util.List;

import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.UNINITIALIZED_DISEASE_NAME;

public class OrphanetDisorder {
    private static final Logger logger = LogManager.getLogger();
    /** This is the internal Orphanet ID which is an attribute of Disorder in the XML file. We can probably ignore this. */
    private int id;
    /** This is the external Orphanet accession number. */
    private int orphaNumber;
    /** Disease name */
    private String name;
    /** HPO Id of the phenotype annotation */
    private TermId hpoId;
    /** HPO Label of the phenotype annotation. */
    private String hpoLabel;
    /** HPO TermId of the frequency of the phenotype in the disease. */
    private TermId frequency;
    /** Flag to indicate if this is an Orphanet Diagnostic Criterion. */
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
        if (name==null || name.equals("null")) {
            logger.error("Orphanet name was set to null...should nevel happen...terminating");
            System.exit(1);
        }
    }

    public void setHPO(TermId id, String label) {
        this.hpoId=id;
        this.hpoLabel=label;
    }

    public void setFrequency(TermId f) {
        this.frequency=f;
    }

    public int getOrphaNumber() {
        return orphaNumber;
    }

    public String getName() {
        return name;
    }

    public TermId getHpoId() {
        return hpoId;
    }

    public String getHpoLabel() {
        return hpoLabel;
    }

    public TermId getFrequency() {
        return frequency;
    }

    public void setDiagnosticCriterion() { isDiagnosticCriterion=true; }


    @Override
    public String toString() {
        return String.format("ORPHA:%d %s", orphaNumber, name);
    }



    public List<SmallFileQCCode> qcCheck() {
        List<SmallFileQCCode> lst = new ArrayList<>();
        if (name==null) {
            lst.add(UNINITIALIZED_DISEASE_NAME);
        }
        return lst;
    }


}
