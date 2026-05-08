package com.alaishat.mohammad.pixellab.features.colorpicker.view;

import com.alaishat.mohammad.pixellab.features.colorpicker.viewmodel.ColorPickerViewModel;
import com.alaishat.mohammad.pixellab.features.visualization3d.usecase.ColorSample;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Synchronization panel (Phase 8.9, Req 5): swatch + the picked color in all
 * six systems at once. Each row has a Copy button that pushes the formatted
 * value to the system clipboard.
 */
public final class ColorPickerView extends VBox {

    public ColorPickerView(ColorPickerViewModel viewModel) {
        setSpacing(6);
        setPadding(new Insets(8, 12, 8, 12));
        setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #dcdcdc; -fx-border-width: 1 0 0 0;");

        Label heading = new Label("Picked color");
        heading.setStyle("-fx-font-weight: bold;");

        Region swatch = new Region();
        swatch.setMinSize(36, 36);
        swatch.setPrefSize(36, 36);
        swatch.setMaxSize(36, 36);
        swatch.setStyle(swatchStyle(viewModel.pickedSampleProperty().get()));
        viewModel.pickedSampleProperty().addListener((obs, old, neu) -> swatch.setStyle(swatchStyle(neu)));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(2);
        addRow(grid, 0, "RGB",   viewModel.rgbText(),   viewModel);
        addRow(grid, 1, "HSV",   viewModel.hsvText(),   viewModel);
        addRow(grid, 2, "CMYK",  viewModel.cmykText(),  viewModel);
        addRow(grid, 3, "YUV",   viewModel.yuvText(),   viewModel);
        addRow(grid, 4, "YCbCr", viewModel.ycbcrText(), viewModel);
        addRow(grid, 5, "LAB",   viewModel.labText(),   viewModel);

        HBox.setHgrow(grid, Priority.ALWAYS);
        HBox row = new HBox(12, swatch, grid);
        row.setAlignment(Pos.TOP_LEFT);

        getChildren().addAll(heading, row);
    }

    private static String swatchStyle(ColorSample sample) {
        if (sample == null) {
            return "-fx-border-color: #999; -fx-border-width: 1; -fx-background-color: transparent;";
        }
        Color c = Color.color(sample.rgb().a(), sample.rgb().b(), sample.rgb().c());
        return "-fx-border-color: #999; -fx-border-width: 1; -fx-background-color: " + toHex(c) + ";";
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    private static void addRow(GridPane grid, int row, String label,
                               StringBinding text, ColorPickerViewModel viewModel) {
        Label k = new Label(label);
        k.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

        Label v = new Label();
        v.setStyle("-fx-font-family: monospace;");
        v.textProperty().bind(text);

        Button copy = new Button("Copy");
        copy.setStyle("-fx-font-size: 10px; -fx-padding: 1 6 1 6;");
        copy.setFocusTraversable(false);
        // Disabled when no color is picked — copying "—" would be nonsense.
        copy.disableProperty().bind(viewModel.pickedSampleProperty().isNull());
        copy.setOnAction(e -> viewModel.copyText(text.get()));

        grid.add(k, 0, row);
        grid.add(v, 1, row);
        grid.add(copy, 2, row);
    }
}
