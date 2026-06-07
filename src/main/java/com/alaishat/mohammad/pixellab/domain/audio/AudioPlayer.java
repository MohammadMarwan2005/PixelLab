package com.alaishat.mohammad.pixellab.domain.audio;

/**
 * Domain port for in-memory audio preview playback (Req. 2). Implemented by
 * infrastructure on top of {@code javax.sound.sampled.Clip} so the domain and
 * view models stay free of Java Sound types.
 *
 * @see com.alaishat.mohammad.pixellab.infrastructure.audio.JavaSoundAudioPlayer
 */
public interface AudioPlayer {

    /** Loads and starts playing {@code audio} from the beginning. */
    void play(AudioBuffer audio);

    /** Pauses playback, retaining position. No-op if not playing. */
    void pause();

    /** Resumes playback from the paused position. No-op if not paused. */
    void resume();

    /** Stops playback and releases the loaded clip. */
    void stop();

    boolean isPlaying();

    double positionSeconds();

    double durationSeconds();

    /** Invoked on the playback thread when playback reaches the end naturally. */
    void setOnFinished(Runnable callback);
}
