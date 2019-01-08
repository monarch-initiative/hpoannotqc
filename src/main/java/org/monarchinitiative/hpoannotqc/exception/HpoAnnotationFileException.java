package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.phenol.base.PhenolException;

/**
 * Exceptions of this class are thrown if there are quality control issues with the
 * generation of a HpoAnnotationModel or HpoAnnotationFileEntry (single line of an HPO annotation file)
 * @author Peter Robinson
 */
public class HpoAnnotationFileException extends PhenolException {
    public HpoAnnotationFileException(String msg) {super(msg);}

}
