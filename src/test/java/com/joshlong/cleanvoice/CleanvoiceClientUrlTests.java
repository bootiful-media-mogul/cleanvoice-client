package com.joshlong.cleanvoice;

import com.joshlong.cleanvoice.CleanvoiceClient;
import com.joshlong.cleanvoice.CleanvoiceProperties;
import com.joshlong.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.cleanvoice.api.SignedUrlStorageApi;
import com.joshlong.cleanvoice.model.EditConfig;
import com.joshlong.cleanvoice.model.EditCreated;
import com.joshlong.cleanvoice.model.EditRequest;
import com.joshlong.cleanvoice.model.SignedUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.UrlResource;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CleanvoiceClientUrlTests {

	private static final String REMOTE = "https://example.com/episode.mp3";

	private final CleanvoiceEditsApi edits = mock(CleanvoiceEditsApi.class);

	private final CleanvoiceUploadsApi uploads = mock(CleanvoiceUploadsApi.class);

	private final SignedUrlStorageApi storage = mock(SignedUrlStorageApi.class);

	private CleanvoiceClient client;

	@BeforeEach
	void setUp() {
		when(this.edits.create(any())).thenReturn(new EditCreated("edit-1"));
		this.client = new CleanvoiceClient(this.edits, this.uploads, mock(CleanvoiceAccountApi.class), this.storage,
				new CleanvoiceProperties.Polling(Duration.ZERO, Duration.ZERO, Duration.ZERO));
	}

	@Test
	void httpUriIsNotUploaded() throws Exception {
		this.client.createEdit(URI.create(REMOTE), EditConfig.enhance());
		this.assertSubmitted(REMOTE);
		verifyNoInteractions(this.uploads, this.storage);
	}

	@Test
	void httpUrlIsNotUploaded() throws Exception {
		this.client.createEdit(URI.create(REMOTE).toURL(), EditConfig.enhance());
		this.assertSubmitted(REMOTE);
		verifyNoInteractions(this.uploads, this.storage);
	}

	@Test
	void httpUrlResourceIsNotUploaded() {
		this.client.createMultitrackEdit(List.of(UrlResource.from(REMOTE)), EditConfig.enhance());
		this.assertSubmitted(REMOTE);
		verifyNoInteractions(this.uploads, this.storage);
	}

	@Test
	void fileUriIsUploaded(@TempDir Path dir) throws Exception {
		var file = Files.writeString(dir.resolve("local.mp3"), "audio");
		when(this.uploads.requestUploadUrl(anyString()))
			.thenReturn(new SignedUpload("https://bucket.example.com/local.mp3?sig=abc"));
		this.client.createEdit(file.toUri(), EditConfig.enhance());
		this.assertSubmitted("https://bucket.example.com/local.mp3");
		verify(this.uploads).requestUploadUrl("local.mp3");
	}

	private void assertSubmitted(String url) {
		var request = ArgumentCaptor.forClass(EditRequest.class);
		verify(this.edits).create(request.capture());
		assertThat(request.getValue().input().files()).containsExactly(url);
	}

}
