package com.joshlong.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response of {@code POST /v2/edits}: {@code {"id": "edit_abc123"}}.
 *
 * @param id the id of the new edit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EditCreated(String id) {
}
