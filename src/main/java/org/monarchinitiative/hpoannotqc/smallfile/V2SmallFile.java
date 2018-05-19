package org.monarchinitiative.hpoannotqc.smallfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents one disease-entity annotation  consisting usually of multiple annotations lines, and using
 * the new format introduced in 2018. The constructor will need to be adapted to input the new V2 file format
 * once the dust has settled. TODO
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 1/20/2018.
 */
public class V2SmallFile {
    private static final Logger logger = LogManager.getLogger();
    /** The base name of the V2 file, which is the same as the v1 small file. */
    private final String basename;
    private List<V2SmallFileEntry> entryList=new ArrayList<>();

    public String getBasename() {
        return basename;
    }


    public V2SmallFile(String name, List<V2SmallFileEntry> entries) {
        basename=name;
        entryList=entries;
    }



    public List<V2SmallFileEntry> getEntryList() {
        return entryList;
    }

    public int getNumberOfAnnotations() { return entryList.size(); }


    /**
     * This is the header of the V2 small files.
     * @return V2 small file header.
     */
    public static String getHeaderV2() {
        String []fields={"#diseaseID",
                "diseaseName",
                "phenotypeID",
                "phenotypeName",
                "onsetID",
                "onsetName",
                "frequency",
                "sex",
                "negation",
                "modifier",
                "description",
            "publication",
             "evidence",
            "assignedBy",
            "dateCreated"};
        return Arrays.stream(fields).collect(Collectors.joining("\t"));
    }






}
