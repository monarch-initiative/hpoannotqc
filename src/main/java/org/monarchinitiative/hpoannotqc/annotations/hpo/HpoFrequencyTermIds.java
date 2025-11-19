package org.monarchinitiative.hpoannotqc.annotations.hpo;

import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Constants for HPO frequency sub-ontology term identifiers.
 *
 * <p>This utility class provides {@link TermId} constants for all frequency terms
 * used in HPO annotations. These terms describe how often a phenotype occurs
 * in patients with a particular disease.</p>
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:sebastian.koehler@charite.de">Sebastian Koehler</a>
 * @author <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 */
public final class HpoFrequencyTermIds {

  /** {@link TermId} for "frequency". */
  public static final TermId FREQUENCY = TermId.of("HP:0040279");

  /** {@link TermId} for "always present (100% of the cases)". */
  public static final TermId OBLIGATE = TermId.of("HP:0040280");

  /** {@link TermId} for "very frequent (80-99% of the cases)". */
  public static final TermId VERY_FREQUENT = TermId.of("HP:0040281");

  /** {@link TermId} for "frequent (30-79% of the cases)". */
  public static final TermId FREQUENT = TermId.of("HP:0040282");

  /** {@link TermId} for "occasional (5-29% of the cases)". */
  public static final TermId OCCASIONAL = TermId.of("HP:0040283");

  /** {@link TermId} for "excluded (1-4% of the cases)". */
  public static final TermId VERY_RARE = TermId.of("HP:0040284");

  /** {@link TermId} for "excluded (0% of the cases)". */
  public static final TermId EXCLUDED = TermId.of("HP:0040285");

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private HpoFrequencyTermIds() {}
}
