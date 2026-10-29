package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Response of {@code GET /v2/edits/{id}}. {@link #result()} is only populated once
 * {@code SUCCESS}.
 *
 * @param taskId the edit id
 * @param status the current status
 * @param result the result, or {@code null} until {@code SUCCESS}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Edit(@JsonProperty("task_id") String taskId, EditStatus status, Result result) {

	/**
	 * Whether the job finished successfully.
	 * @return {@code true} if the status is {@code SUCCESS}
	 */
	public boolean isSuccess() {
		return status == EditStatus.SUCCESS;
	}

	/**
	 * Whether the job failed.
	 * @return {@code true} if the status is {@code FAILURE}
	 */
	public boolean isFailure() {
		return status == EditStatus.FAILURE;
	}

	/**
	 * Whether the job has finished, successfully or not.
	 * @return {@code true} if the status is terminal
	 */
	public boolean isDone() {
		return status != null && status.isTerminal();
	}

	/**
	 * The output of a successful edit.
	 *
	 * @param video whether the output is a video
	 * @param filename the output file name
	 * @param downloadUrl time-limited link to the cleaned file; download it promptly
	 * @param transcription the transcript, if requested
	 * @param summarization the summary, if requested
	 * @param socialContentRaw empty array when not requested, an object
	 * (newsletter/twitter_thread/linkedin) when it is; see {@link #socialContent()}
	 * @param mergedAudioUrl the merged audio, for multi-track jobs with {@code merge}
	 * @param timestampsMarkersUrls timestamp marker files, if requested
	 * @param waveformResult waveform data
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(Boolean video, String filename, @JsonProperty("download_url") String downloadUrl,
			Transcription transcription, Summarization summarization,
			@JsonProperty("social_content") Object socialContentRaw,
			@JsonProperty("merged_audio_url") Object mergedAudioUrl,
			@JsonProperty("timestamps_markers_urls") Object timestampsMarkersUrls,
			@JsonProperty("waveform_result") Object waveformResult) {

		/**
		 * Typed view of {@code social_content}, empty when it wasn't requested or isn't
		 * ready.
		 * @return the social content, if present
		 */
		public Optional<SocialContent> socialContent() {
			if (socialContentRaw instanceof Map<?, ?> map && !map.isEmpty()) {
				return Optional.of(new SocialContent(asString(map.get("newsletter")),
						asString(map.get("twitter_thread")), asString(map.get("linkedin"))));
			}
			return Optional.empty();
		}

		private static String asString(Object o) {
			return o == null ? null : o.toString();
		}
	}

	/**
	 * A transcript.
	 *
	 * @param paragraphs the transcript as paragraphs
	 * @param detail word-level detail plus speaker-labelled paragraphs
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Transcription(List<Paragraph> paragraphs, @JsonProperty("transcription") Detail detail) {

		/**
		 * Word-level transcript detail.
		 *
		 * @param words every word, with timings
		 * @param paragraphs speaker-labelled paragraphs
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Detail(List<Word> words, List<Paragraph> paragraphs) {
		}
	}

	/**
	 * A transcript span. {@code text} is set on top-level paragraphs, {@code speaker} on
	 * detail ones.
	 *
	 * @param id the paragraph id
	 * @param start start time, in seconds
	 * @param end end time, in seconds
	 * @param text the text, on top-level paragraphs
	 * @param speaker the speaker label, on detail paragraphs
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Paragraph(Integer id, Double start, Double end, String text, String speaker) {
	}

	/**
	 * A single transcribed word.
	 *
	 * @param id the word id
	 * @param start start time, in seconds
	 * @param end end time, in seconds
	 * @param text the word
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Word(Integer id, Double start, Double end, String text) {
	}

	/**
	 * Generated summary content.
	 *
	 * @param title a suggested title
	 * @param summary the summary
	 * @param chapters chapter markers
	 * @param summaries per-section summaries
	 * @param keyLearnings key takeaways
	 * @param summaryOfSummary a shorter summary
	 * @param episodeDescription an episode description, in Markdown
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Summarization(String title, String summary, List<Chapter> chapters, List<String> summaries,
			@JsonProperty("key_learnings") String keyLearnings,
			@JsonProperty("summary_of_summary") String summaryOfSummary,
			@JsonProperty("episode_description") String episodeDescription) {

		/**
		 * A chapter marker.
		 *
		 * @param start start time, in seconds
		 * @param title the chapter title
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Chapter(Double start, String title) {
		}
	}

	/**
	 * {@code twitter_thread} is returned as {@code <tweet>…</tweet>} blocks; all fields
	 * are markdown-ish text.
	 *
	 * @param newsletter a newsletter post
	 * @param twitterThread a Twitter/X thread
	 * @param linkedin a LinkedIn post
	 */
	public record SocialContent(String newsletter, String twitterThread, String linkedin) {
	}
}
