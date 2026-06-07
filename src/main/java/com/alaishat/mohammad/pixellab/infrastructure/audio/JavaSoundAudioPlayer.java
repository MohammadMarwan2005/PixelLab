package com.alaishat.mohammad.pixellab.infrastructure.audio;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import com.alaishat.mohammad.pixellab.domain.audio.AudioPlayer;
import com.alaishat.mohammad.pixellab.infrastructure.io.PcmInterop;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;

/**
 * In-memory preview playback (Req. 2) on top of {@code javax.sound.sampled.Clip}
 * — the JDK's whole-buffer-in-memory player, a natural fit since PixelLab
 * already holds the entire decoded file in an {@link AudioBuffer}.
 *
 * <p>{@code Clip} has no native pause: the standard trick is {@code stop()}
 * (which retains {@code getFramePosition()}) followed by {@code start()} from
 * that same position to resume.
 */
public final class JavaSoundAudioPlayer implements AudioPlayer {

    private Clip clip;
    private Runnable onFinished;
    private volatile boolean userStopped;

    @Override
    public synchronized void play(AudioBuffer audio) {
        closeClip();
        userStopped = false;

        AudioFormat format = PcmInterop.toAudioFormat(audio);
        byte[] raw = PcmInterop.toInterleavedBytes(audio);
        try {
            clip = AudioSystem.getClip();
            clip.open(format, raw, 0, raw.length);
            clip.addLineListener(this::onLineEvent);
            clip.start();
        } catch (LineUnavailableException e) {
            throw new IllegalStateException("Audio playback device unavailable", e);
        }
    }

    @Override
    public synchronized void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    @Override
    public synchronized void resume() {
        if (clip != null && !clip.isRunning() && clip.getFramePosition() < clip.getFrameLength()) {
            clip.start();
        }
    }

    @Override
    public synchronized void stop() {
        userStopped = true;
        closeClip();
    }

    @Override
    public synchronized boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    @Override
    public synchronized double positionSeconds() {
        return clip == null ? 0.0 : clip.getMicrosecondPosition() / 1_000_000.0;
    }

    @Override
    public synchronized double durationSeconds() {
        return clip == null ? 0.0 : clip.getMicrosecondLength() / 1_000_000.0;
    }

    @Override
    public void setOnFinished(Runnable callback) {
        this.onFinished = callback;
    }

    private void onLineEvent(LineEvent event) {
        // STOP fires both for natural end-of-clip and for our own stop()/pause().
        // Only treat it as "finished" when playback actually ran off the end.
        if (event.getType() != LineEvent.Type.STOP || userStopped) {
            return;
        }
        Clip current = clip;
        if (current != null && current.getFramePosition() >= current.getFrameLength()) {
            Runnable callback = onFinished;
            if (callback != null) callback.run();
        }
    }

    private void closeClip() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
