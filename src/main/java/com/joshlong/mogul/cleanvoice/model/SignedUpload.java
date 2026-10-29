package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URI;

/**
 * Response of {@code POST /v2/upload}: {@code {"signedUrl": "..."}}.
 *
 * @param signedUrl the pre-signed PUT URL, including its signature
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignedUpload(String signedUrl) {

	/**
	 * The URL to PUT the file bytes to (used verbatim, signature intact).
	 * @return the upload URL
	 */
	public URI uploadUri() {
		return URI.create(signedUrl);
	}

	/**
	 * The URL to pass in {@code input.files}: the signed URL with its query string
	 * stripped.
	 * @return the file URL
	 */
	public String fileUrl() {
		int q = signedUrl.indexOf('?');
		return q < 0 ? signedUrl : signedUrl.substring(0, q);
	}
}
