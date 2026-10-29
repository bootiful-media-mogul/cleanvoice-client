package com.joshlong.cleanvoice;

import com.joshlong.cleanvoice.CleanvoiceClient;
import com.joshlong.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.cleanvoice.api.SignedUrlStorageApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "cleanvoice.api-key=test-key")
class CleanvoiceClientApplicationTests {

	@Autowired
	ApplicationContext context;

	@Test
	void contextLoads() {
		assertThat(this.context.getBean(CleanvoiceClient.class)).isNotNull();
		assertThat(this.context.getBean(CleanvoiceEditsApi.class)).isNotNull();
		assertThat(this.context.getBean(CleanvoiceUploadsApi.class)).isNotNull();
		assertThat(this.context.getBean(CleanvoiceAccountApi.class)).isNotNull();
		assertThat(this.context.getBean(SignedUrlStorageApi.class)).isNotNull();
	}

}
