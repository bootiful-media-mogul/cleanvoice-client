package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URI;

/** Response of {@code POST /v2/upload}: {@code {"signedUrl": "..."}}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignedUpload(String signedUrl) {

	/** The URL to PUT the file bytes to (used verbatim, signature intact). */
	public URI uploadUri() {
		return URI.create(signedUrl);
	}

	/**
	 * The URL to pass in {@code input.files}: the signed URL with its query string
	 * stripped.
	 */
	public String fileUrl() {
		int q = signedUrl.indexOf('?');
		return q < 0 ? signedUrl : signedUrl.substring(0, q);
	}
}
