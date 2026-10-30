package com.joshlong.cleanvoice;

import com.joshlong.cleanvoice.model.Account;
import com.joshlong.cleanvoice.model.Edit;
import com.joshlong.cleanvoice.model.EditConfig;
import com.joshlong.cleanvoice.model.EditCreated;
import com.joshlong.cleanvoice.model.EditRequest;
import org.springframework.core.io.Resource;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Everything the client can do against the Cleanvoice API. {@link CleanvoiceClient} is
 * the implementation, bound to one API key; the auto-configuration registers one unless
 * the application defines its own {@code Cleanvoice} bean, for example one that picks a
 * different key for each user of a multi-tenant application. See
 * {@link CleanvoiceClient#create} for building a client per key.
 */
public interface Cleanvoice {

	/**
	 * Verifies the key and returns remaining credit.
	 * @return the account, including remaining credit
	 */
	Account account();

	/**
	 * Uploads a local file and returns the URL to pass in {@code input.files}.
	 * @param file the local file to upload
	 * @return the URL of the uploaded file
	 */
	String upload(Path file);

	/**
	 * Uploads a {@link Resource} under its own file name.
	 * @param resource the content to upload
	 * @return the URL of the uploaded file
	 */
	String upload(Resource resource);

	/**
	 * Uploads any {@link Resource} under the given file name.
	 * @param resource the content to upload
	 * @param filename the file name; also used to infer the content type
	 * @return the URL of the uploaded file
	 */
	String upload(Resource resource, String filename);

	/**
	 * Submits a raw edit request.
	 * @param request the request body
	 * @return the id of the new edit
	 */
	EditCreated createEdit(EditRequest request);

	/**
	 * Submits a job for a file already reachable at a public or direct-download URL.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createEdit(String fileUrl, EditConfig config);

	/**
	 * Submits a job for a file at the given URI, uploading it first unless it's
	 * {@code http} or {@code https}.
	 * @param fileUri the URI of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createEdit(URI fileUri, EditConfig config);

	/**
	 * Submits a job for a file at the given URL, uploading it first unless it's
	 * {@code http} or {@code https}.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createEdit(URL fileUrl, EditConfig config);

	/**
	 * Uploads a local file, then submits the edit.
	 * @param localFile the local file
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createEdit(Path localFile, EditConfig config);

	/**
	 * Uploads a {@link Resource}, then submits the edit.
	 * @param resource the content to edit
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createEdit(Resource resource, EditConfig config);

	/**
	 * Uploads each local track and submits them as one multi-track edit.
	 * @param localTracks the tracks of a single recording
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createMultitrackEdit(List<Path> localTracks, EditConfig config);

	/**
	 * Uploads each track and submits them as one multi-track edit.
	 * @param tracks the tracks of a single recording
	 * @param config the edit settings
	 * @return the id of the new edit
	 */
	EditCreated createMultitrackEdit(Collection<? extends Resource> tracks, EditConfig config);

	/**
	 * Fetches the current state of an edit.
	 * @param editId the edit id
	 * @return the edit's status and, once {@code SUCCESS}, its result
	 */
	Edit getEdit(String editId);

	/**
	 * Permanently deletes the edit and its files.
	 * @param editId the edit id
	 */
	void deleteEdit(String editId);

	/**
	 * Polls until the job reaches {@code SUCCESS} or {@code FAILURE}.
	 * @param editId the edit id
	 * @param timeout how long to wait in total
	 * @return the finished edit
	 * @throws CleanvoiceEditFailedException if the job fails
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	Edit awaitCompletion(String editId, Duration timeout) throws InterruptedException, TimeoutException;

	/**
	 * Upload, submit, and wait.
	 * @param localFile the local file
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 */
	Edit process(Path localFile, EditConfig config, Duration timeout) throws InterruptedException, TimeoutException;

	/**
	 * Submit and wait for a file at the given URI.
	 * @param fileUri the URI of the file
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 */
	Edit process(URI fileUri, EditConfig config, Duration timeout) throws InterruptedException, TimeoutException;

	/**
	 * Submit and wait for a file at the given URL.
	 * @param fileUrl the URL of the file
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 */
	Edit process(URL fileUrl, EditConfig config, Duration timeout) throws InterruptedException, TimeoutException;

	/**
	 * Upload, submit, and wait.
	 * @param resource the content to edit
	 * @param config the edit settings
	 * @param timeout how long to wait for the job to finish
	 * @return the finished edit
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @throws TimeoutException if it isn't done within {@code timeout}
	 */
	Edit process(Resource resource, EditConfig config, Duration timeout) throws InterruptedException, TimeoutException;

}
