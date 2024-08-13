package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;


import static org.monarchinitiative.hpoannotqc.annotations.hpoaerror.HpoaErrorCategory.ANNOTATION_MODEL_ERROR;

/**
 * Exceptions of this class are thrown for serious errors in the model, e.g., wrong header
 * @author Peter Robinson
 */
public class HpoAnnotationModelError implements HpoaError {

  private final String message;
    public HpoAnnotationModelError(String msg) {
      this.message = msg;
    }

  @Override
  public HpoaErrorCategory category() {
    return ANNOTATION_MODEL_ERROR;
  }



  @Override
  public String getMessage() {
    return message;
  }

}
