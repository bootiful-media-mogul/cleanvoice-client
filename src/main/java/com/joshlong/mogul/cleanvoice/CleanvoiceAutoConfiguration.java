package com.joshlong.mogul.cleanvoice;

import com.joshlong.mogul.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.mogul.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.mogul.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.mogul.cleanvoice.api.SignedUrlStorageApi;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Registers two HTTP service groups:
 * <ul>
 * <li>{@value #API_GROUP} — the Cleanvoice REST API, with base URL and the {@code X-API-Key}
 * interceptor applied by the group configurer below;</li>
 * <li>{@value #STORAGE_GROUP} — PUTs to pre-signed storage URLs: no base URL, no API key, no
 * interceptor (so large files stream instead of being buffered).</li>
 * </ul>
 * Each interface becomes an injectable bean, and {@link CleanvoiceClient} composes them.
 */
@AutoConfiguration
@EnableConfigurationProperties(CleanvoiceProperties.class)
@ImportHttpServices(group = CleanvoiceAutoConfiguration.API_GROUP,
        types = { CleanvoiceEditsApi.class, CleanvoiceUploadsApi.class, CleanvoiceAccountApi.class })
@ImportHttpServices(group = CleanvoiceAutoConfiguration.STORAGE_GROUP,
        types = SignedUrlStorageApi.class)
public class CleanvoiceAutoConfiguration {

    public static final String API_GROUP = "cleanvoice";

    public static final String STORAGE_GROUP = "cleanvoice-storage";

    @Bean
    RestClientHttpServiceGroupConfigurer cleanvoiceGroupConfigurer(CleanvoiceProperties properties) {
        return groups -> groups
                .filterByName(API_GROUP)
                .forEachClient((group, builder) -> builder
                        .baseUrl(properties.baseUrl().toString())
                        .requestInterceptor(new ApiKeyInterceptor(properties.apiKey())));
    }

    @Bean
    @ConditionalOnMissingBean
    CleanvoiceClient cleanvoiceClient(CleanvoiceEditsApi edits, CleanvoiceUploadsApi uploads,
            CleanvoiceAccountApi account, SignedUrlStorageApi storage, CleanvoiceProperties properties) {
        return new CleanvoiceClient(edits, uploads, account, storage, properties.polling());
    }
}
