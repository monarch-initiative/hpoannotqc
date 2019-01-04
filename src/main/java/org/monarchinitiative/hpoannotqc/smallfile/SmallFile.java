package org.monarchinitiative.hpoannotqc.smallfile;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.*;

/**
 * This class represents one disease-entity annotation  consisting usually of multiple annotations lines, and using
 * the new format introduced in 2018.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * Created by peter on 1/20/2018.
 */
public class SmallFile {
    private static final Logger logger = LogManager.getLogger();
    /** The base name of the V2 file, which is the same as the v1 small file. */
    private final String basename;
    /** List of {@link SmallFileEntry} objects representing the original lines of the small file */
    private final List<SmallFileEntry> entryList;

    private enum Database {OMIM,DECIPHER,UNKNOWN}

    /** What is the source of this entry? */
    private final Database database;

    public String getBasename() {
        return basename;
    }

    /** The constructor creates an immutable copy of the original list of {@link SmallFileEntry} objects
     * provided by the parser
     * @param name Name of the "small file"
     * @param entries List of {@link SmallFileEntry} objects -- one per line of the small file.
     */
    public SmallFile(String name, List<SmallFileEntry> entries) {
        basename=name;
        entryList = ImmutableList.copyOf(entries);
        if (basename.contains("OMIM")) this.database=Database.OMIM;
        else if (basename.contains("DECIPHER")) this.database=Database.DECIPHER;
        else this.database=Database.UNKNOWN;
    }


    public boolean isOMIM(){ return this.database.equals(Database.OMIM); }
    public boolean isDECIPHER() { return this.database.equals(Database.DECIPHER);}



    /** @return the {@link SmallFileEntry} objects -- one per line of the small file.*/
    public List<SmallFileEntry> getEntryList() {
        return entryList;
    }

    public int getNumberOfAnnotations() { return entryList.size(); }


}
