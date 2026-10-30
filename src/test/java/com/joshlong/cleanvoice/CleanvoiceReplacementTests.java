package com.joshlong.cleanvoice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * an application with more than one key, say one per user, brings its own
 * {@link Cleanvoice} and doesn't configure {@code cleanvoice.api-key} at all.
 */
class CleanvoiceReplacementTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class, CleanvoiceAutoConfiguration.class));

	@Configuration(proxyBeanMethods = false)
	static class OwnCleanvoice {

		@Bean
		Cleanvoice multitenantCleanvoice() {
			return (Cleanvoice) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Cleanvoice.class },
					(proxy, method, args) -> {
						throw new UnsupportedOperationException(method.getName());
					});
		}

	}

	@Test
	void theAutoConfiguredClientIsTheCleanvoiceByDefault() {
		this.contextRunner.withPropertyValues("cleanvoice.api-key=test-key").run(context -> {
			assertThat(context).hasSingleBean(Cleanvoice.class);
			assertThat(context.getBean(Cleanvoice.class)).isInstanceOf(CleanvoiceClient.class);
		});
	}

	@Test
	void anApplicationsOwnCleanvoiceReplacesTheClient() {
		this.contextRunner.withPropertyValues("cleanvoice.api-key=test-key")
			.withUserConfiguration(OwnCleanvoice.class)
			.run(context -> {
				assertThat(context).hasSingleBean(Cleanvoice.class);
				assertThat(context).doesNotHaveBean(CleanvoiceClient.class);
			});
	}

	@Test
	void anApplicationsOwnCleanvoiceNeedsNoKey() {
		this.contextRunner.withUserConfiguration(OwnCleanvoice.class).run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context).hasSingleBean(Cleanvoice.class);
		});
	}

}
