package com.joshlong.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Body of {@code POST /v2/edits}.
 *
 * @param input the files and settings to process
 */
public record EditRequest(Input input) {

	/**
	 * The {@code input} object of an edit request.
	 *
	 * @param files URLs of the files to process
	 * @param uploadType {@code "multitrack"} for multi-track jobs, otherwise {@code null}
	 * @param config the edit settings
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Input(List<String> files, @JsonProperty("upload_type") String uploadType, EditConfig config) {
	}

	/**
	 * A single audio or video file reachable at a direct-download URL.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @return the request
	 */
	public static EditRequest of(String fileUrl, EditConfig config) {
		return new EditRequest(new Input(List.of(fileUrl), null, config));
	}

	/**
	 * Several tracks of the <em>same</em> recording (e.g. host mic + guest mic). This is
	 * not batch processing: send one request per independent file.
	 * @param trackUrls the URLs of the tracks
	 * @param config the edit settings
	 * @return the request
	 */
	public static EditRequest multitrack(List<String> trackUrls, EditConfig config) {
		return new EditRequest(new Input(List.copyOf(trackUrls), "multitrack", config));
	}
}
