package com.joshlong.mogul.cleanvoice.api;

import com.joshlong.mogul.cleanvoice.model.Account;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Key verification and remaining credit. Note this still lives under {@code /v1}.
 *
 * @see <a href=
 * "https://docs.cleanvoice.ai/docs/v2/rest/authentication">Authentication</a>
 */
@HttpExchange(accept = "application/json")
public interface CleanvoiceAccountApi {

	/**
	 * {@code GET /v1/account} — 200 means the key is valid; 401 means it was rejected.
	 */
	@GetExchange("/v1/account")
	Account account();

}
