package com.joshlong.mogul.cleanvoice;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Adds the {@code X-API-Key} header to every request made by the Cleanvoice API group.
 */
public final class ApiKeyInterceptor implements ClientHttpRequestInterceptor {

	public static final String HEADER = "X-API-Key";

	private final String apiKey;

	public ApiKeyInterceptor(String apiKey) {
		Assert.hasText(apiKey, "cleanvoice.api-key must be set (e.g. cleanvoice.api-key=${CLEANVOICE_API_KEY})");
		this.apiKey = apiKey;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		request.getHeaders().set(HEADER, apiKey);
		return execution.execute(request, body);
	}

}
