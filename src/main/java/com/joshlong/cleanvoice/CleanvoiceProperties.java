package com.joshlong.cleanvoice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;
import java.time.Duration;

/**
 * Configuration under the {@code cleanvoice} prefix.
 *
 * <pre>
 * cleanvoice.api-key=${CLEANVOICE_API_KEY}
 * cleanvoice.base-url=https://api.cleanvoice.ai      # default
 * cleanvoice.polling.initial-delay=30s                # default, per the docs
 * cleanvoice.polling.interval=10s                     # default, per the docs
 * </pre>
 *
 * @param apiKey the Cleanvoice API key, sent as the {@code X-API-Key} header
 * @param baseUrl the API base URL
 * @param polling how {@link CleanvoiceClient#awaitCompletion} polls for results
 */
@ConfigurationProperties("cleanvoice")
public record CleanvoiceProperties(String apiKey, @DefaultValue("https://api.cleanvoice.ai") URI baseUrl,
		@DefaultValue Polling polling) {

	/**
	 * Polling settings for {@link CleanvoiceClient#awaitCompletion}.
	 *
	 * @param initialDelay how long to wait before the first status check
	 * @param interval how long to wait between status checks
	 * @param maxBackoff upper bound for exponential backoff after a rate-limit response
	 */
	public record Polling(@DefaultValue("30s") Duration initialDelay, @DefaultValue("10s") Duration interval,
			@DefaultValue("30s") Duration maxBackoff) {
	}
}
