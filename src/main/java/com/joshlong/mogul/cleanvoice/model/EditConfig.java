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
 * @param fillers remove filler words ("um", "uh")
 * @param longSilences shorten long silences
 * @param mouthSounds remove mouth sounds
 * @param breath remove or soften breathing
 * @param stutters remove stutters
 * @param hesitations remove hesitations
 * @param muted silence edits instead of cutting them, preserving the original timing
 * @param removeNoise remove background noise
 * @param studioSound apply studio sound enhancement
 * @param normalize normalize loudness
 * @param keepMusic keep music when cleaning
 * @param autoeq legacy option; prefer {@link #studioSound()}
 * @param exportFormat the output format
 * @param targetLufs integrated loudness target when {@code normalize} is on
 * @param muteLufs loudness used for muted sections
 * @param exportTimestamps export timestamp markers
 * @param transcription produce a transcript
 * @param summarize produce a summary; also enables transcription
 * @param socialContent produce social posts; also enables summarize
 * @param signedUrl pre-signed PUT URL to deliver the result to
 * @param merge multi-track only: merge all tracks into one output file
 * @param audioForEdl video only: also return uncut enhanced audio for EDL/NLE work
 * @param video must be {@code true} to get an edited video back
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditConfig(
		// audio cleaning
		Boolean fillers, @JsonProperty("long_silences") Boolean longSilences,
		@JsonProperty("mouth_sounds") Boolean mouthSounds, Breath breath, Boolean stutters, Boolean hesitations,
		Boolean muted,
		// audio enhancement
		@JsonProperty("remove_noise") NoiseRemoval removeNoise, @JsonProperty("studio_sound") StudioSound studioSound,
		Boolean normalize, @JsonProperty("keep_music") Boolean keepMusic, @Deprecated Boolean autoeq,
		// output
		@JsonProperty("export_format") ExportFormat exportFormat, @JsonProperty("target_lufs") Double targetLufs,
		@JsonProperty("mute_lufs") Double muteLufs, @JsonProperty("export_timestamps") Boolean exportTimestamps,
		// content generation
		Boolean transcription, Boolean summarize, @JsonProperty("social_content") Boolean socialContent,
		// delivery
		@JsonProperty("signed_url") String signedUrl,
		// advanced
		Boolean merge, @JsonProperty("audio_for_edl") Boolean audioForEdl, Boolean video) {

	/**
	 * Starts a new builder with every option unset.
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Recommended audio enhancement: Remove Noise v2 + Normalize v2.
	 * @return the config
	 */
	public static EditConfig enhance() {
		return builder().removeNoise(NoiseRemoval.ON).normalize(true).build();
	}

	/**
	 * Enhancement plus fillers, silences, mouth sounds, breathing and stutters.
	 * @return the config
	 */
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

		/** {@code true}. */
		ON(true),
		/** {@code "legacy"}. */
		LEGACY("legacy"),
		/** {@code "natural"}. */
		NATURAL("natural"),
		/** {@code false}. */
		OFF(false);

		private final Object value;

		Breath(Object value) {
			this.value = value;
		}

		/**
		 * The value sent to the API.
		 * @return the JSON value
		 */
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

		/** {@code true}: Noise v2, the default. */
		ON(true),
		/** {@code "legacy"}. */
		LEGACY("legacy"),
		/** {@code "v2"}. */
		V2("v2"),
		/** {@code false}. */
		OFF(false);

		private final Object value;

		NoiseRemoval(Object value) {
			this.value = value;
		}

		/**
		 * The value sent to the API.
		 * @return the JSON value
		 */
		@JsonValue
		public Object value() {
			return value;
		}

	}

	/** {@code studio_sound}: {@code true | "nightly" | false}. */
	public enum StudioSound {

		/** {@code true}. */
		ON(true),
		/** {@code "nightly"}. */
		NIGHTLY("nightly"),
		/** {@code false}. */
		OFF(false);

		private final Object value;

		StudioSound(Object value) {
			this.value = value;
		}

		/**
		 * The value sent to the API.
		 * @return the JSON value
		 */
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

		/** Match the input format. */
		AUTO("auto"),
		/** MP3. */
		MP3("mp3"),
		/** WAV. */
		WAV("wav"),
		/** FLAC. */
		FLAC("flac"),
		/** M4A. */
		M4A("m4a"),
		/** Opus. */
		OPUS("opus"),
		/** AAC. */
		AAC("aac");

		private final String value;

		ExportFormat(String value) {
			this.value = value;
		}

		/**
		 * The value sent to the API.
		 * @return the JSON value
		 */
		@JsonValue
		public String value() {
			return value;
		}

	}

	// ---- builder
	// -----------------------------------------------------------------------------

	/** Fluent builder for {@link EditConfig}; unset options are not sent. */
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

		/**
		 * Remove filler words.
		 * @param v the value
		 * @return this builder
		 */
		public Builder fillers(boolean v) {
			this.fillers = v;
			return this;
		}

		/**
		 * Shorten long silences.
		 * @param v the value
		 * @return this builder
		 */
		public Builder longSilences(boolean v) {
			this.longSilences = v;
			return this;
		}

		/**
		 * Remove mouth sounds.
		 * @param v the value
		 * @return this builder
		 */
		public Builder mouthSounds(boolean v) {
			this.mouthSounds = v;
			return this;
		}

		/**
		 * Remove or soften breathing.
		 * @param v the value
		 * @return this builder
		 */
		public Builder breath(Breath v) {
			this.breath = v;
			return this;
		}

		/**
		 * Remove breathing ({@link Breath#ON}) or not ({@link Breath#OFF}).
		 * @param v the value
		 * @return this builder
		 */
		public Builder breath(boolean v) {
			return breath(v ? Breath.ON : Breath.OFF);
		}

		/**
		 * Remove stutters.
		 * @param v the value
		 * @return this builder
		 */
		public Builder stutters(boolean v) {
			this.stutters = v;
			return this;
		}

		/**
		 * Remove hesitations.
		 * @param v the value
		 * @return this builder
		 */
		public Builder hesitations(boolean v) {
			this.hesitations = v;
			return this;
		}

		/**
		 * Silence edits instead of cutting them, preserving the original timing.
		 * @param v the value
		 * @return this builder
		 */
		public Builder muted(boolean v) {
			this.muted = v;
			return this;
		}

		/**
		 * Remove background noise.
		 * @param v the value
		 * @return this builder
		 */
		public Builder removeNoise(NoiseRemoval v) {
			this.removeNoise = v;
			return this;
		}

		/**
		 * Remove background noise ({@link NoiseRemoval#ON}) or not
		 * ({@link NoiseRemoval#OFF}).
		 * @param v the value
		 * @return this builder
		 */
		public Builder removeNoise(boolean v) {
			return removeNoise(v ? NoiseRemoval.ON : NoiseRemoval.OFF);
		}

		/**
		 * Apply studio sound enhancement.
		 * @param v the value
		 * @return this builder
		 */
		public Builder studioSound(StudioSound v) {
			this.studioSound = v;
			return this;
		}

		/**
		 * Apply studio sound ({@link StudioSound#ON}) or not ({@link StudioSound#OFF}).
		 * @param v the value
		 * @return this builder
		 */
		public Builder studioSound(boolean v) {
			return studioSound(v ? StudioSound.ON : StudioSound.OFF);
		}

		/**
		 * Normalize loudness.
		 * @param v the value
		 * @return this builder
		 */
		public Builder normalize(boolean v) {
			this.normalize = v;
			return this;
		}

		/**
		 * Keep music when cleaning.
		 * @param v the value
		 * @return this builder
		 */
		public Builder keepMusic(boolean v) {
			this.keepMusic = v;
			return this;
		}

		/**
		 * @deprecated legacy option slated for removal; use
		 * {@link #studioSound(StudioSound)}.
		 * @param v the value
		 * @return this builder
		 */
		@Deprecated
		public Builder autoeq(boolean v) {
			this.autoeq = v;
			return this;
		}

		/**
		 * The output format.
		 * @param v the value
		 * @return this builder
		 */
		public Builder exportFormat(ExportFormat v) {
			this.exportFormat = v;
			return this;
		}

		/**
		 * Integrated loudness target when {@code normalize} is on. Accepted range: -36 to
		 * -6.
		 * @param v the value
		 * @return this builder
		 */
		public Builder targetLufs(double v) {
			if (v < -36 || v > -6) {
				throw new IllegalArgumentException("target_lufs must be between -36 and -6, was " + v);
			}
			this.targetLufs = v;
			return this;
		}

		/**
		 * Loudness used for muted sections.
		 * @param v the value
		 * @return this builder
		 */
		public Builder muteLufs(double v) {
			this.muteLufs = v;
			return this;
		}

		/**
		 * Export timestamp markers.
		 * @param v the value
		 * @return this builder
		 */
		public Builder exportTimestamps(boolean v) {
			this.exportTimestamps = v;
			return this;
		}

		/**
		 * Produce a transcript.
		 * @param v the value
		 * @return this builder
		 */
		public Builder transcription(boolean v) {
			this.transcription = v;
			return this;
		}

		/**
		 * Also enables transcription server-side.
		 * @param v the value
		 * @return this builder
		 */
		public Builder summarize(boolean v) {
			this.summarize = v;
			return this;
		}

		/**
		 * Also enables summarize (and so transcription) server-side.
		 * @param v the value
		 * @return this builder
		 */
		public Builder socialContent(boolean v) {
			this.socialContent = v;
			return this;
		}

		/**
		 * Pre-signed PUT URL; Cleanvoice uploads the result straight to your storage.
		 * @param v the value
		 * @return this builder
		 */
		public Builder signedUrl(String v) {
			this.signedUrl = v;
			return this;
		}

		/**
		 * Multi-track only: merge all tracks into one output file.
		 * @param v the value
		 * @return this builder
		 */
		public Builder merge(boolean v) {
			this.merge = v;
			return this;
		}

		/**
		 * Video only: also return uncut enhanced audio for EDL/NLE work.
		 * @param v the value
		 * @return this builder
		 */
		public Builder audioForEdl(boolean v) {
			this.audioForEdl = v;
			return this;
		}

		/**
		 * Must be {@code true} to get an edited video back; otherwise input is treated as
		 * audio.
		 * @param v the value
		 * @return this builder
		 */
		public Builder video(boolean v) {
			this.video = v;
			return this;
		}

		/**
		 * Builds the config.
		 * @return the config
		 */
		public EditConfig build() {
			return new EditConfig(fillers, longSilences, mouthSounds, breath, stutters, hesitations, muted, removeNoise,
					studioSound, normalize, keepMusic, autoeq, exportFormat, targetLufs, muteLufs, exportTimestamps,
					transcription, summarize, socialContent, signedUrl, merge, audioForEdl, video);
		}

	}
}
