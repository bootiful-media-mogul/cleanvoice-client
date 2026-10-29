package com.joshlong.mogul.cleanvoice;

import com.joshlong.mogul.cleanvoice.api.CleanvoiceAccountApi;
import com.joshlong.mogul.cleanvoice.api.CleanvoiceEditsApi;
import com.joshlong.mogul.cleanvoice.api.CleanvoiceUploadsApi;
import com.joshlong.mogul.cleanvoice.api.SignedUrlStorageApi;
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
		assertThat(context.getBean(CleanvoiceClient.class)).isNotNull();
		assertThat(context.getBean(CleanvoiceEditsApi.class)).isNotNull();
		assertThat(context.getBean(CleanvoiceUploadsApi.class)).isNotNull();
		assertThat(context.getBean(CleanvoiceAccountApi.class)).isNotNull();
		assertThat(context.getBean(SignedUrlStorageApi.class)).isNotNull();
	}

}
