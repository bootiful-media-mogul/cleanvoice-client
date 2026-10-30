package com.joshlong.cleanvoice;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * one client per key, for applications with more than one: the key goes to the api and
 * nowhere else.
 */
class CleanvoiceClientFactoryTests {

	private static final CleanvoiceProperties.Polling POLLING = new CleanvoiceProperties.Polling(Duration.ZERO,
			Duration.ZERO, Duration.ZERO);

	@Test
	void sendsItsOwnKeyToTheApi() {
		var builder = RestClient.builder();
		var server = MockRestServiceServer.bindTo(builder).build();
		server.expect(requestTo("https://api.example.com/v1/account"))
			.andExpect(header(ApiKeyInterceptor.HEADER, "key-for-one-user"))
			.andRespond(withSuccess("{\"credit\":{\"total\":12.0}}", MediaType.APPLICATION_JSON));

		var client = CleanvoiceClient.create(builder, "key-for-one-user", URI.create("https://api.example.com"),
				POLLING);

		assertThat(client.account().credit().total()).isEqualByComparingTo("12");
		server.verify();
	}

	@Test
	void neverSendsTheKeyToTheStorageHost() {
		var builder = RestClient.builder();
		var server = MockRestServiceServer.bindTo(builder).build();
		server.expect(requestTo("https://api.example.com/v2/upload?filename=episode.mp3"))
			.andExpect(header(ApiKeyInterceptor.HEADER, "a-key"))
			.andRespond(withSuccess("{\"signedUrl\":\"https://storage.example.com/episode.mp3?signature=abc\"}",
					MediaType.APPLICATION_JSON));
		server.expect(requestTo("https://storage.example.com/episode.mp3?signature=abc"))
			.andExpect(method(HttpMethod.PUT))
			.andExpect(headerDoesNotExist(ApiKeyInterceptor.HEADER))
			.andRespond(withSuccess());

		var client = CleanvoiceClient.create(builder, "a-key", URI.create("https://api.example.com"), POLLING);
		var url = client.upload(new ByteArrayResource(new byte[] { 1, 2, 3 }), "episode.mp3");

		assertThat(url).isEqualTo("https://storage.example.com/episode.mp3");
		server.verify();
	}

	@Test
	void doesNotChangeTheBuilderItWasGiven() {
		var builder = RestClient.builder();
		CleanvoiceClient.create(builder, "a-key", URI.create("https://api.example.com"), POLLING);
		var server = MockRestServiceServer.bindTo(builder).build();
		server.expect(requestTo("https://elsewhere.example.com/"))
			.andExpect(headerDoesNotExist(ApiKeyInterceptor.HEADER))
			.andRespond(withSuccess());
		builder.build().get().uri("https://elsewhere.example.com/").retrieve().toBodilessEntity();
		server.verify();
	}

}
