package com.alaishat.mohammad.pixellab;

import com.alaishat.mohammad.pixellab.features.audioworkspace.view.AudioLabView;
import com.alaishat.mohammad.pixellab.features.imageworkspace.view.MainWindowView;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Window shell: a top-level mode switch between "Image Lab" (the existing
 * image workbench, untouched) and "Audio Lab" (the audio compression
 * workbench). Keeps the two domains' panels from fighting over layout.
 */
public final class RootView extends TabPane {

    public RootView(MainWindowView imageLab, AudioLabView audioLab) {
        setSide(Side.TOP);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        Tab imageTab = new Tab("Image Lab", imageLab);
        Tab audioTab = new Tab("Audio Lab", audioLab);

        getTabs().addAll(imageTab, audioTab);
    }
}
