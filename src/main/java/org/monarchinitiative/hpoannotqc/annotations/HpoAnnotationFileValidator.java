package org.monarchinitiative.hpoannotqc.annotations;

import org.monarchinitiative.hpoannotqc.exception.HpoAnnotQcException;

/**
 * Validator utility methods for HPO Annotation Files ("small files").
 */
public class HpoAnnotationFileValidator {
	/**
	 * The column names of the small file.
	 */
	public static final String[] EXPECTED_FIELDS = {
			"#diseaseID",
			"diseaseName",
			"phenotypeID",
			"phenotypeName",
			"onsetID",
			"onsetName",
			"frequency",
			"sex",
			"negation",
			"modifier",
			"description",
			"publication",
			"evidence",
			"biocuration"};
	/**
	 * Number of tab-separated fields in a valid small file.
	 */
	private static final int NUMBER_OF_FIELDS = EXPECTED_FIELDS.length;

	/**
	 * This method checks that the nead has the expected number and order of lines.
	 * If it doesn't, then a serious error has occured somewhere, and it is better to
	 * die and figure out what is wrong than to attempt error correction
	 *
	 * @param line a header line of a small file
	 * @throws HpoAnnotQcException if the header line is malformed
	 */
	public static void qcHeaderLine(String line) throws HpoAnnotQcException  {
		String[] fields = line.split("\t");
		if (fields.length != NUMBER_OF_FIELDS) {
			String msg = String.format("Malformed header line\n" + line +
					"\nExpecting %d fields but got %d", NUMBER_OF_FIELDS, fields.length);
			throw new HpoAnnotQcException(msg);
		}
		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].equals(EXPECTED_FIELDS[i])) {
				throw new HpoAnnotQcException(String.format("Malformed field %d. Expected %s but got %s",
						i, EXPECTED_FIELDS[i], fields[i]));
			}
		}
	}
}
