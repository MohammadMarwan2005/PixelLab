package com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel;

import com.alaishat.mohammad.pixellab.domain.audio.AudioEditSession;
import com.alaishat.mohammad.pixellab.domain.audio.AudioLoader;
import com.alaishat.mohammad.pixellab.domain.audio.AudioMetadata;
import com.alaishat.mohammad.pixellab.domain.audio.AudioPlayer;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.LoadAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.ResetAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.SaveAsAudioUseCase;
import com.alaishat.mohammad.pixellab.features.audioworkspace.usecase.SaveAudioUseCase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Central source of truth for the loaded audio file (mirrors {@code
 * ImageWorkspaceViewModel} on the image side). Owns loading, playback preview
 * (Req. 2), Save/Save As/Reset (Reqs. 9 and 11), and the metadata the
 * properties panel displays (Req. 3). {@code AudioCompressionViewModel}
 * observes {@link #editSessionProperty()} to know what to compress.
 */
public final class AudioWorkspaceViewModel {

    private static final Duration POSITION_TICK = Duration.millis(100);

    private final LoadAudioUseCase loadAudio;
    private final ResetAudioUseCase resetAudio;
    private final SaveAudioUseCase saveAudio;
    private final SaveAsAudioUseCase saveAsAudio;
    private final AudioPlayer player;

    private final ObjectProperty<AudioEditSession> editSession = new SimpleObjectProperty<>();
    private final ObjectProperty<AudioMetadata> currentMetadata = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> currentSource = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<Throwable> lastError = new ReadOnlyObjectWrapper<>();

    /** Bumped whenever the working buffer is replaced — same "explicit re-render signal" as the image side. */
    private final ReadOnlyIntegerWrapper workingBufferRevision = new ReadOnlyIntegerWrapper(0);

    private final ReadOnlyBooleanWrapper playing = new ReadOnlyBooleanWrapper(false);
    private final ReadOnlyDoubleWrapper playbackPosition = new ReadOnlyDoubleWrapper(0);
    private final ReadOnlyDoubleWrapper playbackDuration = new ReadOnlyDoubleWrapper(0);
    private final Timeline positionTicker;

    public AudioWorkspaceViewModel(LoadAudioUseCase loadAudio,
                                   ResetAudioUseCase resetAudio,
                                   SaveAudioUseCase saveAudio,
                                   SaveAsAudioUseCase saveAsAudio,
                                   AudioPlayer player) {
        this.loadAudio = Objects.requireNonNull(loadAudio, "loadAudio");
        this.resetAudio = Objects.requireNonNull(resetAudio, "resetAudio");
        this.saveAudio = Objects.requireNonNull(saveAudio, "saveAudio");
        this.saveAsAudio = Objects.requireNonNull(saveAsAudio, "saveAsAudio");
        this.player = Objects.requireNonNull(player, "player");

        this.positionTicker = new Timeline(new KeyFrame(POSITION_TICK, e -> tickPlayback()));
        this.positionTicker.setCycleCount(Timeline.INDEFINITE);

        player.setOnFinished(() -> Platform.runLater(this::onPlaybackFinished));
    }

    public ObjectProperty<AudioEditSession> editSessionProperty() {
        return editSession;
    }

    public ObjectProperty<AudioMetadata> currentMetadataProperty() {
        return currentMetadata;
    }

    public ObjectProperty<Path> currentSourceProperty() {
        return currentSource;
    }

    public ReadOnlyObjectProperty<Throwable> lastErrorProperty() {
        return lastError.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty workingBufferRevisionProperty() {
        return workingBufferRevision.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty playingProperty() {
        return playing.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty playbackPositionProperty() {
        return playbackPosition.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty playbackDurationProperty() {
        return playbackDuration.getReadOnlyProperty();
    }

    public BooleanBinding hasAudioBinding() {
        return Bindings.isNotNull(editSession);
    }

    public void open(Path source) {
        try {
            AudioLoader.LoadedAudio loaded = loadAudio.execute(source);
            stopPlayback();
            AudioEditSession session = new AudioEditSession(loaded.audio(), source, loaded.metadata().encoding());
            editSession.set(session);
            currentMetadata.set(loaded.metadata());
            // Set source last — listeners observing it (e.g. recents, if wired) see
            // the metadata + session already in place when they fire.
            currentSource.set(source);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }

    /** Plays the working buffer (the "current" audio — original, or a decompressed round trip) from the start. */
    public void play() {
        AudioEditSession session = editSession.get();
        if (session == null) return;
        player.play(session.workingBuffer());
        playbackDuration.set(player.durationSeconds());
        playing.set(true);
        positionTicker.playFromStart();
    }

    public void pause() {
        if (!playing.get()) return;
        player.pause();
        playing.set(false);
        positionTicker.stop();
    }

    public void resume() {
        if (playing.get() || editSession.get() == null) return;
        player.resume();
        playing.set(true);
        positionTicker.play();
    }

    public void stop() {
        stopPlayback();
    }

    public void reset() {
        AudioEditSession session = editSession.get();
        if (session == null) return;
        try {
            stopPlayback();
            resetAudio.execute(session);
            republishWorkingBuffer();
            lastError.set(null);
        } catch (RuntimeException e) {
            lastError.set(e);
        }
    }

    public void save() {
        AudioEditSession session = editSession.get();
        if (session == null) return;
        try {
            saveAudio.execute(session);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }

    public void saveAs(Path target, String format) {
        AudioEditSession session = editSession.get();
        if (session == null) return;
        try {
            saveAsAudio.execute(session, target, format);
            lastError.set(null);
        } catch (IOException | RuntimeException e) {
            lastError.set(e);
        }
    }

    /**
     * Signals that the session's working buffer has been replaced (e.g. after a
     * decompress round trip, or Reset) so observers — playback, waveform,
     * compression — recompute against the new buffer.
     */
    public void republishWorkingBuffer() {
        workingBufferRevision.set(workingBufferRevision.get() + 1);
    }

    private void tickPlayback() {
        playbackPosition.set(player.positionSeconds());
    }

    private void onPlaybackFinished() {
        playing.set(false);
        positionTicker.stop();
        playbackPosition.set(playbackDuration.get());
    }

    private void stopPlayback() {
        player.stop();
        playing.set(false);
        positionTicker.stop();
        playbackPosition.set(0);
        playbackDuration.set(0);
    }
}
