package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.domain.image.PixelBuffer;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.List;

/**
 * Center pane: renders the current PixelBuffer to a WritableImage and accepts
 * dropped image files (Phase 2.5 + 2.7). Empty-state placeholder until something
 * is loaded.
 */
public final class ImageCanvasView extends StackPane {

    private final ImageView imageView = new ImageView();
    private final Label placeholder = new Label("No image loaded.\nUse Open or drop an image here.");

    public ImageCanvasView(ImageWorkspaceViewModel viewModel) {
        getStyleClass().add("image-canvas");
        setAlignment(Pos.CENTER);
        setMinSize(0, 0);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        placeholder.setStyle("-fx-text-fill: gray; -fx-font-size: 14px; -fx-text-alignment: center;");
        placeholder.setWrapText(true);

        getChildren().addAll(placeholder, imageView);
        imageView.setVisible(false);

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
