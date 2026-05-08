package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.shared.threading.UpdateCoalescer;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

/**
 * Center pane: renders the current PixelBuffer to a WritableImage and accepts
 * dropped image files (Phase 2.5 + 2.7).
 *
 * <p>Phase 9.5: shows a subtle progress indicator overlay when the background
 * pipeline is busy for more than 200 ms, so quick recomputes don't flash.
 */
public final class ImageCanvasView extends StackPane {

    private static final Duration SPINNER_DELAY = Duration.millis(200);

    private final ImageView imageView = new ImageView();
    private final Label placeholder = new Label("No image loaded.\nUse Open or drop an image here.");

    public ImageCanvasView(ImageWorkspaceViewModel viewModel, UpdateCoalescer coalescer) {
        getStyleClass().add("image-canvas");
        setAlignment(Pos.CENTER);
        setMinSize(0, 0);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        placeholder.setStyle("-fx-text-fill: gray; -fx-font-size: 14px; -fx-text-alignment: center;");
        placeholder.setWrapText(true);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMinSize(64, 64);
        spinner.setMaxSize(64, 64);
        spinner.setMouseTransparent(true);
        StackPane.setAlignment(spinner, Pos.CENTER);

        // Translucent circular backdrop so the spinner reads on light *and* dark images.
        // Keeping the backdrop and the spinner as separate, equally-sized children of a
        // StackPane guarantees they share the same center even if the spinner's internal
        // padding shifts.
        StackPane backdrop = new StackPane(spinner);
        backdrop.setMinSize(96, 96);
        backdrop.setMaxSize(96, 96);
        backdrop.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 48;");
        backdrop.setMouseTransparent(true);
        StackPane.setAlignment(backdrop, Pos.CENTER);

        StackPane spinnerLayer = new StackPane(backdrop);
        spinnerLayer.setMouseTransparent(true);
        spinnerLayer.setVisible(false);

        getChildren().addAll(placeholder, imageView, spinnerLayer);
        imageView.setVisible(false);

        wireSpinner(coalescer, spinnerLayer);

        viewModel.currentBufferProperty().addListener((obs, oldBuf, newBuf) -> {
            if (newBuf == null) {
                imageView.setImage(null);
                imageView.setVisible(false);
                placeholder.setVisible(true);
            } else {
                imageView.setImage(toWritableImage(newBuf));
                imageView.setVisible(true);
                placeholder.setVisible(false);
            }
        });

        setOnDragOver(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasFiles() && hasImageFile(db.getFiles())) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean accepted = false;
            if (db.hasFiles()) {
                for (File f : db.getFiles()) {
                    if (looksLikeImage(f)) {
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

    private static void wireSpinner(UpdateCoalescer coalescer, StackPane spinnerLayer) {
        PauseTransition delay = new PauseTransition(SPINNER_DELAY);
        delay.setOnFinished(e -> spinnerLayer.setVisible(true));
        coalescer.busyProperty().addListener((obs, was, now) -> {
            if (now) {
                delay.playFromStart();
            } else {
                delay.stop();
                spinnerLayer.setVisible(false);
            }
        });
    }

    private static WritableImage toWritableImage(PixelBuffer buffer) {
        WritableImage image = new WritableImage(buffer.width(), buffer.height());
        image.getPixelWriter().setPixels(
                0, 0,
                buffer.width(), buffer.height(),
                PixelFormat.getIntArgbInstance(),
                buffer.data(), 0,
                buffer.width());
        return image;
    }

    private static boolean hasImageFile(List<File> files) {
        for (File f : files) {
            if (looksLikeImage(f)) return true;
        }
        return false;
    }

    private static boolean looksLikeImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".bmp") || name.endsWith(".gif");
    }
}
