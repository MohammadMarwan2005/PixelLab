package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * 3-pane shell (Phase 2.4): toolbar on top, recent-files on the left (placeholder
 * for Phase 3), image canvas in the center, metadata panel on the right.
 *
 * Also surfaces load errors via a small status line at the bottom — Phase 10 will
 * upgrade this to a proper error dialog, but a visible failure beats a silent one.
 */
public final class MainWindowView extends BorderPane {

    public MainWindowView(ImageWorkspaceViewModel viewModel) {
        setTop(new ToolbarView(viewModel));
        setLeft(buildLeftPlaceholder());
        setCenter(new ImageCanvasView(viewModel));
        setRight(new MetadataPanelView(viewModel));
        setBottom(buildErrorBar(viewModel));
    }

    private static VBox buildLeftPlaceholder() {
        VBox box = new VBox();
        box.setPadding(new Insets(12));
        box.setPrefWidth(200);
        box.setMinWidth(160);
        Label heading = new Label("Recent files");
        heading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label note = new Label("(coming in Phase 3)");
        note.setStyle("-fx-text-fill: gray;");
        box.getChildren().addAll(heading, note);
        return box;
    }

    private static Label buildErrorBar(ImageWorkspaceViewModel viewModel) {
        Label status = new Label();
        status.setPadding(new Insets(4, 12, 4, 12));
        status.setStyle("-fx-text-fill: #b00020;");
        viewModel.lastErrorProperty().addListener((obs, oldErr, newErr) ->
                status.setText(newErr == null ? "" : "Failed to load image: " + newErr.getMessage()));
        return status;
    }
}
