package com.joshlong.cleanvoice;

import com.joshlong.cleanvoice.model.Edit;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class EditJsonTests {

	private final JsonMapper mapper = JsonMapper.builder().build();

	@Test
	void emptyArraysReadAsNull() {
		var edit = this.mapper.readValue("""
				{"task_id":"t1","status":"SUCCESS","result":{
				  "download_url":"https://x/y.mp3","summarization":[],"transcription":[],"social_content":[]}}
				""", Edit.class);
		assertThat(edit.result().downloadUrl()).isEqualTo("https://x/y.mp3");
		assertThat(edit.result().summarization()).isNull();
		assertThat(edit.result().transcription()).isNull();
		assertThat(edit.result().socialContent()).isEmpty();
	}

	@Test
	void objectsStillRead() {
		var edit = this.mapper.readValue("""
				{"task_id":"t1","status":"SUCCESS","result":{
				  "summarization":{"title":"Hi","chapters":[{"start":1.5,"title":"Intro"}]},
				  "transcription":{"paragraphs":[{"id":1,"text":"hello"}]}}}
				""", Edit.class);
		assertThat(edit.result().summarization().title()).isEqualTo("Hi");
		assertThat(edit.result().summarization().chapters()).hasSize(1);
		assertThat(edit.result().transcription().paragraphs().get(0).text()).isEqualTo("hello");
	}

}
