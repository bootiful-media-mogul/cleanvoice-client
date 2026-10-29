package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

/** Response of {@code GET /v1/account}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Account(Credit credit, Map<String, Object> meta) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Credit(BigDecimal total, BigDecimal subscription, BigDecimal payg) {
	}
}
