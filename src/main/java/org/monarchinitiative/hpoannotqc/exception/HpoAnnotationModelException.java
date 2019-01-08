package org.monarchinitiative.hpoannotqc.exception;

import org.monarchinitiative.phenol.base.PhenolException;

/**
 * Exceptions of this class are thrown if there are quality control issues with the
 * generation of a HpoAnnotationModel or HpoAnnotationEntry (single line of an HPO annotation file)
 * @author Peter Robinson
 */
public class HpoAnnotationModelException extends PhenolException {
    public HpoAnnotationModelException(String msg) {super(msg);}

}
