package org.monarchinitiative.hpoannotqc.annotations;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum DiseaseDatabase {
  OMIM("OMIM"),
  ORPHANET("ORPHA"),
  DECIPHER("DECIPHER"),
  UNKNOWN("UNKNOWN");

  private final String prefix;

  DiseaseDatabase(String prefix) {
    this.prefix = prefix;
  }

  public String prefix() {
    return prefix;
  }

  public static DiseaseDatabase fromString(String s) {
      return switch (s.toUpperCase(Locale.ROOT)) {
          case "OMIM" -> OMIM;
          case "ORPHA", "ORPHANET" -> ORPHANET;
          case "DECIPHER" -> DECIPHER;
          default -> UNKNOWN;
      };
  }

  public static Set<DiseaseDatabase> allKnownDiseaseDatabases() {
    return EnumSet.of(OMIM, ORPHANET, DECIPHER);
  }

}
