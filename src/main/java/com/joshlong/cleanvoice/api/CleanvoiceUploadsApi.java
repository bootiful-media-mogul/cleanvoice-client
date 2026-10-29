package com.joshlong.cleanvoice.api;

import com.joshlong.cleanvoice.model.SignedUpload;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Step 1 of the local-file upload flow.
 *
 * @see <a href="https://docs.cleanvoice.ai/docs/v2/rest/uploads">Uploads</a>
 */
@HttpExchange(accept = "application/json")
public interface CleanvoiceUploadsApi {

	/**
	 * {@code POST /v2/upload?filename=...} — returns a pre-signed PUT URL for the file.
	 * @param filename the name of the file to upload
	 * @return the pre-signed upload URL
	 */
	@PostExchange("/v2/upload")
	SignedUpload requestUploadUrl(@RequestParam String filename);

}
