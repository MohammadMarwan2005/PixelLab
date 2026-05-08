package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.channels.view.ChannelControlsView;
import com.alaishat.mohammad.pixellab.features.channels.viewmodel.ChannelsViewModel;
import com.alaishat.mohammad.pixellab.features.imageworkspace.viewmodel.ImageWorkspaceViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

/**
 * Right pane: metadata at the top, channel controls below. Wrapped in a
 * scroll pane so the controls remain reachable when more channels (CMYK = 4)
 * or longer images push them off-screen.
 */
public final class RightPaneView extends ScrollPane {

    public RightPaneView(ImageWorkspaceViewModel workspaceViewModel,
                         ChannelsViewModel channelsViewModel) {
        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.setMinWidth(0);

        content.getChildren().add(new MetadataPanelView(workspaceViewModel));
        content.getChildren().add(new Separator());
        content.getChildren().add(new ChannelControlsView(channelsViewModel));

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setPrefViewportWidth(260);
        setMinWidth(220);
    }
}
