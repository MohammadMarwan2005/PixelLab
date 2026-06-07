package com.alaishat.mohammad.pixellab.features.audioworkspace.view;

import com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel.AudioWorkspaceViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

import java.util.Locale;

/**
 * Play/Pause + Stop transport with a position readout (Req. 2 — preview
 * playback before compression). Sits below the waveform in the Audio Lab.
 */
public final class PlaybackControlsView extends HBox {

    public PlaybackControlsView(AudioWorkspaceViewModel viewModel) {
        setSpacing(10);
        setPadding(new Insets(6));
        setAlignment(Pos.CENTER_LEFT);

        Button playPauseButton = new Button("Play");
        playPauseButton.disableProperty().bind(viewModel.hasAudioBinding().not());
        playPauseButton.textProperty().bind(Bindings.createStringBinding(
                () -> viewModel.playingProperty().get() ? "Pause" : "Play",
                viewModel.playingProperty()));
        playPauseButton.setOnAction(e -> togglePlayPause(viewModel));

        Button stopButton = new Button("Stop");
        stopButton.disableProperty().bind(viewModel.hasAudioBinding().not());
        stopButton.setOnAction(e -> viewModel.stop());

        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(220);
        progress.progressProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double duration = viewModel.playbackDurationProperty().get();
                    if (duration <= 0) return 0.0;
                    return viewModel.playbackPositionProperty().get() / duration;
                },
                viewModel.playbackPositionProperty(), viewModel.playbackDurationProperty()));

        Label positionLabel = new Label("0:00 / 0:00");
        positionLabel.textProperty().bind(Bindings.createStringBinding(
                () -> formatPosition(viewModel.playbackPositionProperty().get(),
                        viewModel.playbackDurationProperty().get()),
                viewModel.playbackPositionProperty(), viewModel.playbackDurationProperty()));

        getChildren().addAll(playPauseButton, stopButton, progress, positionLabel);
    }

    private static void togglePlayPause(AudioWorkspaceViewModel viewModel) {
        if (viewModel.playingProperty().get()) {
            viewModel.pause();
        } else if (viewModel.playbackPositionProperty().get() > 0
                && viewModel.playbackPositionProperty().get() < viewModel.playbackDurationProperty().get()) {
            viewModel.resume();
        } else {
            viewModel.play();
        }
    }

    private static String formatPosition(double positionSeconds, double durationSeconds) {
        return formatSeconds(positionSeconds) + " / " + formatSeconds(durationSeconds);
    }

    private static String formatSeconds(double seconds) {
        long total = Math.round(Math.max(0, seconds));
        return String.format(Locale.ROOT, "%d:%02d", total / 60, total % 60);
    }
}
