package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HpoAnnotQcUtil {



    public static Ontology getHpo(String hpJsonPath) {
        if (hpJsonPath == null) {
            hpJsonPath = String.format("%s%s%s", "data", File.separator, "hp.json");
        }
        File f = new File(hpJsonPath);
        if (! f.isFile()) {
            String err = String.format("Could not find hp.json file at \"%s\".", hpJsonPath);
            System.err.println(err);
            throw new PhenolRuntimeException(err);
        }
        return OntologyLoader.loadOntology(f);
    }


    public static String getTodaysDate() {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        return ft.format(dNow);
    }





}
