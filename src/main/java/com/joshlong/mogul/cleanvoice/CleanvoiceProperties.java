package com.joshlong.mogul.cleanvoice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;
import java.time.Duration;

/**
 * <pre>
 * cleanvoice.api-key=${CLEANVOICE_API_KEY}
 * cleanvoice.base-url=https://api.cleanvoice.ai      # default
 * cleanvoice.polling.initial-delay=30s                # default, per the docs
 * cleanvoice.polling.interval=10s                     # default, per the docs
 * </pre>
 */
@ConfigurationProperties("cleanvoice")
public record CleanvoiceProperties(
        String apiKey,
        @DefaultValue("https://api.cleanvoice.ai") URI baseUrl,
        @DefaultValue Polling polling) {

    public record Polling(
            @DefaultValue("30s") Duration initialDelay,
            @DefaultValue("10s") Duration interval,
            /** Upper bound for exponential backoff after a rate-limit response. */
            @DefaultValue("30s") Duration maxBackoff) {
    }
}
