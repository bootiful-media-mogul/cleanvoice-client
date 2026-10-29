package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of {@code POST /v2/edits}: {@code {"id": "edit_abc123"}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EditCreated(String id) {
}
