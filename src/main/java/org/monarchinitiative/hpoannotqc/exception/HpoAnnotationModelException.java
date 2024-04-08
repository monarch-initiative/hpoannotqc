package org.monarchinitiative.hpoannotqc.exception;



/**
 * Exceptions of this class are thrown for serious errors in the model, e.g., wrong header
 * @author Peter Robinson
 */
public class HpoAnnotationModelException extends HpoAnnotQcException {
  private final static long serialVersionUID = 2;
    public HpoAnnotationModelException(String msg) {super(msg);}
}
