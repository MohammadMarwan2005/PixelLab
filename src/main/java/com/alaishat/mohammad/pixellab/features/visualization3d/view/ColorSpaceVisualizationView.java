package com.alaishat.mohammad.pixellab.features.visualization3d.view;

import com.alaishat.mohammad.pixellab.features.visualization3d.usecase.ColorSample;
import com.alaishat.mohammad.pixellab.features.visualization3d.viewmodel.ColorSpaceVisualizationViewModel;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * 3D visualization of the currently selected color space (Phase 8.2–8.7).
 *
 * <p>Each sample is rendered as a small {@link Sphere} colored with its RGB
 * value. A scaling factor maps sample positions (≈ unit cube) to scene units
 * (240) so the camera framing is consistent across spaces.
 *
 * <p>Interaction:
 * <ul>
 *   <li><b>Drag</b> — yaw + pitch rotate of the sample group</li>
 *   <li><b>Scroll</b> — dolly the camera in/out via translateZ</li>
 *   <li><b>+ / − overlay buttons</b> — same dolly, useful when scroll is unavailable</li>
 *   <li><b>Click on a sphere</b> — sets the picked sample on the view model</li>
 * </ul>
 */
public final class ColorSpaceVisualizationView extends StackPane {

    private static final double SCENE_WIDTH = 480;
    private static final double SCENE_HEIGHT = 360;
    private static final double UNIT_SCALE = 240.0;       // unit cube → scene units
    private static final double SAMPLE_RADIUS = 12.0;     // packed (240/10 = 24 apart, r=12 → touching)
    private static final double CAMERA_NEAR = 0.1;
    private static final double CAMERA_FAR = 5000;
    private static final double DEFAULT_CAMERA_Z = -700;
    private static final double ZOOM_MIN = -3500;
    private static final double ZOOM_MAX = -150;
    private static final double BUTTON_ZOOM_STEP = 120;

    private final ColorSpaceVisualizationViewModel viewModel;

    private final Group worldRoot = new Group();
    private final Group sampleGroup = new Group();
    private final Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);
    private final Rotate rotateX = new Rotate(-25, Rotate.X_AXIS);
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private double anchorMouseX;
    private double anchorMouseY;
    private double anchorAngleX;
    private double anchorAngleY;

    public ColorSpaceVisualizationView(ColorSpaceVisualizationViewModel viewModel) {
        this.viewModel = viewModel;

        sampleGroup.getTransforms().addAll(rotateY, rotateX);
        worldRoot.getChildren().add(sampleGroup);
        worldRoot.getChildren().add(new AmbientLight(Color.gray(0.55)));
        PointLight key = new PointLight(Color.gray(0.7));
        key.setTranslateZ(-600);
        key.setTranslateY(-400);
        key.setTranslateX(400);
        worldRoot.getChildren().add(key);

        camera.setNearClip(CAMERA_NEAR);
        camera.setFarClip(CAMERA_FAR);
        camera.setTranslateZ(DEFAULT_CAMERA_Z);
        camera.setFieldOfView(35);

        SubScene subScene = new SubScene(worldRoot, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#1e1e1e"));
        subScene.setCamera(camera);
        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());

        Pane host = new Pane(subScene);
        host.setStyle("-fx-background-color: #1e1e1e;");
        getChildren().add(host);
        getChildren().add(buildZoomOverlay());
        setAlignment(Pos.TOP_RIGHT);

        rebuildSpheres();
        viewModel.samples().addListener((ListChangeListener<ColorSample>) c -> rebuildSpheres());

        installInteraction(subScene);
    }

    private VBox buildZoomOverlay() {
        Button zoomIn = new Button("+");
        Button zoomOut = new Button("−");
        Button resetView = new Button("⟲");
        for (Button b : new Button[] { zoomIn, zoomOut, resetView }) {
            b.setStyle("-fx-min-width: 32; -fx-min-height: 32; -fx-font-size: 14px; -fx-padding: 0 0 2 0;");
            b.setFocusTraversable(false);
        }
        zoomIn.setOnAction(e -> camera.setTranslateZ(clamp(camera.getTranslateZ() + BUTTON_ZOOM_STEP, ZOOM_MIN, ZOOM_MAX)));
        zoomOut.setOnAction(e -> camera.setTranslateZ(clamp(camera.getTranslateZ() - BUTTON_ZOOM_STEP, ZOOM_MIN, ZOOM_MAX)));
        resetView.setOnAction(e -> {
            camera.setTranslateZ(DEFAULT_CAMERA_Z);
            rotateX.setAngle(-25);
            rotateY.setAngle(-30);
        });
        VBox overlay = new VBox(6, zoomIn, zoomOut, resetView);
        overlay.setMaxWidth(VBox.USE_PREF_SIZE);
        overlay.setMaxHeight(VBox.USE_PREF_SIZE);
        overlay.setPickOnBounds(false);
        overlay.setStyle("-fx-padding: 12;");
        StackPane.setAlignment(overlay, Pos.TOP_RIGHT);
        return overlay;
    }

    private void rebuildSpheres() {
        sampleGroup.getChildren().clear();
        for (ColorSample sample : viewModel.samples()) {
            Sphere sphere = new Sphere(SAMPLE_RADIUS, 8);
            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(Color.color(sample.rgb().a(), sample.rgb().b(), sample.rgb().c()));
            mat.setSpecularColor(Color.color(0.2, 0.2, 0.2));
            sphere.setMaterial(mat);
            Point3D p = sample.position();
            sphere.getTransforms().add(new Translate(p.getX() * UNIT_SCALE,
                    p.getY() * UNIT_SCALE,
                    p.getZ() * UNIT_SCALE));
            sphere.setUserData(sample);
            sphere.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                    viewModel.pickedSampleProperty().set(sample);
                    e.consume();
                }
            });
            sampleGroup.getChildren().add(sphere);
        }
    }

    private void installInteraction(SubScene subScene) {
        subScene.setOnMousePressed(e -> {
            anchorMouseX = e.getSceneX();
            anchorMouseY = e.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });
        subScene.setOnMouseDragged(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            double dx = e.getSceneX() - anchorMouseX;
            double dy = e.getSceneY() - anchorMouseY;
            rotateY.setAngle(anchorAngleY + dx * 0.4);
            rotateX.setAngle(clamp(anchorAngleX - dy * 0.4, -89, 89));
        });
        subScene.setOnScroll(e -> {
            double next = camera.getTranslateZ() + e.getDeltaY() * 1.5;
            camera.setTranslateZ(clamp(next, ZOOM_MIN, ZOOM_MAX));
        });
        subScene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress() && e.getPickResult().getIntersectedNode() == null) {
                viewModel.pickedSampleProperty().set(null);
            }
        });
    }

    private static double clamp(double v, double lo, double hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
