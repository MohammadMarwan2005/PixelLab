package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.channels.view.ChannelControlsView;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import com.alaishat.mohammad.pixellab.features.quantization.view.QuantizationPanelView;
import com.alaishat.mohammad.pixellab.features.quantization.viewmodel.QuantizationViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

/**
 * Right pane: metadata at the top, quantization slider, then channel controls.
 * Wrapped in a scroll pane so controls stay reachable when CMYK adds a 4th
 * channel or smaller windows squeeze the layout.
 */
public final class RightPaneView extends ScrollPane {

    public RightPaneView(ImageWorkspaceViewModel workspaceViewModel,
                         ChannelsViewModel channelsViewModel,
                         QuantizationViewModel quantizationViewModel) {
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.setMinWidth(0);

        content.getChildren().add(new MetadataPanelView(workspaceViewModel));
        content.getChildren().add(new Separator());
        content.getChildren().add(new QuantizationPanelView(quantizationViewModel));
        content.getChildren().add(new Separator());
        content.getChildren().add(new ChannelControlsView(channelsViewModel));

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setPrefViewportWidth(280);
        setMinWidth(240);
    }
}
