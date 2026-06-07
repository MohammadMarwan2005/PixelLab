package com.alaishat.mohammad.pixellab.features.audioworkspace.view;

import com.alaishat.mohammad.pixellab.domain.audio.AudioMetadata;
import com.alaishat.mohammad.pixellab.features.audioworkspace.viewmodel.AudioWorkspaceViewModel;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

/**
 * Right pane: auto-displayed audio properties (Req. 3) — file size, duration,
 * sample rate, channel count, bit rate, and encoding type. Mirrors {@code
 * features.imageworkspace.view.MetadataPanelView}.
 */
public final class AudioPropertiesView extends VBox {

    public AudioPropertiesView(AudioWorkspaceViewModel viewModel) {
        setSpacing(8);

        Label heading = new Label("Audio");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);

        Label nameValue = new Label();
        Label sizeValue = new Label();
        Label durationValue = new Label();
        Label sampleRateValue = new Label();
        Label channelsValue = new Label();
        Label bitRateValue = new Label();
        Label encodingValue = new Label();

        nameValue.setWrapText(true);
        nameValue.setMaxWidth(180);

        bind(nameValue, viewModel, AudioPropertiesView::describeName);
        bind(sizeValue, viewModel, AudioPropertiesView::describeSize);
        bind(durationValue, viewModel, AudioPropertiesView::describeDuration);
        bind(sampleRateValue, viewModel, AudioPropertiesView::describeSampleRate);
        bind(channelsValue, viewModel, AudioPropertiesView::describeChannels);
        bind(bitRateValue, viewModel, AudioPropertiesView::describeBitRate);
        bind(encodingValue, viewModel, AudioPropertiesView::describeEncoding);

        addRow(grid, 0, "Name",        nameValue);
        addRow(grid, 1, "File size",   sizeValue);
        addRow(grid, 2, "Duration",    durationValue);
        addRow(grid, 3, "Sample rate", sampleRateValue);
        addRow(grid, 4, "Channels",    channelsValue);
        addRow(grid, 5, "Bit rate",    bitRateValue);
        addRow(grid, 6, "Encoding",    encodingValue);

        getChildren().addAll(heading, grid);
    }

    private static void bind(Label label, AudioWorkspaceViewModel viewModel,
                             java.util.function.Function<AudioMetadata, String> describe) {
        label.textProperty().bind(Bindings.createStringBinding(
                () -> describe.apply(viewModel.currentMetadataProperty().get()),
                viewModel.currentMetadataProperty()));
    }

    private static void addRow(GridPane grid, int row, String label, Label value) {
        Label k = new Label(label);
        k.setStyle("-fx-text-fill: gray;");
        grid.add(k, 0, row);
        grid.add(value, 1, row);
    }

    private static String describeName(AudioMetadata m)     { return m == null ? "—" : m.name(); }
    private static String describeEncoding(AudioMetadata m) { return m == null ? "—" : m.encoding(); }
    private static String describeChannels(AudioMetadata m) {
        if (m == null) return "—";
        return switch (m.channelCount()) {
            case 1 -> "1 (mono)";
            case 2 -> "2 (stereo)";
            default -> m.channelCount() + " channels";
        };
    }

    private static String describeSampleRate(AudioMetadata m) {
        return m == null ? "—" : String.format(Locale.ROOT, "%,d Hz", m.sampleRate());
    }

    private static String describeBitRate(AudioMetadata m) {
        if (m == null) return "—";
        return String.format(Locale.ROOT, "%,d kbps", Math.round(m.bitRate() / 1000.0));
    }

    private static String describeDuration(AudioMetadata m) {
        if (m == null) return "—";
        long totalSeconds = Math.round(m.durationSeconds());
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }

    private static String describeSize(AudioMetadata m) {
        if (m == null) return "—";
        long bytes = m.fileSize();
        if (bytes < 1024)                  return bytes + " B";
        if (bytes < 1024L * 1024)          return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024)   return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024));
        return String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
