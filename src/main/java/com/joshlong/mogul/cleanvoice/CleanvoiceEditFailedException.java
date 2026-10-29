package com.joshlong.mogul.cleanvoice;

import com.joshlong.mogul.cleanvoice.model.Edit;

/** Thrown by {@link CleanvoiceClient#awaitCompletion} when a job ends in {@code FAILURE}. */
public class CleanvoiceEditFailedException extends RuntimeException {

    private final transient Edit edit;

    public CleanvoiceEditFailedException(Edit edit) {
        super("Cleanvoice edit " + edit.taskId() + " failed");
        this.edit = edit;
    }

    public Edit edit() {
        return edit;
    }
}
