package com.joshlong.cleanvoice.api;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PutExchange;

import java.net.URI;

/**
 * Step 2 of the upload flow: PUT the raw bytes to the pre-signed storage URL.
 * <p>
 * This interface lives in its own HTTP service group on purpose:
 * <ul>
 * <li>the target is a third-party storage host, so the {@code X-API-Key} must never be
 * sent there;</li>
 * <li>a {@code ClientHttpRequestInterceptor} buffers the whole request body in memory,
 * which is fine for small JSON payloads but not for hour-long audio/video files. Without
 * an interceptor the {@link Resource} body is streamed.</li>
 * </ul>
 * The {@link URI} argument is absolute and used verbatim, so the signature in the query
 * string is not re-encoded.
 */
public interface SignedUrlStorageApi {

	/**
	 * PUTs the file to the pre-signed URL.
	 * @param signedUrl the absolute pre-signed URL, used verbatim
	 * @param contentType the file's content type
	 * @param file the file content, streamed
	 */
	@PutExchange
	void put(URI signedUrl, @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType, @RequestBody Resource file);

}
