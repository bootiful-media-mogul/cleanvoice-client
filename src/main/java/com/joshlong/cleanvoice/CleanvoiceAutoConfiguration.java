package com.joshlong.cleanvoice;

import com.joshlong.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.cleanvoice.api.SignedUrlStorageApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Registers two HTTP service groups:
 * <ul>
 * <li>{@value #API_GROUP} — the Cleanvoice REST API, with base URL and the
 * {@code X-API-Key} interceptor applied by the group configurer below;</li>
 * <li>{@value #STORAGE_GROUP} — PUTs to pre-signed storage URLs: no base URL, no API key,
 * no interceptor (so large files stream instead of being buffered).</li>
 * </ul>
 * Each interface becomes an injectable bean, and {@link CleanvoiceClient} composes them.
 * <p>
 * An application that defines its own {@link Cleanvoice} bean, for example one choosing a
 * key per user, replaces the client; it then doesn't need {@code cleanvoice.api-key} at
 * all.
 */
@AutoConfiguration
@EnableConfigurationProperties(CleanvoiceProperties.class)
@ImportHttpServices(group = CleanvoiceAutoConfiguration.API_GROUP,
		types = { CleanvoiceEditsApi.class, CleanvoiceUploadsApi.class, CleanvoiceAccountApi.class })
@ImportHttpServices(group = CleanvoiceAutoConfiguration.STORAGE_GROUP, types = SignedUrlStorageApi.class)
public class CleanvoiceAutoConfiguration {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/** Name of the HTTP service group for the Cleanvoice REST API. */
	public static final String API_GROUP = "cleanvoice";

	/** Name of the HTTP service group for uploads to pre-signed storage URLs. */
	public static final String STORAGE_GROUP = "cleanvoice-storage";

	@Bean
	RestClientHttpServiceGroupConfigurer cleanvoiceGroupConfigurer(CleanvoiceProperties properties) {
		this.log.debug("Configuring HTTP service group '{}' with base URL {}; API key {}", API_GROUP,
				properties.baseUrl(), StringUtils.hasText(properties.apiKey()) ? "is set" : "is MISSING");
		// no key is fine when the application brings its own Cleanvoice bean; without
		// one,
		// every request is rejected by Cleanvoice and the log line above says why
		var apiKey = properties.apiKey();
		return groups -> groups.filterByName(API_GROUP).forEachClient((group, builder) -> {
			builder.baseUrl(properties.baseUrl().toString());
			if (StringUtils.hasText(apiKey))
				builder.requestInterceptor(new ApiKeyInterceptor(apiKey));
		});
	}

	@Bean
	@ConditionalOnMissingBean(Cleanvoice.class)
	CleanvoiceClient cleanvoiceClient(CleanvoiceEditsApi edits, CleanvoiceUploadsApi uploads,
			CleanvoiceAccountApi account, SignedUrlStorageApi storage, CleanvoiceProperties properties) {
		return new CleanvoiceClient(edits, uploads, account, storage, properties.polling());
	}

}
