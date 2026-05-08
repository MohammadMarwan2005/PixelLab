package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.colorpicker.view.ColorPickerView;
import com.alaishat.mohammad.pixellab.features.colorpicker.viewmodel.ColorPickerViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.visualization3d.view.ColorSpaceVisualizationView;
import com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel.ColorSpaceVisualizationViewModel;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Center pane: tabs for the image workspace and the 3D color-space viz.
 * The 3D tab also hosts the synchronized color-picker panel below the scene.
 */
public final class CenterPaneView extends TabPane {

    public CenterPaneView(ImageWorkspaceViewModel workspaceViewModel,
                          ColorSpaceVisualizationViewModel visualizationViewModel,
                          ColorPickerViewModel colorPickerViewModel) {
        setSide(Side.TOP);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        Tab imageTab = new Tab("Image", new ImageCanvasView(workspaceViewModel));

        BorderPane vizContainer = new BorderPane();
        vizContainer.setCenter(new ColorSpaceVisualizationView(visualizationViewModel));
        vizContainer.setBottom(new ColorPickerView(colorPickerViewModel));
        Tab vizTab = new Tab("3D Color Space", vizContainer);

        getTabs().addAll(imageTab, vizTab);
    }
}
