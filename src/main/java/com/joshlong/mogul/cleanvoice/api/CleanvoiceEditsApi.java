package com.joshlong.mogul.cleanvoice.api;

import com.joshlong.mogul.cleanvoice.model.Edit;
import com.joshlong.mogul.cleanvoice.model.EditCreated;
import com.joshlong.mogul.cleanvoice.model.EditRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Edit jobs: create, poll, delete.
 *
 * @see <a href="https://docs.cleanvoice.ai/docs/v2/rest/edits/create">Create an Edit</a>
 * @see <a href="https://docs.cleanvoice.ai/docs/v2/rest/edits/retrieve">Retrieve an
 * Edit</a>
 * @see <a href="https://docs.cleanvoice.ai/docs/v2/rest/delete-files">Delete Files</a>
 */
@HttpExchange(url = "/v2/edits", accept = "application/json")
public interface CleanvoiceEditsApi {

	/**
	 * {@code POST /v2/edits} — submits an asynchronous edit job.
	 * @param request the request body
	 * @return the id of the new edit
	 */
	@PostExchange(contentType = "application/json")
	EditCreated create(@RequestBody EditRequest request);

	/**
	 * {@code GET /v2/edits/{id}} — current status and, once {@code SUCCESS}, the result.
	 * @param editId the edit id
	 * @return the edit
	 */
	@GetExchange("/{editId}")
	Edit retrieve(@PathVariable String editId);

	/**
	 * {@code DELETE /v2/edits/{id}} — permanently deletes the edit and its
	 * source/processed files.
	 * @param editId the edit id
	 */
	@DeleteExchange("/{editId}")
	void delete(@PathVariable String editId);

}
