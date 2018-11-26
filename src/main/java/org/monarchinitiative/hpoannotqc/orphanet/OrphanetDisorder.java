package org.monarchinitiative.hpoannotqc.orphanet;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;

import static org.monarchinitiative.hpoannotqc.smallfile.SmallFileQCCode.UNINITIALIZED_DISEASE_NAME;

/**
 * This class represents on disease as annotated by Orphanet.
 */
public class OrphanetDisorder {
    private static final Logger logger = LogManager.getLogger();
    /** This is the external Orphanet accession number. */
    private int orphaNumber;
    /** Disease name */
    private String name;
    /** HPO Ids of the phenotype annotation */
    private final List<TermId> hpoIdList;
    /** HPO Labels of the phenotype annotation. Note that these are in the same order as the Ids, but since we only
     * output the TermIds in the big file, they will get ignored anyway.*/
    private final List<String> hpoLabelList;
    /** HPO TermId of the frequency of the phenotype in the disease. */
    private TermId frequency;



    public OrphanetDisorder() {
        hpoIdList=new ArrayList<>();
        hpoLabelList=new ArrayList<>();
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
        this.hpoIdList.add(id);
        this.hpoLabelList.add(label);
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

    public List<TermId> getHpoIds() {
        return hpoIdList;
    }



    public TermId getFrequency() {
        return frequency;
    }


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
