package com.alaishat.mohammad.pixellab.features.audiocompression.view;

import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionAlgorithm;
import com.alaishat.mohammad.pixellab.domain.audio.compression.CompressionSettings;
import com.alaishat.mohammad.pixellab.features.audiocompression.viewmodel.AudioCompressionViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Locale;

/**
 * Compression controls (Req. 6): algorithm picker plus the three tunable
 * parameters from {@link CompressionSettings}. Each slider is only enabled for
 * the algorithms that actually use it — {@code CompressionSettings}'s javadoc
 * spells out which is which — so the panel never implies a knob does something
 * it won't. Run / Cancel live in {@link CompressionProgressView}; "Reset"
 * here only restores the *settings* to defaults (Req. 9).
 */
public final class CompressionSettingsView extends VBox {

    public CompressionSettingsView(AudioCompressionViewModel viewModel) {
        setSpacing(8);

        Label heading = new Label("Compression settings");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ComboBox<CompressionAlgorithm> algorithmBox = new ComboBox<>();
        algorithmBox.getItems().setAll(CompressionAlgorithm.values());
        algorithmBox.valueProperty().bindBidirectional(viewModel.algorithmProperty());
        algorithmBox.disableProperty().bind(viewModel.runningProperty());
        algorithmBox.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

        Slider quantizationBits = intSlider(
                CompressionSettings.MIN_QUANTIZATION_BITS, CompressionSettings.MAX_QUANTIZATION_BITS,
                viewModel.quantizationBitsProperty().get());
        Slider stepSize = doubleSlider(1, 2048, viewModel.stepSizeProperty().get());
        Slider adaptationFactor = doubleSlider(1.05, 5.0, viewModel.adaptationFactorProperty().get());

        bindInt(quantizationBits, viewModel.quantizationBitsProperty());
        bindDouble(stepSize, viewModel.stepSizeProperty());
        bindDouble(adaptationFactor, viewModel.adaptationFactorProperty());

        BooleanFn usesQuantizationBits = a -> a == CompressionAlgorithm.DPCM;
        BooleanFn usesStepSize = a -> a == CompressionAlgorithm.DELTA_MODULATION
                || a == CompressionAlgorithm.ADAPTIVE_DELTA_MODULATION;
        BooleanFn usesAdaptationFactor = a -> a == CompressionAlgorithm.ADAPTIVE_DELTA_MODULATION;

        addRow(grid, 0, "Quantization bits", quantizationBits,
                valueLabel(viewModel, "%d bits", viewModel.quantizationBitsProperty()),
                relevance(viewModel, usesQuantizationBits), viewModel.runningProperty());
        addRow(grid, 1, "Step size", stepSize,
                valueLabel(viewModel, "%.0f", viewModel.stepSizeProperty()),
                relevance(viewModel, usesStepSize), viewModel.runningProperty());
        addRow(grid, 2, "Adaptation factor", adaptationFactor,
                valueLabel(viewModel, "%.2f×", viewModel.adaptationFactorProperty()),
                relevance(viewModel, usesAdaptationFactor), viewModel.runningProperty());

        Button resetButton = new Button("Reset settings");
        resetButton.disableProperty().bind(viewModel.runningProperty());
        resetButton.setOnAction(e -> viewModel.resetSettings());

        Button compressButton = new Button("Compress");
        compressButton.setDefaultButton(true);
        compressButton.disableProperty().bind(viewModel.canCompressBinding().not());
        compressButton.setOnAction(e -> viewModel.compress());

        HBox actions = new HBox(8, compressButton, resetButton);

        getChildren().addAll(heading, algorithmBox, grid, actions);
    }

    @FunctionalInterface
    private interface BooleanFn {
        boolean test(CompressionAlgorithm algorithm);
    }

    private static javafx.beans.binding.BooleanBinding relevance(AudioCompressionViewModel viewModel, BooleanFn predicate) {
        return Bindings.createBooleanBinding(
                () -> predicate.test(viewModel.algorithmProperty().get()),
                viewModel.algorithmProperty());
    }

    private static Label valueLabel(AudioCompressionViewModel viewModel, String format, javafx.beans.value.ObservableNumberValue value) {
        Label label = new Label();
        label.setMinWidth(64);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setStyle("-fx-text-fill: gray;");
        label.textProperty().bind(Bindings.createStringBinding(
                () -> String.format(Locale.ROOT, format, value.getValue()),
                value));
        return label;
    }

    private static void addRow(GridPane grid, int row, String name, Slider slider, Label valueLabel,
                               javafx.beans.binding.BooleanBinding relevant,
                               javafx.beans.value.ObservableBooleanValue running) {
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: gray;");

        slider.disableProperty().bind(relevant.not().or(running));
        nameLabel.opacityProperty().bind(Bindings.when(relevant).then(1.0).otherwise(0.45));
        valueLabel.opacityProperty().bind(Bindings.when(relevant).then(1.0).otherwise(0.45));

        HBox.setHgrow(slider, Priority.ALWAYS);
        HBox row1 = new HBox(8, slider, valueLabel);
        row1.setAlignment(Pos.CENTER_LEFT);

        grid.add(nameLabel, 0, row);
        grid.add(row1, 1, row);
        GridPane.setHgrow(row1, Priority.ALWAYS);
    }

    private static Slider intSlider(int min, int max, int initial) {
        Slider slider = new Slider(min, max, initial);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        return slider;
    }

    private static Slider doubleSlider(double min, double max, double initial) {
        Slider slider = new Slider(min, max, initial);
        return slider;
    }

    private static void bindInt(Slider slider, javafx.beans.property.IntegerProperty target) {
        slider.valueProperty().addListener((obs, was, now) -> target.set((int) Math.round(now.doubleValue())));
        target.addListener((obs, was, now) -> slider.setValue(now.intValue()));
    }

    private static void bindDouble(Slider slider, javafx.beans.property.DoubleProperty target) {
        slider.valueProperty().addListener((obs, was, now) -> target.set(now.doubleValue()));
        target.addListener((obs, was, now) -> slider.setValue(now.doubleValue()));
    }
}
