package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.colorpicker.viewmodel.ColorPickerViewModel;
import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import com.alaishat.mohammad.pixellab.features.editsession.viewmodel.EditSessionViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.quantization.viewmodel.QuantizationViewModel;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel.ColorSpaceVisualizationViewModel;
import com.alaishat.mohammad.pixellab.shared.threading.UpdateCoalescer;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

/**
 * 3-pane shell (Phase 2.4): toolbar on top, recent files + color space on the
 * left, image canvas / 3D viz tabs in the center, metadata + processing
 * controls on the right. Errors from load and edit flows surface as Alert
 * dialogs (Phase 10.1).
 */
public final class MainWindowView extends BorderPane {

    public MainWindowView(ImageWorkspaceViewModel workspaceViewModel,
                          EditSessionViewModel editViewModel,
                          ColorSpaceViewModel colorSpaceViewModel,
                          ChannelsViewModel channelsViewModel,
                          QuantizationViewModel quantizationViewModel,
                          ColorSpaceVisualizationViewModel visualizationViewModel,
                          ColorPickerViewModel colorPickerViewModel,
                          RecentFilesViewModel recentFilesViewModel,
                          UpdateCoalescer coalescer) {
        setTop(new ToolbarView(workspaceViewModel, editViewModel));
        setLeft(new LeftPaneView(colorSpaceViewModel, recentFilesViewModel, workspaceViewModel::open));
        setCenter(new CenterPaneView(workspaceViewModel, visualizationViewModel, colorPickerViewModel, coalescer));
        setRight(new RightPaneView(workspaceViewModel, channelsViewModel, quantizationViewModel));

        wireErrorDialogs(workspaceViewModel, editViewModel);
    }

    private static void wireErrorDialogs(ImageWorkspaceViewModel workspace, EditSessionViewModel edit) {
        workspace.lastErrorProperty().addListener((obs, oldErr, newErr) -> {
            if (newErr != null) showError("Failed to load image", newErr);
        });
        edit.lastErrorProperty().addListener((obs, oldErr, newErr) -> {
            if (newErr != null) showError("Edit operation failed", newErr);
        });
    }

    private static void showError(String header, Throwable t) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("PixelLab — Error");
        alert.setHeaderText(header);
        alert.setContentText(t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
        alert.showAndWait();
    }
}
