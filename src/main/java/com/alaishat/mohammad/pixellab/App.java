package com.alaishat.mohammad.pixellab;

import com.alaishat.mohammad.pixellab.features.audioworkspace.view.AudioLabView;
import com.alaishat.mohammad.pixellab.features.imageworkspace.view.MainWindowView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 800;
    private static final String WINDOW_TITLE = "PixelLab";

    private AppComponent component;

    @Override
    public void init() {
        component = new AppComponent();
    }

    @Override
    public void start(Stage stage) {
        MainWindowView imageLab = new MainWindowView(
                component.imageWorkspaceViewModel(),
                component.editSessionViewModel(),
                component.colorSpaceViewModel(),
                component.channelsViewModel(),
                component.quantizationViewModel(),
                component.visualizationViewModel(),
                component.colorPickerViewModel(),
                component.recentFilesViewModel(),
                component.updateCoalescer());
        AudioLabView audioLab = new AudioLabView(component.audioWorkspaceViewModel(), component.audioCompressionViewModel());
        RootView root = new RootView(imageLab, audioLab);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        if (component != null) component.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
