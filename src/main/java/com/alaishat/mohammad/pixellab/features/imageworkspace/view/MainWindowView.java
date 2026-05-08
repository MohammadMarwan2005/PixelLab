package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.recentfiles.view.RecentFilesPanelView;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * 3-pane shell (Phase 2.4): toolbar on top, recent files on the left,
 * image canvas in the center, metadata panel on the right.
 *
 * Load errors surface via a small status line at the bottom — Phase 10 will
 * upgrade this to a proper error dialog, but a visible failure beats a silent one.
 */
public final class MainWindowView extends BorderPane {

    public MainWindowView(ImageWorkspaceViewModel workspaceViewModel,
                          RecentFilesViewModel recentFilesViewModel) {
        setTop(new ToolbarView(workspaceViewModel));
        setLeft(new RecentFilesPanelView(recentFilesViewModel, workspaceViewModel::open));
        setCenter(new ImageCanvasView(workspaceViewModel));
        setRight(new MetadataPanelView(workspaceViewModel));
        setBottom(buildErrorBar(workspaceViewModel));
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
