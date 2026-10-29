package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Status values reported by {@code GET /v2/edits/{id}}. Unrecognised values map to
 * {@link #UNKNOWN} rather than failing deserialization, so new worker phases don't break
 * polling.
 */
public enum EditStatus {

	/** Queued. */
	PENDING,
	/** Picked up by a worker. */
	STARTED,
	/** Preparing the input. */
	PREPROCESSING,
	/** Detecting what to edit. */
	CLASSIFICATION,
	/** Applying edits. */
	EDITING,
	/** Finishing up; not terminal. */
	POSTPROCESSING,
	/** Writing the output. */
	EXPORT,
	/** Being retried after an error. */
	RETRY,
	/** Finished; the result is available. */
	SUCCESS,
	/** Failed. */
	FAILURE,
	/** A value this client doesn't recognise. */
	UNKNOWN;

	/**
	 * Parses a status value case-insensitively.
	 * @param value the raw value, may be {@code null}
	 * @return the matching status, or {@link #UNKNOWN}
	 */
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
	 * @return whether the job has finished
	 */
	public boolean isTerminal() {
		return this == SUCCESS || this == FAILURE;
	}

}
