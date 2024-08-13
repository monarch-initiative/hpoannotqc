package org.monarchinitiative.hpoannotqc.annotations.hpoaerror;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class TermIdError implements HpoaError {


    private final String message;
    private final HpoaErrorCategory category;


    public TermIdError(String msg, HpoaErrorCategory cat) {
        message = msg;
        category = cat;
    }



  @Override
    public HpoaErrorCategory category() {
        return category;
    }


    @Override
    public String getMessage() {
        return message;
    }

    public static TermIdError termIdNotInOntology(TermId tid) {
      String err = String.format("Could not find term id \"%s\" in Ontology", tid.getValue());
      return new TermIdError(err, HpoaErrorCategory.TERM_ID_NOT_IN_ONTOLOGY);
    }

    public static TermIdError idDoesNotMatchPrimary(TermId tid, TermId primary) {
      String err = String.format("Term id \"%s\" does not match primary id \"%s\".",
              tid.getValue(), primary.getValue());
      return new TermIdError(err, HpoaErrorCategory.OBSOLETE_TERM_ID);
    }

  public static HpoaError labelDoesNotMatchPrimary(String hpoLabel, String primaryLabel) {
    String err = String.format("Term label \"%s\" does not match primary label \"%s\".",
            hpoLabel, primaryLabel);
    return new TermIdError(err, HpoaErrorCategory.OBSOLETE_TERM_LABEL);
  }

  public static HpoaError malformed(String termIdString) {
    String err = String.format("Malformed term identifier \"%s\".",
            termIdString);
    return new TermIdError(err, HpoaErrorCategory.MALFORMED_TERM_ID);
  }

  public static HpoaError invalidModifierId(String termIdString) {
        String err = String.format("Term id \"%s\" is not in Clinical modifier subontology.", termIdString);
      return new TermIdError(err, HpoaErrorCategory.INVALID_MODIFIER_TERMID);
  }

  public static HpoaError nullTermIdError(String message) {

        return new TermIdError(message, HpoaErrorCategory.NULL_TERM_ID);
  }


}


