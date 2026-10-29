package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response of {@code GET /v1/account}.
 *
 * @param credit remaining credit
 * @param meta other account metadata, as returned by the server
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Account(Credit credit, Map<String, Object> meta) {

	/**
	 * Remaining credit.
	 *
	 * @param total total remaining credit
	 * @param subscription credit from the subscription
	 * @param payg pay-as-you-go credit
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Credit(BigDecimal total, BigDecimal subscription, BigDecimal payg) {
	}
}
