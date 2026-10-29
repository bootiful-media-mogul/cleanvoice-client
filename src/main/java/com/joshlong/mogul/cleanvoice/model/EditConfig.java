package com.joshlong.mogul.cleanvoice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@code config} object of an edit request. Every field is optional; {@code null}
 * fields are not sent, so the server-side default applies.
 *
 * @see <a href="https://docs.cleanvoice.ai/docs/v2/rest/configuration">Configuration
 * Reference</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditConfig(
		// audio cleaning
		Boolean fillers, @JsonProperty("long_silences") Boolean longSilences,
		@JsonProperty("mouth_sounds") Boolean mouthSounds, Breath breath, Boolean stutters, Boolean hesitations,
		Boolean muted,
		// audio enhancement
		@JsonProperty("remove_noise") NoiseRemoval removeNoise, @JsonProperty("studio_sound") StudioSound studioSound,
		Boolean normalize, @JsonProperty("keep_music") Boolean keepMusic,
		/**
		 * @deprecated legacy option; prefer {@link #studioSound()}. */
		@Deprecated Boolean autoeq,
		// output
		@JsonProperty("export_format") ExportFormat exportFormat, @JsonProperty("target_lufs") Double targetLufs,
		@JsonProperty("mute_lufs") Double muteLufs, @JsonProperty("export_timestamps") Boolean exportTimestamps,
		// content generation
		Boolean transcription, Boolean summarize, @JsonProperty("social_content") Boolean socialContent,
		// delivery
		@JsonProperty("signed_url") String signedUrl,
		// advanced
		Boolean merge, @JsonProperty("audio_for_edl") Boolean audioForEdl, Boolean video) {

	public static Builder builder() {
		return new Builder();
	}

	/** Recommended audio enhancement: Remove Noise v2 + Normalize v2. */
	public static EditConfig enhance() {
		return builder().removeNoise(NoiseRemoval.ON).normalize(true).build();
	}

	/** Enhancement plus fillers, silences, mouth sounds, breathing and stutters. */
	public static EditConfig fullPodcastEdit() {
		return builder().removeNoise(NoiseRemoval.ON)
			.normalize(true)
			.fillers(true)
			.longSilences(true)
			.mouthSounds(true)
			.breath(Breath.ON)
			.stutters(true)
			.build();
	}

	// ---- boolean | string options
	// ------------------------------------------------------------

	/** {@code breath}: {@code true | "legacy" | "natural" | false}. */
	public enum Breath {

		ON(true), LEGACY("legacy"), NATURAL("natural"), OFF(false);

		private final Object value;

		Breath(Object value) {
			this.value = value;
		}

		@JsonValue
		public Object value() {
			return value;
		}

	}

	/**
	 * {@code remove_noise}: {@code true} (Noise v2, the default)
	 * {@code | "legacy" | "v2" | false}.
	 */
	public enum NoiseRemoval {

		ON(true), LEGACY("legacy"), V2("v2"), OFF(false);

		private final Object value;

		NoiseRemoval(Object value) {
			this.value = value;
		}

		@JsonValue
		public Object value() {
			return value;
		}

	}

	/** {@code studio_sound}: {@code true | "nightly" | false}. */
	public enum StudioSound {

		ON(true), NIGHTLY("nightly"), OFF(false);

		private final Object value;

		StudioSound(Object value) {
			this.value = value;
		}

		@JsonValue
		public Object value() {
			return value;
		}

	}

	/**
	 * {@code export_format}. Applies to audio-only jobs; video keeps its original
	 * container.
	 */
	public enum ExportFormat {

		AUTO("auto"), MP3("mp3"), WAV("wav"), FLAC("flac"), M4A("m4a"), OPUS("opus"), AAC("aac");

		private final String value;

		ExportFormat(String value) {
			this.value = value;
		}

		@JsonValue
		public String value() {
			return value;
		}

	}

	// ---- builder
	// -----------------------------------------------------------------------------

	public static final class Builder {

		private Boolean fillers, longSilences, mouthSounds, stutters, hesitations, muted;

		private Breath breath;

		private NoiseRemoval removeNoise;

		private StudioSound studioSound;

		private Boolean normalize, keepMusic, autoeq;

		private ExportFormat exportFormat;

		private Double targetLufs, muteLufs;

		private Boolean exportTimestamps, transcription, summarize, socialContent;

		private String signedUrl;

		private Boolean merge, audioForEdl, video;

		private Builder() {
		}

		public Builder fillers(boolean v) {
			this.fillers = v;
			return this;
		}

		public Builder longSilences(boolean v) {
			this.longSilences = v;
			return this;
		}

		public Builder mouthSounds(boolean v) {
			this.mouthSounds = v;
			return this;
		}

		public Builder breath(Breath v) {
			this.breath = v;
			return this;
		}

		public Builder breath(boolean v) {
			return breath(v ? Breath.ON : Breath.OFF);
		}

		public Builder stutters(boolean v) {
			this.stutters = v;
			return this;
		}

		public Builder hesitations(boolean v) {
			this.hesitations = v;
			return this;
		}

		/** Silence edits instead of cutting them, preserving the original timing. */
		public Builder muted(boolean v) {
			this.muted = v;
			return this;
		}

		public Builder removeNoise(NoiseRemoval v) {
			this.removeNoise = v;
			return this;
		}

		public Builder removeNoise(boolean v) {
			return removeNoise(v ? NoiseRemoval.ON : NoiseRemoval.OFF);
		}

		public Builder studioSound(StudioSound v) {
			this.studioSound = v;
			return this;
		}

		public Builder studioSound(boolean v) {
			return studioSound(v ? StudioSound.ON : StudioSound.OFF);
		}

		public Builder normalize(boolean v) {
			this.normalize = v;
			return this;
		}

		public Builder keepMusic(boolean v) {
			this.keepMusic = v;
			return this;
		}

		/**
		 * @deprecated legacy option slated for removal; use
		 * {@link #studioSound(StudioSound)}.
		 */
		@Deprecated
		public Builder autoeq(boolean v) {
			this.autoeq = v;
			return this;
		}

		public Builder exportFormat(ExportFormat v) {
			this.exportFormat = v;
			return this;
		}

		/**
		 * Integrated loudness target when {@code normalize} is on. Accepted range: -36 to
		 * -6.
		 */
		public Builder targetLufs(double v) {
			if (v < -36 || v > -6) {
				throw new IllegalArgumentException("target_lufs must be between -36 and -6, was " + v);
			}
			this.targetLufs = v;
			return this;
		}

		public Builder muteLufs(double v) {
			this.muteLufs = v;
			return this;
		}

		public Builder exportTimestamps(boolean v) {
			this.exportTimestamps = v;
			return this;
		}

		public Builder transcription(boolean v) {
			this.transcription = v;
			return this;
		}

		/** Also enables transcription server-side. */
		public Builder summarize(boolean v) {
			this.summarize = v;
			return this;
		}

		/** Also enables summarize (and so transcription) server-side. */
		public Builder socialContent(boolean v) {
			this.socialContent = v;
			return this;
		}

		/** Pre-signed PUT URL; Cleanvoice uploads the result straight to your storage. */
		public Builder signedUrl(String v) {
			this.signedUrl = v;
			return this;
		}

		/** Multi-track only: merge all tracks into one output file. */
		public Builder merge(boolean v) {
			this.merge = v;
			return this;
		}

		/** Video only: also return uncut enhanced audio for EDL/NLE work. */
		public Builder audioForEdl(boolean v) {
			this.audioForEdl = v;
			return this;
		}

		/**
		 * Must be {@code true} to get an edited video back; otherwise input is treated as
		 * audio.
		 */
		public Builder video(boolean v) {
			this.video = v;
			return this;
		}

		public EditConfig build() {
			return new EditConfig(fillers, longSilences, mouthSounds, breath, stutters, hesitations, muted, removeNoise,
					studioSound, normalize, keepMusic, autoeq, exportFormat, targetLufs, muteLufs, exportTimestamps,
					transcription, summarize, socialContent, signedUrl, merge, audioForEdl, video);
		}

	}
}
