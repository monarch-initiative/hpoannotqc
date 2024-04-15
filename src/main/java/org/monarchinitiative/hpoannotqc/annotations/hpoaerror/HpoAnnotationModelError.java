package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;


/**
 * Exceptions of this class are thrown for serious errors in the model, e.g., wrong header
 * @author Peter Robinson
 */
public class HpoAnnotationModelError implements HpoaError {
  private final String disease;

  private final String message;
    public HpoAnnotationModelError(String disease, String msg) {
      this.disease = disease;
      this.message = msg;
    }

  @Override
  public String getDisease() {
    return disease;
  }

  @Override
  public String getMessage() {
    return message;
  }

}
