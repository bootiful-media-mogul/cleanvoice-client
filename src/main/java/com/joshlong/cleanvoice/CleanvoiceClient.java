package com.joshlong.cleanvoice;

import com.joshlong.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.cleanvoice.api.SignedUrlStorageApi;
import com.joshlong.cleanvoice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Convenience facade over the declarative Cleanvoice interfaces: the three-step upload
 * flow, and polling until a job finishes. The underlying interfaces are beans too, if you
 * need raw access.
 */
public class CleanvoiceClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

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
		this.log.debug("CleanvoiceClient ready: initial-delay={}, interval={}, max-backoff={}",
				this.polling.initialDelay(), this.polling.interval(), this.polling.maxBackoff());
	}

	// ---- account
	// -----------------------------------------------------------------------------

	/**
	 * Verifies the key and returns remaining credit. Throws
	 * {@code HttpClientErrorException.Unauthorized} on a bad key.
	 * @return the account, including remaining credit
	 */
	public Account account() {
		var result = this.account.account();
		if (this.log.isDebugEnabled()) {
			var credit = result.credit();
			this.log.debug("Account credit: total={}, subscription={}, payg={}", credit == null ? null : credit.total(),
					credit == null ? null : credit.subscription(), credit == null ? null : credit.payg());
		}
		return result;
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
		return this.upload(new FileSystemResource(file));
	}

	/**
	 * Uploads a {@link Resource} under its own {@link Resource#getFilename() file name}.
	 * Works for file, classpath and URL resources; for resources without a name (byte
	 * arrays, streams) use {@link #upload(Resource, String)}.
	 * @param resource the content to upload
	 * @return the URL of the uploaded file
	 */
	public String upload(Resource resource) {
		Assert.notNull(resource, "resource must not be null");
		Assert.isTrue(resource.isReadable(), () -> "Not a readable resource: " + resource);
		var filename = resource.getFilename();
		Assert.hasText(filename, () -> "Resource has no file name, use upload(Resource, String): " + resource);
		return this.upload(resource, filename);
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
		var contentType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
		this.log.debug("Uploading '{}' ({}, {}) from {}", filename, contentType, describeSize(resource), resource);
		var started = System.nanoTime();
		var signed = this.uploads.requestUploadUrl(filename);
		// the query string holds the signature, so only log the host
		this.log.debug("Got pre-signed upload URL for '{}' on host {}", filename, signed.uploadUri().getHost());
		this.storage.put(signed.uploadUri(), contentType.toString(), resource);
		var fileUrl = signed.fileUrl();
		this.log.debug("Uploaded '{}' in {}; file URL is {}", filename, since(started), fileUrl);
		return fileUrl;
	}

	// ---- edits
	// -------------------------------------------------------------------------------

	/**
	 * Submits a raw edit request.
	 * @param request the request body
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(EditRequest request) {
		return this.submit(request);
	}

	/**
	 * Submits a job for a file already reachable at a public / direct-download URL.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(String fileUrl, EditConfig config) {
		return this.submit(EditRequest.of(fileUrl, config));
	}

	/**
	 * Submits a job for a file at the given URI. {@code http}/{@code https} URIs are
	 * passed straight to Cleanvoice with no upload; anything else (e.g. {@code file:}) is
	 * uploaded first.
	 * @param fileUri the URI of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(URI fileUri, EditConfig config) {
		Assert.notNull(fileUri, "fileUri must not be null");
		return this.createEdit(UrlResource.from(fileUri), config);
	}

	/**
	 * Submits a job for a file at the given URL. {@code http}/{@code https} URLs are
	 * passed straight to Cleanvoice with no upload; anything else (e.g. {@code file:}) is
	 * uploaded first.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(URL fileUrl, EditConfig config) {
		Assert.notNull(fileUrl, "fileUrl must not be null");
		return this.createEdit(new UrlResource(fileUrl), config);
	}

	/**
	 * Uploads a local file, then submits the edit (all three steps).
	 * @param localFile the local file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(Path localFile, EditConfig config) {
		return this.createEdit(new FileSystemResource(localFile), config);
	}

	/**
	 * Uploads a {@link Resource}, then submits the edit (all three steps). The resource
	 * must have a file name; see {@link #upload(Resource)}. A {@link UrlResource} with an
	 * {@code http}/{@code https} URL is not uploaded: its URL is passed to Cleanvoice
	 * directly.
	 * @param resource the content to edit
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createEdit(Resource resource, EditConfig config) {
		return this.createEdit(this.fileUrlFor(resource), config);
	}

	/**
	 * Uploads each local track and submits them as one multi-track edit.
	 * @param localTracks the tracks of a single recording
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createMultitrackEdit(List<Path> localTracks, EditConfig config) {
		return this.createMultitrackEdit(localTracks.stream().map(FileSystemResource::new).toList(), config);
	}

	/**
	 * Uploads each track and submits them as one multi-track edit. Each resource must
	 * have a file name; see {@link #upload(Resource)}. {@link UrlResource}s with an
	 * {@code http}/{@code https} URL are passed through without uploading.
	 * @param tracks the tracks of a single recording
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	public EditCreated createMultitrackEdit(Collection<? extends Resource> tracks, EditConfig config) {
		var urls = tracks.stream().map(this::fileUrlFor).toList();
		this.log.debug("Submitting {} tracks as one multi-track edit", urls.size());
		return this.submit(EditRequest.multitrack(urls, config));
	}

	/**
	 * Fetches the current state of an edit.
	 * @param editId the edit id
	 * @return the edit's status and, once {@code SUCCESS}, its result
	 */
	public Edit getEdit(String editId) {
		var edit = this.edits.retrieve(editId);
		this.log.debug("Edit {} is {}", editId, edit.status());
		return edit;
	}

	/**
	 * Permanently deletes the edit and its files (otherwise they expire 7 days after
	 * processing).
	 * @param editId the edit id
	 */
	public void deleteEdit(String editId) {
		this.edits.delete(editId);
		this.log.debug("Deleted edit {}", editId);
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
		var started = System.nanoTime();
		var deadline = Instant.now().plus(timeout);
		this.log.debug("Waiting for edit {} (timeout {}); first poll in {}", editId, timeout,
				this.polling.initialDelay());
		this.sleepUntil(editId, deadline, this.polling.initialDelay());
		var backoff = Duration.ofSeconds(1);
		var lastStatus = (EditStatus) null;
		var polls = 0;
		while (true) {
			try {
				polls++;
				var edit = this.edits.retrieve(editId);
				backoff = Duration.ofSeconds(1);
				if (edit.status() != lastStatus) {
					this.log.debug("Edit {} is now {} (poll #{}, {} elapsed)", editId, edit.status(), polls,
							since(started));
					lastStatus = edit.status();
				}
				else {
					this.log.trace("Edit {} still {} (poll #{}, {} elapsed)", editId, edit.status(), polls,
							since(started));
				}
				if (edit.status() == EditStatus.UNKNOWN) {
					this.log.debug(
							"Edit {} returned a status this client doesn't recognise; treating it as in progress",
							editId);
				}
				if (edit.isSuccess()) {
					this.log.debug("Edit {} succeeded after {} polls in {}; download URL present: {}", editId, polls,
							since(started), edit.result() != null && edit.result().downloadUrl() != null);
					return edit;
				}
				if (edit.isFailure()) {
					this.log.debug("Edit {} failed after {} polls in {}", editId, polls, since(started));
					throw new CleanvoiceEditFailedException(edit);
				}
				this.sleepUntil(editId, deadline, this.polling.interval());
			}
			catch (HttpClientErrorException.TooManyRequests ex) {
				this.log.debug("Rate limited while polling edit {}; backing off {}", editId, backoff);
				this.sleepUntil(editId, deadline, backoff);
				backoff = min(backoff.multipliedBy(2), this.polling.maxBackoff());
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
		return this.process(new FileSystemResource(localFile), config, timeout);
	}

	/**
	 * Submit and wait for a file at the given URI; {@code http}/{@code https} URIs are
	 * not uploaded. See {@link #createEdit(URI, EditConfig)}.
	 * @param fileUri the URI of the file
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws CleanvoiceEditFailedException if the job fails
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public Edit process(URI fileUri, EditConfig config, Duration timeout)
			throws InterruptedException, TimeoutException {
		return this.awaitCompletion(this.createEdit(fileUri, config).id(), timeout);
	}

	/**
	 * Submit and wait for a file at the given URL; {@code http}/{@code https} URLs are
	 * not uploaded. See {@link #createEdit(URL, EditConfig)}.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws CleanvoiceEditFailedException if the job fails
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public Edit process(URL fileUrl, EditConfig config, Duration timeout)
			throws InterruptedException, TimeoutException {
		return this.awaitCompletion(this.createEdit(fileUrl, config).id(), timeout);
	}

	/**
	 * Upload, submit, and wait — the whole flow in one call. The resource must have a
	 * file name; see {@link #upload(Resource)}. An {@code http}/{@code https}
	 * {@link UrlResource} is not uploaded.
	 * @param resource the content to edit
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws CleanvoiceEditFailedException if the job fails
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public Edit process(Resource resource, EditConfig config, Duration timeout)
			throws InterruptedException, TimeoutException {
		return this.awaitCompletion(this.createEdit(resource, config).id(), timeout);
	}

	/**
	 * Returns the URL to hand Cleanvoice for this resource: an {@code http}/{@code https}
	 * {@link UrlResource} is already reachable, so its URL is used as-is; anything else
	 * (including {@code file:} and {@code jar:} URL resources) is uploaded.
	 */
	private String fileUrlFor(Resource resource) {
		Assert.notNull(resource, "resource must not be null");
		if (resource instanceof UrlResource urlResource) {
			try {
				var uri = urlResource.getURI();
				var scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
				if (scheme.equals("http") || scheme.equals("https")) {
					this.log.debug("{} is already reachable over {}; passing the URL to Cleanvoice without uploading",
							uri, scheme);
					return uri.toString();
				}
				this.log.debug("{} uses the '{}' scheme, which Cleanvoice can't reach; uploading it", uri, scheme);
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}
		return this.upload(resource);
	}

	private EditCreated submit(EditRequest request) {
		if (this.log.isDebugEnabled()) {
			var input = request.input();
			this.log.debug("Submitting edit: files={}, upload-type={}, config={}", input.files(), input.uploadType(),
					input.config());
		}
		var created = this.edits.create(request);
		this.log.debug("Created edit {}", created.id());
		return created;
	}

	private void sleepUntil(String editId, Instant deadline, Duration pause)
			throws InterruptedException, TimeoutException {
		if (Instant.now().plus(pause).isAfter(deadline)) {
			this.log.debug("Giving up on edit {}: waiting another {} would pass the deadline", editId, pause);
			throw new TimeoutException("Cleanvoice edit " + editId + " did not finish before the deadline");
		}
		Thread.sleep(pause.toMillis());
	}

	private static String describeSize(Resource resource) {
		try {
			return resource.contentLength() + " bytes";
		}
		catch (IOException | RuntimeException ex) {
			return "unknown size";
		}
	}

	private static Duration since(long startedNanos) {
		return Duration.ofMillis((System.nanoTime() - startedNanos) / 1_000_000);
	}

	private static Duration min(Duration a, Duration b) {
		return a.compareTo(b) <= 0 ? a : b;
	}

}
