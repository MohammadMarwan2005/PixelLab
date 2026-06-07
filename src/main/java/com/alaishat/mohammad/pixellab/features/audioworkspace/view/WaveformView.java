package com.alaishat.mohammad.pixellab.features.audioworkspace.view;

import com.alaishat.mohammad.pixellab.domain.audio.AudioBuffer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Canvas-based min/max amplitude envelope of the first channel of an
 * {@link AudioBuffer} (Req. 1's "display in workspace"). Stateless beyond the
 * buffer it was last told to draw — the workspace re-pushes the buffer
 * whenever {@code workingBufferRevisionProperty} changes.
 */
public final class WaveformView extends StackPane {

    private static final Color BACKGROUND = Color.web("#1e1e1e");
    private static final Color WAVEFORM = Color.web("#3f9fff");
    private static final Color AXIS = Color.web("#555555");

    private final Canvas canvas = new Canvas();
    private AudioBuffer buffer;

    public WaveformView() {
        setMinHeight(100);
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((obs, was, is) -> redraw());
        canvas.heightProperty().addListener((obs, was, is) -> redraw());
    }

    public void setBuffer(AudioBuffer buffer) {
        this.buffer = buffer;
        redraw();
    }

    private void redraw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, width, height);

        if (width <= 0 || height <= 0) return;

        double midY = height / 2.0;
        gc.setStroke(AXIS);
        gc.setLineWidth(1);
        gc.strokeLine(0, midY, width, midY);

        if (buffer == null || buffer.frameCount() == 0) return;

        int[] samples = buffer.data()[0];
        int frameCount = samples.length;
        double magnitude = 1L << (buffer.bitDepth() - 1);
        int columns = (int) Math.ceil(width);

        gc.setStroke(WAVEFORM);
        gc.setLineWidth(1);
        for (int x = 0; x < columns; x++) {
            int start = (int) ((long) x * frameCount / columns);
            int end = (int) ((long) (x + 1) * frameCount / columns);
            end = Math.min(frameCount, Math.max(end, start + 1));

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int i = start; i < end; i++) {
                int sample = samples[i];
                if (sample < min) min = sample;
                if (sample > max) max = sample;
            }

            double yTop = midY - (max / magnitude) * midY;
            double yBottom = midY - (min / magnitude) * midY;
            gc.strokeLine(x + 0.5, yTop, x + 0.5, Math.max(yTop + 1, yBottom));
        }
    }
}
