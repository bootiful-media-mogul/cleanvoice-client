package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Body of {@code POST /v2/edits}.
 */
public record EditRequest(Input input) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Input(List<String> files, @JsonProperty("upload_type") String uploadType, EditConfig config) {
	}

	/** A single audio or video file reachable at a direct-download URL. */
	public static EditRequest of(String fileUrl, EditConfig config) {
		return new EditRequest(new Input(List.of(fileUrl), null, config));
	}

	/**
	 * Several tracks of the <em>same</em> recording (e.g. host mic + guest mic). This is
	 * not batch processing: send one request per independent file.
	 */
	public static EditRequest multitrack(List<String> trackUrls, EditConfig config) {
		return new EditRequest(new Input(List.copyOf(trackUrls), "multitrack", config));
	}
}
