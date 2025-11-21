package org.monarchinitiative.hpoannotqc.annotations;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Enumeration of supported disease databases in HPO annotations.
 *
 * <p>This enum defines the disease databases that are recognized in HPO annotation files,
 * along with their standard prefixes used in CURIE identifiers.</p>
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public enum DiseaseDatabase {
  MONDO("MONDO"),
  OMIM("OMIM"),
  ORPHANET("ORPHA"),
  DECIPHER("DECIPHER"),
  UNKNOWN("UNKNOWN");

  private final String prefix;

  /**
   * Constructs a disease database enum with its associated prefix.
   *
   * @param prefix the standard prefix used in CURIE identifiers for this database
   */
  DiseaseDatabase(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Gets the standard prefix for this disease database.
   *
   * @return the prefix used in CURIE identifiers (e.g., "OMIM", "ORPHA")
   */
  public String prefix() {
    return prefix;
  }

  /**
   * Converts a string representation to the corresponding DiseaseDatabase enum.
   *
   * @param s string representation of the database name (case-insensitive)
   * @return corresponding DiseaseDatabase enum, or UNKNOWN if not recognized
   */
  public static DiseaseDatabase fromString(String s) {
    switch (s.toUpperCase(Locale.ROOT)) {
      case "MONDO":
        return MONDO;
      case "OMIM":
        return OMIM;
      case "ORPHA":
      case "ORPHANET":
        return ORPHANET;
      case "DECIPHER":
        return DECIPHER;
      default:
        return UNKNOWN;
    }
  }

  /**
   * Gets the set of valid disease databases (excluding UNKNOWN).
   *
   * @return set of valid disease databases used in HPO annotations
   */
  public static Set<DiseaseDatabase> validDiseaseDatabases() {
    return EnumSet.of(MONDO, OMIM, ORPHANET, DECIPHER);
  }

}
