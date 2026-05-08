package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.editsession.viewmodel.EditSessionViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Locale;

/**
 * Top bar: Open (Phase 2.6) plus Save / Save As / Reset (Phase 4.5–4.6).
 * Edit-action buttons disable themselves when no image is loaded.
 */
public final class ToolbarView extends ToolBar {

    public ToolbarView(ImageWorkspaceViewModel workspaceVm, EditSessionViewModel editVm) {
        setPadding(new Insets(4));

        Button openButton = new Button("Open…");
        openButton.setOnAction(e -> openImage(workspaceVm));

        Button saveButton = new Button("Save");
        saveButton.disableProperty().bind(editVm.canEditBinding().not());
        saveButton.setOnAction(e -> editVm.save());

        Button saveAsButton = new Button("Save As…");
        saveAsButton.disableProperty().bind(editVm.canEditBinding().not());
        saveAsButton.setOnAction(e -> saveAs(editVm));

        Button resetButton = new Button("Reset");
        resetButton.disableProperty().bind(editVm.canEditBinding().not());
        resetButton.setOnAction(e -> editVm.reset());

        getItems().addAll(openButton, saveButton, saveAsButton, resetButton);
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

    private static void saveAs(EditSessionViewModel editVm) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save image as");
        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG image", "*.png");
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPEG image", "*.jpg", "*.jpeg");
        FileChooser.ExtensionFilter bmpFilter = new FileChooser.ExtensionFilter("BMP image", "*.bmp");
        chooser.getExtensionFilters().addAll(pngFilter, jpgFilter, bmpFilter);
        chooser.setSelectedExtensionFilter(pngFilter);

        File picked = chooser.showSaveDialog(null);
        if (picked == null) return;

        FileChooser.ExtensionFilter selected = chooser.getSelectedExtensionFilter();
        String format;
        String preferredExt;
        if (selected == jpgFilter) {
            format = "JPEG";
            preferredExt = ".jpg";
        } else if (selected == bmpFilter) {
            format = "BMP";
            preferredExt = ".bmp";
        } else {
            format = "PNG";
            preferredExt = ".png";
        }

        File target = ensureExtension(picked, preferredExt);
        editVm.saveAs(target.toPath(), format);
    }

    private static File ensureExtension(File picked, String preferredExt) {
        String name = picked.getName().toLowerCase(Locale.ROOT);
        // Accept any reasonable image extension already present (.jpg/.jpeg both fine for JPEG).
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
            return picked;
        }
        return new File(picked.getParentFile(), picked.getName() + preferredExt);
    }
}
