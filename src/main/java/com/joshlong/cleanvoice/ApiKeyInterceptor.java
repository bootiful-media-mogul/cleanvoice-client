package com.joshlong.cleanvoice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Adds the {@code X-API-Key} header to every request made by the Cleanvoice API group,
 * and logs each request and response at debug level (the key itself is never logged).
 */
public final class ApiKeyInterceptor implements ClientHttpRequestInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Name of the header that carries the API key. */
	public static final String HEADER = "X-API-Key";

	private final String apiKey;

	/**
	 * Creates an interceptor that sends the given key.
	 * @param apiKey the Cleanvoice API key; must not be empty
	 */
	public ApiKeyInterceptor(String apiKey) {
		Assert.hasText(apiKey, "cleanvoice.api-key must be set (e.g. cleanvoice.api-key=${CLEANVOICE_API_KEY})");
		this.apiKey = apiKey;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getHeaders().set(HEADER, this.apiKey);
		if (!this.log.isDebugEnabled()) {
			return execution.execute(request, body);
		}
		var started = System.nanoTime();
		try {
			var response = execution.execute(request, body);
			this.log.debug("{} {} -> {} in {} ms", request.getMethod(), request.getURI(),
					response.getStatusCode().value(), (System.nanoTime() - started) / 1_000_000);
			return response;
		}
		catch (IOException ex) {
			this.log.debug("{} {} failed after {} ms: {}", request.getMethod(), request.getURI(),
					(System.nanoTime() - started) / 1_000_000, ex.toString());
			throw ex;
		}
	}

}
