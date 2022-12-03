package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationEntry;
import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModel;
import org.monarchinitiative.phenol.base.PhenolException;

/**
 * Exceptions of this class are thrown if there are quality control issues with the
 * generation of a {@link HpoAnnotationModel} or
 * {@link HpoAnnotationEntry} (single line of an HPO annotation file)
 * @author Peter Robinson
 */
public class HpoAnnotationModelException extends PhenolException {
  private final static long serialVersionUID = 2;
    public HpoAnnotationModelException(String msg) {super(msg);}
}
