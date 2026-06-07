package com.alaishat.mohammad.pixellab.features.audiocompression.view;

import com.alaishat.mohammad.pixellab.features.audiocompression.viewmodel.AudioCompressionViewModel;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Live monitoring for an in-flight compression run (Reqs. 7, 8): a progress
 * bar driven by {@link AudioCompressionViewModel#progressProperty()}, a
 * Cancel button wired to {@link AudioCompressionViewModel#cancel()}, and two
 * {@link LineChart}s fed by the view model's live series.
 *
 * <p>As the view model's class javadoc explains, the ratio chart converges to
 * a flat line almost immediately — these are fixed-rate codes with no entropy
 * coding, so that's the mathematically honest picture, not a bug.
 */
public final class CompressionProgressView extends VBox {

    public CompressionProgressView(AudioCompressionViewModel viewModel) {
        setSpacing(8);

        Label heading = new Label("Compression progress");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.progressProperty().bind(viewModel.progressProperty());

        Label percentLabel = new Label();
        percentLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%.0f%%", viewModel.progressProperty().get() * 100),
                viewModel.progressProperty()));

        Button cancelButton = new Button("Cancel");
        cancelButton.disableProperty().bind(viewModel.runningProperty().not());
        cancelButton.setOnAction(e -> viewModel.cancel());

        HBox.setHgrow(progressBar, Priority.ALWAYS);
        HBox progressRow = new HBox(8, progressBar, percentLabel, cancelButton);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        LineChart<Number, Number> ratioChart = chart(
                "Compression ratio over time", "Elapsed (s)", "Ratio (original ÷ compressed)", viewModel.ratioSeriesData());
        LineChart<Number, Number> speedChart = chart(
                "Processing speed over time", "Elapsed (s)", "Samples / second", viewModel.speedSeriesData());

        VBox.setVgrow(ratioChart, Priority.ALWAYS);
        VBox.setVgrow(speedChart, Priority.ALWAYS);

        getChildren().addAll(heading, progressRow, ratioChart, speedChart);
    }

    private static LineChart<Number, Number> chart(String title, String xLabel, String yLabel,
                                                    javafx.collections.ObservableList<XYChart.Data<Number, Number>> data) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);

        XYChart.Series<Number, Number> series = new XYChart.Series<>(data);
        chart.getData().add(series);
        return chart;
    }
}
