package com.joshlong.mogul.cleanvoice;

import com.joshlong.mogul.cleanvoice.model.Edit;

/**
 * Thrown by {@link CleanvoiceClient#awaitCompletion} when a job ends in {@code FAILURE}.
 */
public class CleanvoiceEditFailedException extends RuntimeException {

	private final transient Edit edit;

	/**
	 * Creates an exception for the given failed edit.
	 * @param edit the edit, as last reported by the server
	 */
	public CleanvoiceEditFailedException(Edit edit) {
		super("Cleanvoice edit " + edit.taskId() + " failed");
		this.edit = edit;
	}

	/**
	 * Returns the failed edit.
	 * @return the edit, as last reported by the server
	 */
	public Edit edit() {
		return edit;
	}

}
