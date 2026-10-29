package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Response of {@code GET /v2/edits/{id}}. {@link #result()} is only populated once {@code SUCCESS}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Edit(
        @JsonProperty("task_id") String taskId,
        EditStatus status,
        Result result) {

    public boolean isSuccess() {
        return status == EditStatus.SUCCESS;
    }

    public boolean isFailure() {
        return status == EditStatus.FAILURE;
    }

    public boolean isDone() {
        return status != null && status.isTerminal();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Boolean video,
            String filename,
            /** Time-limited link to the cleaned file; download it promptly. */
            @JsonProperty("download_url") String downloadUrl,
            Transcription transcription,
            Summarization summarization,
            /** Empty array when not requested, an object (newsletter/twitter_thread/linkedin) when it is. */
            @JsonProperty("social_content") Object socialContentRaw,
            @JsonProperty("merged_audio_url") Object mergedAudioUrl,
            @JsonProperty("timestamps_markers_urls") Object timestampsMarkersUrls,
            @JsonProperty("waveform_result") Object waveformResult) {

        /** Typed view of {@code social_content}, empty when it wasn't requested or isn't ready. */
        public Optional<SocialContent> socialContent() {
            if (socialContentRaw instanceof Map<?, ?> map && !map.isEmpty()) {
                return Optional.of(new SocialContent(
                        asString(map.get("newsletter")),
                        asString(map.get("twitter_thread")),
                        asString(map.get("linkedin"))));
            }
            return Optional.empty();
        }

        private static String asString(Object o) {
            return o == null ? null : o.toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Transcription(
            List<Paragraph> paragraphs,
            /** Word-level detail plus speaker-labelled paragraphs. */
            @JsonProperty("transcription") Detail detail) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Detail(List<Word> words, List<Paragraph> paragraphs) {
        }
    }

    /** A transcript span. {@code text} is set on top-level paragraphs, {@code speaker} on detail ones. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paragraph(Integer id, Double start, Double end, String text, String speaker) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Word(Integer id, Double start, Double end, String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summarization(
            String title,
            String summary,
            List<Chapter> chapters,
            List<String> summaries,
            @JsonProperty("key_learnings") String keyLearnings,
            @JsonProperty("summary_of_summary") String summaryOfSummary,
            /** Markdown. */
            @JsonProperty("episode_description") String episodeDescription) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Chapter(Double start, String title) {
        }
    }

    /** {@code twitter_thread} is returned as {@code <tweet>…</tweet>} blocks; all fields are markdown-ish text. */
    public record SocialContent(String newsletter, String twitterThread, String linkedin) {
    }
}
