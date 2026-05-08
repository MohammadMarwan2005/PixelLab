package com.alaishat.mohammad.pixellab.features.channels.view;

import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel.ChannelControl;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Right-panel section containing per-channel controls (Phase 6.4–6.6):
 *  - Channel thumbnail (grayscale split, ~60×60)
 *  - Enable/disable toggle (zeros the channel when off)
 *  - Offset slider (-1.0 to +1.0 of the channel's natural range)
 *
 * The list rebuilds whenever the color space changes (3 controls for RGB/HSV/…,
 * 4 for CMYK).
 */
public final class ChannelControlsView extends VBox {

    private static final int THUMB_SIZE = 60;

    public ChannelControlsView(ChannelsViewModel viewModel) {
        setSpacing(8);
        setPadding(new Insets(8, 0, 0, 0));

        Label heading = new Label("Channels");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox rows = new VBox(8);
        rebuildRows(rows, viewModel);
        viewModel.channels().addListener((ListChangeListener<ChannelControl>) c -> rebuildRows(rows, viewModel));

        Button resetButton = new Button("Reset channels");
        resetButton.setOnAction(e -> viewModel.resetAll());

        getChildren().addAll(heading, rows, resetButton);
    }

    private static void rebuildRows(VBox rows, ChannelsViewModel viewModel) {
        rows.getChildren().clear();
        for (ChannelControl c : viewModel.channels()) {
            rows.getChildren().add(buildRow(c));
        }
    }

    private static HBox buildRow(ChannelControl control) {
        ImageView thumbnail = new ImageView();
        thumbnail.setFitWidth(THUMB_SIZE);
        thumbnail.setFitHeight(THUMB_SIZE);
        thumbnail.setPreserveRatio(true);
        thumbnail.setSmooth(false);
        control.thumbnailProperty().addListener((obs, oldBuf, newBuf) -> thumbnail.setImage(toImage(newBuf)));
        thumbnail.setImage(toImage(control.thumbnailProperty().get()));

        VBox controlsCol = new VBox(4);
        HBox.setHgrow(controlsCol, Priority.ALWAYS);

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(control.label());
        label.setMinWidth(28);
        label.setStyle("-fx-font-weight: bold;");
        CheckBox toggle = new CheckBox("on");
        toggle.selectedProperty().bindBidirectional(control.enabledProperty());
        topRow.getChildren().addAll(label, toggle);

        Slider slider = new Slider(-1.0, 1.0, 0.0);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(4);
        slider.setBlockIncrement(0.05);
        slider.disableProperty().bind(control.enabledProperty().not());
        slider.valueProperty().bindBidirectional(control.offsetProperty());

        controlsCol.getChildren().addAll(topRow, slider);

        HBox row = new HBox(8, thumbnail, controlsCol);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static WritableImage toImage(PixelBuffer buffer) {
        if (buffer == null) return null;
        WritableImage image = new WritableImage(buffer.width(), buffer.height());
        image.getPixelWriter().setPixels(
                0, 0, buffer.width(), buffer.height(),
                PixelFormat.getIntArgbInstance(),
                buffer.data(), 0, buffer.width());
        return image;
    }
}
