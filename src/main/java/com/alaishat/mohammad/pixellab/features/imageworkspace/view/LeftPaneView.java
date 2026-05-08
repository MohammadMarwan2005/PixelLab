package com.alaishat.mohammad.pixellab.features.imageworkspace.view;

import com.alaishat.mohammad.pixellab.features.colorspace.view.ColorSpaceSelectorView;
import com.alaishat.mohammad.pixellab.features.colorspace.viewmodel.ColorSpaceViewModel;
import com.alaishat.mohammad.pixellab.features.recentfiles.view.RecentFilesPanelView;
import com.alaishat.mohammad.pixellab.features.recentfiles.viewmodel.RecentFilesViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Left pane: color space selector at the top, recent files filling the rest.
 */
public final class LeftPaneView extends VBox {

    public LeftPaneView(ColorSpaceViewModel colorSpaceViewModel,
                        RecentFilesViewModel recentFilesViewModel,
                        Consumer<Path> onRecentOpen) {
        setPadding(new Insets(12));
        setPrefWidth(220);
        setMinWidth(180);
        setSpacing(8);

        ColorSpaceSelectorView selector = new ColorSpaceSelectorView(colorSpaceViewModel);
        Separator divider = new Separator();
        RecentFilesPanelView recents = new RecentFilesPanelView(recentFilesViewModel, onRecentOpen);
        // RecentFilesPanelView already self-pads — reset its insets so it composes cleanly.
        recents.setPadding(Insets.EMPTY);
        VBox.setVgrow(recents, Priority.ALWAYS);

        getChildren().addAll(selector, divider, recents);
    }
}
