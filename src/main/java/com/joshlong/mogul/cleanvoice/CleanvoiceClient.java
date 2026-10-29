package com.joshlong.mogul.cleanvoice;

import com.joshlong.mogul.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.mogul.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.mogul.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.mogul.cleanvoice.api.SignedUrlStorageApi;
import com.joshlong.mogul.cleanvoice.model.Account;
import com.joshlong.mogul.cleanvoice.model.Edit;
import com.joshlong.mogul.cleanvoice.model.EditConfig;
import com.joshlong.mogul.cleanvoice.model.EditCreated;
import com.joshlong.mogul.cleanvoice.model.EditRequest;
import com.joshlong.mogul.cleanvoice.model.SignedUpload;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Convenience facade over the declarative Cleanvoice interfaces: the three-step upload
 * flow, and polling until a job finishes. The underlying interfaces are beans too, if you
 * need raw access.
 */
public class CleanvoiceClient {

	private final CleanvoiceEditsApi edits;

	private final CleanvoiceUploadsApi uploads;

	private final CleanvoiceAccountApi account;

	private final SignedUrlStorageApi storage;

	private final CleanvoiceProperties.Polling polling;

	/**
	 * Creates a client from the underlying HTTP interfaces.
	 * @param edits the edits API
	 * @param uploads the uploads API
	 * @param account the account API
	 * @param storage the pre-signed storage API
	 * @param polling polling settings for {@link #awaitCompletion}
	 */
	public CleanvoiceClient(CleanvoiceEditsApi edits, CleanvoiceUploadsApi uploads, CleanvoiceAccountApi account,
			SignedUrlStorageApi storage, CleanvoiceProperties.Polling polling) {
		this.edits = edits;
		this.uploads = uploads;
		this.account = account;
		this.storage = storage;
		this.polling = polling;
	}

	// ---- account
	// -----------------------------------------------------------------------------

	/**
	 * Verifies the key and returns remaining credit. Throws
	 * {@code HttpClientErrorException.Unauthorized} on a bad key.
	 * @return the account, including remaining credit
	 */
	public Account account() {
		return account.account();
	}

	// ---- uploads
	// -----------------------------------------------------------------------------

	/**
	 * Uploads a local file (steps 1 and 2 of the upload flow) and returns the URL to pass
	 * in {@code input.files}.
	 * @param file the local file to upload
	 * @return the URL of the uploaded file
	 */
	public String upload(Path file) {
		Assert.isTrue(Files.isRegularFile(file), () -> "Not a readable file: " + file);
		return upload(new FileSystemResource(file), file.getFileName().toString());
	}

	/**
	 * Uploads any {@link Resource} (classpath, byte array, stream…) under the given file
	 * name.
	 * @param resource the content to upload
	 * @param filename the file name; also used to infer the content type
	 * @return the URL of the uploaded file
	 */
	public String upload(Resource resource, String filename) {
		Assert.hasText(filename, "filename must not be empty");
		SignedUpload signed = uploads.requestUploadUrl(filename);
		MediaType contentType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
		storage.put(signed.uploadUri(), contentType.toString(), resource);
		return signed.fileUrl();
	}

	// ---- edits
	// -------------------------------------------------------------------------------

	/**
	 * Submits a raw edit request.
	 * @param request the request body
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(EditRequest request) {
		return edits.create(request);
	}

	/**
	 * Submits a job for a file already reachable at a public / direct-download URL.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(String fileUrl, EditConfig config) {
		return edits.create(EditRequest.of(fileUrl, config));
	}

	/**
	 * Uploads a local file, then submits the edit (all three steps).
	 * @param localFile the local file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(Path localFile, EditConfig config) {
		return createEdit(upload(localFile), config);
	}

	/**
	 * Uploads each local track and submits them as one multi-track edit.
	 * @param localTracks the tracks of a single recording
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createMultitrackEdit(List<Path> localTracks, EditConfig config) {
		List<String> urls = localTracks.stream().map(this::upload).toList();
		return edits.create(EditRequest.multitrack(urls, config));
	}

	/**
	 * Fetches the current state of an edit.
	 * @param editId the edit id
	 * @return the edit's status and, once {@code SUCCESS}, its result
	 */
	public Edit getEdit(String editId) {
		return edits.retrieve(editId);
	}

	/**
	 * Permanently deletes the edit and its files (otherwise they expire 7 days after
	 * processing).
	 * @param editId the edit id
	 */
	public void deleteEdit(String editId) {
		edits.delete(editId);
	}

	/**
	 * Polls until the job reaches {@code SUCCESS} or {@code FAILURE}: waits the initial
	 * delay (default 30s), then polls every interval (default 10s). Rate-limit responses
	 * back off exponentially up to {@code maxBackoff}.
	 * @param editId the edit id
	 * @param timeout how long to wait in total
	 * @return the finished edit
	 * @throws CleanvoiceEditFailedException if the job fails
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public Edit awaitCompletion(String editId, Duration timeout) throws InterruptedException, TimeoutException {
		Instant deadline = Instant.now().plus(timeout);
		sleepUntil(deadline, polling.initialDelay());
		Duration backoff = Duration.ofSeconds(1);
		while (true) {
			try {
				Edit edit = edits.retrieve(editId);
				backoff = Duration.ofSeconds(1);
				if (edit.isSuccess()) {
					return edit;
				}
				if (edit.isFailure()) {
					throw new CleanvoiceEditFailedException(edit);
				}
				sleepUntil(deadline, polling.interval());
			}
			catch (HttpClientErrorException.TooManyRequests ex) {
				sleepUntil(deadline, backoff);
				backoff = min(backoff.multipliedBy(2), polling.maxBackoff());
			}
		}
	}

	/**
	 * Upload, submit, and wait — the whole flow in one call.
	 * @param localFile the local file
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws CleanvoiceEditFailedException if the job fails
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public Edit process(Path localFile, EditConfig config, Duration timeout)
			throws InterruptedException, TimeoutException {
		return awaitCompletion(createEdit(localFile, config).id(), timeout);
	}

	private static void sleepUntil(Instant deadline, Duration pause) throws InterruptedException, TimeoutException {
		if (Instant.now().plus(pause).isAfter(deadline)) {
			throw new TimeoutException("Cleanvoice edit did not finish before the deadline");
		}
		Thread.sleep(pause.toMillis());
	}

	private static Duration min(Duration a, Duration b) {
		return a.compareTo(b) <= 0 ? a : b;
	}

}
