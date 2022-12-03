package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.phenol.annotations.hpo.HpoAnnotationModelException;

public class ObsoleteTermIdException extends HpoAnnotationModelException {
  private final static long serialVersionUID = 2;
    public ObsoleteTermIdException(String msg) {super(msg);}
}
