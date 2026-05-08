package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * Top bar: Open file picker (Phase 2.6). Save / Save As / Reset will be added in
 * Phase 4 — kept thin here so the layout shell is in place.
 */
public final class ToolbarView extends ToolBar {

    public ToolbarView(ImageWorkspaceViewModel viewModel) {
        setPadding(new Insets(4));

        Button openButton = new Button("Open…");
        openButton.setOnAction(e -> openImage(viewModel));

        getItems().add(openButton);
    }

    private static void openImage(ImageWorkspaceViewModel viewModel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        Window owner = null;
        File picked = chooser.showOpenDialog(owner);
        if (picked != null) {
            viewModel.open(picked.toPath());
        }
    }
}
