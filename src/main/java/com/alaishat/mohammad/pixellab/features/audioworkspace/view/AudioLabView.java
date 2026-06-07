package com.alaishat.mohammad.pixellab.features.audioworkspace.view;

import com.alaishat.mohammad.pixellab.features.audiocompression.view.CompressionProgressView;
import com.alaishat.mohammad.pixellab.features.audiocompression.view.CompressionReportView;
import com.alaishat.mohammad.pixellab.features.audiocompression.view.CompressionSettingsView;
import com.alaishat.mohammad.pixellab.features.audiocompression.viewmodel.AudioCompressionViewModel;
import com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel.AudioWorkspaceViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Top-level container for the "Audio Lab" tab — toolbar, waveform + transport
 * in the center (with drag-and-drop, mirroring {@code ImageCanvasView}),
 * properties on the right, and the compression workbench (settings, live
 * progress + charts, report) on the left. Mirrors {@code MainWindowView}'s
 * shell shape, just with one extra side panel for the compression feature.
 */
public final class AudioLabView extends BorderPane {

    public AudioLabView(AudioWorkspaceViewModel workspaceViewModel, AudioCompressionViewModel compressionViewModel) {
        setTop(new AudioToolbarView(workspaceViewModel));
        setCenter(buildCenter(workspaceViewModel));
        setRight(buildRight(workspaceViewModel));
        setLeft(buildLeft(compressionViewModel));

        wireErrorDialogs(workspaceViewModel);
        wireErrorDialogs(compressionViewModel);
    }

    private static ScrollPane buildLeft(AudioCompressionViewModel compressionViewModel) {
        VBox content = new VBox(12,
                new CompressionSettingsView(compressionViewModel),
                new Separator(),
                new CompressionProgressView(compressionViewModel),
                new Separator(),
                new CompressionReportView(compressionViewModel));
        content.setPadding(new Insets(12));
        content.setMinWidth(0);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefViewportWidth(360);
        scroll.setMinWidth(320);
        return scroll;
    }

    private static StackPane buildCenter(AudioWorkspaceViewModel workspaceViewModel) {
        WaveformView waveform = new WaveformView();
        Label placeholder = new Label("No audio loaded.\nUse Open or drop an audio file here.");
        placeholder.setStyle("-fx-text-fill: gray; -fx-font-size: 14px; -fx-text-alignment: center;");
        placeholder.setWrapText(true);
        placeholder.setMouseTransparent(true);

        VBox content = new VBox(0, waveform, new PlaybackControlsView(workspaceViewModel));
        VBox.setVgrow(waveform, javafx.scene.layout.Priority.ALWAYS);

        StackPane center = new StackPane(content, placeholder);
        center.setAlignment(Pos.CENTER);
        center.setMinSize(0, 0);
        center.getStyleClass().add("audio-canvas");

        workspaceViewModel.editSessionProperty().addListener((obs, was, session) ->
                refreshWaveform(waveform, placeholder, workspaceViewModel));
        workspaceViewModel.workingBufferRevisionProperty().addListener((obs, was, now) ->
                refreshWaveform(waveform, placeholder, workspaceViewModel));

        wireDragAndDrop(center, workspaceViewModel);
        return center;
    }

    private static void refreshWaveform(WaveformView waveform, Label placeholder,
                                         AudioWorkspaceViewModel viewModel) {
        var session = viewModel.editSessionProperty().get();
        if (session == null) {
            waveform.setBuffer(null);
            placeholder.setVisible(true);
        } else {
            waveform.setBuffer(session.workingBuffer());
            placeholder.setVisible(false);
        }
    }

    private static ScrollPane buildRight(AudioWorkspaceViewModel workspaceViewModel) {
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.setMinWidth(0);
        content.getChildren().add(new AudioPropertiesView(workspaceViewModel));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefViewportWidth(280);
        scroll.setMinWidth(240);
        return scroll;
    }

    private static void wireDragAndDrop(StackPane target, AudioWorkspaceViewModel viewModel) {
        target.setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles() && hasAudioFile(db.getFiles())) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        target.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean accepted = false;
            if (db.hasFiles()) {
                for (File f : db.getFiles()) {
                    if (looksLikeAudio(f)) {
                        viewModel.open(f.toPath());
                        accepted = true;
                        break;
                    }
                }
            }
            e.setDropCompleted(accepted);
            e.consume();
        });
    }

    private static boolean hasAudioFile(List<File> files) {
        for (File f : files) {
            if (looksLikeAudio(f)) return true;
        }
        return false;
    }

    private static boolean looksLikeAudio(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".wav") || name.endsWith(".wave")
                || name.endsWith(".aiff") || name.endsWith(".aif") || name.endsWith(".au");
    }

    private static void wireErrorDialogs(AudioWorkspaceViewModel workspaceViewModel) {
        workspaceViewModel.lastErrorProperty().addListener((obs, oldErr, newErr) -> {
            if (newErr != null) showError("Audio operation failed", newErr);
        });
    }

    private static void wireErrorDialogs(AudioCompressionViewModel compressionViewModel) {
        compressionViewModel.lastErrorProperty().addListener((obs, oldErr, newErr) -> {
            if (newErr != null) showError("Compression operation failed", newErr);
        });
    }

    private static void showError(String header, Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PixelLab — Error");
        alert.setHeaderText(header);
        alert.setContentText(t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
        alert.showAndWait();
    }
}
