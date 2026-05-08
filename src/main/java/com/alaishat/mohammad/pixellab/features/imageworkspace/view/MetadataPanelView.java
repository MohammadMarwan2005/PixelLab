package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.domain.image.ImageMetadata;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Right pane: shows name / format / file size / dimensions for the loaded image
 * (Phase 2.8 + Requirement 8). Bound to the workspace view model — values clear
 * automatically when the image is unloaded.
 */
public final class MetadataPanelView extends VBox {

    public MetadataPanelView(ImageWorkspaceViewModel viewModel) {
        setSpacing(8);

        Label heading = new Label("Image");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);

        Label nameValue = new Label();
        Label formatValue = new Label();
        Label sizeValue = new Label();
        Label dimsValue = new Label();

        nameValue.setWrapText(true);
        nameValue.setMaxWidth(180);

        nameValue.textProperty().bind(Bindings.createStringBinding(
                () -> describeName(viewModel.currentMetadataProperty().get()),
                viewModel.currentMetadataProperty()));
        formatValue.textProperty().bind(Bindings.createStringBinding(
                () -> describeFormat(viewModel.currentMetadataProperty().get()),
                viewModel.currentMetadataProperty()));
        sizeValue.textProperty().bind(Bindings.createStringBinding(
                () -> describeSize(viewModel.currentMetadataProperty().get()),
                viewModel.currentMetadataProperty()));
        dimsValue.textProperty().bind(Bindings.createStringBinding(
                () -> describeDimensions(viewModel.currentMetadataProperty().get()),
                viewModel.currentMetadataProperty()));

        addRow(grid, 0, "Name",       nameValue);
        addRow(grid, 1, "Format",     formatValue);
        addRow(grid, 2, "File size",  sizeValue);
        addRow(grid, 3, "Dimensions", dimsValue);

        getChildren().addAll(heading, grid);
    }

    private static void addRow(GridPane grid, int row, String label, Label value) {
        Label k = new Label(label);
        k.setStyle("-fx-text-fill: gray;");
        grid.add(k, 0, row);
        grid.add(value, 1, row);
    }

    private static String describeName(ImageMetadata m)     { return m == null ? "—" : m.name(); }
    private static String describeFormat(ImageMetadata m)   { return m == null ? "—" : m.format(); }
    private static String describeDimensions(ImageMetadata m) {
        return m == null ? "—" : m.width() + " × " + m.height();
    }

    private static String describeSize(ImageMetadata m) {
        if (m == null) return "—";
        long bytes = m.fileSize();
        if (bytes < 1024)             return bytes + " B";
        if (bytes < 1024L * 1024)     return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
