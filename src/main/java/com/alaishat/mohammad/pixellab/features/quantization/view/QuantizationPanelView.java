package com.alaishat.mohammad.pixellab.features.quantization.view;

import com.alaishat.mohammad.pixellab.features.quantization.usecase.QuantizeColorsUseCase;
import com.alaishat.mohammad.pixellab.features.quantization.viewmodel.QuantizationViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Right-panel section: 2–256 colors slider with the current value next to it (Phase 7.2).
 */
public final class QuantizationPanelView extends VBox {

    public QuantizationPanelView(QuantizationViewModel viewModel) {
        setSpacing(6);
        setPadding(new Insets(8, 0, 0, 0));

        Label heading = new Label("Quantization");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Slider slider = new Slider(2, QuantizeColorsUseCase.MAX_COLORS, viewModel.colorCountProperty().get());
        slider.setMajorTickUnit(64);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(true);
        // Bind as integer — Slider exposes double, so we round when storing back.
        slider.valueProperty().addListener((obs, old, neu) ->
                viewModel.colorCountProperty().set((int) Math.round(neu.doubleValue())));
        viewModel.colorCountProperty().addListener((obs, old, neu) ->
                slider.setValue(neu.intValue()));

        Label value = new Label();
        value.textProperty().bind(Bindings.createStringBinding(
                () -> viewModel.colorCountProperty().get() + " colors",
                viewModel.colorCountProperty()));
        value.setMinWidth(72);
        value.setAlignment(Pos.CENTER_RIGHT);
        value.setStyle("-fx-text-fill: gray;");

        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox row = new HBox(8, slider, value);
        row.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(heading, row);
    }
}
