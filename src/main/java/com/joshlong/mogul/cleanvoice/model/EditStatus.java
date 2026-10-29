package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Status values reported by {@code GET /v2/edits/{id}}. Unrecognised values map to
 * {@link #UNKNOWN} rather than failing deserialization, so new worker phases don't break
 * polling.
 */
public enum EditStatus {

	PENDING, STARTED, PREPROCESSING, CLASSIFICATION, EDITING, POSTPROCESSING, EXPORT, RETRY, SUCCESS, FAILURE, UNKNOWN;

	@JsonCreator
	public static EditStatus from(String value) {
		if (value == null) {
			return UNKNOWN;
		}
		try {
			return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			return UNKNOWN;
		}
	}

	/**
	 * {@code SUCCESS} or {@code FAILURE}. Note {@code POSTPROCESSING} is <em>not</em>
	 * terminal.
	 */
	public boolean isTerminal() {
		return this == SUCCESS || this == FAILURE;
	}

}
