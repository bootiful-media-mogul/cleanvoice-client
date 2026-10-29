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
		return groups -> groups.filterByName(API_GROUP)
			.forEachClient((group, builder) -> builder.baseUrl(properties.baseUrl().toString())
				.requestInterceptor(new ApiKeyInterceptor(properties.apiKey())));
	}

	@Bean
	@ConditionalOnMissingBean
	CleanvoiceClient cleanvoiceClient(CleanvoiceEditsApi edits, CleanvoiceUploadsApi uploads,
			CleanvoiceAccountApi account, SignedUrlStorageApi storage, CleanvoiceProperties properties) {
		return new CleanvoiceClient(edits, uploads, account, storage, properties.polling());
	}

}
