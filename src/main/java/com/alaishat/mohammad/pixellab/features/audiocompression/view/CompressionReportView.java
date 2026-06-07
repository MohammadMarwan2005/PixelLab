package com.alaishat.mohammad.pixellab.features.audiocompression.view;

import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressedAudioStore;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionReport;
import com.alaishat.mohammad.pixellab.features.audiocompression.viewmodel.AudioCompressionViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Locale;
import java.util.function.Function;

/**
 * Renders the {@link CompressionReport} produced by the last run (Req. 10):
 * sizes before/after, savings, ratio, elapsed time, and the exact algorithm +
 * settings used — plus the actions that consume the result (Decompress, Save
 * compressed). Mirrors {@code AudioPropertiesView}'s grid-of-bound-labels shape.
 */
public final class CompressionReportView extends VBox {

    public CompressionReportView(AudioCompressionViewModel viewModel) {
        setSpacing(8);
        setPadding(new Insets(4));

        Label heading = new Label("Compression report");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);

        addRow(grid, 0, "Original size", viewModel, r -> formatBytes(r.originalBytes()));
        addRow(grid, 1, "Compressed size", viewModel, r -> formatBytes(r.compressedBytes()));
        addRow(grid, 2, "Space saved", viewModel, r -> String.format(Locale.ROOT, "%.1f%%", r.savingsPercent()));
        addRow(grid, 3, "Compression ratio", viewModel, r -> String.format(Locale.ROOT, "%.2f : 1", r.compressionRatio()));
        addRow(grid, 4, "Time taken", viewModel, r -> formatMillis(r.elapsedMillis()));
        addRow(grid, 5, "Algorithm", viewModel, r -> r.algorithm().displayName());
        addRow(grid, 6, "Settings used", viewModel, CompressionReportView::describeSettings);

        Button decompressButton = new Button("Decompress");
        decompressButton.disableProperty().bind(viewModel.canDecompressBinding().not());
        decompressButton.setOnAction(e -> viewModel.decompress());

        Button saveCompressedButton = new Button("Save compressed…");
        saveCompressedButton.disableProperty().bind(viewModel.canSaveCompressedBinding().not());
        saveCompressedButton.setOnAction(e -> saveCompressed(viewModel));

        HBox actions = new HBox(8, decompressButton, saveCompressedButton);
        HBox.setHgrow(decompressButton, Priority.NEVER);

        getChildren().addAll(heading, grid, actions);
        visibleProperty().bind(Bindings.isNotNull(viewModel.reportProperty()));
        managedProperty().bind(visibleProperty());
    }

    private static void addRow(GridPane grid, int row, String name, AudioCompressionViewModel viewModel,
                                Function<CompressionReport, String> describe) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: gray;");

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    CompressionReport report = viewModel.reportProperty().get();
                    return report == null ? "—" : describe.apply(report);
                },
                viewModel.reportProperty()));

        grid.add(nameLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private static String describeSettings(CompressionReport report) {
        return switch (report.algorithm()) {
            case DELTA_MODULATION -> String.format(Locale.ROOT, "step = %.0f", report.settings().stepSize());
            case ADAPTIVE_DELTA_MODULATION -> String.format(Locale.ROOT, "step = %.0f, factor = %.2f×",
                    report.settings().stepSize(), report.settings().adaptationFactor());
            case DPCM -> String.format(Locale.ROOT, "%d-bit residuals", report.settings().quantizationBits());
        };
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.ROOT, "%.1f KB", kb);
        return String.format(Locale.ROOT, "%.1f MB", kb / 1024.0);
    }

    private static String formatMillis(long millis) {
        if (millis < 1000) return millis + " ms";
        return String.format(Locale.ROOT, "%.2f s", millis / 1000.0);
    }

    private static void saveCompressed(AudioCompressionViewModel viewModel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save compressed audio");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "PixelLab compressed audio", "*." + CompressedAudioStore.FILE_EXTENSION));

        File picked = chooser.showSaveDialog(null);
        if (picked == null) return;

        File target = ensureExtension(picked);
        viewModel.saveCompressed(target.toPath());
    }

    private static File ensureExtension(File picked) {
        String suffix = "." + CompressedAudioStore.FILE_EXTENSION;
        if (picked.getName().toLowerCase(Locale.ROOT).endsWith(suffix)) {
            return picked;
        }
        return new File(picked.getParentFile(), picked.getName() + suffix);
    }
}
