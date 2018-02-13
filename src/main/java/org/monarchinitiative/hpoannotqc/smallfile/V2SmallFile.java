package org.monarchinitiative.hpoannotqc.smallfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoannotqc.exception.HPOException;

import java.util.ArrayList;
import java.util.List;

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

    public V2SmallFile(OldSmallFile osf) {
        List<OldSmallFileEntry> oldlist = osf.getEntrylist();
        basename=osf.getBasename();

        for (OldSmallFileEntry oldentry : oldlist) {
            try {
                V2SmallFileEntry v2entry = new V2SmallFileEntry(oldentry);
                if (v2entry.getRow().contains("null")) {
                    logger.error("Detected the String \"null\" for " + v2entry.getRow());
                    //System.exit(1);

                }
                entryList.add(v2entry);
            } catch (HPOException e) {
                e.printStackTrace();
            }

        }
    }

    public V2SmallFile(String name, List<V2SmallFileEntry> entries) {
        basename=name;
        entryList=entries;
    }



    public List<V2SmallFileEntry> getEntryList() {
        return entryList;
    }









}
