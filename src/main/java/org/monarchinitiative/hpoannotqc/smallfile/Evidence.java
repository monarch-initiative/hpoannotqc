package org.monarchinitiative.hpoannotqc.smallfile;

import java.util.Set;

public class Evidence {
    enum ev {IEA,TAS,PCS}

    ev evidence;

    public Evidence(String e) {
        switch (e) {
            case "IEA":
                this.evidence=ev.IEA;
                break;
            case "TAS":
                this.evidence=ev.TAS;
                break;
            case "PCS":
                this.evidence=ev.PCS;
        }
    }

    public String getStrongestEvidenceCode(Set<Evidence> evset) {
        if (evset.contains(ev.PCS)) return "PCS";
        else if (evset.contains(ev.TAS)) return "TAS";
        else return "IEA"; // the default

    }
}
