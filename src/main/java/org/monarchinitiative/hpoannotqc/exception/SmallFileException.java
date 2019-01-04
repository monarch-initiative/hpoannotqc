package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.phenol.base.PhenolException;

/**
 * Exceptions of this class are thrown if there are quality control issues with the
 * generation of a SmallFile or SmallFileEntry (single line of an HPO annotation file)
 * @author Peter Robinson
 */
public class SmallFileException extends PhenolException {
    public SmallFileException(String msg) {super(msg);}

}
