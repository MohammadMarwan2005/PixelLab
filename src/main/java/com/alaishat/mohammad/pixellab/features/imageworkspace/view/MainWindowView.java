package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.colorpicker.viewmodel.ColorPickerViewModel;
import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import com.alaishat.mohammad.pixellab.features.editsession.viewmodel.EditSessionViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.quantization.viewmodel.QuantizationViewModel;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel.ColorSpaceVisualizationViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * 3-pane shell (Phase 2.4): toolbar on top, recent files + color space on the
 * left, image canvas / 3D viz tabs in the center, metadata + processing
 * controls on the right. Errors from load and edit flows surface in a small
 * status line at the bottom.
 */
public final class MainWindowView extends BorderPane {

    public MainWindowView(ImageWorkspaceViewModel workspaceViewModel,
                          EditSessionViewModel editViewModel,
                          ColorSpaceViewModel colorSpaceViewModel,
                          ChannelsViewModel channelsViewModel,
                          QuantizationViewModel quantizationViewModel,
                          ColorSpaceVisualizationViewModel visualizationViewModel,
                          ColorPickerViewModel colorPickerViewModel,
                          RecentFilesViewModel recentFilesViewModel) {
        setTop(new ToolbarView(workspaceViewModel, editViewModel));
        setLeft(new LeftPaneView(colorSpaceViewModel, recentFilesViewModel, workspaceViewModel::open));
        setCenter(new CenterPaneView(workspaceViewModel, visualizationViewModel, colorPickerViewModel));
        setRight(new RightPaneView(workspaceViewModel, channelsViewModel, quantizationViewModel));
        setBottom(buildErrorBar(workspaceViewModel, editViewModel));
    }

    private static Label buildErrorBar(ImageWorkspaceViewModel workspace, EditSessionViewModel edit) {
        Label status = new Label();
        status.setPadding(new Insets(4, 12, 4, 12));
        status.setStyle("-fx-text-fill: #b00020;");
        workspace.lastErrorProperty().addListener((obs, oldErr, newErr) ->
                status.setText(newErr == null ? "" : "Failed to load image: " + newErr.getMessage()));
        edit.lastErrorProperty().addListener((obs, oldErr, newErr) -> {
            if (newErr != null) status.setText("Edit operation failed: " + newErr.getMessage());
        });
        return status;
    }
}
